package com.winterhaven_mc.deathchest;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerEventListener implements Listener {

	// reference to main class
	private PluginMain plugin;

	
	/** class constructor
	 * 
	 * @param plugin reference to main class
	 */
	public PlayerEventListener(PluginMain plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	

	/**
	 * Event listener for PlayerDeathEvent<p>
	 * Attempt to deploy a death chest on player death
	 * @param event	PlayerDeathEvent
	 */
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		Player player = (Player)event.getEntity();
		List<ItemStack> droppedItems = event.getDrops();
		
		// If player's current world is not enabled in config, do nothing
		// and allow inventory items to drop on ground
		if (!playerWorldEnabled(player)) {
			return;
		}
		
		// if player does not have permission for death chest creation,
		// do nothing and allow inventory items to drop on ground
		if (!player.hasPermission("deathchest.chest")) {
			plugin.messageManager.sendPlayerMessage(player, "permission-denied");
			return;
		}
		
		// if player is in creative mode, output message and return
		if (player.getGameMode().equals(GameMode.CREATIVE)) {
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
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		final Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		
		// if block is not a DeathChestBlock, do nothing and return
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}
		
		// if block is wall sign, set block to attached block
		if (block.getType().equals(Material.WALL_SIGN)) {
		    Sign sign = (Sign)block.getState().getData();
		    block = block.getRelative(sign.getAttachedFace());
		}
		
		// if block is sign post, set block to one block below
		else if (block.getType().equals(Material.SIGN_POST)) {
			block = block.getRelative(0, -1, 0);
		}
		
		// confirm block is a death chest
		if (!block.getType().equals(Material.CHEST) || !DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}

		// if player is in creative mode and does not have override permission, cancel event, send message and return
		if (player.getGameMode().equals(GameMode.CREATIVE) && !player.hasPermission("deathchest.creative-access")) {
			event.setCancelled(true);
			plugin.messageManager.sendPlayerMessage(player, "no-creative-access");
			return;
		}

		// if access is blocked by a protection plugin, do nothing and return (allow protection plugin to handle event)
		ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestAccess(player, block);
		if (blockingPlugin != null) {
			if (plugin.debug) {
				plugin.getLogger().info(blockingPlugin.getPluginName() + " is preventing access to this chest.");
			}
			return;
		}
		
		// if chest is already open: cancel event, send message and return
		if (plugin.chestManager.getChestViewerCount(block) > 0) {

			// cancel event
			event.setCancelled(true);
			
			// send player message
			plugin.messageManager.sendPlayerMessage(player, "chest-currently-open");

			// if sound effects are enabled, play denied access sound
			if (plugin.getConfig().getBoolean("sound-effects")) {
				player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
			}
			return;
		}
		
		// if player sneak punched block, try auto-loot
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && player.isSneaking()) {

			// cancel event
			event.setCancelled(true);
			
			// if chest protection is not enabled, loot chest and return
			if (!plugin.getConfig().getBoolean("chest-protection")) {
				plugin.chestManager.lootChest(player, block);
				return;
			}
			
			// if player is owner or has deathchest.loot.other permission, loot chest and return
			if (DeathChestBlock.isDeathChestOwner(player, block) || player.hasPermission("deathchest.loot.other")) {
				plugin.chestManager.lootChest(player, block);
				return;
			}
			
			// if killer looting is enabled  and player is killer, loot chest and return
			if (plugin.getConfig().getBoolean("killer-looting") 
					&& DeathChestBlock.isDeathChestKiller(player, block)) {
				plugin.chestManager.lootChest(player, block);
				return;
			}
			
			// send player not-owner message
			plugin.messageManager.sendPlayerMessage(player, "not-owner");

			// if sound effects are enabled, play denied access sound
			if (plugin.getConfig().getBoolean("sound-effects")) {
				player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
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
		if (DeathChestBlock.isDeathChestOwner(player, block) || player.hasPermission("deathchest.loot.other")) {
			DeathChestBlock.openInventory(player, block);
			return;
		}
		
		// if killer looting is enabled  and player is killer, open chest inventory and return
		if (plugin.getConfig().getBoolean("killer-looting") 
				&& DeathChestBlock.isDeathChestKiller(player, block)) {
			DeathChestBlock.openInventory(player, block);
			return;
		}
		
		// send player not-owner message
		plugin.messageManager.sendPlayerMessage(player, "not-owner");

		// if sound effects are enabled, play denied access sound
		if (plugin.getConfig().getBoolean("sound-effects")) {
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
		}
	}

	
	/**
	 * Test if plugin is enabled in player's current world.
	 * 
	 * @param player	Player to test world enabled.
	 * @return true if player world is enabled, false if not enabled 
	 */
	private boolean playerWorldEnabled(Player player) {
		
		if (plugin.commandManager.getEnabledWorlds().contains(player.getWorld().getName())) {
			return true;
		}
		return false;
	}

}
