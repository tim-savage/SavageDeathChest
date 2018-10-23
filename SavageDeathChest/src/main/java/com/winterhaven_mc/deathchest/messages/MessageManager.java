package com.winterhaven_mc.deathchest.messages;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;

import com.winterhaven_mc.util.StringUtil;
import com.winterhaven_mc.util.LanguageManager;
import com.winterhaven_mc.util.YamlLanguageManager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class MessageManager {

	// reference to main class
	private final PluginMain plugin;

	// message cooldown hashmap
	private ConcurrentHashMap<UUID, EnumMap<MessageId, Long>> messageCooldownMap;

	// language manager
	private final LanguageManager languageManager;

	// configuration object for messages
	private Configuration messages;


	/**
	 * Class constructor
	 * @param plugin reference to main class
	 */
	public MessageManager(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// initialize messageCooldownMap
		this.messageCooldownMap = new ConcurrentHashMap<>();

		// instantiate messageFileHelper
		this.languageManager = new YamlLanguageManager(plugin);

		// load messages from file
		this.messages = languageManager.loadMessages();
	}


	/**
	 * Send message to player
	 * @param player the player to whom to send a message
	 * @param messageId the message identifier
	 */
	public void sendPlayerMessage(final Player player, final MessageId messageId) {
		sendPlayerMessage(player,messageId,null);
	}


	/**
	 * Send message to player
	 * @param player the player to whom to send a message
	 * @param messageId the message identifier
	 * @param protectionPlugin the protection plugin whose name will be used in the message
	 */
	public void sendPlayerMessage(final Player player, final MessageId messageId,
			final ProtectionPlugin protectionPlugin) {

		// if player is null, do nothing and return
		if (player == null) {
			return;
		}

		// if messageId does not exist in language file, do nothing and return
		if (messages.getConfigurationSection("messages." + messageId) == null) {
			plugin.getLogger().warning("Could not read message '" + messageId + "' from language file.");
			return;
		}

		// if message is not enabled, do nothing and return
		if (!messages.getBoolean("messages." + messageId + ".enabled")) {
			return;
		}

		// set substitution variables defaults			
		String playerName;
		String playerNickname;
		String playerDisplayName;
		String worldName;
		String protectionPluginName = "unknown";

		// get protection plugin name
		if (protectionPlugin != null) {
			protectionPluginName = protectionPlugin.getPluginName();
		}

		// get message cooldown time remaining
		long lastDisplayed = getMessageCooldown(player,messageId);

		// get message repeat delay
		int messageRepeatDelay = messages.getInt("messages." + messageId.toString() + ".repeat-delay");

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
		String message = messages.getString("messages." + messageId + ".string");

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
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',message));

	}


	/**
	 * Send message to all players
	 * @param player the player to whose name will be used in the message
	 * @param messageId the message identifier
	 */
	@SuppressWarnings("unused")
	public void broadcastMessage(final Player player, final String messageId) {
		if (!messages.getBoolean("messages." + messageId + ".enabled")) {
			return;
		}
		String message = messages.getString("messages." + messageId + ".string");
		String playername = player.getName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
		String playernickname = player.getPlayerListName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
		String playerdisplayname = player.getDisplayName();
		String worldname = player.getWorld().getName();
		message = message.replaceAll("%playername%", playername);
		message = message.replaceAll("%playerdisplayname%", playerdisplayname);
		message = message.replaceAll("%playernickname%", playernickname);
		message = message.replaceAll("%worldname%", worldname);
		this.plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',message));
	}


	/**
	 * Reload custom messages file
	 */
	public final void reload() {

		// reload messages
		this.messages = languageManager.loadMessages();
	}


	/**
	 * Add entry to message cooldown map
	 * @param player the player to add to the message cooldown map
	 * @param messageId the message identifier to add to the message cooldown map for player
	 */
	private void putMessageCooldown(final Player player, final MessageId messageId) {

		EnumMap<MessageId,Long> tempMap = new EnumMap<>(MessageId.class);

		tempMap.put(messageId, System.currentTimeMillis());
		messageCooldownMap.put(player.getUniqueId(),tempMap);
	}


	/**
	 * get entry from message cooldown map
	 * @param player the player for whom to retrieve a message cooldown expire time
	 * @param messageId the message identifier for which to retrieve a message cooldown expire time for player
	 * @return cooldown expire time
	 */
	private long getMessageCooldown(final Player player, final MessageId messageId) {

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
	 * @param player the player to remove from the message cooldown map
	 */
	@SuppressWarnings("unused")
	public void removePlayerCooldown(final Player player) {
		messageCooldownMap.remove(player.getUniqueId());
	}


	/**
	 * Get expire time as formatted string
	 * @return a formatted expire time string
	 */
	private String getExpireTimeString() {

		String expireTime = "";

		int expiration = this.plugin.getConfig().getInt("expire-time");

		// if configured expire-time < 1, set expiretime string to "unlimited"
		if (expiration < 1) {
			expireTime = messages.getString("unlimited");
		}
		// otherwise, set string to hours and minutes remaining
		else {
			int hours = expiration / 60;
			int minutes = expiration % 60;
			String hour_string = this.messages.getString("hour");
			String hour_plural_string = this.messages.getString("hour_plural");
			String minute_string = this.messages.getString("minute");
			String minute_plural_string = this.messages.getString("minute_plural");
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
		return this.messages.getStringList("sign-text");
	}


	public String getDateFormat() {
		return this.messages.getString("date-format");
	}

}
