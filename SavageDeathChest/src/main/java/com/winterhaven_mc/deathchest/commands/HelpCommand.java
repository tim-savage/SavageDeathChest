package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.messages.Message;
import com.winterhaven_mc.deathchest.messages.MessageId;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.deathchest.messages.MessageId.*;


public class HelpCommand implements Subcommand {

	private final PluginMain plugin;
	private final CommandSender sender;
	private final List<String> args;

	private final static ChatColor usageColor = ChatColor.GOLD;


	HelpCommand(final PluginMain plugin, final CommandSender sender, List<String> args) {
		this.plugin = Objects.requireNonNull(plugin);
		this.sender = Objects.requireNonNull(sender);
		this.args = Objects.requireNonNull(args);
	}


	@Override
	public boolean execute() {

		// if command sender does not have permission to list death chests, output error message and return true
		if (!sender.hasPermission("deathchest.help")) {
			Message.create(sender, COMMAND_FAIL_HELP_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// if no arguments, display usage for all commands
		if (args.size() == 0) {
			displayUsage(sender,"all");
			return true;
		}

		// set command name
		String commandName = args.get(0);
		MessageId helpMessageId;

		switch(commandName.toLowerCase()) {

			case "help":
				helpMessageId = COMMAND_HELP_HELP;
				break;

			case "list":
				helpMessageId = COMMAND_HELP_LIST;
				break;

			case "reload":
				helpMessageId = COMMAND_HELP_RELOAD;
				break;

			case "status":
				helpMessageId = COMMAND_HELP_STATUS;
				break;

			default:
				helpMessageId = COMMAND_HELP_INVALID;
				commandName = "all";
		}

		Message.create(sender, helpMessageId).send();
		displayUsage(sender, commandName);

		return true;
	}


	/**
	 * Display command usage
	 *
	 * @param sender       the command sender
	 * @param passedString the command for which to display usage
	 */
	static void displayUsage(final CommandSender sender, final String passedString) {

		// check for null parameters
		Objects.requireNonNull(sender);
		String commandName = Objects.requireNonNull(passedString);

		if (commandName.isEmpty()) {
			commandName = "all";
		}

		if ((commandName.equalsIgnoreCase("help")
				|| commandName.equalsIgnoreCase("all"))
				&& sender.hasPermission("deathchest.help")) {
			sender.sendMessage(usageColor + "/deathchest help [command]");
		}

		if ((commandName.equalsIgnoreCase("list")
				|| commandName.equalsIgnoreCase("all"))
				&& sender.hasPermission("deathchest.list")) {
			if (sender.hasPermission("deathchest.list.other")) {
				sender.sendMessage(usageColor + "/deathchest list [username] [page]");
			}
			else {
				sender.sendMessage(usageColor + "/deathchest list [page]");
			}
		}

		if ((commandName.equalsIgnoreCase("reload")
				|| commandName.equalsIgnoreCase("all"))
				&& sender.hasPermission("deathchest.reload")) {
			sender.sendMessage(usageColor + "/deathchest reload");
		}

		if ((commandName.equalsIgnoreCase("status")
				|| commandName.equalsIgnoreCase("all"))
				&& sender.hasPermission("deathchest.status")) {
			sender.sendMessage(usageColor + "/deathchest status");
		}
	}

}
