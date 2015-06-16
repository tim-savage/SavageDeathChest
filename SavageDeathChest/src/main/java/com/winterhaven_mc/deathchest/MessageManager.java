package com.winterhaven_mc.deathchest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.onarandombox.MultiverseCore.MultiverseCore;


public class MessageManager {
    private final PluginMain plugin;
    private ConfigAccessor messages;
	private ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> messageCooldownMap;
	MultiverseCore mvCore;
	Boolean mvEnabled = false;
	private String language;

	
	/**
	 * Class constructor
	 * @param plugin
	 */
    public MessageManager(PluginMain plugin) {
        this.plugin = plugin;
        
		// install localization files
		installLocalizationFiles();

		// get configured language
		this.language = languageFileExists(plugin.getConfig().getString("language"));

		// instantiate custom configuration manager
		messages = new ConfigAccessor(plugin, "language" + File.separator + language + ".yml");
		
		// initialize messageCooldownMap
		this.messageCooldownMap = new ConcurrentHashMap<UUID,ConcurrentHashMap<String,Long>>();
		
		// get reference to Multiverse-Core if installed
		mvCore = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
		if (mvCore != null && mvCore.isEnabled()) {
			plugin.getLogger().info("Multiverse-Core detected.");
			this.mvEnabled = true;
		}
    }

    
    /**
     * Send message to player
     * @param player
     * @param messageId
     */
    public void sendPlayerMessage(Player player, String messageId) {
    	
    	// if message is not enabled, do nothing and return
		if (!messages.getConfig().getBoolean("messages." + messageId + ".enabled")) {
			return;
		}
		
		// set substitution variables defaults			
		String playerName = "console";
		String playerNickname = "console";
		String playerDisplayName = "console";
		String worldName = "world";

		// get message cooldown time remaining
		Long lastDisplayed = getMessageCooldown(player,messageId);

		// get message repeat delay
		int messageRepeatDelay = messages.getConfig().getInt("messages." + messageId + ".repeat-delay",1);

		// if message has repeat delay value and was displayed to player more recently, do nothing and return
		if (lastDisplayed > System.currentTimeMillis() - messageRepeatDelay * 1000) {
			return;
		}

		// if repeat delay value is greater than zero, add entry to messageCooldownMap
		if (messageRepeatDelay > 0) {
			putMessageCooldown(player,messageId);
		}

		// assign player dependent variables
		playerName = player.getName();
		playerNickname = player.getPlayerListName();
		playerDisplayName = player.getDisplayName();
		worldName = player.getWorld().getName();

		
		// get message string
		String message = messages.getConfig().getString("messages." + messageId + ".string");

		// if Multiverse is installed, use Multiverse world alias for world name
		if (mvEnabled && mvCore.getMVWorldManager().getMVWorld(worldName) != null) {

			// if Multiverse alias is not blank, set world name to alias
			if (!mvCore.getMVWorldManager().getMVWorld(worldName).getAlias().isEmpty()) {
				worldName = mvCore.getMVWorldManager().getMVWorld(worldName).getAlias();
			}
		}

		// get time to expire formatted string
		String expireTime = getExpireTimeString();

		// do variable substitutions
		message = message.replaceAll("%playername%", playerName);
		message = message.replaceAll("%playerdisplayname%", playerDisplayName);
		message = message.replaceAll("%playernickname%", playerNickname);
		message = message.replaceAll("%worldname%", worldName);
		message = message.replaceAll("%expiretime%", expireTime);
		
		// do variable substitutions, stripping color codes from all caps variables
		message = message.replace("%PLAYERNAME%", 
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',playerName)));
		message = message.replace("%PLAYERNICKNAME%", 
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',playerNickname)));
		message = message.replace("%WORLDNAME%", 
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',worldName)));

		// no stripping of color codes necessary, but do variable substitutions anyhow
		// in case all caps variables were used
		message = message.replace("%PLAYERDISPLAYNAME%", playerDisplayName);

		// send message to player
		player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));

    }


    /**
     * Send message to all players
     * @param player
     * @param messageId
     */
    public void broadcastMessage(Player player, String messageId) {
        if (!messages.getConfig().getBoolean("messages." + messageId + ".enabled")) {
        	return;
        }
        String message = messages.getConfig().getString("messages." + messageId + ".string");
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
     * Reload language files
     */
	void reload() {
		
		// reinstall message files if necessary
		installLocalizationFiles();
		
		// get currently configured language
		String newLanguage = languageFileExists(plugin.getConfig().getString("language"));
		
		// if configured language has changed, instantiate new messages object
		if (!newLanguage.equals(this.language)) {
			this.messages = new ConfigAccessor(plugin, "language" + File.separator + newLanguage + ".yml");
			this.language = newLanguage;
			plugin.getLogger().info("New language " + this.language + " enabled.");
		}
		
		// reload language file
		messages.reloadConfig();
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

	private String languageFileExists(String language) {
		
		// check if localization file for configured language exists, if not then fallback to en-US
		File languageFile = new File(plugin.getDataFolder() 
				+ File.separator + "language" 
				+ File.separator + language + ".yml");
		
		if (languageFile.exists()) {
			return language;
	    }
		plugin.getLogger().info("Language file " + language + ".yml does not exist. Defaulting to en-US.");
		return "en-US";
	}

	public String getLanguage() {
		return this.language;
	}
	
	
	/**
	 * Add entry to message cooldown map
	 * @param player
	 * @param messageId
	 */
	private void putMessageCooldown(Player player, String messageId) {
		
    	ConcurrentHashMap<String, Long> tempMap = new ConcurrentHashMap<String, Long>();
    	tempMap.put(messageId, System.currentTimeMillis());
    	messageCooldownMap.put(player.getUniqueId(), tempMap);
	}


	/**
	 * get entry from message cooldown map
	 * @param player
	 * @param messageId
	 * @return cooldown expire time
	 */
	private long getMessageCooldown(Player player, String messageId) {
		
		// check if player is in message cooldown hashmap
		if (messageCooldownMap.containsKey(player.getUniqueId())) {
			
			// check if messageID is in player's cooldown hashmap
			if (messageCooldownMap.get(player.getUniqueId()).containsKey(messageId)) {
				
				// return cooldown time
				return messageCooldownMap.get(player.getUniqueId()).get(messageId);
			}
		}
		return 0L;
	}
	
	
	/**
	 * Remove player from message cooldown map
	 * @param player
	 */
	void removePlayerCooldown(Player player) {
		messageCooldownMap.remove(player.getUniqueId());
	}

	
	String getExpireTimeString() {
		
		String expireTime = "";
		
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
		return expireTime;
	}
	
}
