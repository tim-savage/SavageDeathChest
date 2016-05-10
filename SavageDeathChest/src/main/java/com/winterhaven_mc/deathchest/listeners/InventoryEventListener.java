package com.winterhaven_mc.deathchest.listeners;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.winterhaven_mc.deathchest.DeathChestBlock;
import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;

public class InventoryEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	
	/** class constructor
	 * 
	 * @param plugin reference to main class
	 */
	public InventoryEventListener(final PluginMain plugin) {
		
		// set reference to main class
		this.plugin = plugin;
		
		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	
	/**
	 * Inventory open event handler<br>
	 * Uncancels an event that was cancelled by a protection plugin
	 * if configured to override the protection plugin and thereby allow
	 * death chest access where chest access would normally be restricted
	 * @param event
	 */
	@EventHandler(priority=EventPriority.HIGH)
	public void onInventoryOpen(final InventoryOpenEvent event) {

		// if event is not cancelled, do nothing and return
		if (!event.isCancelled()) {
			return;
		}

		// get event inventory
		Inventory inventory = event.getInventory();

		// if inventory holder is not a death chest, do nothing and return		
		if (!inventoryIsDeathChest(inventory)) {
			return;
		}

		// if event entity is not a player, do nothing and return
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}

		// get event player
		Player player = (Player) event.getPlayer();

		// get inventory holder block (death chest)
		Block block = null;
		
		// if inventory is a single chest, get chest block
		if (inventory.getHolder() instanceof Chest) {
			Chest chest = (Chest) inventory.getHolder();
			block = chest.getBlock();
		}
		
		// if inventory is a double chest, get left chest block
		else if (inventory.getHolder() instanceof DoubleChest) {
			DoubleChest chest = (DoubleChest) inventory.getHolder();
			Chest leftChest = (Chest) chest.getLeftSide();
			block = leftChest.getBlock();
		}
		
		// if block is not a death chest block, do nothing and return
		if (!DeathChestBlock.isDeathChestBlock(block)) {
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
		
		// uncancel event
		event.setCancelled(false);
	}

	
	/**
	 * Remove empty death chest on inventory close event
	 * @param event
	 */
	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent event) {
		
		// if remove-empty option is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("remove-empty")) {
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
	            Chest leftChest = (Chest) chest.getLeftSide();
	            Chest rightChest = (Chest) chest.getRightSide();
	            Block block = leftChest.getBlock();
	            
	    		// if block is not a DeathChestBlock, do nothing and return
	    		if (!DeathChestBlock.isDeathChestBlock(block)) {
	    			return;
	    		}
	    		
	            // if both chests are empty, call lootChest method to remove chests and sign
	            if (emptyChest(leftChest) && emptyChest(rightChest)) {
	            	plugin.chestManager.lootChest(player, block);
	            	return;
	            }
	        }
		}
	}


	/**
	 * Prevent hoppers from removing or inserting items in death chests
	 * @param event
	 */
	@EventHandler
	public void onInventoryMoveItemEvent(final InventoryMoveItemEvent event) {

		// get inventories involved in event
		Inventory destination = event.getDestination();
		Inventory source = event.getSource();
		
		// if source inventory is a death chest, cancel event and return
		if (inventoryIsDeathChest(source)) {
			event.setCancelled(true);
			return;
		}
		
		// if destination is a death chest and prevent-item-placement is true, cancel event and return
		if (inventoryIsDeathChest(destination) && plugin.getConfig().getBoolean("prevent-item-placement")) {
			event.setCancelled(true);
			return;
		}
	}


	/**
	 * Prevent placing items in death chests if configured
	 * @param event
	 */
	@EventHandler
	public void onInventoryClickEvent(final InventoryClickEvent event) {
		
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
					|| action.equals(InventoryAction.PLACE_ONE)
					|| action.equals(InventoryAction.SWAP_WITH_CURSOR)) {
				
				// if double chest check for slot below 54
				if (inventory.getHolder() instanceof DoubleChest) {

					// if slot is below 54, check for player override permission
					if (event.getRawSlot() < 54) {

						// if player does not have allow-place permission, cancel event
						if (!event.getWhoClicked().hasPermission("deathchest.allow-place")) {
							event.setCancelled(true);
						}
					}
					return;
				}

				// not a double chest, so check for slot below 27
				if (event.getRawSlot() < 27) {

					// if player does not have allow-place permission, cancel event
					if (!event.getWhoClicked().hasPermission("deathchest.allow-place")) {
						event.setCancelled(true);
					}
				}
				return;
			}
			
			// if click action is move to other inventory, test slots
			if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {

				// if double chest, player inventory starts at slot 54
				if (inventory.getHolder() instanceof DoubleChest) {

					// if slot above 53, check for player override permission
					if (event.getRawSlot() > 53) {

						// if player does not have allow-place permission, cancel event
						if (!event.getWhoClicked().hasPermission("deathchest.allow-place")) {
							event.setCancelled(true);
						}
					}
					return;
				}

				// single chest, so check for slot above 26
				if (event.getRawSlot() > 26) {

					// if player does not have allow-place permission, cancel event and return
					if (!event.getWhoClicked().hasPermission("deathchest.allow-place")) {
						event.setCancelled(true);
					}
				}
			}
	    }
	}


	/**
	 * Prevent placing items in death chests if configured
	 * @param event
	 */
	@EventHandler
	public void onInventoryDragEvent(final InventoryDragEvent event) {
		
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
	private boolean emptyChest(final Chest chest) {
	    ItemStack[] items = chest.getInventory().getContents();
	    for (ItemStack item : items) {
	    	if (item != null) {
	    		return false;
	    	}
	    }
	    return true;
	}
	
	
	/**
	 * Test that inventory is a death chest inventory
	 * @param inventory
	 * @return
	 */
	private boolean inventoryIsDeathChest(final Inventory inventory) {
		
		// if inventory is not a chest, return false
	    if (!inventory.getType().equals(InventoryType.CHEST)) {
	    	return false;
	    }
	    
	    // if inventory holder is null, return false
	    if (inventory.getHolder() == null) {
	    	return false;
	    }
	    
	    // try to get inventory holder block
    	Block block = null;
    	
    	try {
			if (inventory.getHolder() instanceof DoubleChest) {
				DoubleChest doubleChest;
				doubleChest = (DoubleChest) inventory.getHolder();
				block = doubleChest.getLocation().getBlock();
			}
			else {
				Chest chest;
				chest = (Chest) inventory.getHolder();
				block = chest.getBlock();
			}
		} catch (Exception e) {
			return false;
		}

		// if inventory holder block is not a DeathChestBlock, return false
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return false;
		}
		return true;
	}

}
