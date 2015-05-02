package com.winterhaven_mc.deathchest;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor {
	
	private DeathChestMain plugin;
	private ArrayList<String> enabledWorlds;

	public CommandHandler(DeathChestMain plugin) {
		
		this.plugin = plugin;		
		plugin.getCommand("deathchest").setExecutor(this);
		updateEnabledWorlds();

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
			sender.sendMessage(ChatColor.GREEN + "Storage Type: " + ChatColor.RESET + plugin.chestManager.getCurrentDatastore().getDatastoreName());
			sender.sendMessage(ChatColor.GREEN + "Chest Expiration: " + ChatColor.RESET + plugin.getConfig().getInt("expire-time") + " minutes");
			sender.sendMessage(ChatColor.GREEN + "Enabled Worlds: " + ChatColor.RESET + getEnabledWorlds().toString());
			return true;
		}
		if ((args[0]).equalsIgnoreCase("reload")) {
			
			// get original storage type
			String originalStorageType = plugin.getConfig().getString("storage-type","sqlite");

			// reload config file
			plugin.reloadConfig();
			
			// update enabledWorlds list
			updateEnabledWorlds();
			
			// reload messages file
			plugin.messageManager.reloadMessages();
			
			// get current storage type
			String currentStorageType = plugin.getConfig().getString("storage-type","sqlite");
			
			// if storage type has changed, instantiate new datastore
			if (!originalStorageType.equals(currentStorageType)) {
				plugin.getLogger().info("Changing storage type from '" 
						+ originalStorageType + "' to '" + currentStorageType + "'...");
				plugin.chestManager.setCurrentDatastore(plugin.chestManager.getNewDatastore());
				
				// convert any old datastore files to new datastore
				plugin.chestManager.convertDatastores();
			}
			
			sender.sendMessage(ChatColor.AQUA + plugin_name + "Configuration reloaded.");
			return true;
		}
		return false;
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
