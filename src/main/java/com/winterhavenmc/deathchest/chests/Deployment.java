package com.winterhavenmc.deathchest.chests;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.chests.search.QuadrantSearch;
import com.winterhavenmc.deathchest.chests.search.ResultCode;
import com.winterhavenmc.deathchest.chests.search.Search;
import com.winterhavenmc.deathchest.chests.search.SearchResult;
import com.winterhavenmc.deathchest.messages.Macro;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.winterhavenmc.deathchest.messages.MessageId.*;
import static com.winterhavenmc.deathchest.chests.LocationUtilities.*;


/**
 * A class that effectuates the deployment of a death chest in response to a player death event.
 * Tasks include determining the appropriate chest size needed, searching for a suitable location
 * for chest placement, placing the death chest in game, putting player's dropped items into the chest
 * and attaching a chest sign with a configured message.
 */
public final class Deployment {

	// reference to main class
	private final PluginMain plugin;

	// death chest object
	private final DeathChest deathChest;

	// set of path block type names as strings
	private static final Collection<String> pathBlockTypeNames = Set.of(
			"GRASS_PATH",
			"LEGACY_GRASS_PATH",
			"DIRT_PATH"	);


	/**
	 * Class constructor for DeathChest deployment
	 *
	 * @param plugin reference to plugin main class instance
	 * @param player the player for whom to deploy a death chest
	 * @param droppedItems list of items dropped by player on death
	 */
	public Deployment(final PluginMain plugin, final Player player, final Collection<ItemStack> droppedItems) {

		// set reference to main class
		this.plugin = plugin;

		// create new deathChest object for player
		this.deathChest = new DeathChest(player);

		// deploy chest
		SearchResult result = deployChest(player, droppedItems);

		// if debugging, log result
		if (plugin.getConfig().getBoolean("debug")) {
			logResult(result);
		}

		// get configured expire-time
		long expireTime = plugin.getConfig().getLong("expire-time");

		// if configured expire-time is zero, set to negative to display infinite time in messages
		if (expireTime == 0) {
			expireTime = -1;
		}

		// send message based on result
		sendResultMessage(player, result, expireTime);

		// drop any remaining items that were not placed in a chest
		for (ItemStack item : result.getRemainingItems()) {
			player.getWorld().dropItemNaturally(player.getLocation(), item);
		}

		// if result is negative cancel expire task and return
		if (!result.getResultCode().equals(ResultCode.SUCCESS)
				&& !result.getResultCode().equals(ResultCode.PARTIAL_SUCCESS)) {

			// cancel DeathChest expire task
			deathChest.cancelExpireTask();
			return;
		}

		// get configured chest protection time
		long chestProtectionTime = plugin.getConfig().getLong("chest-protection-time");

		// protection time is zero, set to negative to display infinite time in message
		if (chestProtectionTime == 0) {
			chestProtectionTime = -1;
		}

		// if chest protection is enabled and chest-protection-time is set (non-zero), send message
		if (plugin.getConfig().getBoolean("chest-protection") && chestProtectionTime > 0) {
			plugin.messageBuilder.build(player, CHEST_DEPLOYED_PROTECTION_TIME)
					.setMacro(Macro.OWNER, player.getName())
					.setMacro(Macro.LOCATION, deathChest.getLocation())
					.setMacro(Macro.PROTECTION_DURATION, TimeUnit.MINUTES.toMillis(chestProtectionTime))
					.setMacro(Macro.PROTECTION_DURATION_MINUTES, TimeUnit.MINUTES.toMillis(chestProtectionTime))
					.send();
		}

		// put DeathChest in DeathChest map
		plugin.chestManager.putChest(deathChest);

		// put DeathChest in datastore
		Set<DeathChest> deathChests = new HashSet<>();
		deathChests.add(deathChest);
		plugin.chestManager.insertChestRecords(deathChests);
	}


	/**
	 * Deploy a DeathChest for player and fill with dropped items on player death
	 *
	 * @param player       the player who died
	 * @param droppedItems the player's items dropped on death
	 * @return SearchResult - the result of the attempted DeathChest deployment
	 */
	private SearchResult deployChest(final Player player, final Collection<ItemStack> droppedItems) {

		// combine stacks of same items where possible
		Collection<ItemStack> remainingItems = consolidateItemStacks(droppedItems);

		// get required chest size
		ChestSize chestSize = ChestSize.selectFor(remainingItems.size());

		// deploy appropriately sized chest
		if (chestSize.equals(ChestSize.SINGLE)
				|| !player.hasPermission("deathchest.doublechest")) {
			return deploySingleChest(player, remainingItems);
		}
		else {
			return deployDoubleChest(player, remainingItems);
		}
	}


