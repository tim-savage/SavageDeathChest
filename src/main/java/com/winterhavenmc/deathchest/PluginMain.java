/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathchest;

import com.winterhavenmc.deathchest.chests.ChestManager;
import com.winterhavenmc.deathchest.commands.CommandManager;
import com.winterhavenmc.deathchest.listeners.BlockEventListener;
import com.winterhavenmc.deathchest.listeners.InventoryEventListener;
import com.winterhavenmc.deathchest.listeners.PlayerEventListener;
import com.winterhavenmc.deathchest.messages.MessageId;
import com.winterhavenmc.deathchest.messages.Macro;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPluginRegistry;

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

		// bStats
		new Metrics(this, 13916);

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
