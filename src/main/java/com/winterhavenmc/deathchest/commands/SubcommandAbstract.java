package com.winterhavenmc.deathchest.commands;

import com.winterhavenmc.deathchest.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.*;


abstract class SubcommandAbstract implements Subcommand {

	private String name;
	private String usageString;
	private MessageId description;
	private int minArgs;
	private int maxArgs;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
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
	public void setUsage(final String usageString) {
		this.usageString = usageString;
	}

	@Override
	public MessageId getDescription() {
		return description;
	}

	@Override
	public void setDescription(final MessageId description) {
		this.description = description;
	}

	@Override
	public int getMinArgs() {
		return this.minArgs;
	}

	@Override
	public void setMinArgs(final int minArgs) {
		this.minArgs = minArgs;
	}

	@Override
	public int getMaxArgs() {
		return this.maxArgs;
	}

	@Override
	public void setMaxArgs(final int maxArgs) {
		this.maxArgs = maxArgs;
	}

	@Override
	public List<String> onTabComplete(final @Nonnull CommandSender sender,
	                                  final @Nonnull Command command,
	                                  final @Nonnull String alias,
	                                  final String[] args) {

		return Collections.emptyList();
	}

}
