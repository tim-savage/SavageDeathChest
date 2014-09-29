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
			sender.sendMessage((Object)ChatColor.RED + plugin_name + "Too many arguments.");
			return false;
		}
		if (args.length < 1) {
			String versionString = this.plugin.getDescription().getVersion();
			sender.sendMessage((Object)ChatColor.AQUA + plugin_name + "Version: " + versionString);
			return true;
		}
		if ((args[0]).equalsIgnoreCase("reload")) {
			this.plugin.reloadConfig();
			sender.sendMessage((Object)ChatColor.AQUA + plugin_name + "Configuration reloaded.");
			return true;
		}
		return false;
	}
}

