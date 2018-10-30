package com.winterhaven_mc.deathchest.storage;

import com.winterhaven_mc.deathchest.DeathChestBlock;
import org.bukkit.Location;

import java.util.ArrayList;


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
	 * Retrieve a record from the datastore
	 * @param location the location key for the record to retrieve
	 * @return DeathChestBlock
	 */
	@SuppressWarnings("unused")
	abstract DeathChestBlock getRecord(final Location location);


	/**
	 * Retrieve a list of all records from the datastore
	 * @return ArrayList of DeathChestBlock
	 */
	abstract ArrayList<DeathChestBlock> getAllRecords();


	/**
	 * Insert a record in the datastore
	 * @param deathChestBlock the DeathChestBlock object to insert into the datastore
	 */
	abstract void putRecord(final DeathChestBlock deathChestBlock);


	/**
	 * Delete a record from the datastore
	 * @param location the location key for the record to delete
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
