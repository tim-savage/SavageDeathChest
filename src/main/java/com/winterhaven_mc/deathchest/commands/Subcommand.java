package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;


public interface Subcommand {

	boolean onCommand(CommandSender sender, List<String> argsList);

	List<String> onTabComplete(final CommandSender sender, final Command command,
							   final String alias, final String[] args);

	String getName();

	void setName(final String name);

	List<String> getAliases();

	@SuppressWarnings("unused")
	void setAliases(List<String> aliases);

	@SuppressWarnings("unused")
	void addAlias(String alias);

	String getUsage();

	void setUsage(String usageString);

	void displayUsage(CommandSender sender);

	MessageId getDescription();

	void setDescription(MessageId messageId);

	@SuppressWarnings("unused")
	int getMinArgs();

	@SuppressWarnings("unused")
	void setMinArgs(int minArgs);

	int getMaxArgs();

	void setMaxArgs(int maxArgs);

}
