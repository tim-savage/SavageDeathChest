package com.winterhaven_mc.deathchest;

public enum DataStoreType {

	YAML("Yaml"),
	SQLITE("SQLite");

	private String name;

	/**
	 * Class constructor
	 * @param name
	 */
	private DataStoreType(final String name) {
		this.setName(name);
	}
	
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
	
	public static DataStoreType match(final String name) {
		for (DataStoreType type : DataStoreType.values()) {
			if (type.getName().equalsIgnoreCase(name)) {
				return type;
			}
		}
		// no match; return default type
		return DataStoreType.SQLITE;
	}
}