	/**
	 * Deploy a single chest for player and fill with dropped items on player death
	 *
	 * @param player       the player who died
	 * @param droppedItems Collection of the player's items dropped on death
	 * @return SearchResult - the result of the attempted DeathChest deployment
	 */
	private SearchResult deploySingleChest(final Player player, final Collection<ItemStack> droppedItems) {

		// make copy of dropped items
		Collection<ItemStack> remainingItems = new LinkedList<>(droppedItems);

		// if require-chest option is enabled
		// and player does not have permission override
		if (plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest")) {

			// check that player has chest in inventory
			if (containsChest(remainingItems)) {

				// if consume-required-chest configured true: remove one chest from remaining items
				if (plugin.getConfig().getBoolean("consume-required-chest")) {
					remainingItems = removeOneChest(remainingItems);
				}
			}
			// else return NO_CHEST result
			else {
				return new SearchResult(ResultCode.NO_CHEST, remainingItems);
			}
		}

		// search for valid chest location
		Search search = new QuadrantSearch(plugin, player, ChestSize.SINGLE);
		search.execute();
		SearchResult result = search.getResult();

		// if search successful, place chest
		if (result.getResultCode().equals(ResultCode.SUCCESS)) {

			// place chest at result location
			placeChest(result.getLocation(), ChestBlockType.RIGHT_CHEST);

			// get chest block state
			BlockState chestBlockState = result.getLocation().getBlock().getState();

			// get chest block data
			Chest chestBlockData = (Chest) chestBlockState.getBlockData();

			// set chest block data type to single chest
			chestBlockData.setType(Chest.Type.SINGLE);

			// set chest block data
			chestBlockState.setBlockData(chestBlockData);

			// update chest block state
			chestBlockState.update();

			// fill chest
			remainingItems = deathChest.fill(remainingItems);

			// place sign on chest
			placeSign(player, result.getLocation().getBlock());
		}

		// set remaining items in result
		result.setRemainingItems(remainingItems);

		// return result
		return result;
	}


	/**
	 * Deploy a double chest for player and fill with dropped items on player death
	 *
	 * @param player       the player who died
	 * @param droppedItems the player's items dropped on death
	 * @return SearchResult - the result of the attempted DeathChest deployment
	 */
	private SearchResult deployDoubleChest(final Player player, final Collection<ItemStack> droppedItems) {

		// make copy of dropped items
		Collection<ItemStack> remainingItems = new LinkedList<>(droppedItems);

		// search for valid chest location
		Search search = new QuadrantSearch(plugin, player, ChestSize.DOUBLE);
		search.execute();
		SearchResult result = search.getResult();

		// if only single chest location found, deploy single chest
		if (result.getResultCode().equals(ResultCode.PARTIAL_SUCCESS)) {
			result = deploySingleChest(player, remainingItems);

			// if single chest deployment was successful, set PARTIAL_SUCCESS result
			if (result.getResultCode().equals(ResultCode.SUCCESS)) {
				result.setResultCode(ResultCode.PARTIAL_SUCCESS);
			}

			// return result
			return result;
		}

		// if search failed, return result with remaining items
		if (!result.getResultCode().equals(ResultCode.SUCCESS)) {
			result.setRemainingItems(remainingItems);
			return result;
		}

		// if require-chest option is enabled
		// and player does not have permission override
		if (plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest")) {

			// check that player has chest in inventory
			if (containsChest(remainingItems)) {

				// if consume-required-chest configured true: remove one chest from remaining items
				if (plugin.getConfig().getBoolean("consume-required-chest")) {
					remainingItems = removeOneChest(remainingItems);
				}
			}
			// else return NO_CHEST result
			else {
				result.setResultCode(ResultCode.NO_CHEST);
				result.setRemainingItems(remainingItems);
				return result;
			}
		}

		// place chest at result location
		placeChest(result.getLocation(), ChestBlockType.RIGHT_CHEST);

		// place sign on chest
		placeSign(player, result.getLocation().getBlock());

		// attempt to place second chest

		// if require-chest option is enabled
		// and player does not have permission override
		if (plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest")) {

			// check that player has chest in inventory
			if (containsChest(remainingItems)) {
				// if consume-required-chest configured true: remove one chest from remaining items
				if (plugin.getConfig().getBoolean("consume-required-chest")) {
					remainingItems = removeOneChest(remainingItems);
				}
			}
			// else return new PARTIAL_SUCCESS result with location and remaining items after filling chest
			else {
				result.setResultCode(ResultCode.PARTIAL_SUCCESS);
				result.setRemainingItems(deathChest.fill(remainingItems));
				return result;
			}
		}

		// place chest at result location
		placeChest(getLocationToRight(result.getLocation()), ChestBlockType.LEFT_CHEST);

		// set chest type to left/right for double chest

		// get left and right chest block state
		BlockState rightChestState = result.getLocation().getBlock().getState();
		BlockState leftChestState = getLocationToRight(result.getLocation()).getBlock().getState();

		// get left and right chest block data
		Chest rightChestBlockData = (Chest) rightChestState.getBlockData();
		Chest leftChestBlockData = (Chest) leftChestState.getBlockData();

		// set left and right chest types in block data
		rightChestBlockData.setType(Chest.Type.RIGHT);
		leftChestBlockData.setType(Chest.Type.LEFT);

		// set left and right block data
		rightChestState.setBlockData(rightChestBlockData);
		leftChestState.setBlockData(leftChestBlockData);

		// update left and right chest block data
		rightChestState.update();
		leftChestState.update();

		// put remaining items after filling chest in result
		result.setRemainingItems(deathChest.fill(remainingItems));

		// return result
		return result;
	}


