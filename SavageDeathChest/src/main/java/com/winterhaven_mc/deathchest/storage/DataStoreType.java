package com.winterhaven_mc.deathchest.storage;

import com.winterhaven_mc.deathchest.PluginMain;

public enum DataStoreType {

	YAML("Yaml") {
		
		@Override
		public DataStore create() {
			// create new yaml datastore object
			return new DataStoreYAML(plugin);
		}
		
	},
	
	SQLITE("SQLite") {

		@Override
		public DataStore create() {
			
			// create new sqlite datastore object
			return new DataStoreSQLite(plugin);
		}
	};

	private String displayName;

	private final static PluginMain plugin = PluginMain.instance;
	
	private final static DataStoreType defaultType = DataStoreType.SQLITE;
	
	public abstract DataStore create();
	

	/**
	 * Class constructor
	 * @param name
	 */
	private DataStoreType(final String displayName) {
		this.displayName = displayName;
	}

	
	@Override
	public String toString() {
		return this.displayName;
	}

	
	public static DataStoreType match(final String name) {
		for (DataStoreType type : DataStoreType.values()) {
			if (type.toString().equalsIgnoreCase(name)) {
				return type;
			}
		}
		// no match; return default type
		return defaultType;
	}
	
	
	public static DataStoreType getDefaultType() {
		return defaultType;
	}
}
