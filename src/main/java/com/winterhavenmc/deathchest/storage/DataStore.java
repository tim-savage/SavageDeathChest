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

package com.winterhavenmc.deathchest.storage;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.chests.ChestBlock;
import com.winterhavenmc.deathchest.chests.DeathChest;

import java.util.*;


/**
 * An interface that declares methods for managing persistent storage of death chests and chest blocks.
 */
public interface DataStore {

	/**
	 * Initialize the datastore
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("JavaDoc")
	void initialize() throws Exception;


	/**
	 * Retrieve a collection of all chest records from the datastore
	 *
	 * @return List of DeathChest
	 */
	Collection<DeathChest> selectAllChestRecords();


	/**
	 * Insert a chest record in the datastore
	 *
	 * @param deathChests a collection of DeathChest objects to insert into the datastore
	 */
	int insertChestRecords(final Collection<DeathChest> deathChests);


	/**
	 * Delete a chest record from the datastore
	 *
	 * @param deathChest the chest to delete
	 */
	void deleteChestRecord(final DeathChest deathChest);


	/**
	 * Retrieve a collection of all block records from the datastore
	 *
	 * @return List of ChestBlock
	 */
	Collection<ChestBlock> selectAllBlockRecords();


	/**
	 * Insert block records in the datastore
	 *
	 * @param blockRecords a collection of ChestBlock objects to insert in the datastore
	 */
	int insertBlockRecords(final Collection<ChestBlock> blockRecords);


	/**
	 * Delete a block record from the datastore
	 *
	 * @param chestBlock the chest block to delete
	 */
	void deleteBlockRecord(final ChestBlock chestBlock);


	/**
	 * Close the datastore
	 */
	void close();


	/**
	 * Sync the datastore to disk
	 */
	void sync();


	/**
	 * Delete the datastore file or equivalent
	 */
	@SuppressWarnings("UnusedReturnValue")
	boolean delete();


	/**
	 * Check if the datastore is initialized
	 *
	 * @return {@code true} if the datastore is initialized, {@code false} if it is not
	 */
	boolean isInitialized();


	/**
	 * Get the datastore type
	 *
	 * @return the datastore type of this datastore instance
	 */
	DataStoreType getType();


	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 *
	 * @param plugin reference to plugin main class
	 * @return the new datastore
	 */
	static DataStore connect(final PluginMain plugin) {

		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// get new data store of specified type
		DataStore newDataStore = dataStoreType.connect(plugin);

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore + " datastore!");
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
		}

		// convert any existing data stores to new type
		DataStoreType.convertAll(plugin, newDataStore);

		// return initialized data store
		return newDataStore;
	}

}
