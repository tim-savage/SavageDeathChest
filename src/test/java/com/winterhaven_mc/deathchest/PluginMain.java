package com.winterhaven_mc.deathchest;

import com.winterhaven_mc.deathchest.chests.ChestManager;
import com.winterhaven_mc.deathchest.commands.CommandManager;
import com.winterhaven_mc.deathchest.listeners.BlockEventListener;
import com.winterhaven_mc.deathchest.listeners.InventoryEventListener;
import com.winterhaven_mc.deathchest.listeners.PlayerEventListener;
import com.winterhaven_mc.deathchest.messages.MessageId;
import com.winterhaven_mc.deathchest.messages.Macro;

import com.winterhaven_mc.util.SoundConfiguration;
import com.winterhaven_mc.util.WorldManager;
import com.winterhaven_mc.util.YamlSoundConfiguration;

import com.winterhavenmc.util.messagebuilder.MessageBuilder;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;


/**
 * The main class for SavageDeathChest plugin
 */
public final class PluginMain extends JavaPlugin {

	public MessageBuilder<MessageId, Macro> messageBuilder;
	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
	public ChestManager chestManager;
	public CommandManager commandManager;


	/**
	 * Class constructor for testing
	 */
	@SuppressWarnings("unused")
	public PluginMain() {
		super();
	}


	/**
	 * Class constructor for testing
	 */
	@SuppressWarnings("unused")
	PluginMain(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
		super(loader, descriptionFile, dataFolder, file);
	}


	@Override
	public void onEnable() {

		// copy default config from jar if it doesn't exist
		saveDefaultConfig();

		messageBuilder = new MessageBuilder<>(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate chest manager
		chestManager = new ChestManager(this);

		// load all chests from datastore
		chestManager.loadChests();

		// instantiate command manager
		commandManager = new CommandManager(this);

		// initialize event listeners
		new PlayerEventListener(this);
		new BlockEventListener(this);
		new InventoryEventListener(this);

		// log detected protection plugins
//        ProtectionPlugin.reportInstalled();
	}


	@Override
	public void onDisable() {

		// close datastore
		chestManager.closeDataStore();
	}

}
