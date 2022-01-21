package com.winterhavenmc.deathchest;

import com.winterhavenmc.deathchest.chests.ChestManager;
import com.winterhavenmc.deathchest.commands.CommandManager;
import com.winterhavenmc.deathchest.listeners.BlockEventListener;
import com.winterhavenmc.deathchest.listeners.InventoryEventListener;
import com.winterhavenmc.deathchest.listeners.PlayerEventListener;
import com.winterhavenmc.deathchest.messages.MessageId;
import com.winterhavenmc.deathchest.messages.Macro;

import com.winterhavenmc.deathchest.protectionchecks.ProtectionPluginRegistry;
import com.winterhavenmc.util.messagebuilder.MessageBuilder;
import com.winterhavenmc.util.soundconfig.SoundConfiguration;
import com.winterhavenmc.util.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.util.worldmanager.WorldManager;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * The main class for SavageDeathChest plugin
 */
public final class PluginMain extends JavaPlugin {

	public MessageBuilder<MessageId, Macro> messageBuilder;
	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
	public ChestManager chestManager;
	public CommandManager commandManager;
	public ProtectionPluginRegistry protectionPluginRegistry;


	@Override
	public void onEnable() {

		// bstats
		final int pluginId = 13916;
		@SuppressWarnings("unused")
		Metrics metrics = new Metrics(this, pluginId);

		// copy default config from jar if it doesn't exist
		saveDefaultConfig();

		// initialize message builder
		messageBuilder = new MessageBuilder<>(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

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

		// instantiate protection plugin registry
		protectionPluginRegistry = new ProtectionPluginRegistry(this);
	}


	@Override
	public void onDisable() {

		// close datastore
		chestManager.closeDataStore();
	}

}
