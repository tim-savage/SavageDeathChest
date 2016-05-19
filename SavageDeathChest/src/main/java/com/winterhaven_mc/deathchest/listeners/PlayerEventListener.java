package com.winterhaven_mc.deathchest.listeners;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import com.winterhaven_mc.deathchest.DeathChestBlock;
import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;

import org.bukkit.event.player.PlayerInteractEvent;

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
		
		final Player player = (Player)event.getEntity();
		List<ItemStack> droppedItems = event.getDrops();
		
		// if player's current world is not enabled in config, do nothing
		// and allow inventory items to drop on ground
		if (!plugin.worldManager.isEnabled(player.getWorld())) {
			return;
		}
		
		// if player does not have permission for death chest creation,
		// do nothing and allow inventory items to drop on ground
		if (!player.hasPermission("deathchest.chest")) {
			plugin.messageManager.sendPlayerMessage(player, "permission-denied");
			return;
		}
		
		// if player is in creative mode,
		// and creative-deploy is configured false,
		// and player does not have creative-deploy permission override:
		// output message and return
		if (player.getGameMode().equals(GameMode.CREATIVE)
				&& !plugin.getConfig().getBoolean("creative-deploy")
				&& !player.hasPermission("deathchest.creative-deploy")) {
			plugin.messageManager.sendPlayerMessage(player, "creative-mode");
			return;
		}
		
		// if player inventory is empty, output message and return
		if (droppedItems.isEmpty()) {
			plugin.messageManager.sendPlayerMessage(player, "inventory-empty");
			return;
		}
		
		// deploy chest, putting items that don't fit in chest into dropped_items list
		droppedItems = plugin.chestManager.deployChest(player, droppedItems);
		
		// clear dropped items
		event.getDrops().clear();
		
		// drop any items that couldn't be placed in a death chest
		event.getDrops().addAll(droppedItems);
		return;
	}

	
	/** prevent deathchest opening by non-owners or creative players
	 * 
	 * @param	event	PlayerInteractEvent
	 * @return	void
	 */
	@EventHandler(priority=EventPriority.HIGH)
	public final void onPlayerInteract(final PlayerInteractEvent event) {
		
		final Player player = event.getPlayer();
		
		// get event block 
		final Block block = event.getClickedBlock();

		// get deathChestBlock instance from block
		final DeathChestBlock deathChestBlock = DeathChestBlock.getChestInstance(event.getClickedBlock());
		
		// if getInstance returned null object, block is not a DeathChestBlock so do nothing and return
		if (deathChestBlock == null) {
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
			plugin.messageManager.sendPlayerMessage(player, "no-creative-access");
			return;
		}

		// if access is blocked by a protection plugin, do nothing and return (allow protection plugin to handle event)
		final ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestAccess(player, block);
		if (blockingPlugin != null) {
			if (plugin.debug) {
				plugin.getLogger().info(blockingPlugin.getPluginName() + " is preventing access to this chest.");
			}
			return;
		}
		
		// if chest inventory is already being viewed: cancel event, send message and return
		if (deathChestBlock.getViewerCount() > 0 ) {

			// cancel event
			event.setCancelled(true);
			
			// send player message
			plugin.messageManager.sendPlayerMessage(player, "chest-currently-open");

			// play denied access sound
			plugin.messageManager.playerSound(player,"CHEST_DENIED_ACCESS");
			return;
		}
		
		// if player sneak punched block, try auto-loot
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && player.isSneaking()) {

			// cancel event
			event.setCancelled(true);
			
			// if chest protection is not enabled, loot chest and return
			if (!plugin.getConfig().getBoolean("chest-protection")) {
				deathChestBlock.autoLoot(player);
				return;
			}
			
			// if player is owner or has deathchest.loot.other permission, loot chest and return
			if (deathChestBlock.isOwner(player) || player.hasPermission("deathchest.loot.other")) {
				deathChestBlock.autoLoot(player);
				return;
			}
			
			// if killer looting is enabled  and player is killer, loot chest and return
			if (plugin.getConfig().getBoolean("killer-looting") 
					&& deathChestBlock.isKiller(player)) {
				deathChestBlock.autoLoot(player);
				return;
			}
			
			// send player not-owner message
			plugin.messageManager.sendPlayerMessage(player, "not-owner");

			// play denied access sound
			plugin.messageManager.playerSound(player,"CHEST_DENIED_ACCESS");
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
		if (deathChestBlock.isOwner(player) || player.hasPermission("deathchest.loot.other")) {
			deathChestBlock.openInventory(player);
			return;
		}
		
		// if killer looting is enabled  and player is killer, open chest inventory and return
		if (plugin.getConfig().getBoolean("killer-looting") 
				&& deathChestBlock.isKiller(player)) {
			deathChestBlock.openInventory(player);
			return;
		}
		
		// send player not-owner message
		plugin.messageManager.sendPlayerMessage(player, "not-owner");

		// play denied access sound
		plugin.messageManager.playerSound(player,"CHEST_DENIED_ACCESS");
	}
	
}
