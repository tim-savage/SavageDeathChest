package com.winterhaven_mc.deathchest;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryEventListener implements Listener {

	// reference to main class
	private DeathChestMain plugin;

	
	/** class constructor
	 * 
	 * @param plugin reference to main class
	 */
	public InventoryEventListener(DeathChestMain plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
	        if (inventory.getHolder() instanceof Chest) {
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


	@EventHandler
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		
		Inventory destination = event.getDestination();
		
		// if destination inventory is not a death chest inventory, do nothing and return
	    if (!inventoryIsDeathChest(destination)) {
	    	return;
	    }
	
		// if prevent-item-placement is configured false, do nothing and return
		if (!plugin.getConfig().getBoolean("prevent-item-placement")) {
			return;
		}
			
		// cancel event
		event.setCancelled(true);
	}


	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent event) {
		
		Inventory inventory = event.getInventory();
		InventoryAction action = event.getAction();
		
		// if inventory is a death chest inventory
	    if (inventoryIsDeathChest(inventory)) {
	
			// if prevent-item-placement is configured false, do nothing and return
			if (!plugin.getConfig().getBoolean("prevent-item-placement")) {
				return;
			}
			
			// if click action is place, test for chest slots
			if (action.equals(InventoryAction.PLACE_ALL) 
					|| action.equals(InventoryAction.PLACE_SOME)
					|| action.equals(InventoryAction.PLACE_ONE)) {
				
				// if place action is in chest slots, cancel event
				if ((inventory.getHolder() instanceof DoubleChest && event.getRawSlot() < 54)
						|| event.getRawSlot() < 27) {
					// if player does not have allow-place permission, cancel event
					if (!event.getWhoClicked().hasPermission("deathchest.allow-place")) {
						event.setCancelled(true);
					}
				}
			}
	    }
	}


	@EventHandler
	public void onInventoryDragEvent(InventoryDragEvent event) {
		
		Inventory inventory = event.getInventory();
		
		// if inventory is a death chest inventory
	    if (inventoryIsDeathChest(inventory)) {
	
			// if prevent-item-placement is configured false, do nothing and return
			if (!plugin.getConfig().getBoolean("prevent-item-placement")) {
				return;
			}
			
			// get set of slots dragged over
			Set<Integer> rawSlots = event.getRawSlots();
			
			// if single chest set max slot to 27
			int maxSlot = 27;
			
			// if double chest set max slot to 54
			if (inventory.getHolder() instanceof DoubleChest) {
				maxSlot = 54;
			}
			
			// iterate over dragged slots and if any are above max slot, cancel event
			for (int slot : rawSlots) {
				if (slot < maxSlot) {
					
					// if player does not have allow-place permission, cancel event
					if (!event.getWhoClicked().hasPermission("deathchest.allow-place")) {
						event.setCancelled(true);
					}
					break;
				}
			}
	    }
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
	
	
	private boolean inventoryIsDeathChest(Inventory inventory) {
		
		// if inventory is not a chest, return false
	    if (!inventory.getType().equals(InventoryType.CHEST)) {
	    	return false;
	    }
	    
	    // try to get inventory holder block
    	Block block;
    	if (inventory.getHolder() instanceof DoubleChest) {
    		DoubleChest doubleChest;
			try {
				doubleChest = (DoubleChest) inventory.getHolder();
			} catch (ClassCastException e) {
				// inventory holder cannot be cast to DoubleChest
				return false;
			}
    		block = doubleChest.getLocation().getBlock();
    	}
    	else {
    		Chest chest;
			try {
				chest = (Chest) inventory.getHolder();
			} catch (ClassCastException e) {
				// inventory holder cannot be cast to Chest
				return false;
			}
    		block = chest.getBlock();
    	}

		// if block is not a DeathChestBlock, return false
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return false;
		}
		return true;
	}

}
