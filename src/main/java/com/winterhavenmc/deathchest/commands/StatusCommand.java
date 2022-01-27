package com.winterhavenmc.deathchest.commands;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.sounds.SoundId;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.winterhavenmc.deathchest.messages.MessageId.*;


final class StatusCommand extends AbstractSubcommand {

	private final PluginMain plugin;


	StatusCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		setName("status");
		setUsage("/deathchest status");
		setDescription(COMMAND_HELP_STATUS);
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {
		if (!sender.hasPermission("deathchest.status")) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_STATUS_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String versionString = this.plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + "DeathChest " + ChatColor.AQUA + "Version: "
				+ ChatColor.RESET + versionString);

		if (plugin.getConfig().getBoolean("debug")) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}

		sender.sendMessage(ChatColor.GREEN + "Language: "
				+ ChatColor.RESET + plugin.getConfig().getString("language"));

		int expireTime = plugin.getConfig().getInt("expire-time");
		if (expireTime == 0) {
			expireTime = -1;
		}

		sender.sendMessage(ChatColor.GREEN + "Chest Expiration: "
				+ ChatColor.RESET + plugin.messageBuilder.getTimeString(TimeUnit.MINUTES.toMillis(expireTime)));

		int chestProtectionTime = plugin.getConfig().getInt("chest-protection-time");
		if (chestProtectionTime == 0) {
			chestProtectionTime = -1;
		}

		sender.sendMessage(ChatColor.GREEN + "Chest Protection: "
				+ ChatColor.RESET + plugin.messageBuilder
					.getTimeString(TimeUnit.MINUTES.toMillis(chestProtectionTime)));

		sender.sendMessage(ChatColor.GREEN + "Search Distance: "
				+ ChatColor.RESET + plugin.getConfig().getString("search-distance"));

		sender.sendMessage(ChatColor.GREEN + "Require Chest: "
				+ ChatColor.RESET + plugin.getConfig().getString("require-chest"));

		sender.sendMessage(ChatColor.GREEN + "Quick Loot: "
				+ ChatColor.RESET + plugin.getConfig().getString("quick-loot"));

		sender.sendMessage(ChatColor.GREEN + "Killer Looting: "
				+ ChatColor.RESET + plugin.getConfig().getString("killer-looting"));

		sender.sendMessage(ChatColor.GREEN + "Protection Plugin Support:");

		int count = 0;
		for (ProtectionPlugin protectionPlugin : plugin.protectionPluginRegistry.getAll()) {

			Collection<String> pluginSettings = new LinkedList<>();

			count++;
			String statusString = ChatColor.AQUA + "  " + protectionPlugin.getPluginName() + ": ";

			if (protectionPlugin.isIgnoredOnPlace()) {
				pluginSettings.add("ignore on placement");
			}
			else {
				pluginSettings.add("comply on placement");
			}
			if (protectionPlugin.isIgnoredOnAccess()) {
				pluginSettings.add("ignore on access");
			}
			else {
				pluginSettings.add("comply on access");
			}
			statusString = statusString + ChatColor.RESET + pluginSettings;
			sender.sendMessage(statusString);
		}
		if (count == 0) {
			sender.sendMessage(ChatColor.AQUA + "  [ NONE ENABLED ]");
		}

		sender.sendMessage(ChatColor.GREEN + "Enabled Worlds: " + ChatColor.RESET +
				plugin.worldManager.getEnabledWorldNames().toString());

		sender.sendMessage(ChatColor.GREEN + "Replaceable Blocks: " + ChatColor.RESET +
				plugin.chestManager.getReplaceableBlocks());

		return true;
	}

}
