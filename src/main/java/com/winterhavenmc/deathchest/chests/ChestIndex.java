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

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


final class ChestIndex {

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
	 *
	 * @param chestUUID UUID of DeathChest object to retrieve
	 * @return DeathChest object, or null if no DeathChest exists in map with passed chestUUID
	 */
	DeathChest get(final UUID chestUUID) {

		// check for null key
		if (chestUUID == null) {
			return null;
		}

		return this.deathChestMap.get(chestUUID);
	}


	/**
	 * Put DeathChest object in map
	 *
	 * @param deathChest the DeathChest object to put in map
	 */
	void put(final DeathChest deathChest) {

		// check for null key
		if (deathChest == null || deathChest.getChestUid() == null) {
			return;
		}

		this.deathChestMap.put(deathChest.getChestUid(), deathChest);
	}


	/**
	 * Remove DeathChest object from map
	 *
	 * @param deathChest the DeathChest object to remove from map
	 */
	void remove(final DeathChest deathChest) {

		// check for null key
		if (deathChest == null || deathChest.getChestUid() == null) {
			return;
		}

		this.deathChestMap.remove(deathChest.getChestUid());
	}


	/**
	 * Check if chestUUID key exists in map
	 *
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
	 *
	 * @return Collection of DeathChests in map
	 */
	Collection<DeathChest> values() {
		return deathChestMap.values();
	}

}

