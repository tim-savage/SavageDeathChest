package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.chests.DeathChest;
import com.winterhaven_mc.deathchest.messages.Macro;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
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
	public void displayUsage(final CommandSender sender) {

		if (sender.hasPermission("deathchest.list.other")) {
			sender.sendMessage("/deathchest list [username] [page]");
		}
		else {
			sender.sendMessage(getUsage());
		}
	}


	@Override
	public List<String> onTabComplete(final @Nonnull CommandSender sender,
	                                  final @Nonnull Command command,
	                                  final @Nonnull String alias,
	                                  final String[] args) {

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
			plugin.messageBuilder.build(sender, COMMAND_FAIL_LIST_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check if max args exceeded
		if (args.size() > this.getMaxArgs()) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			displayUsage(sender);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// get player name string from args, or null if not found
		String passedPlayerName = getNameFromArgs(args);

		// if sender is player, cast sender to player; else player is null
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		// create empty list of records
		List<DeathChest> displayRecords = new ArrayList<>();

		// should listing include player name
		boolean displayNames = true;

		// if no target player entered
		if (passedPlayerName.isEmpty()) {
			// if sender is a player, add all of player's chests to display list
			if (sender instanceof Player) {
				displayRecords = getChestsForPlayer(player);
				displayNames = false;
			}
			// else add all chests to display list
			else {
				displayRecords = new ArrayList<>(plugin.chestManager.getAllChests());
			}
		}

		else {
			// if player has deathchest.list.other permission...
			if (sender.hasPermission("deathchest.list.other")) {

				// if wildcard character entered, add all chest records to display list
				if (passedPlayerName.equals("*")) {
					displayRecords = new ArrayList<>(plugin.chestManager.getAllChests());
				}

				// else match chest records to entered target player name prefix
				else {
					for (DeathChest deathChest : plugin.chestManager.getAllChests()) {
						if (deathChest.getOwnerName().toLowerCase().startsWith(passedPlayerName.toLowerCase())) {
							displayRecords.add(deathChest);
						}
					}
				}
			}

			// else send permission denied message and return true
			else {
				plugin.messageBuilder.build(sender, COMMAND_FAIL_LIST_OTHER_PERMISSION).send();
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
				return true;
			}
		}

		// if display list is empty, output list empty message and return
		if (displayRecords.isEmpty()) {
			plugin.messageBuilder.build(sender, LIST_EMPTY).send();
			return true;
		}

		// sort displayRecords
		displayRecords.sort(Comparator.comparingLong(DeathChest::getExpirationTime));

		// get list page size from configuration
		int itemsPerPage = plugin.getConfig().getInt("list-page-size-player");
		if (sender instanceof ConsoleCommandSender) {
			itemsPerPage = plugin.getConfig().getInt("list-page-size-console");
		}

		// get page number from args; defaults to 1 if not found
		int page = getPageFromArgs(args);

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
			if (displayNames) {
				plugin.messageBuilder.build(sender, LIST_ITEM_ALL)
						.setMacro(Macro.ITEM_NUMBER, listCount)
						.setMacro(Macro.LOCATION, deathChest.getLocation())
						.setMacro(Macro.OWNER, ownerName)
						.setMacro(Macro.KILLER, killerName)
						.setMacro(Macro.DURATION, remainingTime)
						.send();
			}
			else {
				plugin.messageBuilder.build(sender, LIST_ITEM)
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


	private List<DeathChest> getChestsForPlayer(final Player player) {
		List<DeathChest> returnList = new ArrayList<>();

		for (DeathChest deathChest : plugin.chestManager.getAllChests()) {
			if (deathChest.getOwnerUid().equals(player.getUniqueId())) {
				returnList.add(deathChest);
			}
		}
		return returnList;
	}


	private void displayListHeader(final CommandSender sender, final int page, final int pageCount) {
		// display list header
		plugin.messageBuilder.build(sender, LIST_HEADER)
				.setMacro(Macro.PAGE_NUMBER, page)
				.setMacro(Macro.PAGE_TOTAL, pageCount)
				.send();
	}


	private void displayListFooter(final CommandSender sender, int page, int pageCount) {
		// display list footer
		plugin.messageBuilder.build(sender, LIST_FOOTER)
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
