package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.chests.DeathChest;
import com.winterhaven_mc.deathchest.messages.Macro;
import com.winterhaven_mc.deathchest.messages.Message;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

import static com.winterhaven_mc.deathchest.messages.MessageId.*;


public class ListCommand implements Subcommand {

	private final PluginMain plugin;
	private final CommandSender sender;
	private final List<String> args;


	ListCommand(final PluginMain plugin, final CommandSender sender, final List<String> args) {
		this.plugin = Objects.requireNonNull(plugin);
		this.sender = Objects.requireNonNull(sender);
		this.args = Objects.requireNonNull(args);
	}


	@Override
	public boolean execute() {

		// if command sender does not have permission to list death chests, output error message and return true
		if (!sender.hasPermission("deathchest.list")) {
			Message.create(sender, COMMAND_FAIL_LIST_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		Player player = null;

		// cast sender to player
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		// argument limits
		int maxArgs = 3;

		if (args.size() > maxArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			HelpCommand.displayUsage(sender, "list");
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		OfflinePlayer targetPlayer = null;

		// get list of offline players
		OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();

		String passedPlayerName = "";

		int page = 1;

		if (args.size() == 2) {

			passedPlayerName = args.get(1);

			// if second argument not a number, try to match player name
			try {
				page = Integer.parseInt(args.get(1));
			}
			catch (NumberFormatException e) {
				// if sender does not have list other permission, send message and return
				if (!sender.hasPermission("deathchest.list.other")) {
					Message.create(sender, COMMAND_FAIL_LIST_OTHER_PERMISSION).send();
					plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
					return true;
				}
				for (OfflinePlayer offlinePlayer : offlinePlayers) {
					if (args.get(1).equalsIgnoreCase(offlinePlayer.getName())) {
						targetPlayer = offlinePlayer;
					}
				}
				if (targetPlayer == null && !passedPlayerName.equals("*")) {
					Message.create(sender, LIST_PLAYER_NOT_FOUND).send();
					plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
					return true;
				}
			}
		}

		if (args.size() == 3) {
			try {
				page = Integer.parseInt(args.get(2));
			}
			catch (NumberFormatException e) {
				// third argument not a number, ignore
			}
		}

		page = Math.max(1, page);

		int itemsPerPage = plugin.getConfig().getInt("list-page-size");

		// get all records from chest manager
		final Collection<DeathChest> chestList = plugin.chestManager.getAllChests();

		// create empty list of records
		List<DeathChest> displayRecords = new ArrayList<>();

		// add chests to displayRecords list
		for (DeathChest deathChest : chestList) {

			// if passed player name is wildcard, add chest to list
			if ((passedPlayerName.equals("*")) && sender.hasPermission("deathchest.list.other")) {
				displayRecords.add(deathChest);
			}

			// if passed player is valid player and matches chest owner, add chest to list
			else if (targetPlayer != null
					&& deathChest.getOwnerUUID().equals(targetPlayer.getUniqueId())
					&& sender.hasPermission("deathchest.list.other")) {
				displayRecords.add(deathChest);
			}
			// if message recipient is valid player and matches chest owner, add chest to list
			else if (player != null && player.getUniqueId().equals(deathChest.getOwnerUUID())) {
				displayRecords.add(deathChest);
			}

		}

		// if display list is empty, output list empty message and return
		if (displayRecords.isEmpty()) {
			Message.create(sender, LIST_EMPTY).send();
			return true;
		}

		// sort displayRecords
		displayRecords.sort(Comparator.comparingLong(DeathChest::getExpirationTime));

		// get page count
		int pageCount = ((displayRecords.size() - 1) / itemsPerPage) + 1;
		if (page > pageCount) {
			page = pageCount;
		}
		int startIndex = ((page - 1) * itemsPerPage);
		int endIndex = Math.min((page * itemsPerPage), displayRecords.size());

		List<DeathChest> displayRange = displayRecords.subList(startIndex, endIndex);


		int listCount = startIndex;

		// display list header
		Message.create(sender, LIST_HEADER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();

		for (DeathChest deathChest : displayRange) {

			// increment list counter
			listCount++;

			String ownerName = "-";
			if (deathChest.getOwnerUUID() != null) {
				ownerName = plugin.getServer().getOfflinePlayer(deathChest.getOwnerUUID()).getName();
			}

			String killerName = "-";
			if (deathChest.getKillerUUID() != null) {
				killerName = plugin.getServer().getOfflinePlayer(deathChest.getKillerUUID()).getName();
			}

			// get remaining time
			Long remainingTime = deathChest.getExpirationTime() - System.currentTimeMillis();

			// if passedPlayerName is wildcard, display LIST_ITEM_ALL
			if (passedPlayerName.equals("*")) {
				Message.create(sender, LIST_ITEM_ALL)
						.setMacro(Macro.ITEM_NUMBER, listCount)
						.setMacro(Macro.LOCATION, deathChest.getLocation())
						.setMacro(Macro.OWNER, ownerName)
						.setMacro(Macro.KILLER, killerName)
						.setMacro(Macro.DURATION, remainingTime)
						.send();
			}
			else {
				Message.create(sender, LIST_ITEM)
						.setMacro(Macro.ITEM_NUMBER, listCount)
						.setMacro(Macro.LOCATION, deathChest.getLocation())
						.setMacro(Macro.OWNER, ownerName)
						.setMacro(Macro.KILLER, killerName)
						.setMacro(Macro.DURATION, remainingTime)
						.send();
			}
		}

		// display list footer
		Message.create(sender, LIST_FOOTER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();

		return true;
	}

}
