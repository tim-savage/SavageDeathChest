package com.winterhaven_mc.deathchest;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;

public class DatastoreYAML extends Datastore {
	
	// reference to main class
	private DeathChestMain plugin = DeathChestMain.plugin;
	
	// datastore name
	static final String NAME = "YAML";
	
	// data file filename
	static final String FILENAME = "deathchests.yml";
	
	// ConfigAccessor for yml datafile
	private ConfigAccessor dataFile = new ConfigAccessor(plugin, FILENAME);


	void initialize() throws Exception {
		
		// create default data file from embedded resource if it doesn't already exist
		dataFile.saveDefaultConfig();
		
	}


	void close() {
		
		// save data file
		dataFile.saveConfig();

	}

	
	DeathChestBlock getRecord(Location location) {
		
		String key = locationToString(location);
		Character pathSeparator = dataFile.getConfig().options().pathSeparator();

		if (dataFile.getConfig().get(key) == null) {
			return null;
		}
		
		UUID owneruuid = null;
		try {
			owneruuid = UUID.fromString(dataFile.getConfig().getString(key + pathSeparator + "owneruuid"));
		}
		catch (Exception e) {
			owneruuid = null;
		}

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


	ArrayList<DeathChestBlock> getAllRecords() {
		
		ArrayList<DeathChestBlock> result = new ArrayList<DeathChestBlock>();
		
		for (String key : dataFile.getConfig().getKeys(false)) {
			
			DeathChestBlock deathChestBlock = getRecord(stringToLocation(key));
			if (deathChestBlock != null) {
				result.add(deathChestBlock);
			}
			
		}
		return result;
	}


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
		
		// create string from killer uuid
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


	void deleteRecord(Location location) {
		
		String key = locationToString(location);
		dataFile.getConfig().set(key, null);
		
		dataFile.saveConfig();
	}

	String getDatastoreName() {
		return NAME;
	}
	
	String getFilename() {
		return FILENAME;
	}
	
	void deleteFile() {
		
		File file = new File(plugin.getDataFolder() + File.separator + FILENAME);
		file.delete();
		
	}

	
	/**
	 * Create a unique key string based on a location
	 * 
	 * @param location Location to create unique location key string
	 * @return String key
	 */
	private String locationToString(Location location) {

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
		
		// assign location elements to variables
		String worldname = elements[0];
		int x = Integer.parseInt(elements[1]);
		int y = Integer.parseInt(elements[2]);
		int z = Integer.parseInt(elements[3]);
		
		// create location object from location string elements
		Location location = new Location(plugin.getServer().getWorld(worldname),x,y,z);

		// return newly formed location object
		return location;		
	}

}
