/*
 * Decompiled with CFR 0_79.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.plugin.PluginDescriptionFile
 */
package com.winterhaven_mc.deathchest;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler
implements CommandExecutor {
	private DeathChestMain plugin;

	public CommandHandler(DeathChestMain plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		int maxArgs = 1;
		String plugin_name = "[" + this.plugin.getName() + "] ";
		if (args.length > maxArgs) {
			sender.sendMessage(ChatColor.RED + plugin_name + "Too many arguments.");
			return false;
		}
		if (args.length < 1) {
			String versionString = this.plugin.getDescription().getVersion();
			sender.sendMessage(ChatColor.GREEN + plugin_name + "Version: " + ChatColor.RESET + versionString);
			sender.sendMessage(ChatColor.GREEN + "Language: " + ChatColor.RESET + plugin.getConfig().getString("language"));
			sender.sendMessage(ChatColor.GREEN + "Storage Type: " + ChatColor.RESET + plugin.chestmanager.getCurrentDatastore().getDatastoreName());
			sender.sendMessage(ChatColor.GREEN + "Chest Expiration: " + ChatColor.RESET + plugin.getConfig().getInt("expire-time") + " minutes");
			sender.sendMessage(ChatColor.GREEN + "Enabled Worlds: " + ChatColor.RESET + plugin.getConfig().getStringList("enabled-worlds").toString());
			return true;
		}
		if ((args[0]).equalsIgnoreCase("reload")) {
			
			// get original storage type
			String originalStorageType = plugin.getConfig().getString("storage-type","sqlite");

			// reload config file
			plugin.reloadConfig();
			
			// reload messages file
			plugin.messagemanager.reloadMessages();
			
			// get current storage type
			String currentStorageType = plugin.getConfig().getString("storage-type","sqlite");
			
			// if storage type has changed, instantiate new datastore
			if (!originalStorageType.equals(currentStorageType)) {
				plugin.getLogger().info("Changing storage type from '" 
						+ originalStorageType + "' to '" + currentStorageType + "'...");
				plugin.chestmanager.setCurrentDatastore(plugin.chestmanager.getNewDatastore());
				
				// convert any old datastore files to new datastore
				plugin.chestmanager.convertDatastores();
			}
			
			sender.sendMessage(ChatColor.AQUA + plugin_name + "Configuration reloaded.");
			return true;
		}
		return false;
	}
}

