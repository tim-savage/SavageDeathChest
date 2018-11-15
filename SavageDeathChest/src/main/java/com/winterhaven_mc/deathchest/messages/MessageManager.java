package com.winterhaven_mc.deathchest.messages;


import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;
import com.winterhaven_mc.util.AbstractMessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class MessageManager extends AbstractMessageManager {

	// reference to main class
	private final PluginMain plugin;

	/**
	 * Class constructor
	 * @param plugin reference to main class
	 */
	public MessageManager(final PluginMain plugin) {

		// call super class constructor
		//noinspection unchecked
		super(plugin, MessageId.class);

		this.plugin = plugin;
	}

//# Variable substitutions:
//# %PLAYER_NAME%          Player's name
//# %PLAYER_NICKNAME%      Player's nickname
//# %PLAYER_DISPLAYNAME%   Player's display name, including prefix/suffix
//# %WORLD_NAME%           World name that player is in
//# %EXPIRE_TIME%          Remaining time at chest deployment


	@Override
	protected Map<String,String> getDefaultReplacements(CommandSender recipient) {

		Map<String,String> replacements = new HashMap<>();

		// strip color codes
		replacements.put("%PLAYER_NAME%",ChatColor.stripColor(recipient.getName()));
		replacements.put("%WORLD_NAME%",ChatColor.stripColor(getWorldName(recipient)));

//		replacements.put("%EXPIRE_TIME%",getTimeString(plugin.getConfig().getInt("teleport-warmup")));
		replacements.put("%EXPIRE_TIME%",getExpireTimeString());

		if (recipient instanceof Player) {
			Player player = (Player)recipient;
			replacements.put("%PLAYER_NICKNAME%",ChatColor.stripColor(player.getPlayerListName()));
			replacements.put("%PLAYER_DISPLAYNAME%",ChatColor.stripColor(player.getDisplayName()));
		}

		return replacements;
	}


	/**
	 * Send message to recipient
	 * @param recipient the recipient to whom to send a message
	 * @param messageId the message identifier
	 */
	public void sendMessage(final CommandSender recipient, final MessageId messageId) {

		// get default replacement map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		// send message
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Send message to recipient
	 * @param recipient the recipient to whom to send a message
	 * @param messageId the message identifier
	 * @param protectionPlugin the protection plugin whose name will be used in the message
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final ProtectionPlugin protectionPlugin) {

		//TODO is this check necessary?
		// if recipient is null, do nothing and return
		if (recipient == null) {
			return;
		}

		// get default replacement map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		replacements.put("%PLUGIN%",protectionPlugin.getPluginName());

		// send message
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Get expire time as formatted string
	 * @return a formatted expire time string
	 */
	private String getExpireTimeString() {

		String expireTime = "";

		int expiration = this.plugin.getConfig().getInt("expire-time");

		// if configured expire-time < 1, set expireTime string to "unlimited"
		if (expiration < 1) {
			expireTime = messages.getString("time_strings.UNLIMITED");
		}
		// otherwise, set string to hours and minutes remaining
		else {
			int hours = expiration / 60;
			int minutes = expiration % 60;
			String hour_string = this.messages.getString("time_strings.HOUR");
			String hour_plural_string = this.messages.getString("time_strings.HOUR_PLURAL");
			String minute_string = this.messages.getString("time_strings.MINUTE");
			String minute_plural_string = this.messages.getString("time_strings.MINUTE_PLURAL");
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
