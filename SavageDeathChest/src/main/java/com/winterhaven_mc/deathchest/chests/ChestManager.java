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


/**
 * A class that tracks the state of death chests and chest blocks
 */
public final class ChestManager {

	// reference to main class
	private final PluginMain plugin;

	private final ChestIndex chestIndex;

	private final BlockIndex blockIndex;

	public final ReplaceableBlocks replaceableBlocks;

	// DeathChest material types
	final static Set<Material> deathChestMaterials =
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
					Material.CHEST,
					Material.WALL_SIGN,
					Material.SIGN)));


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public ChestManager(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		replaceableBlocks = new ReplaceableBlocks(plugin);

		// initialize chestIndex
		chestIndex = new ChestIndex();

		// initialize blockIndex
		blockIndex = new BlockIndex();
	}


	/**
	 * Load death chest blocks from datastore.
	 * Expire death chest blocks whose time has passed.
	 * schedule tasks to expire remaining loaded chests.
	 */
	public final void loadDeathChests() {

		// populate chestIndex with all death chest records retrieved from datastore
		for (DeathChest deathChest : plugin.dataStore.getAllChestRecords()) {
			this.addDeathChest(deathChest);
		}

		// populate chest block map with all valid chest blocks retrieved from datastore
		for (ChestBlock chestBlock : plugin.dataStore.getAllBlockRecords()) {

			// get chest block type from in game block
			ChestBlockType chestBlockType = ChestBlockType.getType(chestBlock.getLocation().getBlock());

			// if chest block type is null or parent chest not in chest map, delete block record
			if (chestBlockType == null || !chestIndex.containsKey(chestBlock.getChestUUID())) {
				plugin.dataStore.deleteBlockRecord(chestBlock);
			}
			else {
				// add chestBlock to block index
				this.blockIndex.addChestBlock(chestBlockType, chestBlock);
			}
		}

		// get current time
		long currentTime = System.currentTimeMillis();

		// expire chests with no blocks or past expiration
		for (DeathChest deathChest : chestIndex.getChests()) {

			// if DeathChest has no children, remove from map and datastore
			if (this.getBlockSet(deathChest.getChestUUID()).isEmpty()) {
				chestIndex.removeDeathChest(deathChest);
				plugin.dataStore.deleteChestRecord(deathChest);
			}
			else if (deathChest.getExpirationTime() < currentTime) {
				deathChest.expire();
			}
			else {
				// set chest metadata
				deathChest.setMetadata();
			}
		}
	}


	/**
	 * Put DeathChest object in map
	 *
	 * @param deathChest the DeathChest object to put in map
	 */
	final void addDeathChest(final DeathChest deathChest) {
		this.chestIndex.addChest(deathChest);
	}


	/**
	 * Get DeathChest object by chestUUID
	 *
	 * @param chestUUID UUID of DeathChest object to retrieve
	 * @return DeathChest object, or null if no DeathChest exists in map with passed chestUUID
	 */
	public final DeathChest getDeathChest(final UUID chestUUID) {
		return this.chestIndex.getDeathChest(chestUUID);
	}


	/**
	 * Get DeathChest object by block
	 *
	 * @param block the block to retrieve DeathChest object
	 * @return DeathChest object, or null if no DeathChest exists in map that contains passed block location
	 */
	public final DeathChest getDeathChest(final Block block) {

		ChestBlock chestBlock = this.blockIndex.getChestBlock(block.getLocation());

		if (chestBlock == null) {
			return null;
		}

		return getDeathChest(chestBlock.getChestUUID());
	}


	/**
	 * Remove DeathChest object from chest index
	 *
	 * @param deathChest the DeathChest object to remove from map
	 */
	final void removeDeathChest(final DeathChest deathChest) {
		this.chestIndex.removeDeathChest(deathChest);
	}


	/**
	 * Put ChestBlock object in block index
	 *
	 * @param chestBlock the ChestBlock to put in map
	 */
	final void addChestBlock(final ChestBlockType chestBlockType, final ChestBlock chestBlock) {
		this.blockIndex.addChestBlock(chestBlockType, chestBlock);
	}


	/**
	 * Get ChestBlock object from block index by location
	 *
	 * @param location the location to retrieve ChestBlock object
	 * @return ChestBlock object, or null if no ChestBlock exists in map with passed location
	 */
	public final ChestBlock getChestBlock(final Location location) {
		return this.blockIndex.getChestBlock(location);
	}


	/**
	 * Get chestBlock set from block index by chest uuid
	 *
	 * @param chestUUID the UUID of the chest of which to retrieve a set of chest blocks
	 * @return Set of Blocks in uuidBlockMap, or empty set if no blocks exist for chest UUID
	 */
	public final Set<ChestBlock> getBlockSet(final UUID chestUUID) {
		return this.blockIndex.getChestBlockSet(chestUUID);
	}


	/**
	 * Get chestBlock map from block index by chest uuid
	 *
	 * @param chestUUID the UUID of the chest of which to retrieve a map of chest blocks
	 * @return Map of Blocks in uuidBlockMap, or empty map if no blocks exist for chest UUID
	 */
	final Map<ChestBlockType, ChestBlock> getChestBlockMap(final UUID chestUUID) {
		return this.blockIndex.getChestBlockMap(chestUUID);
	}


	/**
	 * Remove ChestBlock object from map
	 *
	 * @param chestBlock the ChestBlock object to remove from map
	 */
	final void removeChestBlock(final ChestBlock chestBlock) {
		this.blockIndex.removeChestBlock(chestBlock);
	}


	/**
	 * Test if ChestBlock exists in map with passed block location
	 *
	 * @param block the block to check for existence in map
	 * @return {@code true} if a ChestBlock exists in map with passed block location,
	 * {@code false} if no ChestBlock exists in map with passed block location
	 */
	public final boolean isChestBlock(final Block block) {

		// if passed block is null, return false
		if (block == null) {
			return false;
		}

		// confirm block is death chest material
		if (!deathChestMaterials.contains(block.getType())) {
			return false;
		}

		// if passed block location is in chest block map return true, else return false
		return this.blockIndex.containsKey(block.getLocation());
	}


	/**
	 * Test if a block is a DeathChest chest block
	 *
	 * @param block The block to test
	 * @return {@code true} if block is Material.CHEST and block location exists in block map, {@code false} if not
	 */
	public final boolean isChestBlockChest(final Block block) {

		// if passed block is null return false
		if (block == null) {
			return false;
		}

		// if passed block is chest and is in block map, return true; else return false
		return (block.getType().equals(Material.CHEST) && blockIndex.containsKey(block.getLocation()));
	}


	/**
	 * Test if a block is a deathchest sign
	 *
	 * @param block The block to test if it is a DeathSign
	 * @return {@code true} if block is Material.SIGN or Material.WALL_SIGN and block location exists in block map,
	 * {@code false} if not
	 */
	public final boolean isChestBlockSign(final Block block) {

		// if passed block is null return false
		if (block == null) {
			return false;
		}

		// if block is sign or wall sign material and exists in block map, return true
		return ((block.getType().equals(Material.SIGN)
				|| block.getType().equals(Material.WALL_SIGN))
				&& blockIndex.containsKey(block.getLocation()));
	}


	/**
	 * Test that inventory is a death chest inventory
	 *
	 * @param inventory The inventory whose holder will be tested to see if it is a DeathChest
	 * @return {@code true} if the inventory's holder is a DeathChest, {@code false} if not
	 */
	public final boolean isDeathChestInventory(final Inventory inventory) {

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
		}
		catch (Exception e) {
			if (plugin.debug) {
				plugin.getLogger().warning("isDeathChest(inventory) threw an exception "
						+ "while trying to get inventory holder block.");
				plugin.getLogger().warning(e.getMessage());
			}
			return false;
		}

		// if inventory holder block is a DeathChest return true, else return false
		return this.isChestBlockChest(block);
	}


	public final Collection<DeathChest> getChestList() {
		return this.chestIndex.getChests();
	}

}
