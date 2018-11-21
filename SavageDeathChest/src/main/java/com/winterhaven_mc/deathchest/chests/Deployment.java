package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;
import com.winterhaven_mc.deathchest.messages.MessageId;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.winterhaven_mc.deathchest.util.LocationUtilities.*;


public class Deployment {

	// reference to main class
	private final PluginMain plugin = PluginMain.instance;

	private DeathChest deathChest;


	/**
	 * Class constructor for DeathChest deployment
	 * @param event player death event that triggers DeathChest deployment
	 */
	public Deployment (final PlayerDeathEvent event) {

		// get player from event
		Player player = event.getEntity();

		// get dropped items
		List<ItemStack> droppedItems = event.getDrops();

		// if player's current world is not enabled in config, do nothing
		// and allow inventory items to drop on ground
		if (!plugin.worldManager.isEnabled(player.getWorld())) {
			return;
		}

		// if player does not have permission for death chest creation,
		// do nothing and allow inventory items to drop on ground
		if (!player.hasPermission("deathchest.chest")) {
			plugin.messageManager.sendMessage(player, MessageId.CHEST_DENIED_PERMISSION);
			return;
		}

		// if player is in creative mode,
		// and creative-deploy is configured false,
		// and player does not have creative-deploy permission override:
		// output message and return
		if (player.getGameMode().equals(GameMode.CREATIVE)
				&& !plugin.getConfig().getBoolean("creative-deploy")
				&& !player.hasPermission("deathchest.creative-deploy")) {
			plugin.messageManager.sendMessage(player, MessageId.CREATIVE_MODE);
			return;
		}

		// if player inventory is empty, output message and return
		if (droppedItems.isEmpty()) {
			plugin.messageManager.sendMessage(player, MessageId.INVENTORY_EMPTY);
			return;
		}

		// create new deathChest object for player
		this.deathChest = new DeathChest(player);

		// deploy chest, putting items that don't fit in chest into droppedItems list of ItemStack
		Result result = deployChest(player, droppedItems);

		// clear dropped items
		event.getDrops().clear();

		// drop any items that couldn't be placed in a death chest
		event.getDrops().addAll(result.getRemainingItems());

		// send message based on result
		switch (result.getResultCode()) {
			case SUCCESS:
				plugin.messageManager.sendMessage(player, MessageId.CHEST_SUCCESS, deathChest);
				break;

			case PARTIAL_SUCCESS:
				plugin.messageManager.sendMessage(player, MessageId.DOUBLECHEST_PARTIAL_SUCCESS, deathChest);
				break;

			case PROTECTION_PLUGIN:
				plugin.messageManager.sendMessage(player, MessageId.CHEST_DENIED_PLUGIN,result.getProtectionPlugin());
				break;

			case ABOVE_GRASS_PATH:
				plugin.messageManager.sendMessage(player, MessageId.CHEST_DENIED_BLOCK);
				break;

			case NON_REPLACEABLE_BLOCK:
				plugin.messageManager.sendMessage(player, MessageId.CHEST_DENIED_BLOCK);
				break;

			case ADJACENT_CHEST:
				plugin.messageManager.sendMessage(player, MessageId.CHEST_DENIED_ADJACENT);
				break;

			case NO_CHEST:
				plugin.messageManager.sendMessage(player, MessageId.NO_CHEST_IN_INVENTORY);
				break;
		}

		// if result is negative, return now
		if (!result.getResultCode().equals(ResultCode.SUCCESS)
				&& !result.getResultCode().equals(ResultCode.PARTIAL_SUCCESS)) {
			return;
		}

		// create expire task for deathChest
		deathChest.createExpireTask();

		// put DeathChest in DeathChest map
		plugin.chestManager.addDeathChest(deathChest);

		// put DeathChest in datastore
		plugin.dataStore.putChestRecord(deathChest);
	}


	/**
	 * Deploy a DeathChest for player and fill with dropped items on player death
	 * @param player the player who died
	 * @param droppedItems the player's items dropped on death
	 * @return Result - the result of the attempted DeathChest deployment
	 */
	private Result deployChest(final Player player, final Collection<ItemStack> droppedItems) {

		// combine stacks of same items where possible
		List<ItemStack> remainingItems = consolidateItemStacks(droppedItems);

		// get required chest size
		ChestSize chestSize = ChestSize.selectFor(remainingItems.size());

		// declare result object
		Result result;

		// deploy appropriately sized chest
		if (chestSize.equals(ChestSize.SINGLE)
				|| !player.hasPermission("deathchest.doublechest")) {
			result = deploySingleChest(player, remainingItems);
		}
		else {
			result = deployDoubleChest(player, remainingItems);
		}

		return result;
	}


