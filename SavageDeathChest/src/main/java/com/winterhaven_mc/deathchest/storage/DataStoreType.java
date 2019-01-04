package com.winterhaven_mc.deathchest.storage;

import com.winterhaven_mc.deathchest.PluginMain;


/**
 * An enum whose values represent the types of data store available.<br>
 * Note: Only SQLite data store is implemented at this time.
 */
enum DataStoreType {

	SQLITE("SQLite") {
		@Override
		public DataStore create() {

			// create new SQLite datastore object
			return new DataStoreSQLite(plugin);
		}
	};

	private final static PluginMain plugin = PluginMain.instance;

	private final String displayName;

	private final static DataStoreType defaultType = DataStoreType.SQLITE;

	public abstract DataStore create();


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
}
