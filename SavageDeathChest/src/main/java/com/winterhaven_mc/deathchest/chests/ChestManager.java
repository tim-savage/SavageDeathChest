package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.PluginMain;

import com.winterhaven_mc.deathchest.storage.DataStore;
import com.winterhaven_mc.deathchest.storage.DataStoreType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.*;


/**
 * A class that tracks the state of death chests and chest blocks
 */
public final class ChestManager {

	// reference to main class
	private final PluginMain plugin;

	// map of death chests
	private final ChestIndex chestIndex;

	// map of chest blocks
	private final BlockIndex blockIndex;

	// instantiate datastore
	private DataStore dataStore = DataStore.create();

	// set of replaceable blocks
	public final ReplaceableBlocks replaceableBlocks;

	// DeathChest material types
	final static Set<Material> deathChestMaterials =
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
					Material.CHEST,
					Material.OAK_WALL_SIGN,
					Material.OAK_SIGN)));


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public ChestManager(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// initialize replaceableBlocks
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

		if (plugin.debug) {
			plugin.getLogger().info("Loading Death Chests...");
		}

		// populate chestIndex with all death chest records retrieved from datastore
		for (DeathChest deathChest : dataStore.selectAllChestRecords()) {
			this.addDeathChest(deathChest);
		}

		// populate chest block map with all valid chest blocks retrieved from datastore
		for (ChestBlock chestBlock : dataStore.selectAllBlockRecords()) {

			// if chest block location is null, continue to next chest block
			if (chestBlock.getLocation() == null) {
				if (plugin.debug) {
					plugin.getLogger().info("chest block " + chestBlock.getChestUid() + " has null location.");
				}
				continue;
			}

			// get chest block type from in game block
			ChestBlockType chestBlockType = ChestBlockType.getType(chestBlock.getLocation().getBlock());

			// if chest block type is null or parent chest not in chest map, delete block record
			if (chestBlockType == null || !chestIndex.containsKey(chestBlock.getChestUid())) {
				dataStore.deleteBlockRecord(chestBlock);
			}
			else {
				// add chestBlock to block index
				this.blockIndex.put(chestBlockType, chestBlock);
			}
		}

		// get current time
		long currentTime = System.currentTimeMillis();

		// expire chests with no blocks or past expiration
		for (DeathChest deathChest : chestIndex.values()) {

			// if DeathChest has no children, remove from index and datastore
			if (this.getBlockSet(deathChest.getChestUid()).isEmpty()) {
				chestIndex.remove(deathChest);
				dataStore.deleteChestRecord(deathChest);
			}
			// if DeathChest is past expiration, expire chest
			else if (deathChest.getExpirationTime() < currentTime) {
				deathChest.expire();
			}
			else {
				// set chest metadata
				deathChest.setMetadata();
				if (plugin.debug) {
					plugin.getLogger().info("[loadDeathChests] Setting metadata for chest " + deathChest.getChestUid());
				}
			}
		}
	}


	/**
	 * Put DeathChest object in map
	 *
	 * @param deathChest the DeathChest object to put in map
	 */
	final void addDeathChest(final DeathChest deathChest) {
		this.chestIndex.put(deathChest);
	}


	/**
	 * Get DeathChest object by chestUUID
	 *
	 * @param chestUUID UUID of DeathChest object to retrieve
	 * @return DeathChest object, or null if no DeathChest exists in map with passed chestUUID
	 */
	public final DeathChest getDeathChest(final UUID chestUUID) {
		return this.chestIndex.get(chestUUID);
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

		return getDeathChest(chestBlock.getChestUid());
	}


	/**
	 * Remove DeathChest object from chest index
	 *
	 * @param deathChest the DeathChest object to remove from map
	 */
	final void removeDeathChest(final DeathChest deathChest) {
		this.chestIndex.remove(deathChest);
	}


	/**
	 * Put ChestBlock object in block index
	 *
	 * @param chestBlock the ChestBlock to put in map
	 */
	final void addChestBlock(final ChestBlockType chestBlockType, final ChestBlock chestBlock) {
		this.blockIndex.put(chestBlockType, chestBlock);
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
		this.blockIndex.remove(chestBlock);
	}


	/**
	 * Test if ChestBlock exists in map with passed block location
	 *
	 * @param block the block to check for existence in block index
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
	 * @return {@code true} if block is Material.CHEST and block location exists in block index, {@code false} if not
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
	 * @return {@code true} if block is Material.SIGN or Material.WALL_SIGN and block location exists in block index,
	 * {@code false} if not
	 */
	public final boolean isChestBlockSign(final Block block) {

		// if passed block is null return false
		if (block == null) {
			return false;
		}

		// get block state
		BlockState blockState = block.getState();

		// if block is sign or wall sign and exists in block index, return true
		return ((blockState instanceof WallSign
				|| blockState instanceof Sign)
				&& blockIndex.containsKey(block.getLocation()));
	}


	/**
	 * Test if an inventory is a death chest inventory
	 *
	 * @param inventory The inventory whose holder will be tested to see if it is a DeathChest
	 * @return {@code true} if the inventory's holder is a DeathChest, {@code false} if not
	 */
	public final boolean isDeathChestInventory(final Inventory inventory) {

		// if passed inventory is null, return false
		if (inventory == null) {
			return false;
		}

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


	/**
	 * Get all death chests in chest index
	 * @return Collection of DeathChest - all death chests in the chest index
	 */
	public final Collection<DeathChest> getAllChests() {
		return this.chestIndex.values();
	}


	public final void insertChestRecords(Collection<DeathChest> deathChests) {
		dataStore.insertChestRecords(deathChests);
	}


	public final void deleteBlockRecord(ChestBlock chestBlock) {
		dataStore.deleteBlockRecord(chestBlock);
	}


	public final void deleteChestRecord(DeathChest deathChest) {
		dataStore.deleteChestRecord(deathChest);
	}


	public final void closeDataStore() {
		dataStore.close();
	}


	public final String getDataStoreType() {
		return dataStore.getType().toString();
	}


	/**
	 * Check if a new datastore type has been configured, and
	 * convert old datastore to new type if necessary
	 */
	public void reload() {

		// get current datastore type
		DataStoreType currentType = dataStore.getType();

		// get configured datastore type
		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// create new datastore
			dataStore = DataStore.create(newType, dataStore);
		}
	}

}
