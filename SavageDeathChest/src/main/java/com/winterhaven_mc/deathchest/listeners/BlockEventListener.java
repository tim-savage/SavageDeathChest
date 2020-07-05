package com.winterhaven_mc.deathchest.listeners;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.messages.Message;
import com.winterhaven_mc.deathchest.util.ProtectionPlugin;
import com.winterhaven_mc.deathchest.chests.DeathChest;
import com.winterhaven_mc.deathchest.sounds.SoundId;

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

import static com.winterhaven_mc.deathchest.util.LocationUtilities.*;
import static com.winterhaven_mc.deathchest.messages.MessageId.*;



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
	public final void onBlockPlace(final BlockPlaceEvent event) {

		final Block block = event.getBlock();
		final Location location = block.getLocation();

		// if placed block is not a chest, do nothing and return
		if (!block.getType().equals(Material.CHEST)) {
			return;
		}

		// check for adjacent death chests and cancel event if found
		if (plugin.chestManager.isChestBlockChest(getBlockToLeft(location))
				|| plugin.chestManager.isChestBlockChest(getBlockToRight(location))) {
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
	public final void onBlockBreak(final BlockBreakEvent event) {

		final Block block = event.getBlock();
		final Player player = event.getPlayer();

		// if event block is not a DeathChestBlock, do nothing and return
		if (!plugin.chestManager.isChestBlock(block)) {
			return;
		}

		// get instance of DeathChest from event block
		final DeathChest deathChest = plugin.chestManager.getDeathChest(block);

		// if returned DeathChest is null, do nothing and return
		if (deathChest == null) {
			return;
		}

		// if access is blocked by a protection plugin, do nothing and return (allow protection plugin to handle event)
		ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestAccess(player, block);
		if (blockingPlugin != null) {
			if (plugin.debug) {
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
			Message.create(player, NO_CREATIVE_ACCESS).send();
			event.setCancelled(true);
			return;
		}

		// if chest-protection option is not enabled, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}

		// cancel event
		event.setCancelled(true);

		// if chest is already open, disallow breakage; send message and return
		if (deathChest.getViewerCount() > 0) {

			// send player message
			Message.create(player, CHEST_CURRENTLY_OPEN).send();

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

		// send player not-owner message
		Message.create(player, NOT_OWNER).send();

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
	public final void onEntityExplode(final EntityExplodeEvent event) {

		// if chest-protection is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}

		// iterate through all blocks in explosion event and remove those that are DeathChest chests or signs
		ArrayList<Block> blocks = new ArrayList<>(event.blockList());
		for (Block block : blocks) {
			if (plugin.chestManager.isChestBlock(block)) {
				event.blockList().remove(block);
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
	public final void onBlockExplode(final BlockExplodeEvent event) {

		// if chest-protection is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}

		// iterate through all blocks in explosion event and remove those that are DeathChest chests or signs
		ArrayList<Block> blocks = new ArrayList<>(event.blockList());
		for (Block block : blocks) {
			if (plugin.chestManager.isChestBlock(block)) {
				event.blockList().remove(block);
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
	public final void signDetachCheck(final BlockPhysicsEvent event) {

		// if event block is a DeathChest component, cancel event
		if (plugin.chestManager.isChestBlockSign(event.getBlock())) {
			event.setCancelled(true);
		}
	}

}
