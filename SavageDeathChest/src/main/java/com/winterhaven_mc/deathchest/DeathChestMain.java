package com.winterhaven_mc.deathchest;

import org.bukkit.plugin.java.JavaPlugin;

public final class DeathChestMain extends JavaPlugin {

	public static DeathChestMain plugin;
	public CommandHandler commandHandler;
	public MessageManager messageManager;
	public ChestManager chestManager;
	public boolean debug = getConfig().getBoolean("debug");

	public void onEnable() {

		// static reference to plugin instance
		plugin = this;

		// copy default config from jar if it doesn't exist
		saveDefaultConfig();

		// register command handler
		commandHandler = new CommandHandler(this);

		// instantiate message manager
		messageManager = new MessageManager(this);

		// instantiate chest manager
		chestManager = new ChestManager(this);

		// initialize listeners
		new PlayerEventListener(this);
		new BlockEventListener(this);

	}
	
	public void onDisable() {
		
		// close datastore
		chestManager.closeDatastore();
		
	}

}
