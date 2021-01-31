package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.messages.Message;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

import static com.winterhaven_mc.deathchest.messages.MessageId.COMMAND_FAIL_INVALID_COMMAND;
import static com.winterhaven_mc.deathchest.sounds.SoundId.COMMAND_INVALID;


/**
 * A class that implements player commands for the plugin
 */
public final class CommandManager implements CommandExecutor, TabCompleter {

	private final PluginMain plugin;
	private final SubcommandMap subcommandMap = new SubcommandMap();


	public CommandManager(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		Objects.requireNonNull(plugin.getCommand("deathchest")).setExecutor(this);

		for (SubcommandType subcommandType : SubcommandType.values()) {
			subcommandType.register(plugin, subcommandMap);
		}
	}


	/**
	 * Tab completer for DeathChest
	 *
	 * @param sender  the command sender
	 * @param command the command typed
	 * @param alias   alias for the command
	 * @param args    additional command arguments
	 * @return List of String - the possible matching values for tab completion
	 */
	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		// if more than one argument, use tab completer of subcommand
		if (args.length > 1) {

			// get subcommand from map
			Subcommand subcommand = subcommandMap.getCommand(args[0]);

			// if no subcommand returned from map, return empty list
			if (subcommand == null) {
				return Collections.emptyList();
			}

			// return subcommand tab completer output
			return subcommand.onTabComplete(sender, command, alias, args);
		}

		// return list of subcommands for which sender has permission
		return matchingCommands(sender, args[0]);
	}


	/**
	 * Command handler for DeathChest
	 *
	 * @param sender   the command sender
	 * @param command  the command typed
	 * @param label    the command label
	 * @param args     Array of String - command arguments
	 * @return boolean - always returns {@code true}, to suppress bukkit builtin help message
	 */
	@Override
	public final boolean onCommand(final CommandSender sender,
								   final Command command,
								   final String label,
								   final String[] args) {

		// convert args array to list
		List<String> argsList = new ArrayList<>(Arrays.asList(args));

		String subcommandName;

		// get subcommand, remove from front of list
		if (argsList.size() > 0) {
			subcommandName = argsList.remove(0);
		}

		// if no arguments, set command to help
		else {
			subcommandName = "help";
		}

		// get subcommand from map by name
		Subcommand subcommand = subcommandMap.getCommand(subcommandName);

		// if subcommand is null, get help command from map
		if (subcommand == null) {
			subcommand = subcommandMap.getCommand("help");
			Message.create(sender, COMMAND_FAIL_INVALID_COMMAND).send();
			plugin.soundConfig.playSound(sender, COMMAND_INVALID);
		}

		// execute subcommand
		return subcommand.onCommand(sender, argsList);
	}


	/**
	 * Get matching list of subcommands for which sender has permission
	 * @param sender the command sender
	 * @param matchString the string prefix to match against command names
	 * @return List of String - command names that match prefix and sender has permission
	 */
	private List<String> matchingCommands(CommandSender sender, String matchString) {

		List<String> returnList = new ArrayList<>();

		for (String subcommand : subcommandMap.getNames()) {
			if (sender.hasPermission("deathchest." + subcommand)
					&& subcommand.startsWith(matchString.toLowerCase())) {
				returnList.add(subcommand);
			}
		}
		return returnList;
	}

}
