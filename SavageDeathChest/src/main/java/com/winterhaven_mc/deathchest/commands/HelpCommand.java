package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.messages.Message;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.deathchest.messages.MessageId.*;
import static com.winterhaven_mc.deathchest.sounds.SoundId.COMMAND_INVALID;


public class HelpCommand extends AbstractSubcommand {

	private final PluginMain plugin;
	private final SubcommandMap subcommandMap;


	HelpCommand(final PluginMain plugin, final SubcommandMap subcommandMap) {
		this.plugin = Objects.requireNonNull(plugin);
		this.subcommandMap = Objects.requireNonNull(subcommandMap);
		this.setName("help");
		this.setUsage("/deathchest help [command]");
		this.setDescription(COMMAND_HELP_HELP);
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		List<String> returnList = new ArrayList<>();

		if (args.length == 2) {
			for (String subcommand : subcommandMap.getNames()) {
				if (sender.hasPermission("deathchest." + subcommand)
						&& subcommand.startsWith(args[1].toLowerCase())
						&& !subcommand.equalsIgnoreCase("help")) {
					returnList.add(subcommand);
				}
			}
		}

		return returnList;
	}


	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {

		// if command sender does not have permission to display help, output error message and return true
		if (!sender.hasPermission("deathchest.help")) {
			Message.create(sender, COMMAND_FAIL_HELP_PERMISSION).send(plugin.languageHandler);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// if no arguments, display usage for all commands
		if (args.size() == 0) {
			displayUsageAll(sender);
			return true;
		}

		// get subcommand name
		String subcommandName = args.get(0);
		displayHelp(sender, subcommandName);
		return true;
	}


	/**
	 * Display help message and usage for a command
	 * @param sender the command sender
	 * @param commandName the name of the command for which to show help and usage
	 */
	void displayHelp(final CommandSender sender, final String commandName) {

		// get subcommand from map by name
		Subcommand subcommand = subcommandMap.getCommand(commandName);

		// if subcommand found in map, display help message and usage
		if (subcommand != null) {
			Message.create(sender, subcommand.getDescription()).send(plugin.languageHandler);
			subcommand.displayUsage(sender);
		}

		// else display invalid command help message and usage for all commands
		else {
			Message.create(sender, COMMAND_HELP_INVALID).send(plugin.languageHandler);
			plugin.soundConfig.playSound(sender, COMMAND_INVALID);
			displayUsageAll(sender);
		}
	}


	/**
	 * Display usage message for all commands
	 * @param sender the command sender
	 */
	void displayUsageAll(CommandSender sender) {

		Message.create(sender, COMMAND_HELP_USAGE).send(plugin.languageHandler);

		for (String subcommandName : subcommandMap.getNames()) {
			if (subcommandMap.getCommand(subcommandName) != null) {
				subcommandMap.getCommand(subcommandName).displayUsage(sender);
			}
		}
	}

}
