package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.chests.DeathChest;
import com.winterhaven_mc.deathchest.messages.Macro;
import com.winterhaven_mc.deathchest.messages.Message;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;

import static com.winterhaven_mc.deathchest.messages.MessageId.*;


public class ListCommand extends AbstractSubcommand {

	private final PluginMain plugin;


	ListCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("list");
		this.setUsage("/deathchest list [page]");
		this.setDescription(COMMAND_HELP_LIST);
		this.setMaxArgs(2);
	}


	@Override
	public void displayUsage(CommandSender sender) {

		if (sender.hasPermission("deathchest.list.other")) {
			sender.sendMessage("/deathchest list [username] [page]");
		}
		else {
			sender.sendMessage(getUsage());
		}
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		// initialize return list
		final List<String> returnList = new ArrayList<>();

		if (args.length == 2) {
			if (sender.hasPermission("deathchest.list.other")) {
				// get map of chest ownerUUID,name from all current chests
				Map<UUID, String> chestOwners = new HashMap<>();
				for (DeathChest deathChest : plugin.chestManager.getAllChests()) {
					chestOwners.put(deathChest.getOwnerUid(),
							plugin.getServer().getOfflinePlayer(deathChest.getOwnerUid()).getName());
				}
				returnList.addAll(chestOwners.values());
			}
		}

		return returnList;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		// if command sender does not have permission to list death chests, output error message and return true
		if (!sender.hasPermission("deathchest.list")) {
			Message.create(sender, COMMAND_FAIL_LIST_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check if max args exceeded
		if (args.size() > this.getMaxArgs()) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			displayUsage(sender);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get list page size from configuration
		int itemsPerPage = plugin.getConfig().getInt("list-page-size");

		// get page number from args; defaults to 1 if not found
		int page = getPageFromArgs(args);

		// get player name string from args, or null if not found
		String passedPlayerName = getNameFromArgs(args);

		// match passed player name to offline player
		OfflinePlayer targetPlayer = matchOfflinePlayer(passedPlayerName);

		// check if sender is console
		boolean senderIsConsole = (sender instanceof ConsoleCommandSender);

		Player player = null;

		// if sender is player, cast sender to player
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		// get all records from chest manager
		final Collection<DeathChest> chestList = plugin.chestManager.getAllChests();

		// create empty list of records
		List<DeathChest> displayRecords = new ArrayList<>();

		// iterate over all chest records
		for (DeathChest deathChest : chestList) {

			// if passed player name is wildcard, add chest to list
			if ((passedPlayerName.equals("*"))
					&& (sender.hasPermission("deathchest.list.other") || senderIsConsole)) {
				displayRecords.add(deathChest);
			}

			// if sender has list.other permission, and passed player is valid player and matches chest owner, add chest to list
			else if (targetPlayer != null
					&& deathChest.getOwnerUid().equals(targetPlayer.getUniqueId())
					&& sender.hasPermission("deathchest.list.other") || senderIsConsole) {
				displayRecords.add(deathChest);
			}

			// if message recipient is valid player and matches chest owner, add chest to list
			else if (player != null
					&& player.getUniqueId().equals(deathChest.getOwnerUid())) {
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

		// if sender is console or wildcard used, set displayFull true; else false
		boolean displayFull = senderIsConsole || passedPlayerName.equals("*");

		// display list header
		displayListHeader(sender, page, pageCount);

		for (DeathChest deathChest : displayRange) {

			// increment list counter
			listCount++;

			String ownerName = "-";
			if (deathChest.hasValidOwnerUid()) {
				ownerName = plugin.getServer().getOfflinePlayer(deathChest.getOwnerUid()).getName();
			}

			String killerName = "-";
			if (deathChest.hasValidKillerUid()) {
				killerName = plugin.getServer().getOfflinePlayer(deathChest.getKillerUid()).getName();
			}

			// get remaining time
			Long remainingTime = deathChest.getExpirationTime() - System.currentTimeMillis();

			// if passedPlayerName is wildcard, display LIST_ITEM_ALL
			if (displayFull) {
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
		displayListFooter(sender, page, pageCount);

		return true;
	}


	private OfflinePlayer matchOfflinePlayer(String name) {

		OfflinePlayer matchedPlayer = null;

		for (OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {

			if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(name)) {
				matchedPlayer = offlinePlayer;
				break;
			}
		}
		return matchedPlayer;
	}


	private Collection<DeathChest> getChestsForPlayer(Player player) {
		Set<DeathChest> returnSet = new HashSet<>();

		for (DeathChest deathChest : plugin.chestManager.getAllChests()) {
			if (deathChest.getOwnerUid().equals(player.getUniqueId())) {
				returnSet.add(deathChest);
			}
		}
		return returnSet;
	}


	private void displayListHeader(CommandSender sender, int page, int pageCount) {
		// display list header
		Message.create(sender, LIST_HEADER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}


	private void displayListFooter(CommandSender sender, int page, int pageCount) {
		// display list footer
		Message.create(sender, LIST_FOOTER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}

	private String getNameFromArgs(final List<String> args) {

		if (args.size() > 0) {
			if (!isNumeric(args.get(0))) {
				return args.get(0);
			}
		}
		return "";
	}

	private int getPageFromArgs(final List<String> args) {

		int returnInt = 1;

		if (args.size() == 1) {
			try {
				returnInt = Integer.parseInt(args.get(0));
			} catch (NumberFormatException nfe) {
				// not a number
			}
		}
		else if (args.size() == 2) {
			try {
				returnInt = Integer.parseInt(args.get(1));
			} catch (NumberFormatException nfe) {
				// not a number
			}
		}
		return Math.max(1, returnInt);
	}


	private boolean isNumeric(final String strNum) {

		// if string is null, return false
		if (strNum == null) {
			return false;
		}

		try {
			Integer.parseInt(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
