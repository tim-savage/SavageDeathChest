package com.winterhaven_mc.deathchest.util;

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
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;

import com.winterhaven_mc.util.StringUtil;
import com.winterhaven_mc.util.ConfigAccessor;


public final class MessageManager {

	// reference to main class
	private final PluginMain plugin;

	// configuration file manager for messages
	private ConfigAccessor messages;

	// configuration file manager for sounds
	private ConfigAccessor sounds;

	// cool down map
	private ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> messageCooldownMap;

	// currently selected language
	private String language;

	// language directory name
	private final String directoryName = "language";


	/**
	 * Class constructor
	 * @param plugin
	 */
	public MessageManager(final PluginMain plugin) {

		// set reference to main
		this.plugin = plugin;

		// install localization files
		installLocalizationFiles();

		// get configured language
		this.language = languageFileExists(plugin.getConfig().getString("language"));

		// instantiate custom configuration manager
		messages = new ConfigAccessor(plugin, directoryName + File.separator + language + ".yml");

		// initialize messageCooldownMap
		this.messageCooldownMap = new ConcurrentHashMap<UUID,ConcurrentHashMap<String,Long>>();

		// default sound file name
		String soundFileName = "sounds.yml";

		// old sound file name
		String oldsoundFileName = "pre-1.9_sounds.yml";

		// instantiate custom sound manager
		sounds = new ConfigAccessor(plugin, soundFileName);

		// install sound file if not present
		sounds.saveDefaultConfig();

		// install alternate sound file if not present
		File oldSounds = new File(plugin.getDataFolder() + File.separator + oldsoundFileName);
		if (!oldSounds.exists()) {
			plugin.saveResource(oldsoundFileName, false);
		}
		// release file object
		oldSounds = null;
	}

	/**
	 * Send message to player
	 * @param player
	 * @param messageId
	 */
	public void sendPlayerMessage(final Player player, final String messageId) {
		sendPlayerMessage(player,messageId,null);
	}

