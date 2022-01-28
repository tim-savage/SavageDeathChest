package com.winterhavenmc.deathchest.commands;

import com.winterhavenmc.deathchest.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.*;


abstract class SubcommandAbstract implements Subcommand {

	protected String name;
	protected String usageString;
	protected MessageId description;
	protected int minArgs;
	protected int maxArgs;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getUsage() {
		return usageString;
	}

	@Override
	public void displayUsage(final CommandSender sender) {
		sender.sendMessage(usageString);
	}

	@Override
	public MessageId getDescription() {
		return description;
	}

	@Override
	public int getMinArgs() {
		return this.minArgs;
	}

	@Override
	public int getMaxArgs() {
		return this.maxArgs;
	}

	@Override
	public List<String> onTabComplete(final @Nonnull CommandSender sender,
	                                  final @Nonnull Command command,
	                                  final @Nonnull String alias,
	                                  final String[] args) {

		return Collections.emptyList();
	}

}
