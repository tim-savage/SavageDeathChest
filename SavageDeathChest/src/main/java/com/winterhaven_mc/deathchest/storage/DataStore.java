package com.winterhaven_mc.deathchest.storage;

import com.winterhaven_mc.deathchest.chests.ChestBlock;
import com.winterhaven_mc.deathchest.chests.DeathChest;

import java.util.List;


public abstract class DataStore {

	private boolean initialized;

	DataStoreType type;

	String filename;

	/**
	 * Initialize the datastore
	 * @throws Exception
	 */
	@SuppressWarnings("JavaDoc")
	abstract void initialize() throws Exception;


	/**
	 * Retrieve a list of all chest records from the datastore
	 * @return List of DeathChest
	 */
	public abstract List<DeathChest> getAllChestRecords();


	/**
	 * Retrieve a list of all block records from the datastore
	 * @return List of ChestBlock
	 */
	public abstract List<ChestBlock> getAllBlockRecords();


	/**
	 * Insert a chest record in the datastore
	 * @param deathChest the DeathChest object to insert into the datastore
	 */
	public abstract void putChestRecord(final DeathChest deathChest);


	/**
	 * Insert a block record in the datastore
	 * @param blockRecord the BlockChest object to insert in the datastore
	 */
	public abstract void putBlockRecord(final ChestBlock blockRecord);


	/**
	 * Delete a block record from the datastore
	 * @param chestBlock the chest block to delete
	 */
	public abstract void deleteBlockRecord(final ChestBlock chestBlock);


	/**
	 * Delete a chest record from the datastore
	 * @param deathChest the chest to delete
	 */
	public abstract void deleteChestRecord(final DeathChest deathChest);


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
	 * @return {@code true} if the datastore file (or equivilent) exists, {@code false} if it does not
	 */
	abstract boolean exists();

	
	/**
	 * Check if the datastore is initialized
	 * @return {@code true} if the datastore is initialize, {@code false} if it is not
	 */
	boolean isInitialized() {
		return this.initialized;
	}

	
	/**
	 * Set datastore initialized value
	 * @param initialized the boolean value to assign to the datastore initialized field
	 */
	void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}

	
	/**
	 * Get the datastore type
	 * @return the datastore type of this datastore instance
	 */
	DataStoreType getType() {
		return this.type;
	}

	
	/**
	 * Get the datastore name
	 * @return the name of this datastore instance
	 */
	public String getName() {
		return this.getType().toString();
	}

	
	/**
	 * Get the datastore filename or equivalent
	 * @return the filename (or equivalent) of this datastore instance
	 */
	String getFilename() {
		return this.filename;
	}

}