	/**
	 * Send message to player
	 * @param player
	 * @param messageId
	 * @param plugin
	 */
	public void sendPlayerMessage(final Player player, final String messageId, 
			final ProtectionPlugin protectionPlugin) {

		// if player is null, do nothing and return
		if (player == null) {
			return;
		}

		// if messageId does not exist in language file, do nothing and return
		if (messages.getConfig().getConfigurationSection("messages." + messageId) == null) {
			plugin.getLogger().warning("Could not read message '" + messageId + "' from language file.");
			return;
		}

		// if message is not enabled, do nothing and return
		if (!messages.getConfig().getBoolean("messages." + messageId + ".enabled")) {
			return;
		}

		// set substitution variables defaults			
		String playerName = "console";
		String playerNickname = "console";
		String playerDisplayName = "console";
		String worldName = "world";
		String protectionPluginName = "unknown";

		// get protection plugin name
		if (protectionPlugin != null) {
			protectionPluginName = protectionPlugin.getPluginName();
		}

		// get message cooldown time remaining
		Long lastDisplayed = getMessageCooldown(player,messageId);

		// get message repeat delay
		int messageRepeatDelay = messages.getConfig().getInt("messages." + messageId + ".repeat-delay");

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

		// get player world name from world manager
		worldName = plugin.worldManager.getWorldName(player.getWorld());

		// get message string
		String message = messages.getConfig().getString("messages." + messageId + ".string");

		// get time to expire formatted string
		String expireTime = getExpireTimeString();

		// do variable substitutions
		if (message.contains("%")) {
			message = StringUtil.replace(message,"%playername%", playerName);
			message = StringUtil.replace(message,"%playerdisplayname%", playerDisplayName);
			message = StringUtil.replace(message,"%playernickname%", playerNickname);
			message = StringUtil.replace(message,"%worldname%", worldName);
			message = StringUtil.replace(message,"%expiretime%", expireTime);
			message = StringUtil.replace(message,"%plugin%", protectionPluginName);

			// do variable substitutions, stripping color codes from all caps variables
			message = StringUtil.replace(message,"%PLAYERNAME%", 
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',playerName)));
			message = StringUtil.replace(message,"%PLAYERNICKNAME%", 
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',playerNickname)));
			message = StringUtil.replace(message,"%WORLDNAME%", 
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',worldName)));

			// no stripping of color codes necessary, but do variable substitutions anyhow
			// in case all caps variables were used
			message = StringUtil.replace(message,"%PLAYERDISPLAYNAME%", playerDisplayName);
			message = StringUtil.replace(message,"%PLUGIN%", protectionPluginName);
		}

		// send message to player
		player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));

	}


	/**
	 * Send message to all players
	 * @param player
	 * @param messageId
	 */
	public void broadcastMessage(final Player player, final String messageId) {
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
	 * Play sound effect for action
	 * @param sender
	 * @param soundId
	 */
	public void playerSound(final CommandSender sender, final String soundId) {

		if (sender instanceof Player) {
			playerSound((Player)sender,soundId);
		}
	}

	/**
	 * Play sound effect for action
	 * @param player
	 * @param soundId
	 */
	public void playerSound(final Player player, final String soundId) {

		// if sound effects are disabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("sound-effects")) {
			return;
		}

		// if sound is set to enabled in sounds file
		if (sounds.getConfig().getBoolean("sounds." + soundId + ".enabled")) {

			// get player only setting from config file
			boolean playerOnly = sounds.getConfig().getBoolean("sounds." + soundId + ".player-only");

			// get sound name from config file
			String soundName = sounds.getConfig().getString("sounds." + soundId + ".sound");

			// get sound volume from config file
			float volume = (float) sounds.getConfig().getDouble("sounds." + soundId + ".volume");

			// get sound pitch from config file
			float pitch = (float) sounds.getConfig().getDouble("sounds." + soundId + ".pitch");

			try {
				// if sound is set player only, use player.playSound()
				if (playerOnly) {
					player.playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);
				}
				// else use world.playSound() so other players in vicinity can hear
				else {
					player.getWorld().playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);
				}
			} catch (IllegalArgumentException e) {
				plugin.getLogger().warning("An error occured while trying to play the sound '" + soundName 
						+ "'. You probably need to update the sound name in your sounds.yml file.");
			}
		}
	}


	/**
	 * Reload language files
	 */
	public void reload() {

		// reinstall message files if necessary
		installLocalizationFiles();

		// get currently configured language
		String newLanguage = languageFileExists(plugin.getConfig().getString("language"));

		// if configured language has changed, instantiate new messages object
		if (!newLanguage.equals(this.language)) {
			this.messages = new ConfigAccessor(plugin, directoryName + File.separator + newLanguage + ".yml");
			this.language = newLanguage;
			plugin.getLogger().info("New language " + this.language + " enabled.");
		}

		// reload language file
		messages.reloadConfig();
	}


	/**
	 * Install localization files from language directory in jar 
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


	/**
	 * Check if file exists for a given language
	 * @param language
	 * @return
	 */
	private String languageFileExists(final String language) {

		// check if localization file for configured language exists, if not then fallback to en-US
		File languageFile = new File(plugin.getDataFolder() 
				+ File.separator + directoryName 
				+ File.separator + language + ".yml");

		if (languageFile.exists()) {
			return language;
		}
		plugin.getLogger().info("Language file " + language + ".yml does not exist. Defaulting to en-US.");
		return "en-US";
	}


	/**
	 * Add entry to message cooldown map
	 * @param player
	 * @param messageId
	 */
	private void putMessageCooldown(final Player player, final String messageId) {

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
	private long getMessageCooldown(final Player player, final String messageId) {

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
	 * Get expire time as formatted string
	 * @return
	 */
	private String getExpireTimeString() {

		String expireTime = "";

		int expiration = this.plugin.getConfig().getInt("expire-time");

		// if configured expire-time < 1, set expiretime string to "unlimited"
		if (expiration < 1) {
			expireTime = messages.getConfig().getString("unlimited");
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


	public List<String> getSignText() {
		return this.messages.getConfig().getStringList("sign-text");
	}


	public String getDateFormat() {
		return this.messages.getConfig().getString("date-format");
	}

}
