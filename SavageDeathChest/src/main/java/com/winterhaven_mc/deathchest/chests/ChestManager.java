package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.PluginMain;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ChestManager {

	// reference to main class
	private PluginMain plugin;

	// map of DeathChests
	private Map<UUID, DeathChest> deathChestMap;

	// map of ChestBlocks
	private Map<Location, ChestBlock> chestBlockMap;

	public ReplaceableBlocks replaceableBlocks;

	// DeathChest material types
	private final Set<Material> deathChestMaterials =
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
					Material.CHEST,
					Material.WALL_SIGN,
					Material.SIGN)));


	/**
	 * Class constructor
	 * @param plugin reference to main class
	 */
	public ChestManager(PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		replaceableBlocks = new ReplaceableBlocks(plugin);

		// initialize deathChestMap
		deathChestMap = new ConcurrentHashMap<>();

		// initialize chestBlockMap
		chestBlockMap = new ConcurrentHashMap<>();

		// load all chests
		loadDeathChests();
	}


	/**
	 * Load death chest blocks from datastore.
	 * Expire death chest blocks whose time has passed.
	 * schedule tasks to expire remaining loaded chests.
	 */
	private void loadDeathChests() {

		// retrieve all death chest records from datastore
		List<DeathChest> chestQueryResult = plugin.dataStore.getAllChestRecords();

		// populate deathChestMap with all death chest records retrieved from datastore
		for (DeathChest deathChest : chestQueryResult) {
			deathChestMap.put(deathChest.getChestUUID(),deathChest);
		}

		// retrieve all death block records from datastore
		List<ChestBlock> blockQueryResult = plugin.dataStore.getAllBlockRecords();

		// populate death chests in map with all valid chest blocks retrieved from datastore
		for (ChestBlock chestBlock : blockQueryResult) {

			// if chestUUID exists in deathChestMap and in game block at location is valid chest material,
			// add chestBlock to deathChest in map
			if (deathChestMap.containsKey(chestBlock.getChestUUID())
					&& plugin.chestManager.getDeathChestMaterials().contains(chestBlock.getLocation().getBlock().getType())) {
				deathChestMap.get(chestBlock.getChestUUID()).addChestBlock(chestBlock);
				chestBlockMap.put(chestBlock.getLocation(),chestBlock);
			}
			else {
				plugin.dataStore.deleteBlockRecord(chestBlock);
			}
		}

		// get current time
		long currentTime = System.currentTimeMillis();

		// expire chests with no blocks or past expiration
		for (DeathChest deathChest : deathChestMap.values()) {

			if (deathChest.getChestBlocks().isEmpty()
					|| deathChest.getExpiration() > currentTime) {
				deathChest.expire();
			}
			else {
				// set chest metadata
				deathChest.setMetadata();

				// create chest expire task
				deathChest.createExpireTask();
			}
		}
	}


	/**
	 * Get DeathChest object by chestUUID
	 * @param chestUUID UUID of DeathChest object to retrieve
	 * @return DeathChest
	 */
	public DeathChest getDeathChest(UUID chestUUID) {
		return this.deathChestMap.get(chestUUID);
	}


	/**
	 * Get DeathChest object by block
	 * @param block the block to retrieve DeathChest object
	 * @return DeathChest object, or null if no DeathChest exists in map that contains passed block
	 */
	public DeathChest getDeathChest(Block block) {

		ChestBlock chestBlock = getChestBlock(block.getLocation());

		if (chestBlock == null) {
			return null;
		}

		return getDeathChest(chestBlock.getChestUUID());
	}


	/**
	 * Put DeathChest object in map
	 * @param deathChest the DeathChest object to put in map
	 */
	void addDeathChest(DeathChest deathChest) {

		this.deathChestMap.put(deathChest.getChestUUID(),deathChest);

		if (plugin.debug) {
			plugin.getLogger().info("DeathChest added to Map.");
		}
	}


	/**
	 * Remove DeathChest object from map
	 * @param deathChest the DeathChest object to remove from map
	 */
	void removeDeathChest(DeathChest deathChest) {
		this.deathChestMap.remove(deathChest.getChestUUID());
	}


	/**
	 * Get ChestBlock object by location
	 * @param location the location to retrieve ChestBlock object
	 * @return ChestBlock object, or null if no ChestBlock exists in map with passed location
	 */
	public ChestBlock getChestBlock(Location location) { return this.chestBlockMap.get(location); }


	/**
	 * Put ChestBlock object in map
	 * @param chestBlock the ChestBlock to put in map
	 */
	void addChestBlock(ChestBlock chestBlock) {

		this.chestBlockMap.put(chestBlock.getLocation(),chestBlock);

		if (plugin.debug) {
			plugin.getLogger().info("ChestBlock added to Map.");
		}
	}


	/**
	 * Remove ChestBlock object from map
	 * @param chestBlock the ChestBlock object to remove from map
	 */
	void removeChestBlock(ChestBlock chestBlock) { this.chestBlockMap.remove(chestBlock.getLocation()); }


	/**
	 * Test if ChestBlock exists in map with passed block location
	 * @param block the block to check for existence in map
	 * @return {@code true} if a ChestBlock exists in map with passed block location,
	 * {@code false} if no ChestBlock exists in map with passed block location
	 */
	boolean isChestBlock(Block block) {
		return chestBlockMap.containsKey(block.getLocation());
	}


	/**
	 * Get set of valid death chest materials
	 * @return set of valid death chest materials
	 */
	Set<Material> getDeathChestMaterials() { return this.deathChestMaterials; }


	/**
	 * Test if a block is a DeathChestBlock; either signs or chests with deathchest metadata
	 * @param block The block to test if it is a DeathChestBlock
	 * @return boolean True if block has deathchest-owner metadata, false if it does not
	 */
	public boolean isDeathChestComponent(final Block block) {

		// if passed block is null, return false
		if (block == null) {
			return false;
		}

		// confirm block is death chest material
		if (!deathChestMaterials.contains(block.getType())) {
			return false;
		}

		// if passed block location is in chest block map return true, else return false
		return chestBlockMap.containsKey(block.getLocation());
	}


	/**
	 * Test if a block is a DeathChest chest block
	 * @param block The block to test
	 * @return true if block is a DeathChest chest, false if not
	 */
	public boolean isDeathChestChestBlock(final Block block) {

		// if passed block is null return false
		if (block == null) {
			return false;
		}

		// if passed block is not a chest return false
		if (!block.getType().equals(Material.CHEST)) {
			return false;
		}

		// if passed block is in chest block map return true, else return false
		return chestBlockMap.containsKey(block.getLocation());
	}


	/**
	 * Test if a block is a deathchest sign; wall sign or sign post with deathchest metadata
	 * @param block The block to test if it is a DeathSign
	 * @return true if block is a deathchest sign, false if not
	 */
	public boolean isDeathChestSignBlock(final Block block) {

		// if passed block is null return false
		if (block == null) {
			return false;
		}

		// if block is wall sign or sign post and has death chest metadata, return true
		return ((block.getType().equals(Material.WALL_SIGN)
				|| block.getType().equals(Material.SIGN)))
				&& block.hasMetadata("deathchest-owner");
	}


	/**
	 * Test that inventory is a death chest inventory
	 * @param inventory The inventory whose holder will be tested to see if it is a DeathChest
	 * @return {@code true} if the inventory's holder is a DeathChest, {@code false} if not
	 */
	public boolean isDeathChestInventory(final Inventory inventory) {

		// if inventory type is not a chest inventory, return false
		if (!inventory.getType().equals(InventoryType.CHEST)) {
			return false;
		}

		// if inventory holder is null, return false
		if (inventory.getHolder() == null) {
			return false;
		}

		// try to get inventory holder block
		Block block;

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
			if (plugin.debug) {
				plugin.getLogger().warning("isDeathChest(inventory) threw an exception "
						+ "while trying to get inventory holder block.");
				plugin.getLogger().warning(e.getMessage());
			}
			return false;
		}

		// if inventory holder block is a DeathChest return true, else false
		return plugin.chestManager.isDeathChestChestBlock(block);
	}

}
