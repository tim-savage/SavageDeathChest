package com.winterhaven_mc.deathchest;

import org.bukkit.plugin.java.JavaPlugin;

public final class DeathChestMain extends JavaPlugin {

	static DeathChestMain instance;
	
	CommandManager commandManager;
	MessageManager messageManager;
	DataStore dataStore;
	public ChestManager chestManager;

	boolean debug = getConfig().getBoolean("debug");

	public void onEnable() {

		// static reference to plugin instance
		instance = this;

		// copy default config from jar if it doesn't exist
		saveDefaultConfig();

		// instantiate command manager
		commandManager = new CommandManager(this);

		// instantiate message manager
		messageManager = new MessageManager(this);
		
		// instantiate datastore
		dataStore = DataStoreFactory.create();
		
		// instantiate chest manager
		chestManager = new ChestManager(this);

		// initialize event listeners
		new PlayerEventListener(this);
		new BlockEventListener(this);
		new InventoryEventListener(this);
	}
	
	public void onDisable() {
		
		// close datastore
		dataStore.close();
	}

}
