package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.chests.DeathChest;
import com.winterhaven_mc.deathchest.messages.Message;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

import static com.winterhaven_mc.deathchest.messages.MessageId.COMMAND_FAIL_INVALID_COMMAND;


/**
 * A class that implements player commands for the plugin
 */
public final class CommandManager implements CommandExecutor, TabCompleter {

	private final PluginMain plugin;

	// constant List of subcommands
	private final static List<String> subcommands =
			Collections.unmodifiableList(new ArrayList<>(
					Arrays.asList("help", "list", "reload", "status")));


	public CommandManager(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		Objects.requireNonNull(plugin.getCommand("deathchest")).setExecutor(this);
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
	public final List<String> onTabComplete(final CommandSender sender,
											final Command command,
											final String alias,
											final String[] args) {


		// initialize return list
		final List<String> returnList = new ArrayList<>();

		// if first argument, return list of valid matching subcommands
		if (args.length == 1) {

			for (String subcommand : subcommands) {
				if (sender.hasPermission("deathchest." + subcommand)
						&& subcommand.startsWith(args[0].toLowerCase())) {
					returnList.add(subcommand);
				}
			}
		}
		else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("list")
					&& sender.hasPermission("deathchest.list.other")) {

				// get map of chest ownerUUID,name from all current chests
				Map<UUID, String> chestOwners = new HashMap<>();
				for (DeathChest deathChest : plugin.chestManager.getAllChests()) {
					chestOwners.put(deathChest.getOwnerUUID(),
							plugin.getServer().getOfflinePlayer(deathChest.getOwnerUUID()).getName());
				}
				returnList.addAll(chestOwners.values());
			}
			else if (args[0].equalsIgnoreCase("help")
					&& sender.hasPermission("deathchest.help")) {

				for (String subcommand : subcommands) {
					if (sender.hasPermission("deathchest." + subcommand)
							&& subcommand.startsWith(args[1].toLowerCase())) {
						returnList.add(subcommand);
					}
				}
			}
		}

		return returnList;
	}


	/**
	 * Command handler for DeathChest
	 *
	 * @param sender the command sender
	 * @param cmd    the command typed
	 * @param label  the command label
	 * @param args   additional command arguments
	 * @return boolean - always returns {@code true}, to suppress bukkit builtin help message
	 */
	@Override
	public final boolean onCommand(final CommandSender sender,
								   final Command cmd,
								   final String label,
								   final String[] args) {

		// convert arguments array to list
		List<String> argsList = new ArrayList<>(Arrays.asList(args));

		String subcommandString;

		// get subcommand, remove from front of list
		if (args.length > 0) {
			subcommandString = argsList.remove(0);
		}

		// if no arguments, display usage for all commands and return
		else {
			HelpCommand.displayUsage(sender, "all");
			return true;
		}

		Subcommand subcommand;

		switch(subcommandString.toLowerCase()) {

			case "status":
				subcommand = new StatusCommand(plugin, sender);
				break;

			case "reload":
				subcommand = new ReloadCommand(plugin, sender);
				break;

			case "list":
				subcommand = new ListCommand(plugin, sender, argsList);
				break;

			case "help":
				subcommand = new HelpCommand(plugin, sender, argsList);
				break;

			default:
				Message.create(sender, COMMAND_FAIL_INVALID_COMMAND).send();
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
				HelpCommand.displayUsage(sender, "all");
				return true;
		}

		// execute subcommand
		return subcommand.execute();
	}

}