	/**
	 * Combine ItemStacks of same material up to max stack size
	 *
	 * @param itemStacks Collection of ItemStacks to combine
	 * @return Collection of ItemStack with same materials combined
	 */
	private Collection<ItemStack> consolidateItemStacks(final Collection<ItemStack> itemStacks) {

		final Collection<ItemStack> returnList = new LinkedList<>();

		for (ItemStack itemStack : itemStacks) {
			if (itemStack == null) {
				continue;
			}

			for (ItemStack checkStack : returnList) {
				if (checkStack == null) {
					continue;
				}
				if (checkStack.isSimilar(itemStack)) {
					int transferAmount =
							Math.min(itemStack.getAmount(), checkStack.getMaxStackSize() - checkStack.getAmount());
					itemStack.setAmount(itemStack.getAmount() - transferAmount);
					checkStack.setAmount(checkStack.getAmount() + transferAmount);
				}
			}
			if (itemStack.getAmount() > 0) {
				returnList.add(itemStack);
			}
		}
		return returnList;
	}


	/**
	 * Check if Collection of ItemStack contains at least one chest
	 *
	 * @param itemStacks Collection of ItemStack to check for chest
	 * @return boolean - {@code true} if collection contains at least one chest, {@code false} if not
	 */
	private boolean containsChest(final Collection<ItemStack> itemStacks) {

		// check for null parameter
		if (itemStacks == null) {
			return false;
		}

		boolean result = false;
		for (ItemStack itemStack : itemStacks) {
			if (itemStack.getType().equals(Material.CHEST)) {
				result = true;
				break;
			}
		}
		return result;
	}


	/**
	 * Remove one chest from list of item stacks. If a stack contains only one chest, remove the stack from
	 * the list and return. If a stack contains more than one chest, decrease the stack amount by one and return.
	 *
	 * @param itemStacks List of ItemStack to remove chest
	 * @return Collection of ItemStacks with one chest item removed. If passed collection contained no chest items,
	 * the returned collection will be a copy of the passed collection.
	 */
	private Collection<ItemStack> removeOneChest(final Collection<ItemStack> itemStacks) {

		Collection<ItemStack> remainingItems = new LinkedList<>(itemStacks);

		Iterator<ItemStack> iterator = remainingItems.iterator();

		while (iterator.hasNext()) {

			ItemStack itemStack = iterator.next();

			if (itemStack.getType().equals(Material.CHEST) && !itemStack.hasItemMeta()) {
				if (itemStack.getAmount() == 1) {
					iterator.remove();
				}
				else {
					itemStack.setAmount(itemStack.getAmount() - 1);
				}
				break;
			}
		}
		return remainingItems;
	}


