package com.winterhavenmc.deathchest.listeners;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.messages.Macro;
import com.winterhavenmc.deathchest.chests.DeathChest;
import com.winterhavenmc.deathchest.protectionplugins.ProtectionPlugin;
import com.winterhavenmc.deathchest.sounds.SoundId;

import com.winterhavenmc.deathchest.chests.LocationUtilities;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Collection;
import java.util.LinkedList;

import static com.winterhavenmc.deathchest.messages.Macro.*;
import static com.winterhavenmc.deathchest.messages.MessageId.*;



/**
 * A class that contains {@code EventHandler} methods to process block related events
 */
public final class BlockEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;
	
	// reference to helper class
	private final Helper helper;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public BlockEventListener(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// create instance of helper class
		this.helper = new Helper(plugin);

		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Block place event handler<br>
	 * prevent placing chests adjacent to existing death chest
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {

		final Block block = event.getBlock();
		final Location location = block.getLocation();

		// if placed block is not a chest, do nothing and return
		if (!block.getType().equals(Material.CHEST)) {
			return;
		}

		// check for adjacent death chests and cancel event if found
		if (plugin.chestManager.isChestBlockChest(LocationUtilities.getBlockToLeft(location))
				|| plugin.chestManager.isChestBlockChest(LocationUtilities.getBlockToRight(location))) {
			event.setCancelled(true);
		}
	}


	/**
	 * Block break event handler<br>
	 * Checks for ownership of death chests and prevents breakage by non-owners.<br>
	 * Listens at EventPriority.LOW to handle event before protection plugins
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(final BlockBreakEvent event) {

		final Block block = event.getBlock();
		final Player player = event.getPlayer();

		// if event block is not a DeathChestBlock, do nothing and return
		if (!plugin.chestManager.isChestBlock(block)) {
			return;
		}

		// get instance of DeathChest from event block
		final DeathChest deathChest = plugin.chestManager.getChest(block);

		// if returned DeathChest is null, do nothing and return
		if (deathChest == null) {
			return;
		}

		// if access is blocked by a protection plugin, do nothing and return (allow protection plugin to handle event)
		ProtectionPlugin blockingPlugin = plugin.protectionPluginRegistry.AccessAllowed(player, block.getLocation());

		if (helper.pluginBlockedAccess(blockingPlugin)) {
			// do not cancel event - allow protection plugin to handle it
			helper.logDebugMessage(blockingPlugin.getPluginName() + " prevented access to a chest.");
			return;
		}

		// if player is in creative mode
		// and creative-access is configured false
		// and player does not have override permission:
		// cancel event, send message and return
		if (helper.creativeModeAccessDisabled(player)) {
			event.setCancelled(true);
			plugin.messageBuilder.build(player, NO_CREATIVE_ACCESS)
					.setMacro(LOCATION, player.getLocation())
					.send();
			helper.logDebugMessage(event.getEventName() + " cancelled by creative access check.");
			return;
		}

		// if chest is already open: cancel event, send message and return
		if (helper.chestCurrentlyOpen(deathChest)) {
			String viewerName = deathChest.getInventory().getViewers().get(0).getName();
			event.setCancelled(true);
			plugin.messageBuilder.build(player, CHEST_CURRENTLY_OPEN)
					.setMacro(LOCATION, deathChest.getLocation())
					.setMacro(OWNER, deathChest.getOwnerName())
					.setMacro(KILLER, deathChest.getKillerName())
					.setMacro(VIEWER, viewerName)
					.send();
			plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
			helper.logDebugMessage(event.getEventName() + " cancelled by chest already open check.");
			return;
		}

		// if player is owner: cancel event, break chest and return
		if (deathChest.isOwner(player)) {
			helper.cancelEventAndDestroyChest(event, deathChest);
			helper.logDebugMessage(event.getEventName() + " cancelled and chest destroyed (drop items) " +
					"because player is chest owner.");
			return;
		}

		// if chest-protection not enabled: cancel event, break chest and return
		if (helper.chestProtectionDisabled()) {
			helper.cancelEventAndDestroyChest(event, deathChest);
			helper.logDebugMessage(event.getEventName() + " remaining tests skipped because chest protection disabled.");
			return;
		}

		// if chest protection enabled and has expired, cancel event, break chest and return
		if (helper.chestProtectionExpired(deathChest)) {
			helper.cancelEventAndDestroyChest(event, deathChest);
			helper.logDebugMessage(event.getEventName() + " remaining tests skipped because chest protection expired.");
			return;
		}

		// if chest protection enabled and player has deathchest.loot.other permission: cancel event, break chest and return
		if (helper.playerHasLootOtherPermission(player)) {
			helper.cancelEventAndDestroyChest(event, deathChest);
			helper.logDebugMessage(event.getEventName() + " remaining tests skipped because player has loot.other permission.");
			return;
		}

		// if killer looting is enabled and player is killer and has permission: cancel event, break chest and return
		if (helper.playerIsKillerLooting(player, deathChest)) {
			helper.cancelEventAndDestroyChest(event, deathChest);
			helper.logDebugMessage(event.getEventName() + " remaining tests skipped because killer looting enabled and " +
					"player is killer and has loot.killer permission.");
			return;
		}

		// cancel event
		event.setCancelled(true);

		if (helper.chestProtectionNotExpired(deathChest)) {
			// if chest protection enabled and not expired, send protection time remaining message
			long protectionTimeRemainingMillis = deathChest.getProtectionTime() - System.currentTimeMillis();
			plugin.messageBuilder.build(player, CHEST_ACCESSED_PROTECTION_TIME)
					.setMacro(Macro.OWNER, deathChest.getOwnerName())
					.setMacro(Macro.LOCATION, deathChest.getLocation())
					.setMacro(PROTECTION_DURATION, protectionTimeRemainingMillis)
					.setMacro(PROTECTION_DURATION_MINUTES, protectionTimeRemainingMillis)
					.send();
		}
		else {
			// send player not-owner message
			plugin.messageBuilder.build(player, NOT_OWNER)
					.setMacro(LOCATION, deathChest.getLocation())
					.setMacro(OWNER, deathChest.getOwnerName())
					.setMacro(KILLER, deathChest.getKillerName())
					.send();
		}

		// play denied access sound
		plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
	}


	/**
	 * Entity explode event handler<br>
	 * Make death chests explosion proof if chest-protection is enabled
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent event) {

		// if chest-protection is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}

		// iterate through all blocks in explosion event and remove those that are DeathChest chests or signs
		Collection<Block> blocks = new LinkedList<>(event.blockList());
		for (Block block : blocks) {
			if (plugin.chestManager.isChestBlock(block)) {
				// remove death chest block from blocks exploded list if protection has not expired
				DeathChest deathChest = plugin.chestManager.getChest(block);
				if (deathChest != null && !deathChest.protectionExpired()) {
					event.blockList().remove(block);
				}
			}
		}
	}


	/**
	 * Block explode event handler<br>
	 * Make death chests explosion proof if chest-protection is enabled
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public void onBlockExplode(final BlockExplodeEvent event) {

		// if chest-protection is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}

		// iterate through all blocks in explosion event and remove those that are DeathChest chests or signs
		Collection<Block> blocks = new LinkedList<>(event.blockList());
		for (Block block : blocks) {
			if (plugin.chestManager.isChestBlock(block)) {
				// remove death chest block from blocks exploded list if protection has not expired
				DeathChest deathChest = plugin.chestManager.getChest(block);
				if (deathChest != null && !deathChest.protectionExpired()) {
					event.blockList().remove(block);
				}
			}
		}
	}


	/**
	 * Block physics event handler<br>
	 * remove detached death chest signs from game to prevent players gaining additional signs
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public void signDetachCheck(final BlockPhysicsEvent event) {

		// if event block is a DeathChest component, cancel event
		if (plugin.chestManager.isChestBlockSign(event.getBlock())) {
			event.setCancelled(true);
		}
	}

}