	/**
	 * Deploy a single chest for player and fill with dropped items on player death
	 * @param player the player who died
	 * @param droppedItems the player's items dropped on death
	 * @return Result - the result of the attempted DeathChest deployment
	 */
	private Result deploySingleChest(final Player player, final List<ItemStack> droppedItems) {

		List<ItemStack> remainingItems = droppedItems;

		// initialize new result object
		Result result = new Result();

		// if require-chest option is enabled
		// and player does not have a chest in inventory
		// and player does not have permission override
		if (plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest")
				&& !containsChest(remainingItems)) {

			// create result object
			result.setResultCode(ResultCode.NO_CHEST);

			// put remaining items in result
			result.setRemainingItems(remainingItems);

			// return result
			return result;
		}

		// search for valid chest location
		result = findChestLocation(player, ChestSize.SINGLE);

		// if search successful, place chest
		if (result.getResultCode().equals(ResultCode.SUCCESS)) {

			// if require-chest option is enabled
			// and player does not have permission override
			// remove one chest from chest items
			if (plugin.getConfig().getBoolean("require-chest")
					&& !player.hasPermission("deathchest.freechest")) {
				removeOneChest(remainingItems);
			}

			// place chest at result location
			remainingItems = placeChest(result.getLocation(), remainingItems, ChestBlockType.RIGHT_CHEST);

			// place sign on chest
			placeSign(player, result.getLocation().getBlock());
		}

		// put remaining items in result
		result.setRemainingItems(remainingItems);

		return result;
	}


	/**
	 * Deploy a double chest for player and fill with dropped items on player death
	 * @param player the player who died
	 * @param droppedItems the player's items dropped on death
	 * @return Result - the result of the attempted DeathChest deployment
	 */
	private Result deployDoubleChest(final Player player, final List<ItemStack> droppedItems) {

		List<ItemStack> remainingItems = droppedItems;

		// initialize new result object
		Result result = new Result();

		// if require-chest option is enabled
		// and player does not have a chest in inventory
		// and player does not have permission override
		if (plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest")
				&& !containsChest(remainingItems)) {

			// create result object
			result.setResultCode(ResultCode.NO_CHEST);

			// put remaining items in result
			result.setRemainingItems(remainingItems);

			// return result
			return result;
		}

		// search for valid chest location
		result = findChestLocation(player, ChestSize.DOUBLE);

		// if only single chest location found, deploy single chest
		if (result.getResultCode().equals(ResultCode.PARTIAL_SUCCESS)) {
			result = deploySingleChest(player, droppedItems);
			result.setResultCode(ResultCode.PARTIAL_SUCCESS);
			return result;
		}

		// if search failed, return result
		if (!result.getResultCode().equals(ResultCode.SUCCESS)) {

			// put remaining items in result
			result.setRemainingItems(remainingItems);

			// return result
			return result;
		}

		// if require-chest option is enabled
		// and player does not have permission override
		// remove one chest from chest items
		if (plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest")) {
			removeOneChest(remainingItems);
		}

		// place chest at result location
		remainingItems = placeChest(result.getLocation(), remainingItems, ChestBlockType.RIGHT_CHEST);

		// place sign on right chest
		placeSign(player, result.getLocation().getBlock());

		// if second chest still needed
		// and require-chest option is enabled
		// and player does not have permission override
		// and player does not have a chest in inventory
		// set remaining items in result
		if (remainingItems.size() > 0
				&& plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest")
				&& !containsChest(remainingItems)) {

			result.setRemainingItems(remainingItems);
		}
		else {
			// if second chest still needed
			// and require-chest option is enabled
			// and player does not have permission override
			// and player does have chest in inventory
			// remove one chest from remaining items
			if (remainingItems.size() > 0
					&& plugin.getConfig().getBoolean("require-chest")
					&& !player.hasPermission("deathchest.freechest")
					&& containsChest(remainingItems)) {
				removeOneChest(remainingItems);
			}

			// if second chest still needed
			// and valid double chest location was found,
			// place second chest at location to right of result location
			if (remainingItems.size() > 0
					&& result.getResultCode().equals(ResultCode.SUCCESS)) {

				remainingItems = placeChest(getLocationToRight(result.getLocation()), remainingItems, ChestBlockType.LEFT_CHEST);
			}
		}

		// set chest type to left/right for double chest

		// get right chest block
		Block rightBlock = result.getLocation().getBlock();

		// left block is to player's right
		Block leftBlock = getLocationToRight(result.getLocation()).getBlock();

		// get left and right chest block state
		BlockState rightChestState = rightBlock.getState();
		BlockState leftChestState = leftBlock.getState();

		// cast to (Block) chest
		Chest rightChest = (Chest) rightChestState;
		Chest leftChest = (Chest) leftChestState;

		// get block data
		BlockData rightBlockData = rightChest.getBlockData();
		BlockData leftBlockData = leftChest.getBlockData();

		((org.bukkit.block.data.type.Chest)rightBlockData).setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
		rightChest.setBlockData(rightBlockData);
		rightChest.update();

		((org.bukkit.block.data.type.Chest)leftBlockData).setType(org.bukkit.block.data.type.Chest.Type.LEFT);
		leftChest.setBlockData(leftBlockData);
		leftChest.update();

		// put remaining items in result
		result.setRemainingItems(remainingItems);

		return result;
	}


