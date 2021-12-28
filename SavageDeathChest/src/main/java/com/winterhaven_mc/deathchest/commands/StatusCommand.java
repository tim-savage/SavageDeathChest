package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.chests.search.ProtectionPlugin;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.winterhaven_mc.deathchest.messages.MessageId.*;


public class StatusCommand extends AbstractSubcommand {

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
			plugin.messageBuilder.build(sender, COMMAND_FAIL_STATUS_PERMISSION).send(plugin.languageHandler);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String versionString = this.plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + "DeathChest " + ChatColor.AQUA + "Version: "
				+ ChatColor.RESET + versionString);

		if (plugin.debug) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}

		sender.sendMessage(ChatColor.GREEN + "Language: "
				+ ChatColor.RESET + plugin.getConfig().getString("language"));

		sender.sendMessage(ChatColor.GREEN + "Storage Type: "
				+ ChatColor.RESET + plugin.chestManager.getDataStoreType());

		int expireTime = plugin.getConfig().getInt("expire-time");
		if (expireTime == 0) {
			expireTime = -1;
		}

		sender.sendMessage(ChatColor.GREEN + "Chest Expiration: "
				+ ChatColor.RESET + plugin.languageHandler.getTimeString(TimeUnit.MINUTES.toMillis(expireTime)));

		int chestProtectionTime = plugin.getConfig().getInt("chest-protection-time");
		if (chestProtectionTime == 0) {
			chestProtectionTime = -1;
		}

		sender.sendMessage(ChatColor.GREEN + "Chest Protection Time: "
				+ ChatColor.RESET + plugin.languageHandler
					.getTimeString(TimeUnit.MINUTES.toMillis(chestProtectionTime)));

		sender.sendMessage(ChatColor.GREEN + "Require Chest: "
				+ ChatColor.RESET + plugin.getConfig().getString("require-chest"));

		sender.sendMessage(ChatColor.GREEN + "Protection Plugin Support:");

		int count = 0;
		for (ProtectionPlugin pp : ProtectionPlugin.values()) {

			if (pp.isInstalled()) {

				List<String> pluginSettings = new ArrayList<>();

				count++;
				String statusString = ChatColor.AQUA + "  " + pp.getPluginName() + ": ";

				if (plugin.getConfig().getBoolean("protection-plugins." + pp.getPluginName() + ".ignore-on-place")) {
					pluginSettings.add("ignore on placement");
				}
				else {
					pluginSettings.add("comply on placement");
				}
				if (plugin.getConfig().getBoolean("protection-plugins." + pp.getPluginName() + ".ignore-on-access")) {
					pluginSettings.add("ignore on access");
				}
				else {
					pluginSettings.add("comply on access");
				}
				statusString = statusString + ChatColor.RESET + pluginSettings;
				sender.sendMessage(statusString);
			}
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
