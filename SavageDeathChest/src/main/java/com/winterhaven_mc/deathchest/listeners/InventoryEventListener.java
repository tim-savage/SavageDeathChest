package com.winterhaven_mc.deathchest.listeners;


import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.util.ProtectionPlugin;
import com.winterhaven_mc.deathchest.chests.DeathChest;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


/**
 * A class that contains {@code EventHandler} methods to process inventory related events
 */

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
		if (!plugin.chestManager.isDeathChestInventory(inventory)) {
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

		// if inventory is a chest, get chest block
		if (inventory.getHolder() instanceof Chest) {
			Chest chest = (Chest) inventory.getHolder();
			block = chest.getBlock();
		}

		// if block is not a death chest, do nothing and return
		if (!plugin.chestManager.isChestBlockChest(block)) {
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

		// get event inventory
		final Inventory inventory = event.getInventory();

		// if inventory is null, do nothing and return
		if (inventory == null) {
			return;
		}

		// if inventory type is not a chest, do nothing and return
		if (!inventory.getType().equals(InventoryType.CHEST)) {
			return;
		}

		// if inventory location is null, do nothing and return
		if (inventory.getLocation() == null) {
			return;
		}

		// get inventory block from location
		final Block block = inventory.getLocation().getBlock();

		final DeathChest deathChest = plugin.chestManager.getDeathChest(block);

		// if inventory is not a DeathChest inventory, return
		if (deathChest == null) {
			return;
		}

		// if inventory is empty, destroy chest(s) and sign
		if (isEmpty(inventory)) {
			deathChest.destroy();
		}
	}


	/**
	 * Prevent hoppers from removing or inserting items in death chests
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onInventoryMoveItem(final InventoryMoveItemEvent event) {

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// get inventories involved in event
		final Inventory destination = event.getDestination();
		final Inventory source = event.getSource();

		// prevent extracting items from death chest using hopper
		if (plugin.chestManager.isDeathChestInventory(source)) {
			event.setCancelled(true);
			return;
		}

		// prevent inserting items into death chest using hopper if prevent-item-placement configured true
		if (plugin.getConfig().getBoolean("prevent-item-placement")) {

			// if destination inventory is a death chest, cancel event and return
			if (plugin.chestManager.isDeathChestInventory(destination)) {
				event.setCancelled(true);
			}
		}
	}


	/**
	 * Prevent placing items in death chests if configured
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onInventoryClick(final InventoryClickEvent event) {

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// if prevent-item-placement is configured false, do nothing and return
		if (!plugin.getConfig().getBoolean("prevent-item-placement")) {
			return;
		}

		final Inventory inventory = event.getInventory();
		final InventoryAction action = event.getAction();

		// if inventory is a death chest inventory
	    if (plugin.chestManager.isDeathChestInventory(inventory)) {

			// if click action is place, test for chest slots
			if (action.equals(InventoryAction.PLACE_ALL) 
					|| action.equals(InventoryAction.PLACE_SOME)
					|| action.equals(InventoryAction.PLACE_ONE)
					|| action.equals(InventoryAction.SWAP_WITH_CURSOR)) {

				// if slot is in chest inventory area, check for player override permission
				if (event.getRawSlot() < inventory.getSize()) {

					// if player does not have allow-place permission, cancel event
					if (!event.getWhoClicked().hasPermission("deathchest.allow-place")) {
						event.setCancelled(true);
					}
				}
				return;
			}

			// prevent shift-click transfer to death chest
			if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {

				if (event.getRawSlot() >= inventory.getSize()) {

					// if player does not have allow-place permission, cancel event
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

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		final Inventory inventory = event.getInventory();

		// if inventory is a death chest inventory
	    if (plugin.chestManager.isDeathChestInventory(inventory)) {

			// if prevent-item-placement is configured false, do nothing and return
			if (!plugin.getConfig().getBoolean("prevent-item-placement")) {
				return;
			}

			// if player has allow-place permission, do nothing and return
			if (event.getWhoClicked().hasPermission("deathchest.allow-place")) {
				return;
			}

			// iterate over dragged slots and if any are above max slot, cancel event
			for (int slot : event.getRawSlots()) {
				if (slot < inventory.getSize()) {
					event.setCancelled(true);
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
