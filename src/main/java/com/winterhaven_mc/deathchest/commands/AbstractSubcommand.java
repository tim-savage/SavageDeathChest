package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class AbstractSubcommand implements Subcommand {

	private String name;
	private List<String> aliases = new ArrayList<>();
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
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}

	@Override
	public void addAlias(String alias) {
		this.aliases.add(alias);
	}

	@Override
	public String getUsage() {
		return usageString;
	}

	@Override
	public void displayUsage(CommandSender sender) {
		sender.sendMessage(usageString);
	}

	@Override
	public void setUsage(String usageString) {
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
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		return Collections.emptyList();
	}

}
