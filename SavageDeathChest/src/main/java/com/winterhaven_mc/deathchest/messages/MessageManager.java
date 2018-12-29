package com.winterhaven_mc.deathchest.messages;


import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.util.ProtectionPlugin;
import com.winterhaven_mc.deathchest.chests.DeathChest;
import com.winterhaven_mc.util.AbstractMessageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * A class that manages player messages. Tasks include sending player messages,
 * tracking message cooldown times and replacing message placeholders with appropriate text.
 */
public final class MessageManager extends AbstractMessageManager {

	// reference to main class
	private final PluginMain plugin;

	/**
	 * Class constructor
	 *
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
//# %EXPIRE_TIME%          Remaining time at chest deployment
//# %WORLD_NAME%           World name of chest (or player if no chest)
//# %LOC_X%                Chest coordinates (or player if no chest)
//# %LOC_Y%                Chest coordinates (or player if no chest)
//# %LOC_Z%                Chest coordinates (or player if no chest)


	@Override
	protected Map<String, String> getDefaultReplacements(CommandSender recipient) {

		Map<String, String> replacements = new HashMap<>();

		// get expire time from config
		long expireTime = plugin.getConfig().getLong("expire-time");

		// convert time to milliseconds
		expireTime = TimeUnit.MINUTES.toMillis(expireTime);

		// if expire time is zero, convert to negative (allow config to specify zero for unlimited time)
		if (expireTime == 0) {
			expireTime = -1;
		}

		replacements.put("%EXPIRE_TIME%", getTimeString(expireTime));
		replacements.put("%PLAYER_NAME%", ChatColor.stripColor(recipient.getName()));

		if (recipient instanceof Player) {
			Player player = (Player) recipient;
			replacements.put("%PLAYER_NICKNAME%", ChatColor.stripColor(player.getPlayerListName()));
			replacements.put("%PLAYER_DISPLAYNAME%", ChatColor.stripColor(player.getDisplayName()));

			replacements.put("%WORLD_NAME%", ChatColor.stripColor(getWorldName(recipient)));
			replacements.put("%LOC_X%", String.valueOf(player.getLocation().getBlockX()));
			replacements.put("%LOC_Y%", String.valueOf(player.getLocation().getBlockY()));
			replacements.put("%LOC_Z%", String.valueOf(player.getLocation().getBlockZ()));
		}

		return replacements;
	}


	/**
	 * Send message to recipient
	 *
	 * @param recipient the recipient to whom to send a message
	 * @param messageId the message identifier
	 */
	public void sendMessage(final CommandSender recipient, final MessageId messageId) {

		// get default replacement map
		Map<String, String> replacements = getDefaultReplacements(recipient);

		// send message
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Send message to recipient
	 *
	 * @param recipient  the recipient to whom to send a message
	 * @param messageId  the message identifier
	 * @param deathChest the chest being referenced in this message
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final DeathChest deathChest) {

		// get default replacement map
		Map<String, String> replacements = getDefaultReplacements(recipient);

		if (deathChest != null) {
			if (deathChest.getLocation() != null) {
				replacements.put("%WORLD_NAME%", plugin.worldManager.getWorldName(deathChest.getLocation().getWorld()));
				replacements.put("%LOC_X%", String.valueOf(deathChest.getLocation().getBlockX()));
				replacements.put("%LOC_Y%", String.valueOf(deathChest.getLocation().getBlockY()));
				replacements.put("%LOC_Z%", String.valueOf(deathChest.getLocation().getBlockZ()));
			}

			replacements.put("%OWNER_NAME%",
					ChatColor.stripColor(plugin.getServer().getPlayer(deathChest.getOwnerUUID()).getName()));

			if (deathChest.getKillerUUID() == null) {
				replacements.put("%KILLER_NAME%", "-");
			}
			else {
				replacements.put("%KILLER_NAME%",
						ChatColor.stripColor(plugin.getServer().getPlayer(deathChest.getKillerUUID()).getName()));
			}

			replacements.put("%REMAINING_TIME%",
					getTimeString(deathChest.getExpirationTime() - System.currentTimeMillis()));
		}

		// send message
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Send message to recipient
	 *
	 * @param recipient  the recipient to whom to send a message
	 * @param messageId  the message identifier
	 * @param deathChest the chest being referenced in this message
	 * @param listCount  the item number of a list of which this message is a single entry
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final DeathChest deathChest,
							final int listCount) {

		// get default replacement map
		Map<String, String> replacements = getDefaultReplacements(recipient);

		if (deathChest != null) {
			if (deathChest.getLocation() != null) {
				replacements.put("%WORLD_NAME%", plugin.worldManager.getWorldName(deathChest.getLocation().getWorld()));
				replacements.put("%LOC_X%", String.valueOf(deathChest.getLocation().getBlockX()));
				replacements.put("%LOC_Y%", String.valueOf(deathChest.getLocation().getBlockY()));
				replacements.put("%LOC_Z%", String.valueOf(deathChest.getLocation().getBlockZ()));
			}

			replacements.put("%OWNER_NAME%",
					ChatColor.stripColor(plugin.getServer().getOfflinePlayer(deathChest.getOwnerUUID()).getName()));

			if (deathChest.getKillerUUID() == null) {
				replacements.put("%KILLER_NAME%", "-");
			}
			else {
				replacements.put("%KILLER_NAME%",
						ChatColor.stripColor(plugin.getServer().getOfflinePlayer(deathChest.getKillerUUID()).getName()));
			}

			replacements.put("%REMAINING_TIME%",
					getTimeString(deathChest.getExpirationTime() - System.currentTimeMillis(), TimeUnit.MINUTES));

			replacements.put("%ITEM_NUMBER%", String.valueOf(listCount));
		}

		// send message
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Send message to recipient
	 *
	 * @param recipient        the recipient to whom to send a message
	 * @param messageId        the message identifier
	 * @param protectionPlugin the protection plugin whose name will be used in the message
	 */
	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final ProtectionPlugin protectionPlugin) {

		// if recipient is null, do nothing and return
		if (recipient == null) {
			return;
		}

		// get default replacement map
		Map<String, String> replacements = getDefaultReplacements(recipient);

		replacements.put("%PLUGIN%", protectionPlugin.getPluginName());

		// send message
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}


	public void sendMessage(final CommandSender recipient,
							final MessageId messageId,
							final int page,
							final int pageCount) {

		// if recipient is null, do nothing and return
		if (recipient == null) {
			return;
		}

		// get default replacement map
		Map<String, String> replacements = getDefaultReplacements(recipient);

		replacements.put("%PAGE%", String.valueOf(page));
		replacements.put("%PAGE_COUNT%", String.valueOf(pageCount));

		// send message
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Get sign text from language file
	 *
	 * @return List of String - lines of sign text
	 */
	public List<String> getSignText() {
		return this.messages.getStringList("SIGN_TEXT");
	}


	/**
	 * Get date format string from language file
	 *
	 * @return String - date format string
	 */
	public String getDateFormat() {
		return this.messages.getString("DATE_FORMAT");
	}

}
