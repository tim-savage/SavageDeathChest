package com.winterhaven_mc.deathchest.storage;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.chests.ChestBlock;
import com.winterhaven_mc.deathchest.chests.DeathChest;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


/**
 * An abstract class that declares methods for managing persistent storage of death chests and chest blocks.
 */
public abstract class DataStore {

	// static reference to main class instance
	private static final PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	private boolean initialized;

	DataStoreType type;

	String filename;


	/**
	 * Initialize the datastore
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("JavaDoc")
	abstract void initialize() throws Exception;


	/**
	 * Retrieve a collection of all chest records from the datastore
	 *
	 * @return List of DeathChest
	 */
	public abstract Collection<DeathChest> selectAllChestRecords();


	/**
	 * Insert a chest record in the datastore
	 *
	 * @param deathChests a collection of DeathChest objects to insert into the datastore
	 */
//	public abstract void insertChestRecord(final DeathChest deathChest);

	public abstract int insertChestRecords(final Collection<DeathChest> deathChests);


	/**
	 * Delete a chest record from the datastore
	 *
	 * @param deathChest the chest to delete
	 */
	public abstract void deleteChestRecord(final DeathChest deathChest);


	/**
	 * Retrieve a collection of all block records from the datastore
	 *
	 * @return List of ChestBlock
	 */
	public abstract Collection<ChestBlock> selectAllBlockRecords();


	/**
	 * Insert block records in the datastore
	 *
	 * @param blockRecords a collection of ChestBlock objects to insert in the datastore
	 */
//	abstract void insertBlockRecord(final ChestBlock blockRecord);

	abstract int insertBlockRecords(final Collection<ChestBlock> blockRecords);


	/**
	 * Delete a block record from the datastore
	 *
	 * @param chestBlock the chest block to delete
	 */
	public abstract void deleteBlockRecord(final ChestBlock chestBlock);


	/**
	 * Close the datastore
	 */
	public abstract void close();


	/**
	 * Sync the datastore to disk
	 */
	abstract void sync();


	/**
	 * Delete the datastore file or equivalent
	 */
	@SuppressWarnings("UnusedReturnValue")
	abstract boolean delete();


	/**
	 * Check for existence of datastore file or equivalent
	 *
	 * @return {@code true} if the datastore file (or equivilent) exists, {@code false} if it does not
	 */
	abstract boolean exists();


	/**
	 * Check if the datastore is initialized
	 *
	 * @return {@code true} if the datastore is initialize, {@code false} if it is not
	 */
	boolean isInitialized() {
		return this.initialized;
	}


	/**
	 * Set datastore initialized value
	 *
	 * @param initialized the boolean value to assign to the datastore initialized field
	 */
	void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}


	/**
	 * Get the datastore type
	 *
	 * @return the datastore type of this datastore instance
	 */
	public DataStoreType getType() {
		return this.type;
	}


	/**
	 * Override toString method to return the datastore type name
	 *
	 * @return the name of this datastore instance
	 */
	@Override
	public String toString() {
		return this.type.toString();
	}

	/**
	 * Get the datastore filename or equivalent
	 *
	 * @return the filename (or equivalent) of this datastore instance
	 */
	String getFilename() {
		return this.filename;
	}


	/**
	 * Create new data store of given type.<br>
	 * No parameter version used when no current datastore exists
	 * and datastore type should be read from configuration
	 *
	 * @return new datastore of configured type
	 */
	public static DataStore create() {

		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.getDefaultType();
		}
		return create(dataStoreType, null);
	}


	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 *
	 * @param dataStoreType new datastore type
	 * @param oldDataStore  existing datastore reference
	 * @return the new datastore
	 */
	public static DataStore create(final DataStoreType dataStoreType, final DataStore oldDataStore) {

		// get new data store of specified type
		DataStore newDataStore = dataStoreType.create();

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore + " datastore!");
			if (plugin.debug) {
				e.printStackTrace();
			}
		}

		// if old data store was passed, convert to new data store
		if (oldDataStore != null) {
			convert(oldDataStore, newDataStore);
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
	public DataStore reload() {

		// get current datastore type
		DataStoreType currentType = this.getType();

		// get configured datastore type
		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		DataStore returnDataStore = this;

		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// create new datastore
			returnDataStore = DataStore.create(newType, this);
		}

		return returnDataStore;
	}


//	/**
//	 * Check if a new datastore type has been configured, and
//	 * convert old datastore to new type if necessary
//	 */
//	public static void reload() {
//
//		// get current datastore type
//		DataStoreType currentType = plugin.chestManager.dataStore.getType();
//
//		// get configured datastore type
//		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
//
//		// if current datastore type does not match configured datastore type, create new datastore
//		if (!currentType.equals(newType)) {
//
//			// create new datastore
//			plugin.dataStore = create(newType, plugin.dataStore);
//		}
//	}


	/**
	 * convert old data store to new data store
	 *
	 * @param oldDataStore the existing datastore to be converted from
	 * @param newDataStore the new datastore to be converted to
	 */
	private static void convert(final DataStore oldDataStore, final DataStore newDataStore) {

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
	private static void convertAll(final DataStore newDataStore) {

		// get array list of all data store types
		ArrayList<DataStoreType> dataStores = new ArrayList<>(Arrays.asList(DataStoreType.values()));

		// remove newDataStore from list of types to convert
		//noinspection SuspiciousMethodCalls
		dataStores.remove(newDataStore);

		for (DataStoreType type : dataStores) {

			// create oldDataStore holder
			DataStore oldDataStore = null;

			if (type.equals(DataStoreType.SQLITE)) {
				oldDataStore = new DataStoreSQLite(plugin);
			}

			// add additional datastore types here as they become available

			if (oldDataStore != null && oldDataStore.exists()) {
				convert(oldDataStore, newDataStore);
			}
		}
	}

}
