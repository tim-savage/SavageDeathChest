package com.winterhavenmc.deathchest.commands;

import com.winterhavenmc.deathchest.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;


interface Subcommand {

	boolean onCommand(final CommandSender sender, final List<String> argsList);

	List<String> onTabComplete(final CommandSender sender,
	                           final Command command,
	                           final String alias,
	                           final String[] args);

	String getName();

	String getUsage();

	void displayUsage(final CommandSender sender);

	MessageId getDescription();

	@SuppressWarnings("unused")
	int getMinArgs();

	int getMaxArgs();

}
