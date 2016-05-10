package com.winterhaven_mc.deathchest;

import org.bukkit.plugin.java.JavaPlugin;

import com.winterhaven_mc.deathchest.listeners.BlockEventListener;
import com.winterhaven_mc.deathchest.listeners.InventoryEventListener;
import com.winterhaven_mc.deathchest.listeners.PlayerEventListener;
import com.winterhaven_mc.deathchest.storage.ChestManager;
import com.winterhaven_mc.deathchest.storage.DataStore;
import com.winterhaven_mc.deathchest.storage.DataStoreFactory;
import com.winterhaven_mc.deathchest.util.CommandManager;
import com.winterhaven_mc.deathchest.util.MessageManager;
import com.winterhaven_mc.deathchest.util.WorldManager;

public final class PluginMain extends JavaPlugin {

	public static PluginMain instance;
	
	public WorldManager worldManager;
	public CommandManager commandManager;
	public MessageManager messageManager;
	public DataStore dataStore;
	public ChestManager chestManager;

	public boolean debug = getConfig().getBoolean("debug");

	public void onEnable() {

		// static reference to plugin instance
		instance = this;

		// copy default config from jar if it doesn't exist
		saveDefaultConfig();

		// instantiate world manager
		worldManager = new WorldManager(this);
		
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
		
		// log detected protection plugins
		ProtectionPlugin.reportInstalled();
		
	}
	
	public void onDisable() {
		
		// close datastore
		dataStore.close();
	}

}
