package com.winterhaven_mc.deathchest.listeners;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.util.ProtectionPlugin;
import com.winterhaven_mc.deathchest.chests.ChestBlock;
import com.winterhaven_mc.deathchest.chests.DeathChest;
import com.winterhaven_mc.deathchest.chests.Deployment;
import com.winterhaven_mc.deathchest.messages.MessageId;
import com.winterhaven_mc.deathchest.sounds.SoundId;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;


/**
 * A class that contains {@code EventHandler} methods to process player related events
 */

public final class PlayerEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;


	/** class constructor
	 * 
	 * @param plugin reference to main class
	 */
	public PlayerEventListener(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Event listener for PlayerDeathEvent<p>
	 * Attempt to deploy a death chest on player death
	 * @param event	PlayerDeathEvent
	 */
	@EventHandler(priority=EventPriority.HIGH)
	public final void onPlayerDeath(final PlayerDeathEvent event) {

		// deploy DeathChest
		new Deployment(event);
	}


	/** prevent deathchest opening by non-owners or creative players
	 * 
	 * @param event PlayerInteractEvent
	 */
	@EventHandler(priority=EventPriority.HIGH)
	public final void onPlayerInteract(final PlayerInteractEvent event) {

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// get player
		final Player player = event.getPlayer();

		// get block
		final Block block = event.getClickedBlock();

		// if block is not DeathChest block, do nothing and return
		if (!plugin.chestManager.isChestBlock(block)) {
			return;
		}

		ChestBlock chestBlock = plugin.chestManager.getChestBlock(block.getLocation());
		if (chestBlock == null) {
			return;
		}

		// get DeathChest
		DeathChest deathChest = plugin.chestManager.getDeathChest(chestBlock.getChestUUID());

		// if DeathChest returned null, do nothing and return
		if (deathChest == null) {
			return;
		}

		// if access is blocked by a protection plugin, do nothing and return (allow protection plugin to handle event)
		final ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestAccess(player, block);
		if (blockingPlugin != null) {
			return;
		}

		// if player is in creative mode,
		// and creative-access is configured false,
		// and player does not have override permission,
		// then cancel event, send message and return
		if (player.getGameMode().equals(GameMode.CREATIVE)
				&& !plugin.getConfig().getBoolean("creative-access")
				&& !player.hasPermission("deathchest.creative-access")) {
			event.setCancelled(true);
			plugin.messageManager.sendMessage(player, MessageId.NO_CREATIVE_ACCESS, deathChest);
			return;
		}

		// if chest inventory is already being viewed: cancel event, send message and return
		if (deathChest.getViewerCount() > 0 ) {

			// cancel event
			event.setCancelled(true);

			// send player message
			plugin.messageManager.sendMessage(player, MessageId.CHEST_CURRENTLY_OPEN, deathChest);

			// play denied access sound
			plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
			return;
		}

		// if player sneak punched block and quick-loot is enabled, try auto-loot
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)
				&& player.isSneaking()
				&& plugin.getConfig().getBoolean("quick-loot")
				&& player.hasPermission("deathchest.loot")) {

			// cancel event
			event.setCancelled(true);

			// if chest protection is not enabled, loot chest and return
			if (!plugin.getConfig().getBoolean("chest-protection")) {
				deathChest.autoLoot(player);
				return;
			}

			// if player is owner or has deathchest.loot.other permission, loot chest and return
			if (deathChest.isOwner(player) || player.hasPermission("deathchest.loot.other")) {
				deathChest.autoLoot(player);
				return;
			}

			// if killer looting is enabled and player is killer and has permission, loot chest and return
			if (plugin.getConfig().getBoolean("killer-looting") 
					&& deathChest.isKiller(player)
					&& player.hasPermission("deathchest.loot.killer")) {
				deathChest.autoLoot(player);
				return;
			}
			else {
				// send player not-owner message
				plugin.messageManager.sendMessage(player, MessageId.NOT_OWNER, deathChest);

				// play denied access sound
				plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
			}
		}

		// if player did not right click block, do nothing and return
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}

		// if chest-protection option is not enabled, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}

		// cancel event
		event.setCancelled(true);

		// if player is owner or has deathchest.loot.other permission, open chest inventory and return
		if (deathChest.isOwner(player) || player.hasPermission("deathchest.loot.other")) {
			chestBlock.openInventory(player);
			return;
		}

		// if killer looting is enabled  and player is killer and has permission, open chest inventory and return
		if (plugin.getConfig().getBoolean("killer-looting") 
				&& deathChest.isKiller(player)
				&& player.hasPermission("deathchest.loot.killer")) {
			chestBlock.openInventory(player);
		}
		else {
			// send player not-owner message
			plugin.messageManager.sendMessage(player, MessageId.NOT_OWNER, deathChest);

			// play denied access sound
			plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
		}
	}

}
