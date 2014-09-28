package com.winterhaven_mc.deathchest;

import org.bukkit.Location;

public abstract class Datastore {
	
	abstract void initializeDatastore() throws Exception;
	
	abstract void closeDatastore();
	
	abstract DeathChestBlock getRecord(Location location);
	
	abstract void putRecord(DeathChestBlock deathChestBlock);

}
