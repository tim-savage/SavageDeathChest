package com.winterhavenmc.deathchest.commands;

import com.winterhavenmc.deathchest.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;


interface Subcommand {

	boolean onCommand(final CommandSender sender, final List<String> argsList);

	List<String> onTabComplete(final CommandSender sender,
	                           final Command command,
	                           final String alias,
	                           final String[] args);

	String getName();

	void setName(final String name);

	Collection<String> getAliases();

	@SuppressWarnings("unused")
	void setAliases(final List<String> aliases);

	@SuppressWarnings("unused")
	void addAlias(final String alias);

	String getUsage();

	void setUsage(final String usageString);

	void displayUsage(final CommandSender sender);

	MessageId getDescription();

	void setDescription(final MessageId messageId);

	@SuppressWarnings("unused")
	int getMinArgs();

	@SuppressWarnings("unused")
	void setMinArgs(final int minArgs);

	int getMaxArgs();

	void setMaxArgs(final int maxArgs);

}
