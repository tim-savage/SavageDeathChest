package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.messages.Message;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import com.winterhaven_mc.deathchest.storage.DataStore;
import com.winterhaven_mc.util.LanguageManager;
import org.bukkit.command.CommandSender;

import java.util.Objects;

import static com.winterhaven_mc.deathchest.messages.MessageId.COMMAND_FAIL_RELOAD_PERMISSION;
import static com.winterhaven_mc.deathchest.messages.MessageId.COMMAND_SUCCESS_RELOAD;


public class ReloadCommand implements Subcommand {

	private final PluginMain plugin;
	private final CommandSender sender;


	ReloadCommand(final PluginMain plugin, final CommandSender sender) {
		this.plugin = Objects.requireNonNull(plugin);
		this.sender = Objects.requireNonNull(sender);
	}


	public boolean execute() {
		// check for null parameter
		Objects.requireNonNull(sender);

		if (!sender.hasPermission("deathchest.reload")) {
			Message.create(sender, COMMAND_FAIL_RELOAD_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// copy default config from jar if it doesn't exist
		plugin.saveDefaultConfig();

		// reload config file
		plugin.reloadConfig();

		// reload replaceable blocks
		plugin.chestManager.replaceableBlocks.reload();

		// update debug field
		plugin.debug = plugin.getConfig().getBoolean("debug");

		// update enabledWorlds list
		plugin.worldManager.reload();

		// reload messages
		LanguageManager.reload();

		// reload sounds
		plugin.soundConfig.reload();

		// reload datastore if changed
		DataStore.reload();

		// send success message
		Message.create(sender, COMMAND_SUCCESS_RELOAD).send();

		// return true to prevent bukkit command help display
		return true;
	}

}
