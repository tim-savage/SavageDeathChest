package com.winterhaven_mc.deathchest;

import com.sk89q.worldguard.bukkit.WGBukkit;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathChestMain extends JavaPlugin {

	public static DeathChestMain plugin;
	public MessageManager messagemanager;
	public ChestManager chestmanager;
	public GriefPrevention gp;
	public boolean debug = getConfig().getBoolean("debug");
	public boolean gp_loaded = false;


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

		// check if grief prevention is enabled
		GriefPrevention gp = getGriefPrevention();
		if (gp != null) {
			getLogger().info("GriefPrevention detected.");
			gp_loaded = true;
		}

		// check if world guard is enabled
		if (WGBukkit.getPlugin().isEnabled()) {
			this.getLogger().info("WorldGuard detected.");
		}
	}
	
	public void onDisable() {
		
		// close datastore
		chestmanager.close();
		
	}

	// check for grief prevention plugin
	private GriefPrevention getGriefPrevention() {
		Plugin plugin = getServer().getPluginManager().getPlugin("GriefPrevention");
		if (plugin != null && plugin instanceof GriefPrevention) {
			return (GriefPrevention)plugin;
		}
		return null;
	}
}
