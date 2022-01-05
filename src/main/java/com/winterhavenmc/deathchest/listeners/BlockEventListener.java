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

		// if access is blocked by a protection plugin, do nothing and return (allow protection plugin to handle event)
		ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestAccess(player, block);
		if (blockingPlugin != null) {
			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().info(blockingPlugin.getPluginName() + " prevented access to a chest.");
			}
			return;
		}

		// if player is in creative mode
		// and creative-access is configured false
		// and player does not have override permission:
		// cancel event, send message and return
		if (player.getGameMode().equals(GameMode.CREATIVE)
				&& !plugin.getConfig().getBoolean("creative-access")
				&& !player.hasPermission("deathchest.creative-access")) {
			plugin.messageBuilder.build(player, NO_CREATIVE_ACCESS)
					.setMacro(LOCATION, player.getLocation())
					.send();
			event.setCancelled(true);
			return;
		}

		// if chest-protection option is not enabled, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}

		// if chest protection has expired, do nothing and return
		if (deathChest.protectionExpired()) {
			return;
		}
		// else send message with chest expiration time remaining
		else {
			long remainingProtectionTime = System.currentTimeMillis() - deathChest.getProtectionExpirationTime();
			plugin.messageBuilder.build(player, CHEST_ACCESSED_PROTECTION_TIME)
					.setMacro(Macro.OWNER, deathChest.getOwnerName())
					.setMacro(Macro.LOCATION, deathChest.getLocation())
					.setMacro(Macro.DURATION, remainingProtectionTime)
					.send();
		}

		// cancel event
		event.setCancelled(true);

		// if chest is already open, disallow breakage; send message and return
		if (deathChest.getViewerCount() > 0) {

			// only one chest viewer allowed, so get name viewer at index 0
			String viewerName = deathChest.getInventory().getViewers().get(0).getName();

			String ownerName = "-";
			if (deathChest.hasValidOwnerUid()) {
				ownerName = plugin.getServer().getOfflinePlayer(deathChest.getOwnerUid()).getName();
			}

			String killerName = "-";
			if (deathChest.hasValidKillerUid()) {
				killerName = plugin.getServer().getOfflinePlayer(deathChest.getKillerUid()).getName();
			}

			// send player message
			plugin.messageBuilder.build(player, CHEST_CURRENTLY_OPEN)
					.setMacro(LOCATION, deathChest.getLocation())
					.setMacro(OWNER, ownerName)
					.setMacro(KILLER, killerName)
					.setMacro(VIEWER, viewerName)
					.send();

			// play denied access sound
			plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
			return;
		}

		// if player is owner or has deathchest.loot.other permission, break chest and return
		if (deathChest.isOwner(player) || player.hasPermission("deathchest.loot.other")) {
			deathChest.destroy();
			return;
		}

		// if killer looting is enabled and player is killer and has permission, break chest and return
		// TODO: this will need to be removed when items taken limit is implemented
		if (plugin.getConfig().getBoolean("killer-looting")
				&& deathChest.isKiller(player)
				&& player.hasPermission("deathchest.loot.killer")) {
			deathChest.destroy();
			return;
		}

		// get owner name
		String ownerName = "-";
		if (deathChest.hasValidOwnerUid()) {
			ownerName = plugin.getServer().getOfflinePlayer(deathChest.getOwnerUid()).getName();
		}

		// get killer name
		String killerName = "-";
		if (deathChest.hasValidKillerUid()) {
			killerName = plugin.getServer().getOfflinePlayer(deathChest.getKillerUid()).getName();
		}

		// send player not-owner message
		plugin.messageBuilder.build(player, NOT_OWNER)
				.setMacro(LOCATION, deathChest.getLocation())
				.setMacro(OWNER, ownerName)
				.setMacro(KILLER, killerName)
				.send();

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