	/**
	 * Place a chest block and fill with items
	 *
	 * @param location       the location to place the chest block
	 * @param chestBlockType the type of chest block (left or right)
	 */
	private void placeChest(final Location location,
							final ChestBlockType chestBlockType) {

		// get current block at location
		Block block = location.getBlock();

		// set block material to chest
		block.setType(Material.CHEST);

		// get block direction
		Directional blockData = (Directional) block.getBlockData();

		// set new direction
		blockData.setFacing(getCardinalDirection(location));

		// set block data
		block.setBlockData(blockData);

		// create new ChestBlock object
		ChestBlock chestBlock = new ChestBlock(deathChest.getChestUid(), block.getLocation());

		// add this ChestBlock to block map
		plugin.chestManager.putBlock(chestBlockType, chestBlock);

		// set block metadata
		chestBlock.setMetadata(deathChest);
	}


	/**
	 * Place sign on chest
	 *
	 * @param player     Chest owner
	 * @param chestBlock Chest block
	 * @return boolean - Success or failure to place sign
	 */
	@SuppressWarnings("UnusedReturnValue")
	private boolean placeSign(final Player player, final Block chestBlock) {

		// if chest-signs are not enabled in configuration, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-signs")) {
			return false;
		}

		// try placing sign on chest, catching any exception thrown
		try {
			// get block adjacent to chest facing player direction
			Block signBlock = chestBlock.getRelative(getCardinalDirection(player));

			// if chest face is valid location, create wall sign
			if (isValidSignLocation(signBlock.getLocation())) {

				signBlock.setType(Material.OAK_WALL_SIGN);

				BlockState bs = signBlock.getState();
				org.bukkit.block.data.type.WallSign signBlockDataType = (org.bukkit.block.data.type.WallSign) bs.getBlockData();
				signBlockDataType.setFacing(getCardinalDirection(player));
				bs.setBlockData(signBlockDataType);
				bs.update();
			}
			else {
				// create sign post on top of chest if chest face was invalid location
				signBlock = chestBlock.getRelative(BlockFace.UP);
				if (isValidSignLocation(signBlock.getLocation())) {

					signBlock.setType(Material.OAK_SIGN);

					BlockState bs = signBlock.getState();
					org.bukkit.block.data.type.Sign signBlockDataType = (org.bukkit.block.data.type.Sign) bs.getBlockData();
					signBlockDataType.setRotation(getCardinalDirection(player));
					bs.setBlockData(signBlockDataType);
					bs.update();
				}
				else {
					// if top of chest is also an invalid location, do nothing and return
					return false;
				}
			}

			// get block state of sign block
			BlockState signBlockState = signBlock.getState();

			// if block has not been successfully transformed into a sign, return false
			if (!(signBlockState instanceof org.bukkit.block.Sign)) {
				return false;
			}

			// Place text on sign with player name and death date
			// cast signBlockState to org.bukkit.block.Sign type object
			org.bukkit.block.Sign sign = (org.bukkit.block.Sign) signBlockState;

			// get configured date format from config file
			String dateFormat = plugin.getConfig().getString("DATE_FORMAT");

			// if configured date format is null or empty, use default format
			if (dateFormat == null || dateFormat.isEmpty()) {
				dateFormat = "MMM d, yyyy";
			}

			// create formatted date string from current time
			String dateString = new SimpleDateFormat(dateFormat).format(System.currentTimeMillis());

			// get sign text from config file
			List<String> lines = plugin.getConfig().getStringList("SIGN_TEXT");

			int lineCount = 0;
			for (String line : lines) {

				// stop after four lines (zero indexed)
				if (lineCount > 3) {
					break;
				}

				// do string replacements
				line = line.replace("%PLAYER%", player.getName());
				line = line.replace("%DATE%", dateString);
				line = line.replace("%WORLD%", plugin.worldManager.getWorldName(player.getWorld()));
				line = ChatColor.translateAlternateColorCodes('&', line);

				// set sign text
				sign.setLine(lineCount, line);
				lineCount++;
			}

			// update sign block with text and direction
			sign.update();

			// create ChestBlock for this sign block
			ChestBlock signChestBlock = new ChestBlock(deathChest.getChestUid(), signBlock.getLocation());

			// add this ChestBlock to block map
			plugin.chestManager.putBlock(ChestBlockType.SIGN, signChestBlock);

			// set sign block metadata
			signChestBlock.setMetadata(deathChest);

			// return success
			return true;
		}
		catch (Exception e) {
			plugin.getLogger().severe("An error occurred while trying to place the death chest sign.");
			plugin.getLogger().severe(e.getLocalizedMessage());
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
			return false;
		}
	}


	/**
	 * Check if sign can be placed at location
	 *
	 * @param location Location to check
	 * @return boolean {@code true} if location is valid for sign placement, {@code false} if not
	 */
	private boolean isValidSignLocation(final Location location) {

		// check for null parameter
		if (location == null) {
			return false;
		}

		// get block at location
		Block block = location.getBlock();

		if (isAbovePath(block)) {
			return false;
		}

		// check if block at location is a ReplaceableBlock
		return plugin.chestManager.isReplaceableBlock(block);
	}


	public static boolean isAbovePath(final Block block) {

		// get string for block material type at location below block
		String materialType = block.getRelative(0, -1, 0).getType().toString();

		// if block at location is above grass path, return negative result
		return pathBlockTypeNames.contains(materialType);
	}


	private void sendResultMessage(final Player player, final SearchResult result, final long expireTime) {

		// send message based on result
		switch (result.getResultCode()) {
			case SUCCESS:
				plugin.messageBuilder.build(player, CHEST_SUCCESS)
						.setMacro(Macro.LOCATION, result.getLocation())
						.setMacro(Macro.EXPIRATION_DURATION, TimeUnit.MINUTES.toMillis(expireTime))
						.setMacro(Macro.EXPIRATION_DURATION_MINUTES, TimeUnit.MINUTES.toMillis(expireTime))
						.setMacro(Macro.PROTECTION_DURATION, TimeUnit.MINUTES.toMillis(deathChest.getProtectionTime()))
						.setMacro(Macro.PROTECTION_DURATION_MINUTES, TimeUnit.MINUTES.toMillis(deathChest.getProtectionTime()))
						.send();
				break;

			case PARTIAL_SUCCESS:
				plugin.messageBuilder.build(player, DOUBLECHEST_PARTIAL_SUCCESS)
						.setMacro(Macro.LOCATION, result.getLocation())
						.setMacro(Macro.EXPIRATION_DURATION, TimeUnit.MINUTES.toMillis(expireTime))
						.setMacro(Macro.EXPIRATION_DURATION_MINUTES, TimeUnit.MINUTES.toMillis(expireTime))
						.send();
				break;

			case PROTECTION_PLUGIN:
				plugin.messageBuilder.build(player, CHEST_DENIED_PLUGIN)
						.setMacro(Macro.LOCATION, result.getLocation())
						.setMacro(Macro.PLUGIN, result.getProtectionPlugin())
						.send();
				break;

			case ABOVE_GRASS_PATH:
			case NON_REPLACEABLE_BLOCK:
				plugin.messageBuilder.build(player, CHEST_DENIED_BLOCK)
						.setMacro(Macro.LOCATION, result.getLocation())
						.send();
				break;

			case ADJACENT_CHEST:
				plugin.messageBuilder.build(player, CHEST_DENIED_ADJACENT)
						.setMacro(Macro.LOCATION, result.getLocation())
						.send();
				break;

			case NO_CHEST:
				plugin.messageBuilder.build(player, NO_CHEST_IN_INVENTORY)
						.setMacro(Macro.LOCATION, result.getLocation())
						.send();
				break;

			case SPAWN_RADIUS:
				plugin.messageBuilder.build(player, CHEST_DENIED_SPAWN_RADIUS)
						.setMacro(Macro.LOCATION, result.getLocation())
						.send();
				break;

			case VOID:
				plugin.messageBuilder.build(player, CHEST_DENIED_VOID)
						.setMacro(Macro.LOCATION, result.getLocation())
						.send();
				break;
		}
	}


	private void logResult(final SearchResult result) {

		if (result == null) {
			plugin.getLogger().info("SearchResult is null!");
			return;
		}

		if (result.getResultCode() != null) {
			plugin.getLogger().info("SearchResult Code: " + result.getResultCode().toString());
		}
		if (result.getLocation() != null) {
			plugin.getLogger().info("Location: " + result.getLocation().toString());
		}
		if (result.getProtectionPlugin() != null) {
			plugin.getLogger().info("Protection Plugin: " + result.getProtectionPlugin().getPluginName());
		}
		if (result.getRemainingItems() != null) {
			plugin.getLogger().info("Remaining Items: " + result.getRemainingItems().toString());
		}
	}

}