package com.winterhaven_mc.deathchest;

import org.bukkit.plugin.java.JavaPlugin;

public final class DeathChestMain extends JavaPlugin {

	public static DeathChestMain plugin;
	public MessageManager messagemanager;
	public ChestManager chestmanager;
	public boolean debug = getConfig().getBoolean("debug");

	public void onEnable() {

		// static reference to main class
		plugin = this;

		// register command handler
		getCommand("deathchest").setExecutor(new CommandHandler(this));

		// copy default config from jar if it doesn't exist
		this.saveDefaultConfig();

		// instantiate message manager
		this.messagemanager = new MessageManager(this);

		// instantiate chest manager
		this.chestmanager = new ChestManager(this);

		// initialize listeners
		new PlayerEventListener(this);
		new BlockEventListener(this);

	}
	
	public void onDisable() {
		
		// close datastore
		chestmanager.closeDatastore();
		
	}

}
