package com.winterhaven_mc.deathchest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.onarandombox.MultiverseCore.MultiverseCore;


public class MessageManager {
    private final DeathChestMain plugin;
    private ConfigAccessor messages;
	MultiverseCore mvCore;
	Boolean mvEnabled = false;

    public MessageManager(DeathChestMain plugin) {
        this.plugin = plugin;
        
		// install localization files
		installLocalizationFiles();

		// get configured language
		String language = plugin.getConfig().getString("language");
		
		if (!new File(plugin.getDataFolder()
				+ File.separator + "language" 
				+ File.separator + language + ".yml").exists()) {
			plugin.getLogger().info("Language file for " + language + " not found. Defaulting to en-US.");
			language = "en-US";
		}
		
		// instantiate custom configuration manager
		messages = new ConfigAccessor(plugin, "language" + File.separator + language + ".yml");
		
		// get reference to Multiverse-Core if installed
		mvCore = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
		if (mvCore != null && mvCore.isEnabled()) {
			plugin.getLogger().info("Multiverse-Core detected.");
			this.mvEnabled = true;
		}
		
    }

    public void sendPlayerMessage(Player player, String messageID) {
		if (messages.getConfig().getBoolean("messages." + messageID + ".enabled")) {
			String message = messages.getConfig().getString("messages." + messageID + ".string");

			// strip colorcodes and special characters from variables
			String playerName = player.getName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playerNickname = player.getPlayerListName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playerDisplayName = player.getDisplayName();
			String worldName = player.getWorld().getName();
			String expireTime = "";
			
			// if Multiverse is installed, use Multiverse world alias for world name
			if (mvEnabled && mvCore.getMVWorldManager().getMVWorld(worldName) != null) {
				
				// if Multiverse alias is not blank, set world name to alias
				if (!mvCore.getMVWorldManager().getMVWorld(worldName).getAlias().isEmpty()) {
					worldName = mvCore.getMVWorldManager().getMVWorld(worldName).getAlias();
				}
			}
	        

			int expiration = this.plugin.getConfig().getInt("expire-time");
			
			// if configured expire-time < 1, set expiretime string to "unlimited"
			if (expiration < 1) {
				expireTime = "unlimited";
			}
			// otherwise, set string to hours and minutes remaining
			else {
				int hours = expiration / 60;
				int minutes = expiration % 60;
				String hour_string = this.messages.getConfig().getString("hour");
				String hour_plural_string = this.messages.getConfig().getString("hour_plural");
				String minute_string = this.messages.getConfig().getString("minute");
				String minute_plural_string = this.messages.getConfig().getString("minute_plural");
				if (hours > 1) {
					expireTime = String.valueOf(expireTime) + hours + " " + hour_plural_string + " ";
				} else if (hours == 1) {
					expireTime = String.valueOf(expireTime) + hours + " " + hour_string + " ";
				}
				if (minutes > 1) {
					expireTime = String.valueOf(expireTime) + minutes + " " + minute_plural_string;
				} else if (minutes == 1) {
					expireTime = String.valueOf(expireTime) + minutes + " " + minute_string;
				}
				expireTime = expireTime.trim();
			}
			message = message.replaceAll("%playername%", playerName);
			message = message.replaceAll("%playerdisplayname%", playerDisplayName);
			message = message.replaceAll("%playernickname%", playerNickname);
			message = message.replaceAll("%worldname%", worldName);
			message = message.replaceAll("%expiretime%", expireTime);
			
			// send message to player
			player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
		}
    }


    public void broadcastMessage(Player player, String messageID) {
        if (!messages.getConfig().getBoolean("messages." + messageID + ".enabled")) {
        	return;
        }
        String message = messages.getConfig().getString("messages." + messageID + ".string");
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


	/**
	 * Install localization files from <em>language</em> directory in jar 
	 */
	private void installLocalizationFiles() {

		List<String> filelist = new ArrayList<String>();

		// get the absolute path to this plugin as URL
		URL pluginURL = plugin.getServer().getPluginManager().getPlugin(plugin.getName()).getClass().getProtectionDomain().getCodeSource().getLocation();

		// read files contained in jar, adding language/*.yml files to list
		ZipInputStream zip;
		try {
			zip = new ZipInputStream(pluginURL.openStream());
			while (true) {
				ZipEntry e = zip.getNextEntry();
				if (e == null) {
					break;
				}
				String name = e.getName();
				if (name.startsWith("language" + '/') && name.endsWith(".yml")) {
					filelist.add(name);
				}
			}
		} catch (IOException e1) {
			plugin.getLogger().warning("Could not read language files from jar.");
		}

		// iterate over list of language files and install from jar if not already present
		for (String filename : filelist) {
			// this check prevents a warning message when files are already installed
			if (new File(plugin.getDataFolder() + File.separator + filename).exists()) {
				continue;
			}
			plugin.saveResource(filename, false);
			plugin.getLogger().info("Installed localization file:  " + filename);
		}
	}
	

    public void reloadMessages() {
		installLocalizationFiles();
		messages.reloadConfig();
	}
}
