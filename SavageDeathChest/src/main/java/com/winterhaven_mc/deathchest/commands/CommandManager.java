package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;
import com.winterhaven_mc.deathchest.storage.DataStoreFactory;
import com.winterhaven_mc.deathchest.util.LocationUtilities;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class CommandManager implements CommandExecutor {
	
	private final PluginMain plugin;
	private final String pluginName;
	public CommandManager(final PluginMain plugin) {
		
		this.plugin = plugin;	
		plugin.getCommand("deathchest").setExecutor(this);
		pluginName = "[" + this.plugin.getName() + "] ";
	}

	public final boolean onCommand(final CommandSender sender, final Command cmd, 
			final String label, final String[] args) {


		int maxArgs = 1;

		if (args.length > maxArgs) {
			sender.sendMessage(ChatColor.RED + pluginName + "Too many arguments.");
			return false;
		}

		String subcmd;

		// if no arguments passed, set subcmd to status
		if (args.length < 1) {
			subcmd = "status";
		}
		else {
			subcmd = args[0];
		}

		// status command
		if (subcmd.equalsIgnoreCase("status")) {
			return statusCommand(sender);
		}

		// reload command
		//noinspection SimplifiableIfStatement
		if (subcmd.equalsIgnoreCase("reload")) {
			return reloadCommand(sender);
		}
		return false;
	}
	
	
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
		
		return true;
	}

	
	private boolean reloadCommand(final CommandSender sender) {
		
		// copy default config from jar if it doesn't exist
		plugin.saveDefaultConfig();
		
		// reload config file
		plugin.reloadConfig();
		
		// reload replaceable blocks
		LocationUtilities.loadReplaceableBlocks();
		
		// update debug field
		plugin.debug = plugin.getConfig().getBoolean("debug");
		
		// update enabledWorlds list
		plugin.worldManager.reload();
		
		// reload messages
		plugin.messageManager.reload();

		// reload sounds
		plugin.soundManager.reload();

		// reload datastore if changed
		DataStoreFactory.reload();
		
		sender.sendMessage(ChatColor.AQUA + pluginName + "Configuration reloaded.");
		
		return true;
	}
	
}
