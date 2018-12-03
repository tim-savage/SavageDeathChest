package com.winterhaven_mc.deathchest.chests;

import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class BlockIndex {

	// map of ChestBlocks indexed by location
	private final Map<Location,ChestBlock> locationMap;

	// nested map of ChestBlocks indexed by ChestUUID, ChestBlockType
	private final Map<UUID, EnumMap<ChestBlockType,ChestBlock>> uuidMap;


	/**
	 * Constructor
	 */
	BlockIndex() {
		locationMap = new ConcurrentHashMap<>();
		uuidMap = new ConcurrentHashMap<>();
	}


	/**
	 * Put ChestBlock object in map
	 * @param chestBlock the ChestBlock to put in map
	 */
	void addChestBlock(final ChestBlockType chestBlockType, final ChestBlock chestBlock) {

		// if passed key or value is null, do nothing and return
		if (chestBlockType == null || chestBlock == null || chestBlock.getLocation() == null) {
			return;
		}

		// add chestBlock to locationMap
		this.locationMap.put(chestBlock.getLocation(), chestBlock);

		// if chestUUID key does not exist in map, add entry with chestUUID key and empty map as value
		if (!uuidMap.containsKey(chestBlock.getChestUUID())) {
			uuidMap.put(chestBlock.getChestUUID(), new EnumMap<>(ChestBlockType.class));
		}

		// add new entry to map with chestUUID as key
		uuidMap.get(chestBlock.getChestUUID()).put(chestBlockType, chestBlock);
	}


	/**
	 * Get ChestBlock object by location
	 * @param location the location to retrieve ChestBlock object
	 * @return ChestBlock object, or null if no ChestBlock exists in map with passed location
	 */
	ChestBlock getChestBlock(final Location location) {
		return this.locationMap.get(location);
	}


	/**
	 * Getter method for DeathChest chestBlocks
	 * @param chestUUID the UUID of the chest of which to retrieve a set of chest blocks
	 * @return Set of Blocks in uuidMap, or empty set if no blocks exist for chest UUID
	 */
	Set<ChestBlock> getChestBlockSet(final UUID chestUUID) {

		// create empty Set for return
		Set<ChestBlock> returnSet = new HashSet<>();

		// if chestUUID key exists in map, add map values to returnSet
		if (chestUUID != null && uuidMap.containsKey(chestUUID)) {
			returnSet.addAll((uuidMap.get(chestUUID)).values());
		}
		return returnSet;
	}


	/**
	 * Getter method for DeathChest chestBlocks
	 * @param chestUUID the UUID of the chest of which to retrieve a map of chest blocks
	 * @return Map of Blocks in uuidMap, or empty map if no blocks exist for chest UUID
	 */
	Map<ChestBlockType,ChestBlock> getChestBlockMap(final UUID chestUUID) {

		// create empty map for return
		Map<ChestBlockType,ChestBlock> returnMap = new EnumMap<>(ChestBlockType.class);

		// if chestUUID exists in map, add values to returnMap
		if (chestUUID != null && this.uuidMap.containsKey(chestUUID)) {
			returnMap.putAll(this.uuidMap.get(chestUUID));
		}
		return returnMap;
	}


	/**
	 * Remove ChestBlock object from map
	 * @param chestBlock the ChestBlock object to remove from map
	 */
	void removeChestBlock(final ChestBlock chestBlock) {
		this.locationMap.remove(chestBlock.getLocation());
	}


	/**
	 * Check for location key in map
	 * @param location the key to check
	 * @return {@code true} if location key exists in map, {@code false} if it does not
	 */
	boolean containsKey(final Location location) {

		// check for null location
		if (location == null) {
			return false;
		}

		return locationMap.containsKey(location);
	}
}
