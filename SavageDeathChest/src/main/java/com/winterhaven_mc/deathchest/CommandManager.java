package com.winterhaven_mc.deathchest;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {
	
	private DeathChestMain plugin;
	private ArrayList<String> enabledWorlds;
	private final String pluginName;

	public CommandManager(DeathChestMain plugin) {
		
		this.plugin = plugin;		
		plugin.getCommand("deathchest").setExecutor(this);
		pluginName = "[" + this.plugin.getName() + "] ";
		updateEnabledWorlds();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {


		int maxArgs = 1;

		if (args.length > maxArgs) {
			sender.sendMessage(ChatColor.RED + pluginName + "Too many arguments.");
			return false;
		}
		
		String subcmd = "";
		
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
		if (subcmd.equalsIgnoreCase("reload")) {
			return reloadCommand(sender);
		}
		return false;
	}
	
	
	boolean statusCommand(CommandSender sender) {
		
		String versionString = this.plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + pluginName + ChatColor.AQUA + "Version: " + ChatColor.RESET + versionString);
		sender.sendMessage(ChatColor.GREEN + "Language: " + ChatColor.RESET + plugin.getConfig().getString("language"));
		sender.sendMessage(ChatColor.GREEN + "Storage Type: " + ChatColor.RESET + plugin.dataStore.getName());
		sender.sendMessage(ChatColor.GREEN + "Chest Expiration: " + ChatColor.RESET + plugin.getConfig().getInt("expire-time") + " minutes");
		sender.sendMessage(ChatColor.GREEN + "Enabled Worlds: " + ChatColor.RESET + getEnabledWorlds().toString());
		return true;		
	}
	
	boolean reloadCommand(CommandSender sender) {
		
		// reload config file
		plugin.reloadConfig();
		
		// update debug field
		plugin.debug = plugin.getConfig().getBoolean("debug");
		
		// update enabledWorlds list
		updateEnabledWorlds();
		
		// reload messages
		plugin.messageManager.reload();
		
		// reload datastore if changed
		DataStoreFactory.reload();
		
		sender.sendMessage(ChatColor.AQUA + pluginName + "Configuration reloaded.");
		return true;

	}
	
	/**
	 * update enabledWorlds ArrayList field from config file settings
	 */
	void updateEnabledWorlds() {
		
		// copy list of enabled worlds from config into enabledWorlds ArrayList field
		this.enabledWorlds = new ArrayList<String>(plugin.getConfig().getStringList("enabled-worlds"));
		
		// if enabledWorlds ArrayList is empty, add all worlds to ArrayList
		if (this.enabledWorlds.isEmpty()) {
			for (World world : plugin.getServer().getWorlds()) {
				enabledWorlds.add(world.getName());
			}
		}
		
		// remove each disabled world from enabled worlds field
		for (String disabledWorld : plugin.getConfig().getStringList("disabled-worlds")) {
			this.enabledWorlds.remove(disabledWorld);
		}
	}
	
	
	/**
	 * get list of enabled worlds
	 * @return ArrayList of String enabledWorlds
	 */
	ArrayList<String> getEnabledWorlds() {
		return this.enabledWorlds;
	}

}
