package com.winterhaven_mc.deathchest;

//import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerEventListener implements Listener {

	private DeathChestMain plugin; // pointer to main class
	
	/** class constructor
	 * 
	 * @param plugin reference to main class
	 */
	public PlayerEventListener(DeathChestMain plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	

	/**
	 * Event listener for PlayerDeathEvent<p>
	 * Attempt to deploy a death chest on player death
	 * @param event	PlayerDeathEvent
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		Player player = (Player)event.getEntity();
		List<ItemStack> dropped_items = event.getDrops();
		
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
		if (dropped_items.isEmpty()) {
			plugin.messageManager.sendPlayerMessage(player, "inventory-empty");
			return;
		}
		
		// deploy chest
		if (plugin.debug) {
			plugin.getLogger().info("Deploying chest..");
		}
		dropped_items = plugin.chestManager.deployChest(player, dropped_items);
		
		// clear dropped items
		event.getDrops().clear();
		
		// drop any items that couldn't be placed in a death chest
		event.getDrops().addAll(dropped_items);
		return;
	}

	
	/** prevent deathchest opening by non-owners OR CREATIVE PLAYERS
	 * 
	 * @param	event	PlayerInteractEvent
	 * @return	void
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		final Player player = event.getPlayer();
		final Block block = event.getClickedBlock();
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		// if clicked block is not a chest, do nothing and return
		if (!block.getType().equals(Material.CHEST)) {
			return;
		}
		
		// if block is not a DeathChestBlock, do nothing and return
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}
		
		// if player did not right click chest, do nothing and return 
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		// if player is in creative mode and does not have override permission, cancel event, send message and return
		if (player.getGameMode().equals(GameMode.CREATIVE) && !player.hasPermission("deathchest.creative-access")) {
			event.setCancelled(true);
			plugin.messageManager.sendPlayerMessage(player, "no-creative-access");
			return;
		}
		
		// if chest-protection option is not enabled, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}
		
		// if player is chest owner, do nothing and return
		if (block.getMetadata("deathchest-owner").get(0).asString().equals(player.getUniqueId().toString())) {
			return;
		}

		// if player has deathchest.loot.other permission, do nothing and return
		if (player.hasPermission("deathchest.loot.other")) {
			return;
		}
		
		// if killer-looting is enabled check if player is killer
		if (plugin.getConfig().getBoolean("killer-looting",false)) {
			
			// if player is killer, do nothing and return
			if (block.hasMetadata("deathchest-killer") && block.getMetadata("deathchest-killer").get(0).asString().equals(player.getUniqueId().toString())) {
				if (plugin.debug) {
					plugin.getLogger().info(player.getName() + " was allowed to killer-loot a deathchest.");
				}
				return;
			}
		}
		
		// cancel event
		event.setCancelled(true);
		
		// send player not-owner message
		plugin.messageManager.sendPlayerMessage(player, "not-owner");
	}

	
	/**
	 * Remove empty death chest on inventory close event
	 * @param event
	 */
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		
		// if remove-empty option is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("remove-empty",true)) {
			return;
		}
		
		if (event.getPlayer() instanceof Player) {
			Player player = (Player)event.getPlayer();
			Inventory inventory = event.getInventory();

			// if inventory is a single chest
	        if (inventory.getHolder() instanceof Chest){
	            Chest chest = (Chest) inventory.getHolder();
	            Block block = chest.getBlock();
	            
	    		// if block is not a DeathChestBlock, do nothing and return
	    		if (!DeathChestBlock.isDeathChestBlock(block)) {
	    			return;
	    		}
	    		
	            // if chest is empty, call lootChest method to remove chest and sign
	            if (emptyChest(chest)) {
	            	plugin.chestManager.lootChest(player, block);
	            	return;
	            }
	        }
	        
	        // if inventory is a double chest
	        if (inventory.getHolder() instanceof DoubleChest) {
	            DoubleChest chest = (DoubleChest) inventory.getHolder();
	            Chest left = (Chest) chest.getLeftSide();
	            Chest right = (Chest) chest.getRightSide();
	            Block block = left.getBlock();
	            
	    		// if block is not a DeathChestBlock, do nothing and return
	    		if (!DeathChestBlock.isDeathChestBlock(block)) {
	    			return;
	    		}
	    		
	            // if both chests are empty, call lootChest method to remove chests and sign
	            if (emptyChest(left) && emptyChest(right)) {
	            	plugin.chestManager.lootChest(player, block);
	            	return;
	            }
	        }
		}
	}

	
	/**
	 * Test if plugin is enabled in player's current world.
	 * 
	 * @param player	Player to test world enabled.
	 * @return true if player world is enabled, false if not enabled 
	 */
	private boolean playerWorldEnabled(Player player) {
		List<String> enabledworlds = plugin.getConfig().getStringList("enabled-worlds");
		if (!enabledworlds.contains(player.getWorld().getName())) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Test if chest is empty
	 * 
	 * @param chest
	 * @return true if chest is empty, false if chest has any contents
	 */
	private boolean emptyChest(Chest chest) {
        ItemStack[] items = chest.getInventory().getContents();
        for (ItemStack item : items) {
        	if (item != null) {
        		return false;
        	}
        }
        return true;
	}
	
}
