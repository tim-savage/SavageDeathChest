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
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;


/**
 * An enum whose values represent the types of data store available.<br>
 * Note: Only SQLite data store is implemented at this time.
 */
public enum DataStoreType {

	SQLITE("SQLite", "deathchests.db") {

		@Override
		public DataStore connect(final PluginMain plugin) {

			// create new SQLite datastore object
			return new DataStoreSQLite(plugin);
		}

		@Override
		boolean storageObjectExists(final JavaPlugin plugin) {
			// get path name to data store file
			File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getStorageName());
			return dataStoreFile.exists();
		}
	};

	private final String displayName;

	private final String storageName;

	private final static DataStoreType defaultType = DataStoreType.SQLITE;


	/**
	 * Class constructor
	 *
	 * @param displayName the formatted display name of a datastore type
	 */
	DataStoreType(final String displayName, final String storageName) {
		this.displayName = displayName;
		this.storageName = storageName;
	}


	/**
	 * Get new instance of DataStore of configured type
	 * @return new instance of DataStore
	 */
	public abstract DataStore connect(final PluginMain plugin);


	/**
	 * Getter for storage object name.
	 *
	 * @return the name of the backing store object for a data store type
	 */
	String getStorageName() {
		return storageName;
	}


	/**
	 * Test if datastore backing object (file, database) exists
	 *
	 * @param plugin reference to plugin main class
	 * @return true if backing object exists, false if not
	 */
	abstract boolean storageObjectExists(final JavaPlugin plugin);


	/**
	 * Get display name of DataStoreType
	 *
	 * @return String - display name of DataStoreType
	 */
	@Override
	public final String toString() {
		return this.displayName;
	}


	/**
	 * Attempt to match a DataStoreType by name
	 * @param name the name to attempt to match to a DataStoreType
	 * @return A DataStoreType whose name matched the passed string,
	 * or the default DataStoreType if no match
	 */
	public static DataStoreType match(final String name) {
		for (DataStoreType type : DataStoreType.values()) {
			if (type.toString().equalsIgnoreCase(name)) {
				return type;
			}
		}
		// no match; return default type
		return defaultType;
	}


	/**
	 * convert old data store to new data store
	 *
	 * @param oldDataStore the existing datastore to be converted from
	 * @param newDataStore the new datastore to be converted to
	 */
	static void convert(final PluginMain plugin, final DataStore oldDataStore, final DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}

		// if old datastore file exists, attempt to read all records
		if (oldDataStore.getType().storageObjectExists(plugin)) {

			plugin.getLogger().info("Converting existing " + oldDataStore + " datastore to "
					+ newDataStore + " datastore...");

			// initialize old datastore if necessary
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				}
				catch (Exception e) {
					plugin.getLogger().warning("Could not initialize "
							+ oldDataStore + " datastore for conversion.");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}

			// get count of chest records inserted in new datastore from old datastore
			int chestRecordCount = newDataStore.insertChestRecords(oldDataStore.selectAllChestRecords());

			// log chest record count message
			plugin.getLogger().info(chestRecordCount + " chest records converted to "
					+ newDataStore + " datastore.");

			// get count of block records inserted in new datastore from old datastore
			int recordCount = newDataStore.insertBlockRecords(oldDataStore.selectAllBlockRecords());

			// log block record count message
			plugin.getLogger().info(recordCount + " block records converted to "
					+ newDataStore + " datastore.");

			// flush new datastore to disk if applicable
			newDataStore.sync();

			// close old datastore
			oldDataStore.close();

			// delete old datastore
			oldDataStore.delete();
		}
	}


	/**
	 * convert all existing data stores to new data store
	 *
	 * @param newDataStore the new datastore to convert all other datastores to
	 */
	static void convertAll(final PluginMain plugin, final DataStore newDataStore) {

		// get collection of all data store types
		Collection<DataStoreType> dataStores = new HashSet<>(Arrays.asList(DataStoreType.values()));

		// remove newDataStore from list of types to convert
		dataStores.remove(newDataStore.getType());

		// convert each datastore in list to new datastore
		for (DataStoreType type : dataStores) {
			if (type.storageObjectExists(plugin)) {
				convert(plugin, type.connect(plugin), newDataStore);
			}
		}
	}

}
