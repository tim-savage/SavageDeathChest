package com.winterhaven_mc.deathchest.chests;

import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


final class BlockIndex {

	// map of ChestBlocks indexed by location
	private final Map<Location, ChestBlock> locationMap;

	// nested map of ChestBlocks indexed by ChestUUID, ChestBlockType
	private final Map<UUID, EnumMap<ChestBlockType, ChestBlock>> uuidMap;


	/**
	 * Constructor
	 */
	BlockIndex() {

		// initialize location map
		locationMap = new ConcurrentHashMap<>();

		// initialize uuid map
		uuidMap = new ConcurrentHashMap<>();
	}


	/**
	 * Put ChestBlock object in map
	 *
	 * @param chestBlockType the ChestBlockType of ChestBlock to put in map
	 * @param chestBlock the ChestBlock to put in map
	 */
	final void addChestBlock(final ChestBlockType chestBlockType, final ChestBlock chestBlock) {

		// if passed key or value is null, do nothing and return
		if (chestBlockType == null || chestBlock == null) {
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
	 *
	 * @param location the location to retrieve ChestBlock object
	 * @return ChestBlock object, or null if no ChestBlock exists in map with passed location
	 */
	final ChestBlock getChestBlock(final Location location) {
		return this.locationMap.get(location);
	}


	/**
	 * Getter method for DeathChest chestBlocks
	 *
	 * @param chestUUID the UUID of the chest of which to retrieve a set of chest blocks
	 * @return Set of Blocks in uuidMap, or empty set if no blocks exist for chest UUID
	 */
	final Set<ChestBlock> getChestBlockSet(final UUID chestUUID) {

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
	 *
	 * @param chestUUID the UUID of the chest of which to retrieve a map of chest blocks
	 * @return Map of Blocks in uuidMap, or empty map if no blocks exist for chest UUID
	 */
	final Map<ChestBlockType, ChestBlock> getChestBlockMap(final UUID chestUUID) {

		// create empty map for return
		Map<ChestBlockType, ChestBlock> returnMap = new EnumMap<>(ChestBlockType.class);

		// if chestUUID exists in map, add values to returnMap
		if (chestUUID != null && this.uuidMap.containsKey(chestUUID)) {
			returnMap.putAll(this.uuidMap.get(chestUUID));
		}
		return returnMap;
	}


	/**
	 * Remove ChestBlock object from map
	 *
	 * @param chestBlock the ChestBlock object to remove from map
	 */
	final void removeChestBlock(final ChestBlock chestBlock) {

		// check for null key
		if (chestBlock == null) {
			return;
		}

		// get chest location
		Location location = chestBlock.getLocation();

		// remove chest block from location map
		this.locationMap.remove(location);

		// if passed chest block UUID is not null, remove chest block from uuid map
		if (chestBlock.getChestUUID() != null) {

			// get chest UUID
			UUID chestUUID = chestBlock.getChestUUID();

			// iterate over inner map
			for (ChestBlockType chestBlockType : this.uuidMap.get(chestUUID).keySet()) {

				// if passed chest block location equals mapped chest block location, remove block from inner map
				if (this.uuidMap.get(chestUUID).get(chestBlockType).getLocation().equals(location)) {
					this.uuidMap.get(chestUUID).remove(chestBlockType);

					// if inner map is now empty, remove from outer map
					if (this.uuidMap.get(chestUUID).isEmpty()) {
						this.uuidMap.remove(chestUUID);
					}
				}
			}
		}
	}


	/**
	 * Check for location key in map
	 *
	 * @param location the key to check
	 * @return {@code true} if location key exists in map, {@code false} if it does not
	 */
	final boolean containsKey(final Location location) {

		// check for null location
		if (location == null) {
			return false;
		}

		return locationMap.containsKey(location);
	}

}
