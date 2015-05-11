package com.winterhaven_mc.deathchest;

import java.util.ArrayList;

import org.bukkit.Location;


public abstract class DataStore {
	
	protected boolean initialized;

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
	abstract DeathChestBlock getRecord(Location location);
	
	/**
	 * Retrieve a list of all records from the datastore
	 * @return ArrayList of DeathChestBlock
	 */
	abstract ArrayList<DeathChestBlock> getAllRecords();
	
	/**
	 * Insert a record in the datastore
	 * @param deathChestBlock
	 */
	abstract void putRecord(DeathChestBlock deathChestBlock);
	
	/**
	 * Delete a record from the datastore
	 * @param location
	 */
	abstract void deleteRecord(Location location);

	/**
	 * Return datastore FILENAME constant
	 * @return
	 */
	abstract String getFilename();

	boolean isInitialized() {
		return this.initialized;
	}

	void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * Close the datastore
	 */
	abstract void close();

	abstract void sync();

	abstract void delete();

	abstract boolean exists();

	abstract DataStoreType getType();

	String getName() {
		return this.getType().getName();
	}

}
