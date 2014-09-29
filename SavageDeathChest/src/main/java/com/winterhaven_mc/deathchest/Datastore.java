package com.winterhaven_mc.deathchest;

import java.util.ArrayList;

import org.bukkit.Location;

public abstract class Datastore {
	
	abstract void initialize() throws Exception;
	
	abstract void close();
	
	abstract DeathChestBlock getRecord(Location location);
	
	abstract ArrayList<DeathChestBlock> getAllRecords();
	
	abstract void putRecord(DeathChestBlock deathChestBlock);
	
	abstract void deleteRecord(Location location);

}
