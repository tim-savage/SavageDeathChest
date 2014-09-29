package com.winterhaven_mc.deathchest;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;

public class DatastoreYML extends Datastore {
	
	// reference to main class
	private DeathChestMain plugin = DeathChestMain.plugin;
	
	// ConfigAccessor for yml datafile
	private ConfigAccessor dataFile = new ConfigAccessor(plugin , "deathchests.yml");

	@Override
	void initialize() throws Exception {
		
		// create default data file from embedded resource if it doesn't already exist
		dataFile.saveDefaultConfig();
	}

	@Override
	void close() {
		
		// save data file
		dataFile.saveConfig();

	}

	
	DeathChestBlock getRecord(Location location) {
		
		String key = makeKey(location);
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
		
		DeathChestBlock deathChestBlock = new DeathChestBlock();
		deathChestBlock.setOwnerUUID(owneruuid);
		deathChestBlock.setKillerUUID(killeruuid);
		deathChestBlock.setLocation(location);
		deathChestBlock.setExpiration(dataFile.getConfig().getLong(key + pathSeparator + "expiration"));

		return deathChestBlock;
	}

	@Override
	ArrayList<DeathChestBlock> getAllRecords() {
		
		ArrayList<DeathChestBlock> result = new ArrayList<DeathChestBlock>();
		
		for (String key : dataFile.getConfig().getKeys(false)) {
			
			DeathChestBlock deathChestBlock = getRecord(makeLocation(key));
			if (deathChestBlock != null) {
				result.add(deathChestBlock);
			}
			
		}
		return result;
	}

	@Override
	void putRecord(DeathChestBlock deathChestBlock) {
		
		// create key based on block location
		String key = makeKey(deathChestBlock.getLocation());
		
		// create string from owner uuid
		String owneruuid = null;
		try {
			owneruuid = deathChestBlock.getOwnerUUID().toString();
		}
		catch (Exception e) {
			owneruuid = "";
		}
		
		// if owneruuid is empty, this is not a valid record
		if (owneruuid.isEmpty()) {
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

	@Override
	void deleteRecord(Location location) {
		
		String key = makeKey(location);
		dataFile.getConfig().set(key, null);
		
		dataFile.saveConfig();
	}

	
    /**
	 * Create a unique key string based on a location
	 * 
	 * @param location Location to create unique location key string
	 * @return String key
	 */
	private String makeKey(Location location) {
		String worldname = location.getWorld().getName();
		String x = String.valueOf(location.getBlockX());
		String y = String.valueOf(location.getBlockY());
		String z = String.valueOf(location.getBlockZ());
		String key = worldname + "|" + x + "|" + y + "|" + z;
		return key;
	}
	
	
	/**
	 * create a new location object from a given key
	 * @param key
	 * @return location
	 */
	private Location makeLocation(String key) {
		
		String elements[] = key.split("|");
		
		String worldname = elements[0];
		int x = Integer.parseInt(elements[1]);
		int y = Integer.parseInt(elements[2]);
		int z = Integer.parseInt(elements[3]);
		
		Location location = new Location(plugin.getServer().getWorld(worldname),x,y,z);
		
		return location;		
	}

	
}
