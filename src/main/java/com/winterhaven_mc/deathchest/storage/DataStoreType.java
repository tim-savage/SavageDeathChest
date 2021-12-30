package com.winterhaven_mc.deathchest.storage;

import com.winterhaven_mc.deathchest.PluginMain;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * An enum whose values represent the types of data store available.<br>
 * Note: Only SQLite data store is implemented at this time.
 */
public enum DataStoreType {

	SQLITE("SQLite") {
		@Override
		public DataStore create(PluginMain plugin) {

			// create new SQLite datastore object
			return new DataStoreSQLite(plugin);
		}
	};

//	private final static PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	private final String displayName;

	private final static DataStoreType defaultType = DataStoreType.SQLITE;


	/**
	 * Get new instance of DataStore of configured type
	 * @return new instance of DataStore
	 */
	public abstract DataStore create(PluginMain plugin);


	/**
	 * Class constructor
	 *
	 * @param displayName the formatted display name of a datastore type
	 */
	DataStoreType(final String displayName) {
		this.displayName = displayName;
	}


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
	 * Get the default DataStoreType
	 * @return DataStoreType - the default DataStoreType
	 */
	public static DataStoreType getDefaultType() {
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
		if (oldDataStore.exists()) {

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

			int chestRecordCount = newDataStore.insertChestRecords(oldDataStore.selectAllChestRecords());
			plugin.getLogger().info(chestRecordCount + " chest records converted to "
					+ newDataStore + " datastore.");

			int recordCount = newDataStore.insertBlockRecords(oldDataStore.selectAllBlockRecords());
			plugin.getLogger().info(recordCount + " block records converted to "
					+ newDataStore + " datastore.");

			newDataStore.sync();

			oldDataStore.close();
			oldDataStore.delete();
		}
	}


	/**
	 * convert all existing data stores to new data store
	 *
	 * @param newDataStore the new datastore to convert all other datastores to
	 */
	static void convertAll(final PluginMain plugin, final DataStore newDataStore) {

		// get array list of all data store types
		ArrayList<DataStoreType> dataStores = new ArrayList<>(Arrays.asList(DataStoreType.values()));

		// remove newDataStore from list of types to convert
		dataStores.remove(newDataStore.getType());

		// convert each datastore in list to new datastore
		for (DataStoreType type : dataStores) {
			convert(plugin, type.create(plugin), newDataStore);
		}
	}

}