	/**
	 * Combine ItemStacks of same material up to max stack size
	 * @param itemStacks Collection of ItemStacks to combine
	 * @return List of ItemStack with same materials combined
	 */
	private List<ItemStack> consolidateItemStacks(final Collection<ItemStack> itemStacks) {

		final List<ItemStack> returnList = new ArrayList<>();

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
							Math.min(itemStack.getAmount(),checkStack.getMaxStackSize() - checkStack.getAmount());
					itemStack.setAmount(itemStack.getAmount() - transferAmount);
					checkStack.setAmount(checkStack.getAmount()	+ transferAmount);
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
	 * @param itemStacks Collection of ItemStack to check for chest
	 * @return boolean
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean containsChest(final Collection<ItemStack> itemStacks) {
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
	 * @param itemStacks List of ItemStack to remove chest
	 */
	private void removeOneChest(final Collection<ItemStack> itemStacks) {

		Iterator<ItemStack> iterator = itemStacks.iterator();

		while (iterator.hasNext()) {

			ItemStack itemStack = iterator.next();

			if (itemStack.getType().equals(Material.CHEST)) {
				if (itemStack.getAmount() == 1) {
					iterator.remove();
				} else {
					itemStack.setAmount(itemStack.getAmount() - 1);
				}
				break;
			}
		}
	}


	/**
	 * Search for a valid location to place a single chest,
	 * taking into account replaceable blocks, grass path blocks and
	 * restrictions from other block protection plugins if configured
	 * @param player Player that deathchest is being deployed for
	 * @return SearchResult
	 */
	private Result findChestLocation(final Player player, final ChestSize chestSize) {

		// count number of tests performed, for debugging purposes
		int testCount = 0;

		// get distance to search from config
		int radius = plugin.getConfig().getInt("search-distance");

		// get clone of player death location
		Location testLocation = player.getLocation().clone();

		// if player died in the void, start search at y=1 if place-above-void configured true
		if (plugin.getConfig().getBoolean("place-above-void")
				&& testLocation.getY() < 1) {
			testLocation.setY(1);
		}

		// if player died above world build height, start search at build height minus search distance
		else if (plugin.getConfig().getBoolean("place-below-max")
				&& testLocation.getY() >= player.getWorld().getMaxHeight()) {
			testLocation.setY(player.getWorld().getMaxHeight() - plugin.getConfig().getInt("search-distance"));
		}

		// declare search result object
		Result result;

		// iterate over all locations with search distance until a valid location is found
		for (int y = 0; y < radius; y = y + 1) {
			for (int x = 0; x < radius; x = x + 1) {
				for (int z = 0; z < radius; z = z + 1) {

					// set new test location
					testLocation.add(x,y,z);

					// get result for test location
					result = validateChestLocation(player, testLocation, chestSize);
					testCount = testCount + 1;

					// if test location is valid, return search result object
					if (result.getResultCode().equals(ResultCode.SUCCESS)) {
						if (plugin.debug) {
							plugin.getLogger().info("Locations tested: " + testCount);
						}
						return result;
					}
					else {
						// reset test location
						testLocation.add(-x,-y,-z);
					}

					// location 0,y,0 has already been checked, so skip ahead
					if (x == 0 && z == 0) {
						continue;
					}

					// set new test location
					testLocation.add(-x,y,z);

					// get result for test location
					result = validateChestLocation(player, testLocation, chestSize);
					testCount = testCount + 1;

					// if location is valid, return search result object
					if (result.getResultCode().equals(ResultCode.SUCCESS)) {
						if (plugin.debug) {
							plugin.getLogger().info("Locations tested: " + testCount);
						}
						return result;
					}
					else {
						// reset test location
						testLocation.add(x,-y,-z);
					}

					// locations 0,y,z and x,y,0 had already been checked, so skip ahead
					if (x == 0 || z == 0) {
						continue;
					}

					// set new test location
					testLocation.add(-x,y,-z);

					// get result for test location
					result = validateChestLocation(player, testLocation, chestSize);
					testCount = testCount + 1;

					// if location is valid, return search result object
					if (result.getResultCode().equals(ResultCode.SUCCESS)) {
						if (plugin.debug) {
							plugin.getLogger().info("Locations tested: " + testCount);
						}
						return result;
					}
					else {
						// reset test location
						testLocation.add(x,-y,z);
					}

					// set new test location
					testLocation.add(x,y,-z);

					// get result for test location
					result = validateChestLocation(player, testLocation, chestSize);
					testCount = testCount + 1;

					// if location is valid, return search result object
					if (result.getResultCode().equals(ResultCode.SUCCESS)) {
						if (plugin.debug) {
							plugin.getLogger().info("Locations tested: " + testCount);
						}
						return result;
					}
					else {
						// reset test location
						testLocation.add(-x,-y,z);
					}
				}
			}
		}

		// no valid location could be found, so return result
		if (plugin.debug) {
			plugin.getLogger().info("Locations tested: " + testCount);
		}

		return new Result(ResultCode.NON_REPLACEABLE_BLOCK);
	}


	private List<ItemStack> placeChest(final Location location,
									   final List<ItemStack> chestItems,
									   final ChestBlockType chestBlockType) {

		// get current block at location
		Block block = location.getBlock();

		// get block state
		BlockState blockState = block.getState();

		// set material to chest
		blockState.setType(Material.CHEST);

		// set direction
		blockState.setData(new org.bukkit.material.Chest(getCardinalDirection(location)));

		// update chest BlockState
		blockState.update(true, false);

		// get updated BlockState
		blockState = block.getState();

		// cast to Chest
		org.bukkit.block.Chest chest = (org.bukkit.block.Chest) blockState;

		// create new ChestBlock object
		new ChestBlock(deathChest, block, chestBlockType);

		// put items into chest inventory, items that don't fit are returned as List of ItemStack
		return fillChest(chest, chestItems);
	}


	private Result validateChestLocation(final Player player,
										 final Location location,
										 final ChestSize chestSize) {

		// test left chest location
		Result result = validateChestLocation(player, location, ChestBlockType.LEFT_CHEST);

		// if left chest is not successful, return result
		if (!result.getResultCode().equals(ResultCode.SUCCESS)) {
			return result;
		}

		// if chest is to be a double chest, test right chest location
		if (chestSize.equals(ChestSize.DOUBLE)) {

			// test right chest block location
			result = validateChestLocation(player, location, ChestBlockType.RIGHT_CHEST);
		}

		return result;
	}


	private Result validateChestLocation(final Player player,
										 final Location location,
										 final ChestBlockType chestBlockType) {

		Location testLocation = location;

		if (chestBlockType.equals(ChestBlockType.RIGHT_CHEST)) {
			testLocation = getLocationToRight(location);
		}

		Block block = testLocation.getBlock();

		// if block at location is not replaceable block, return negative result
		if (!plugin.chestManager.replaceableBlocks.contains(block.getType())) {
			return new Result(ResultCode.NON_REPLACEABLE_BLOCK);
		}

		// if block at location is above grass path, return negative result
		if (isAboveGrassPath(block)) {
			return new Result(ResultCode.ABOVE_GRASS_PATH);
		}

		// if block at location is protected by plugin, return negative result
		ProtectionPlugin protectionPlugin = ProtectionPlugin.allowChestPlacement(player, block);
		if (protectionPlugin != null) {
			Result result = new Result(ResultCode.PROTECTION_PLUGIN);
			result.setProtectionPlugin(protectionPlugin);
			return result;
		}

		Result result = new Result(ResultCode.SUCCESS);
		result.setLocation(location);
		return result;
	}


	/**
	 * Place sign on chest
	 * @param player		Chest owner
	 * @param chestBlock	Chest block
	 * @return boolean		Success or failure to place sign
	 */
	@SuppressWarnings("UnusedReturnValue")
	private boolean placeSign(final Player player, final Block chestBlock) {

		// if chest-signs are not enabled in configuration, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-signs")) {
			return false;
		}

		// get block adjacent to chest facing player direction
		Block signBlock = chestBlock.getRelative(getCardinalDirection(player));

		// if chest face is valid location, create wall sign
		if (isValidSignLocation(signBlock.getLocation())) {
			signBlock.setType(Material.WALL_SIGN);
		}
		else {
			// create sign post on top of chest if chest face was invalid location
			signBlock = chestBlock.getRelative(BlockFace.UP);
			if (isValidSignLocation(signBlock.getLocation())) {
				signBlock.setType(Material.SIGN);
			}
			else {
				// if top of chest is also an invalid location, do nothing and return
				return false;
			}
		}

		// get block state of sign block
		BlockState signblockState = signBlock.getState();

		// if block has not been successfully transformed into a sign, return false
		if (!(signblockState instanceof org.bukkit.block.Sign)) {
			return false;
		}

		// Place text on sign with player name and death date
		org.bukkit.block.Sign sign = (org.bukkit.block.Sign)signblockState;
		String dateFormat = plugin.messageManager.getDateFormat();
		String dateString = new SimpleDateFormat(dateFormat).format(System.currentTimeMillis());

		// get sign text from language file
		List<String> lines = plugin.messageManager.getSignText();

		if (lines.isEmpty()) {
			sign.setLine(0, ChatColor.BOLD + "R.I.P.");
			sign.setLine(1, ChatColor.RESET + player.getName());
			sign.setLine(3, "D: " + dateString);
		}
		else {
			// use try..catch block so chest will still deploy even if error exists in yaml
			try {
				int lineCount = 0;
				for (String line : lines) {
					line = line.replace("%PLAYER_NAME%", player.getName());
					line = line.replace("%DATE%", dateString);
					line = line.replace("%WORLD_NAME%", plugin.worldManager.getWorldName(player.getWorld()));
					line = ChatColor.translateAlternateColorCodes('&', line);
					sign.setLine(lineCount, line);
					lineCount++;
				}
			}
			catch (Exception e) {
				sign.setLine(0, ChatColor.BOLD + "R.I.P.");
				sign.setLine(1, ChatColor.RESET + player.getName());
				sign.setLine(3, "D: " + dateString);
			}
		}

		// set sign facing direction
		org.bukkit.material.Sign signData = (org.bukkit.material.Sign) signblockState.getData();
		signData.setFacingDirection(getCardinalDirection(player));
		sign.setData(signData);

		// update sign block with text and direction
		sign.update();

		// create ChestBlock for this sign block
		ChestBlock signChestBlock = new ChestBlock(this.deathChest, signBlock, ChestBlockType.SIGN);

		// add sign to chestBlocks
		this.deathChest.addChestBlock(ChestBlockType.SIGN, signChestBlock);

		// return success
		return true;
	}


	/** Check if sign can be placed at location
	 *
	 * @param location	Location to check
	 * @return boolean
	 */
	private boolean isValidSignLocation(final Location location) {

		Block block = location.getBlock();

		// if block at location is above grass path, return negative result
		if (isAboveGrassPath(block)) {
			return false;
		}

		// check if block at location is a ReplaceableBlock
		return plugin.chestManager.replaceableBlocks.contains(block.getType());
	}


	/**
	 * Place dropped items in chest
	 * @param chest the chest to place items
	 * @param itemStacks Collection of ItemStacks to place in chest
	 * @return List of ItemStacks that did not fit in chest
	 */
	private List<ItemStack> fillChest(final Chest chest, final Collection<ItemStack> itemStacks) {

		// convert itemStacks list to array
		ItemStack[] stackArray = new ItemStack[itemStacks.size()];
		stackArray = itemStacks.toArray(stackArray);

		// return list of items that did not fit in chest
		return new ArrayList<>(chest.getInventory().addItem(stackArray).values());
	}


	/**
	 * Check if block is above a grass path block
	 * @param block the block to check underneath
	 * @return true if passed block is above a grass path block, false if not
	 */
	private boolean isAboveGrassPath(final Block block) {
		return block.getRelative(0, -1, 0).getType().equals(Material.GRASS_PATH);
	}

}
