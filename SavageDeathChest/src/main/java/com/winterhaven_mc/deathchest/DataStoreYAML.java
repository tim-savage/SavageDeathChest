package com.winterhaven_mc.deathchest;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;


public class DataStoreYAML extends DataStore {

	// reference to main class
	private DeathChestMain plugin;

	// ConfigAccessor for yml datafile
	private ConfigAccessor dataFile;


	/**
	 * Class constructor
	 * @param plugin
	 */
	DataStoreYAML (DeathChestMain plugin) {

		// reference to main class
		this.plugin = plugin;

		// set datastore type
		this.type = DataStoreType.YAML;

		// set filename
		this.filename = "deathchests.yml";
	}

	@Override
	void initialize() throws Exception {

		// if data store is already initialized, do nothing and return
		if (this.isInitialized()) {
			plugin.getLogger().info(this.getName() + " datastore already initialized.");
			return;
		}

		// instantiate config accessor for datafile
		dataFile = new ConfigAccessor(plugin, filename);

		// create default data file from embedded resource if it doesn't already exist
		dataFile.saveDefaultConfig();
		
		// set initialized true
		setInitialized(true);
		plugin.getLogger().info(this.getName() + " datastore initialized.");
	}

	@Override
	DeathChestBlock getRecord(Location location) {

		String key = locationToString(location);
		Character pathSeparator = dataFile.getConfig().options().pathSeparator();

		// if stored record is null, return null
		if (dataFile.getConfig().get(key) == null) {
			return null;
		}

		// get ownerid from stored record, or set to null if invalid
		UUID owneruuid = null;
		try {
			owneruuid = UUID.fromString(dataFile.getConfig().getString(key + pathSeparator + "owneruuid"));
		}
		catch (Exception e) {
			owneruuid = null;
		}

		// get killerid from stored record, or set to null if invalid
		UUID killeruuid = null;
		try {
			killeruuid = UUID.fromString(dataFile.getConfig().getString(key + pathSeparator + "killeruuid"));
		}
		catch (Exception e) {
			killeruuid = null;
		}

		// if owner uuid is null this is an invalid record, so delete it from the datastore and return a null object
		if (owneruuid == null) {
			deleteRecord(location);
			return null;
		}

		// create a new DeathChestBlock object to return
		DeathChestBlock deathChestBlock = new DeathChestBlock();
		deathChestBlock.setOwnerUUID(owneruuid);
		deathChestBlock.setKillerUUID(killeruuid);
		deathChestBlock.setLocation(location);
		deathChestBlock.setExpiration(dataFile.getConfig().getLong(key + pathSeparator + "expiration"));

		return deathChestBlock;
	}

	@Override
	ArrayList<DeathChestBlock> getAllRecords() {

		Character pathSeparator = dataFile.getConfig().options().pathSeparator();

		ArrayList<DeathChestBlock> result = new ArrayList<DeathChestBlock>();

		for (String key : dataFile.getConfig().getKeys(false)) {

			DeathChestBlock deathChestBlock = getRecord(stringToLocation(key));
			if (deathChestBlock != null) {
				result.add(deathChestBlock);
			}
			else {
				// delete expired records from file if returned record is null,
				// so that expired records in invalid worlds get deleted from datastore
				if (dataFile.getConfig().getLong(key + pathSeparator + "expiration") > System.currentTimeMillis()) {
					deleteRecord(key);
				}
			}			
		}
		return result;
	}

	@Override
	void putRecord(DeathChestBlock deathChestBlock) {

		// create key based on block location
		String key = locationToString(deathChestBlock.getLocation());

		// create string from owner uuid
		String owneruuid = null;
		try {
			owneruuid = deathChestBlock.getOwnerUUID().toString();
		}
		catch (Exception e) {
			plugin.getLogger().warning("[YAML putRecord] Error converting ownerUUID to string.");
			if (plugin.debug) {
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
			owneruuid = "";
		}

		// if owneruuid is empty, this is not a valid record
		if (owneruuid.isEmpty()) {
			plugin.getLogger().warning("[YAML putRecord] OwnerUUID string is empty. Record not inserted.");
			return;
		}

		// create string from killer uuid, or set to empty string if no killer uuid exists
		String killeruuid = null;
		try {
			killeruuid = deathChestBlock.getKillerUUID().toString();
		}
		catch (Exception e) {
			killeruuid = "";
		}

		// write record to dataFile in memory object
		Character pathSeparator = dataFile.getConfig().options().pathSeparator();
		dataFile.getConfig().createSection(key);
		dataFile.getConfig().set(key + pathSeparator + "owneruuid", owneruuid);
		dataFile.getConfig().set(key + pathSeparator + "killeruuid", killeruuid);
		dataFile.getConfig().set(key + pathSeparator + "expiration", deathChestBlock.getExpiration());

		// save in memory object to disk
		dataFile.saveConfig();

	}


	/**
	 * Delete record by location
	 * @param location
	 */
	@Override
	void deleteRecord(Location location) {
		String key = locationToString(location);
		dataFile.getConfig().set(key, null);
		dataFile.saveConfig();
	}


	@Override
	void sync() {
		dataFile.saveConfig();
	}

	@Override
	void close() {
		// save data to file
		dataFile.saveConfig();

		// set initialized to false
		setInitialized(false);
		
		// output log message
		plugin.getLogger().info(this.getName() + " datastore closed.");
	}


	@Override
	void delete() {
		// delete this datastore file
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + getFilename());
		if (dataStoreFile.exists()) {
			dataStoreFile.delete();
		}
	}


	@Override
	boolean exists() {
		// get path name to this datastore file
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + getFilename());
		return dataStoreFile.exists();
	}


	/**
	 * Delete record by key
	 * @param key
	 */
	void deleteRecord(String key) {

		dataFile.getConfig().set(key, null);		
		dataFile.saveConfig();
	}


	/**
	 * Create a unique key string based on a location
	 * 
	 * @param location Location to create unique location key string
	 * @return String key
	 */
	private String locationToString(Location location) {

		// if location is null, return null string
		if (location == null) {
			return null;
		}

		// parse location elements into distinct variables
		String worldname = location.getWorld().getName();
		String x = String.valueOf(location.getBlockX());
		String y = String.valueOf(location.getBlockY());
		String z = String.valueOf(location.getBlockZ());

		// concatenate location elements into string
		String locationString = worldname + "|" + x + "|" + y + "|" + z;

		// return concatenated string
		return locationString;
	}


	/**
	 * create a new location object from a given key
	 * @param locationString
	 * @return location
	 */
	private Location stringToLocation(String locationString) {

		// split location string into distinct elements
		String[] elements = locationString.split("\\|");

		// if location string did not split into 4 fields, return null location
		if (elements.length < 4) {
			return null;
		}

		// assign location elements to variables
		String worldname = elements[0];
		int x = Integer.parseInt(elements[1]);
		int y = Integer.parseInt(elements[2]);
		int z = Integer.parseInt(elements[3]);

		// check that world exists
		if (plugin.getServer().getWorld(worldname) == null) {
			// world does not exist, so output log message and return null
			plugin.getLogger().warning("Deathchest world '" + worldname + "' does not exist.");
			return null;
		}

		// create location object from location string elements
		Location location = new Location(plugin.getServer().getWorld(worldname),x,y,z);

		// return newly formed location object
		return location;		
	}

}
