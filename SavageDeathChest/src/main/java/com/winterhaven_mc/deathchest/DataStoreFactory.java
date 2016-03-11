package com.winterhaven_mc.deathchest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataStoreFactory {

	static PluginMain plugin = PluginMain.instance;


	/**
	 * Create new data store of given type.<br>
	 * No parameter version used when no current datastore exists
	 * and datastore type should be read from configuration
	 * @return new datastore of configured type
	 */
	static DataStore create() {
		
		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.SQLITE;
		}
		return create(dataStoreType, null);
	}
	
	
	/**
	 * Create new data store of given type.<br>
	 * Single parameter version used when no current datastore exists
	 * but the required datastore type is known
	 * @param dataStoreType
	 * @return
	 */
	static DataStore create(DataStoreType dataStoreType) {
		return create(dataStoreType, null);
	}
	
	
	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 * @param dataStoreType		new datastore type
	 * @param oldDataStore		existing datastore reference
	 * @return
	 */
	static DataStore create(DataStoreType dataStoreType, DataStore oldDataStore) {
	
		DataStore newDataStore = null;
		
		// if file storage type yaml is specified
		if (dataStoreType.equals(DataStoreType.YAML)) {
			
			// get initialized yaml data store
			newDataStore = createYaml();
		}
		else {
			// get initialized sqlite data store
			newDataStore = createSqlite();
		}
		
		// if old data store was passed and it exists, convert to new data store
		if (oldDataStore != null && oldDataStore.exists()) {
				convertDataStore(oldDataStore, newDataStore);
		}
		else {
			convertAll(newDataStore);
		}
		// return initialized data store
		return newDataStore;
	}
	
	
	/**
	 * Check if a new datastore type has been configured, and
	 * convert old datastore to new type if necessary
	 */
	static void reload() {
		
		// get current datastore type
		DataStoreType currentType = plugin.dataStore.getType();
		
		// get configured datastore type
		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
				
		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {
			
			// create new datastore
			plugin.dataStore = create(newType,plugin.dataStore);
		}
		
	}


	/**
	 * try to create new yaml datastore; disable plugin on failure
	 * @return
	 */
	private static DataStore createYaml() {

		// create new yaml datastore object
		DataStore newDataStore = new DataStoreYAML(plugin);

		// initialize yaml datastore
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			// error initializing yaml datastore, so disable plugin.
			plugin.getLogger().severe("An error occurred while trying to initialize the yaml datastore.");
			plugin.getLogger().severe(e.getLocalizedMessage());
			plugin.getLogger().severe("Disabling plugin.");
			plugin.getPluginLoader().disablePlugin(plugin);
			return null;
		}
		return newDataStore;
	}

	
	/**
	 * try to create new sqlite data store; create yaml data store on failure
	 * @return
	 */
	private static DataStore createSqlite() {

		// create new sqlite datastore object
		DataStore newDataStore = new DataStoreSQLite(plugin);

		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			// error initializing sqlite datastore, so try yaml as fallback
			plugin.getLogger().warning("An error occurred while trying to initialize the sqlite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
			plugin.getLogger().warning("Trying yaml datastore as fallback...");
			newDataStore = createYaml();
		}
		return newDataStore;
	}

	
	/**
	 * convert old data store to new data store
	 * @param oldDataStore
	 * @param newDataStore
	 */
	private static void convertDataStore(DataStore oldDataStore, DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}
		
		// if old datastore file exists, attempt to read all records
		if (oldDataStore.exists()) {
			
			plugin.getLogger().info("Converting existing " + oldDataStore.getName() + " datastore to "
					+ newDataStore.getName() + " datastore...");
			
			// initialize old datastore if necessary
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				} catch (Exception e) {
					plugin.getLogger().warning("Could not initialize " 
							+ oldDataStore.getName() + " datastore for conversion.");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}
			
			List<DeathChestBlock> allRecords = new ArrayList<DeathChestBlock>();
			
			allRecords = oldDataStore.getAllRecords();
			
			int count = 0;
			for (DeathChestBlock record : allRecords) {
				newDataStore.putRecord(record);
				count++;
			}
			plugin.getLogger().info(count + " records converted to " + newDataStore.getName() + " datastore.");
			
			newDataStore.sync();
			
			oldDataStore.close();
			oldDataStore.delete();
		}
	}

	
	/**
	 * convert all existing data stores to new data store
	 * @param newDataStore
	 */
	private static void convertAll(DataStore newDataStore) {
		
		// get array list of all data store types
		ArrayList<DataStoreType> dataStores = new ArrayList<DataStoreType>(Arrays.asList(DataStoreType.values()));
		
		// remove newDataStore from list of types to convert
		dataStores.remove(newDataStore);
		
		for (DataStoreType type : dataStores) {

			// create oldDataStore holder
			DataStore oldDataStore = null;
			
			if (type.equals(DataStoreType.YAML)) {
				oldDataStore = new DataStoreYAML(plugin);
			}
			else if (type.equals(DataStoreType.SQLITE)) {
				oldDataStore = new DataStoreSQLite(plugin);
			}
			
			// add additional datastore types here as they become available
			
			if (oldDataStore != null && oldDataStore.exists()) {
				convertDataStore(oldDataStore, newDataStore);
			}
		}
	}
	
}
