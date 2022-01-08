package com.winterhavenmc.deathchest.listeners;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.messages.Macro;
import com.winterhavenmc.deathchest.chests.search.ProtectionPlugin;
import com.winterhavenmc.deathchest.chests.DeathChest;
import com.winterhavenmc.deathchest.sounds.SoundId;

import com.winterhavenmc.deathchest.util.LocationUtilities;
import org.bukkit.GameMode;
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

import java.util.ArrayList;

import static com.winterhavenmc.deathchest.messages.Macro.*;
import static com.winterhavenmc.deathchest.messages.MessageId.*;



/**
 * A class that contains {@code EventHandler} methods to process block related events
 */
public final class BlockEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public BlockEventListener(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

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

		// BEGIN CHEST ACCESS CHECKS

		// if access is blocked by a protection plugin, do nothing and return (allow protection plugin to handle event)
		ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestAccess(player, block);
		if (blockingPlugin != null) {
			// do not cancel event - protection plugin will take care of it

			// log debug message
			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().info(blockingPlugin.getPluginName() + " prevented access to a chest.");
			}

			// skip remaining checks
			return;
		}

		// if player is in creative mode
		// and creative-access is configured false
		// and player does not have override permission:
		// cancel event, send message and return
		if (player.getGameMode().equals(GameMode.CREATIVE)
				&& !plugin.getConfig().getBoolean("creative-access")
				&& !player.hasPermission("deathchest.creative-access")) {

			// cancel event
			event.setCancelled(true);

			// send message
			plugin.messageBuilder.build(player, NO_CREATIVE_ACCESS)
					.setMacro(LOCATION, player.getLocation())
					.send();

			// log debug message
			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().info(event.getEventName() + " cancelled by creative access check.");
			}

			// skip remaining checks
			return;
		}

		// if chest is already open, disallow breakage:
		// cancel event, send message and return
		if (deathChest.getViewerCount() > 0) {

			// only one chest viewer allowed, so get name viewer at index 0
			String viewerName = deathChest.getInventory().getViewers().get(0).getName();

			// cancel event
			event.setCancelled(true);

			// send player message
			plugin.messageBuilder.build(player, CHEST_CURRENTLY_OPEN)
					.setMacro(LOCATION, deathChest.getLocation())
					.setMacro(OWNER, deathChest.getOwnerName())
					.setMacro(KILLER, deathChest.getKillerName())
					.setMacro(VIEWER, viewerName)
					.send();

			// play denied access sound
			plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);

			// log debug message
			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().info(event.getEventName() + " cancelled by chest already open check.");
			}

			// skip remaining checks
			return;
		}

		// if player is owner: cancel event, break chest and return
		if (deathChest.isOwner(player)) {

			// cancel event
			event.setCancelled(true);

			// break chest, drop contents (but not deathchest blocks)
			deathChest.destroy();

			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().info(event.getEventName() + " cancelled and chest destroyed (drop items) because player is chest owner.");
			}

			// skip remaining checks
			return;
		}

		// if chest-protection option is not enabled, cancel event, break chest and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {

			// cancel event
			event.setCancelled(true);

			// drop chest contents
			deathChest.destroy();

			// log debug message
			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().info(event.getEventName() + " remaining tests skipped because chest protection disabled.");
			}
			// skip remaining checks
			return;
		}

		// REMAINING TESTS ARE ONLY PERFORMED IF CHEST PROTECTION ENABLED

		// if chest protection has expired, cancel event, break chest and return
		if (deathChest.protectionExpired()) {

			// cancel event
			event.setCancelled(true);

			// drop chest contents
			deathChest.destroy();

			// log debug message
			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().info(event.getEventName() + " remaining tests skipped because chest protection expired.");
			}

			// skip remaining checks
			return;
		}

		// if player has deathchest.loot.other permission: cancel event, break chest and return
		if (player.hasPermission("deathchest.loot.other")) {

			// cancel event
			event.setCancelled(true);

			// break chest, drop contents (but not deathchest blocks)
			deathChest.destroy();

			// log debug message
			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().info(event.getEventName() + " remaining tests skipped because player has loot.other permission.");
			}

			// skip remaining checks
			return;
		}

		// if killer looting is enabled and player is killer and has permission: cancel event, break chest and return
		if (plugin.getConfig().getBoolean("killer-looting")
				&& deathChest.isKiller(player)
				&& player.hasPermission("deathchest.loot.killer")) {

			// cancel event
			event.setCancelled(true);

			// break chest, drop contents (but not deathchest blocks)
			deathChest.destroy();

			// log debug message
			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().info(event.getEventName() + " remaining tests skipped because killer looting enabled and player is killer and pas loot.killer permission.");
			}

			// skip remaining checks
			return;
		}

		// cancel event
		event.setCancelled(true);

		// if chest protection enabled and not expired, send protection time remaining message
		long protectionTimeRemainingMillis = deathChest.getProtectionExpirationTime() - System.currentTimeMillis();
		if (plugin.getConfig().getBoolean("chest-protection") && protectionTimeRemainingMillis > 0) {
			plugin.messageBuilder.build(player, CHEST_ACCESSED_PROTECTION_TIME)
					.setMacro(Macro.OWNER, deathChest.getOwnerName())
					.setMacro(Macro.LOCATION, deathChest.getLocation())
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
		ArrayList<Block> blocks = new ArrayList<>(event.blockList());
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
		ArrayList<Block> blocks = new ArrayList<>(event.blockList());
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
