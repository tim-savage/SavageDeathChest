package com.winterhaven_mc.deathchest;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class MessageManager {
    private final DeathChestMain plugin;
    private ConfigAccessor messages;

    public MessageManager(DeathChestMain plugin) {
        this.plugin = plugin;
        
		// install localization files
		String[] localization_files = {"en-US","es-ES"};	
		installLocalizationFiles(localization_files);

		// get configured language
		String language = plugin.getConfig().getString("language","en-US");
		
		if (!new File(plugin.getDataFolder() + "/language/" + language + ".yml").exists()) {
			plugin.getLogger().info("Language file for " + language + " not found. Defaulting to en-US.");
			language = "en-US";
		}
		
		// instantiate custom configuration manager
		messages = new ConfigAccessor(plugin, "language/" + language + ".yml");
    }

    public void sendPlayerMessage(Player player, String messageID) {
		if (messages.getConfig().getBoolean("messages." + messageID + ".enabled",false)) {
			String message = messages.getConfig().getString("messages." + messageID + ".string");

			// strip colorcodes and special characters from variables
			String playername = player.getName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playernickname = player.getPlayerListName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playerdisplayname = player.getDisplayName();
			String worldname = player.getWorld().getName();
			String expiretime = "";

			int expiration = this.plugin.getConfig().getInt("expire-time");
			
			// if configured expire-time < 1, set expiretime string to "unlimited"
			if (expiration < 1) {
				expiretime = "unlimited";
			}
			// otherwise, set string to hours and minutes remaining
			else {
				int hours = expiration / 60;
				int minutes = expiration % 60;
				String hour_string = this.messages.getConfig().getString("hour", "hour");
				String hour_plural_string = this.messages.getConfig().getString("hour_plural", "hours");
				String minute_string = this.messages.getConfig().getString("minute", "minute");
				String minute_plural_string = this.messages.getConfig().getString("minute_plural", "minutes");
				if (hours > 1) {
					expiretime = String.valueOf(expiretime) + hours + " " + hour_plural_string + " ";
				} else if (hours == 1) {
					expiretime = String.valueOf(expiretime) + hours + " " + hour_string + " ";
				}
				if (minutes > 1) {
					expiretime = String.valueOf(expiretime) + minutes + " " + minute_plural_string;
				} else if (minutes == 1) {
					expiretime = String.valueOf(expiretime) + minutes + " " + minute_string;
				}
				expiretime = expiretime.trim();
			}
			message = message.replaceAll("%playername%", playername);
			message = message.replaceAll("%playerdisplayname%", playerdisplayname);
			message = message.replaceAll("%playernickname%", playernickname);
			message = message.replaceAll("%worldname%", worldname);
			message = message.replaceAll("%expiretime%", expiretime);
			
			// send message to player
			player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
		}
    }


    public void broadcastMessage(Player player, String messageID) {
        if (!this.messages.getConfig().getBoolean("messages." + messageID + ".enabled", false)) return;
        String message = this.messages.getConfig().getString("messages." + messageID + ".string");
        String playername = player.getName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
        String playernickname = player.getPlayerListName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
        String playerdisplayname = player.getDisplayName();
        String worldname = player.getWorld().getName();
        message = message.replaceAll("%playername%", playername);
        message = message.replaceAll("%playerdisplayname%", playerdisplayname);
        message = message.replaceAll("%playernickname%", playernickname);
        message = message.replaceAll("%worldname%", worldname);
        this.plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
    }


    private void installLocalizationFiles(String[] filelist) {
		
		for (String filename : filelist) {
			if (!new File(plugin.getDataFolder() + "/language/" + filename + ".yml").exists()) {
				this.plugin.saveResource("language/" + filename + ".yml",false);
				plugin.getLogger().info("Installed localization files for " + filename + ".");
			}
		}
	}
	

    public void reloadMessages() {
		messages.reloadConfig();
	}
}
