package com.winterhaven_mc.deathchest.chests;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class ChestIndex {

	// map of DeathChests
	private final Map<UUID, DeathChest> deathChestMap;


	/**
	 * Constructor
	 */
	ChestIndex() {
		deathChestMap = new ConcurrentHashMap<>();
	}


	/**
	 * Get DeathChest object by chestUUID
	 * @param chestUUID UUID of DeathChest object to retrieve
	 * @return DeathChest object, or null if no DeathChest exists in map with passed chestUUID
	 */
	DeathChest getDeathChest(final UUID chestUUID) {
		return this.deathChestMap.get(chestUUID);
	}


	/**
	 * Put DeathChest object in map
	 * @param deathChest the DeathChest object to put in map
	 */
	void addChest(final DeathChest deathChest) {
		this.deathChestMap.put(deathChest.getChestUUID(),deathChest);
	}


	/**
	 * Remove DeathChest object from map
	 * @param deathChest the DeathChest object to remove from map
	 */
	void removeDeathChest(final DeathChest deathChest) {
		this.deathChestMap.remove(deathChest.getChestUUID());
	}


	/**
	 * Check if chestUUID key exists in map
	 * @param chestUUID the chest UUID to check
	 * @return {@code true} if key exists in map, {@code false} if it does not
	 */
	boolean containsKey(final UUID chestUUID) {

		// check for null chestUUID
		if (chestUUID == null) {
			return false;
		}

		return deathChestMap.containsKey(chestUUID);
	}


	/**
	 * Get collection of all chests in map
	 * @return Collection of DeathChests in map
	 */
	Collection<DeathChest> getChests() {
		return deathChestMap.values();
	}

}

