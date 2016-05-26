package com.winterhaven_mc.deathchest.listeners;

import com.winterhaven_mc.deathchest.DeathChestBlock;
import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public final class InventoryEventListener implements Listener {

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
	 * @param event the event being handled by this method
	 */
	@EventHandler(priority=EventPriority.HIGH)
	public final void onInventoryOpen(final InventoryOpenEvent event) {

		// if event is not cancelled, do nothing and return
		if (!event.isCancelled()) {
			return;
		}

		// get event inventory
		final Inventory inventory = event.getInventory();

		// if inventory holder is not a death chest, do nothing and return		
		if (!DeathChestBlock.isDeathChest(inventory)) {
			return;
		}

		// if event entity is not a player, do nothing and return
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}

		// get event player
		final Player player = (Player) event.getPlayer();

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
		
		// if block is not a death chest, do nothing and return
		if (!DeathChestBlock.isDeathChest(block)) {
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
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onInventoryClose(final InventoryCloseEvent event) {
		
		// if remove-empty option is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("remove-empty")) {
			return;
		}
		
		if (event.getPlayer() instanceof Player) {
			
			final Player player = (Player)event.getPlayer();
			final Inventory inventory = event.getInventory();
	
			// get DeathChestBlock instance from inventory
			final DeathChestBlock deathChestBlock = DeathChestBlock.getChestInstance(inventory);

			// if inventory is not a DeathChest inventory, return
			if (deathChestBlock == null) {
				return;
			}
			
			// if inventory is empty, loot chest to destroy chest(s) and sign
			// TODO: create a method to destroy chest(s) and sign, and we won't need to deal with player here
			if (isEmpty(inventory)) {
				deathChestBlock.autoLoot(player);
			}
		}
	}


	/**
	 * Prevent hoppers from removing or inserting items in death chests
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onInventoryMoveItem(final InventoryMoveItemEvent event) {

		// get inventories involved in event
		final Inventory destination = event.getDestination();
		final Inventory source = event.getSource();
		
		// if source inventory is a death chest, cancel event and return
		if (DeathChestBlock.isDeathChest(source)) {
			event.setCancelled(true);
			return;
		}
		
		// if destination is a death chest and prevent-item-placement is true, cancel event and return
		if (DeathChestBlock.isDeathChest(destination) && plugin.getConfig().getBoolean("prevent-item-placement")) {
			event.setCancelled(true);
		}
	}


	/**
	 * Prevent placing items in death chests if configured
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onInventoryClick(final InventoryClickEvent event) {
		
		final Inventory inventory = event.getInventory();
		final InventoryAction action = event.getAction();
		
		// if inventory is a death chest inventory
	    if (DeathChestBlock.isDeathChest(inventory)) {
	    	
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
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onInventoryDrag(final InventoryDragEvent event) {
		
		final Inventory inventory = event.getInventory();
		
		// if inventory is a death chest inventory
	    if (DeathChestBlock.isDeathChest(inventory)) {
	
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
	 * Test if inventory is empty
	 * 
	 * @param inventory the inventory to test for emptiness
	 * @return true if inventory is empty, false if inventory has any contents
	 */
	private boolean isEmpty(final Inventory inventory) {
		
	    final ItemStack[] items = inventory.getContents();
	    for (ItemStack item : items) {
	    	if (item != null) {
	    		return false;
	    	}
	    }
	    return true;
	}

}
