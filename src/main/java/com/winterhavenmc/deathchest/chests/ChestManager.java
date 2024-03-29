/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathchest.chests;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.chests.deployment.DeploymentFactory;
import com.winterhavenmc.deathchest.storage.DataStore;
import com.winterhavenmc.deathchest.storage.DataStoreType;

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
 * A class that tracks the state of death chests and their component blocks
 */
public final class ChestManager {

	// reference to main class
	private final PluginMain plugin;

	// map of death chests
	private final ChestIndex chestIndex;

	// map of chest blocks
	private final BlockIndex blockIndex;

	// instantiate datastore
	private DataStore dataStore;

	// set of replaceable blocks
	private final ReplaceableBlocks replaceableBlocks;

	// DeathChest material types
	final static Collection<Material> deathChestMaterials = Set.of(
			Material.CHEST,
			Material.OAK_WALL_SIGN,
			Material.OAK_SIGN );

	private final DeploymentFactory deploymentFactory;


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

		// initialize datastore
		dataStore = DataStore.connect(plugin);

		// initialize chestIndex
		chestIndex = new ChestIndex();

		// initialize blockIndex
		blockIndex = new BlockIndex();

		deploymentFactory = new DeploymentFactory();
	}

	public DeploymentFactory getDeploymentFactory() {
		return this.deploymentFactory;
	}

	/**
	 * Load death chest blocks from datastore.
	 * Expire death chest blocks whose time has passed.
	 * schedule tasks to expire remaining loaded chests.
	 */
	public void loadChests() {

		if (plugin.getConfig().getBoolean("debug")) {
			plugin.getLogger().info("Loading Death Chests...");
		}

		// populate chestIndex with all death chest records retrieved from datastore
		for (DeathChest deathChest : dataStore.selectAllChestRecords()) {
			this.putChest(deathChest);
		}

		// populate chest block map with all valid chest blocks retrieved from datastore
		for (ChestBlock chestBlock : dataStore.selectAllBlockRecords()) {

			// if chest block location is null, continue to next chest block
			if (chestBlock.getLocation() == null) {
				if (plugin.getConfig().getBoolean("debug")) {
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
			if (this.getBlocks(deathChest.getChestUid()).isEmpty()) {
				chestIndex.remove(deathChest);
				dataStore.deleteChestRecord(deathChest);
			}
			// if DeathChest is past expiration (not infinite, denoted by zero or less expiration time), expire chest
			else if (deathChest.getExpirationTime() > 0 && deathChest.getExpirationTime() < currentTime) {
				deathChest.expire();
			}
			else {
				// set chest metadata
				deathChest.setMetadata();
				if (plugin.getConfig().getBoolean("debug")) {
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
	public void putChest(final DeathChest deathChest) {
		this.chestIndex.put(deathChest);
	}


	/**
	 * Get DeathChest object by chestUUID
	 *
	 * @param chestUUID UUID of DeathChest object to retrieve
	 * @return DeathChest object, or null if no DeathChest exists in map with passed chestUUID
	 */
	public DeathChest getChest(final UUID chestUUID) {
		return this.chestIndex.get(chestUUID);
	}


	/**
	 * Get DeathChest object by block
	 *
	 * @param block the block to retrieve DeathChest object
	 * @return DeathChest object, or null if no DeathChest exists in map that contains passed block location
	 */
	public DeathChest getChest(final Block block) {

		// if passed block is null, return null
		if (block == null) {
			return null;
		}

		// get chest block from index by location
		ChestBlock chestBlock = this.blockIndex.get(block.getLocation());

		// if returned chest block is null, return null
		if (chestBlock == null) {
			return null;
		}

		// return death chest referenced by uid in chest block
		return getChest(chestBlock.getChestUid());
	}


	public DeathChest getChest(final Inventory inventory) {

		// if inventory is not a death chest, do nothing and return
		if (!plugin.chestManager.isDeathChestInventory(inventory)) {
			return null;
		}

		// get inventory holder block (death chest)
		Block block = null;

		// if inventory is a chest, get chest block
		if (inventory.getHolder() instanceof Chest) {
			Chest chest = (Chest) inventory.getHolder();
			block = chest.getBlock();
		}

		// return death chest for block (returns null if block is not valid chest block)
		return getChest(block);
	}


	/**
	 * Remove DeathChest object from chest index
	 *
	 * @param deathChest the DeathChest object to remove from map
	 */
	void removeChest(final DeathChest deathChest) {
		this.chestIndex.remove(deathChest);
	}


	/**
	 * Put ChestBlock object in block index
	 *
	 * @param chestBlock the ChestBlock to put in map
	 */
	public void putBlock(final ChestBlockType chestBlockType, final ChestBlock chestBlock) {
		this.blockIndex.put(chestBlockType, chestBlock);
	}


	/**
	 * Get ChestBlock object from block index by location
	 *
	 * @param location the location to retrieve ChestBlock object
	 * @return ChestBlock object, or null if no ChestBlock exists in map with passed location
	 */
	@SuppressWarnings("unused")
	public ChestBlock getBlock(final Location location) {
		return this.blockIndex.get(location);
	}


	/**
	 * Get chestBlock set from block index by chest uuid
	 *
	 * @param chestUid the UUID of the chest of which to retrieve a set of chest blocks
	 * @return Set of Blocks in uuidBlockMap, or empty set if no blocks exist for chest UUID
	 */
	public Collection<ChestBlock> getBlocks(final UUID chestUid) {
		return this.blockIndex.getBlocks(chestUid);
	}


	/**
	 * Get chestBlock map from block index by chest uuid
	 *
	 * @param chestUid the UUID of the chest of which to retrieve a map of chest blocks
	 * @return Map of Blocks in uuidBlockMap, or empty map if no blocks exist for chest UUID
	 */
	Map<ChestBlockType, ChestBlock> getBlockMap(final UUID chestUid) {
		return this.blockIndex.getBlockMap(chestUid);
	}


	/**
	 * Remove ChestBlock object from map
	 *
	 * @param chestBlock the ChestBlock object to remove from map
	 */
	void removeBlock(final ChestBlock chestBlock) {
		this.blockIndex.remove(chestBlock);
	}


	/**
	 * Test if ChestBlock exists in map with passed block location
	 *
	 * @param block the block to check for existence in block index
	 * @return {@code true} if a ChestBlock exists in map with passed block location,
	 * {@code false} if no ChestBlock exists in map with passed block location
	 */
	public boolean isChestBlock(final Block block) {

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
	public boolean isChestBlockChest(final Block block) {

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
	public boolean isChestBlockSign(final Block block) {

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
	public boolean isDeathChestInventory(final Inventory inventory) {

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

		if (inventory.getHolder() instanceof Chest) {
			Chest chest;
			chest = (Chest) inventory.getHolder();
			block = chest.getBlock();
		}
		else if (inventory.getHolder() instanceof DoubleChest) {
			DoubleChest doubleChest;
			doubleChest = (DoubleChest) inventory.getHolder();
			block = doubleChest.getLocation().getBlock();
		}
		else {
			return false;
		}

		// if inventory holder block is a DeathChest return true, else return false
		return this.isChestBlockChest(block);
	}


	/**
	 * Get all death chests in chest index
	 * @return Collection of DeathChest - all death chests in the chest index
	 */
	public Collection<DeathChest> getAllChests() {
		return this.chestIndex.values();
	}


	public void insertChestRecords(final Collection<DeathChest> deathChests) {
		dataStore.insertChestRecords(deathChests);
	}


	public void deleteBlockRecord(final ChestBlock chestBlock) {
		dataStore.deleteBlockRecord(chestBlock);
	}


	public void deleteChestRecord(final DeathChest deathChest) {
		dataStore.deleteChestRecord(deathChest);
	}


	public void closeDataStore() {
		dataStore.close();
	}


	@SuppressWarnings("unused")
	public String getDataStoreType() {
		return dataStore.getType().toString();
	}


	public void reload() {
		replaceableBlocks.reload();

		// get current datastore type
		DataStoreType currentType = dataStore.getType();

		// get configured datastore type
		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// create new datastore
			dataStore = DataStore.connect(plugin);
		}
	}

	@SuppressWarnings("unused")
	public boolean isReplaceableBlock(final Material material) {
		return replaceableBlocks.contains(material);
	}

	public boolean isReplaceableBlock(final Block block) {
		return replaceableBlocks.contains(block.getType());
	}

	public ReplaceableBlocks getReplaceableBlocks() {
		return replaceableBlocks;
	}

	public int getChestCount() {
		return this.dataStore.getChestCount();
	}

}
