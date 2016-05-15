package com.winterhaven_mc.deathchest.storage;

import java.util.ArrayList;

import org.bukkit.Location;

import com.winterhaven_mc.deathchest.DeathChestBlock;


public abstract class DataStore {

	protected boolean initialized;

	protected DataStoreType type;

	protected String filename;

	/**
	 * Initialize the datastore
	 * @throws Exception
	 */
	abstract void initialize() throws Exception;


	/**
	 * Retrieve a record from the datastore
	 * @param location
	 * @return DeathChestBlock
	 */
	abstract DeathChestBlock getRecord(final Location location);


	/**
	 * Retrieve a list of all records from the datastore
	 * @return ArrayList of DeathChestBlock
	 */
	abstract ArrayList<DeathChestBlock> getAllRecords();


	/**
	 * Insert a record in the datastore
	 * @param deathChestBlock
	 */
	abstract void putRecord(final DeathChestBlock deathChestBlock);


	/**
	 * Delete a record from the datastore
	 * @param location
	 */
	public abstract void deleteRecord(final Location location);


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
	abstract void delete();

	
	/**
	 * Check for existence of datastore file or equivalent
	 * @return
	 */
	abstract boolean exists();

	
	/**
	 * Check if the datastore is initialized
	 * @return
	 */
	boolean isInitialized() {
		return this.initialized;
	}

	
	/**
	 * Set datastore initialized value
	 * @param initialized
	 */
	void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}

	
	/**
	 * Get the datastore type
	 * @return
	 */
	DataStoreType getType() {
		return this.type;
	}

	
	/**
	 * Get the datastore name
	 * @return
	 */
	public String getName() {
		return this.getType().toString();
	}

	
	/**
	 * Get the datastore filename or equivalent
	 * @return
	 */
	String getFilename() {
		return this.filename;
	}

}
