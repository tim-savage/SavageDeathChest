package com.winterhaven_mc.deathchest.storage;


/**
 * An abstract class that declares methods for managing persistent storage of death chests and chest blocks.
 */
public abstract class DataStoreAbstract {

	private boolean initialized;

	DataStoreType type;

	String filename;


	/**
	 * Check if the datastore is initialized
	 *
	 * @return {@code true} if the datastore is initialize, {@code false} if it is not
	 */
	public boolean isInitialized() {
		return this.initialized;
	}


	/**
	 * Set datastore initialized value
	 *
	 * @param initialized the boolean value to assign to the datastore initialized field
	 */
	public void setInitialized(final boolean initialized) {
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

}
