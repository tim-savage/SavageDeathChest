package com.winterhavenmc.deathchest.commands;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.sounds.SoundId;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

import static com.winterhavenmc.deathchest.messages.MessageId.*;
import static com.winterhavenmc.deathchest.sounds.SoundId.COMMAND_RELOAD_SUCCESS;


public class ReloadCommand extends AbstractSubcommand {

	private final PluginMain plugin;


	ReloadCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		setName("reload");
		setUsage("/deathchest reload");
		setDescription(COMMAND_HELP_RELOAD);
	}


	public boolean onCommand(final CommandSender sender, final List<String> args) {
		// check for null parameter
		Objects.requireNonNull(sender);

		if (!sender.hasPermission("deathchest.reload")) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_RELOAD_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// copy default config from jar if it doesn't exist
		plugin.saveDefaultConfig();

		// reload config file
		plugin.reloadConfig();

		// update enabledWorlds list
		plugin.worldManager.reload();

		// reload messages
		plugin.messageBuilder.reload();

		// reload sounds
		plugin.soundConfig.reload();

		// reload ChestManager
		plugin.chestManager.reload();

		// send success message
		plugin.messageBuilder.build(sender, COMMAND_SUCCESS_RELOAD).send();

		// play success sound
		plugin.soundConfig.playSound(sender, COMMAND_RELOAD_SUCCESS);

		// return true to prevent bukkit command help display
		return true;
	}

}
