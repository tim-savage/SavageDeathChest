package com.winterhaven_mc.deathchest.commands;


import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;
import com.winterhaven_mc.deathchest.storage.DataStoreFactory;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public final class CommandManager implements CommandExecutor, TabCompleter {

	private final PluginMain plugin;
	private final String pluginName;
	// constant List of subcommands
	private final static List<String> subcommands =
			Collections.unmodifiableList(new ArrayList<>(
					Arrays.asList("status","reload")));


	public CommandManager(final PluginMain plugin) {

		this.plugin = plugin;
		plugin.getCommand("deathchest").setExecutor(this);
		pluginName = "[" + this.plugin.getName() + "] ";
	}


	/**
	 * Tab completer for SpawnStar
	 */
	@Override
	public final List<String> onTabComplete(final CommandSender sender, final Command command,
											final String alias, final String[] args) {

		// initalize return list
		final List<String> returnList = new ArrayList<>();

		// if first argument, return list of valid matching subcommands
		if (args.length == 1) {

			for (String subcommand : subcommands) {
				if (sender.hasPermission("spawnstar." + subcommand)
						&& subcommand.startsWith(args[0].toLowerCase())) {
					returnList.add(subcommand);
				}
			}
		}
		return returnList;
	}


	public final boolean onCommand(final CommandSender sender, final Command cmd,
			final String label, final String[] args) {

		int maxArgs = 1;

		if (args.length > maxArgs) {
			sender.sendMessage(ChatColor.RED + pluginName + "Too many arguments.");
			return false;
		}

		String subcommand;

		// if no arguments passed, set subcommand to status
		if (args.length < 1) {
			subcommand = "status";
		}
		else {
			subcommand = args[0];
		}

		// status command
		if (subcommand.equalsIgnoreCase("status")) {
			return statusCommand(sender);
		}

		// reload command
		//noinspection SimplifiableIfStatement
		if (subcommand.equalsIgnoreCase("reload")) {
			return reloadCommand(sender);
		}
		return false;
	}


	/**
	 * Status command
	 * @param sender Command sender
	 * @return true if command executed without error, false to output help message
	 */
	private boolean statusCommand(final CommandSender sender) {
		
		String versionString = this.plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + pluginName + ChatColor.AQUA + "Version: " 
				+ ChatColor.RESET + versionString);
		if (plugin.debug) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}
		sender.sendMessage(ChatColor.GREEN + "Language: " 
				+ ChatColor.RESET + plugin.getConfig().getString("language"));
		sender.sendMessage(ChatColor.GREEN + "Storage Type: " 
				+ ChatColor.RESET + plugin.dataStore.getName());
		sender.sendMessage(ChatColor.GREEN + "Chest Expiration: " 
				+ ChatColor.RESET + plugin.getConfig().getInt("expire-time") + " minutes");
		sender.sendMessage(ChatColor.GREEN + "Protection Plugin Support:");
		
		int count = 0;
		for (ProtectionPlugin pp : ProtectionPlugin.values()) {
			
			if (pp.isInstalled()) {
				
				List<String> pluginSettings = new ArrayList<>();
				
				count++;
				String statusString = ChatColor.AQUA + "  " + pp.getPluginName() + ": ";
				
				if (plugin.getConfig().getBoolean("protection-plugins." + pp.getPluginName() + ".check-on-place")) {
					pluginSettings.add("check-on-place");
				}
				if (plugin.getConfig().getBoolean("protection-plugins." + pp.getPluginName() + ".check-on-access")) {
					pluginSettings.add("check-on-access");
				}
				statusString = statusString + ChatColor.RESET + pluginSettings.toString();
				sender.sendMessage(statusString);
			}
		}		
		if (count == 0) {
			sender.sendMessage(ChatColor.AQUA + "  [ NONE ENABLED ]");
		}
		
		sender.sendMessage(ChatColor.GREEN + "Enabled Worlds: " + ChatColor.RESET + 
				plugin.worldManager.getEnabledWorldNames().toString());

		if (plugin.debug) {
			sender.sendMessage(ChatColor.GREEN + "Replaceable Blocks: " + ChatColor.RESET +
					plugin.chestManager.getReplaceableBlocks().toString());
		}

		return true;
	}

	
	private boolean reloadCommand(final CommandSender sender) {
		
		// copy default config from jar if it doesn't exist
		plugin.saveDefaultConfig();
		
		// reload config file
		plugin.reloadConfig();
		
		// reload replaceable blocks
		plugin.chestManager.loadReplaceableBlocks();
		
		// update debug field
		plugin.debug = plugin.getConfig().getBoolean("debug");
		
		// update enabledWorlds list
		plugin.worldManager.reload();
		
		// reload messages
		plugin.messageManager.reload();

		// reload sounds
		plugin.soundConfig.reload();

		// reload datastore if changed
		DataStoreFactory.reload();
		
		sender.sendMessage(ChatColor.AQUA + pluginName + "Configuration reloaded.");
		
		return true;
	}
	
}
