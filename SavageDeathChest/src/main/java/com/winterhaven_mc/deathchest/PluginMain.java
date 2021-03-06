package com.winterhaven_mc.deathchest;

import com.winterhaven_mc.deathchest.chests.ChestManager;
import com.winterhaven_mc.deathchest.listeners.BlockEventListener;
import com.winterhaven_mc.deathchest.listeners.InventoryEventListener;
import com.winterhaven_mc.deathchest.listeners.PlayerEventListener;
import com.winterhaven_mc.deathchest.storage.DataStore;
import com.winterhaven_mc.deathchest.commands.CommandManager;
import com.winterhaven_mc.deathchest.chests.search.ProtectionPlugin;
import com.winterhaven_mc.util.LanguageManager;
import com.winterhaven_mc.util.SoundConfiguration;
import com.winterhaven_mc.util.WorldManager;
import com.winterhaven_mc.util.YamlSoundConfiguration;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * The main class for SavageDeathChest plugin
 */
public final class PluginMain extends JavaPlugin {

	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
	public DataStore dataStore;
	public ChestManager chestManager;

	public boolean debug = getConfig().getBoolean("debug");


	@Override
	public void onEnable() {

		// copy default config from jar if it doesn't exist
		saveDefaultConfig();

		// initialize language manager
		LanguageManager.init();

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate datastore
		dataStore = DataStore.create();

		// instantiate chest manager
		chestManager = new ChestManager(this);

		// load all chests from datastore
		chestManager.loadDeathChests();

		// instantiate command manager
		new CommandManager(this);

		// initialize event listeners
		new PlayerEventListener(this);
		new BlockEventListener(this);
		new InventoryEventListener(this);

		// log detected protection plugins
		ProtectionPlugin.reportInstalled();
	}


	@Override
	public void onDisable() {

		// close datastore
		dataStore.close();
	}

}
