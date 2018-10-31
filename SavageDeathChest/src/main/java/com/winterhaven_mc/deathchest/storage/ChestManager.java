package com.winterhaven_mc.deathchest.storage;

import com.winterhaven_mc.deathchest.*;
import com.winterhaven_mc.deathchest.tasks.TaskManager;
import com.winterhaven_mc.deathchest.util.ChestUtilities;
import com.winterhaven_mc.deathchest.util.LocationUtilities;

import static com.winterhaven_mc.deathchest.util.ChestUtilities.*;
import static com.winterhaven_mc.deathchest.util.LocationUtilities.*;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;


public final class ChestManager {

	// reference to main class
	private final PluginMain plugin;

	private final TaskManager taskManager;

	// material types that can be replaced by death chests
	private Set<Material> replaceableBlocks;

	// DeathChestBlock material types
	private static final Set<Material> deathChestMaterials = 
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
					Material.CHEST,
					Material.WALL_SIGN,
					Material.SIGN )));
	

	/**
	 * constructor method for {@code ChestManager}
	 * 
	 * @param plugin reference to plugin main class
	 */
	public ChestManager(final PluginMain plugin) {
		
		// set reference to main class
		this.plugin = plugin;

		// instantiate task manager
		taskManager = new TaskManager(plugin);

		// load replaceable blocks
		replaceableBlocks = new HashSet<>(loadReplaceableBlocks());

		// load death chest blocks from datastore
		loadDeathChestBlocks();
	}


	/**
	 * Get Set of replaceable blocks
	 * @return Set of replaceable blocks
	 */
	public Set<Material> getReplaceableBlocks() {
		return replaceableBlocks;
	}


	/**
	 * Load list of replaceable blocks from config file
	 */
	public Set<Material> loadReplaceableBlocks() {

		// get string list of materials from config file
		List<String> materialStringList = plugin.getConfig().getStringList("replaceable-blocks");

		Set<Material> returnSet = new HashSet<>();

		// iterate over string list
		for (String materialString : materialStringList) {

			// if material string matches a valid material type, add to replaceableBlocks HashSet
			if (Material.matchMaterial(materialString) != null) {
				returnSet.add(Material.matchMaterial(materialString));
			}
		}

		return returnSet;
	}


	/**
	 * Loads death chest blocks from datastore<br>
	 * expires death chest blocks whose time has passed<br>
	 * schedules tasks to expire remaining blocks 
	 */
	private void loadDeathChestBlocks() {
		
		long currentTime = System.currentTimeMillis();

		for (DeathChestBlock deathChestBlock : plugin.dataStore.getAllRecords()) {
			
			// get current block at deathChestBlock location
			Block block = deathChestBlock.getLocation().getBlock();
			
			// if block at location is not a DeathChestBlock material, remove from datastore
			if (!deathChestMaterials.contains(block.getType())) {
				plugin.dataStore.deleteRecord(deathChestBlock.getLocation());
				
				// send debug message to log
				if (plugin.debug) {
					plugin.getLogger().info("Block at loaded location is not a DeathChestBlock type."
							+ " Removed from datastore.");
				}
				continue;
			}

			// set block metadata
			deathChestBlock.setBlockMetadata();
			
			// if expiration time has passed, expire deathChestBlock now
			if (deathChestBlock.getExpiration() < currentTime) {
				
				// if death chest expiration is greater than 0, expire it (zero or less means never expire)
				if (deathChestBlock.getExpiration() > 0) {
					deathChestBlock.expire();
				}
			}
			else {
				// schedule task to expire at appropriate time
				taskManager.createExpireBlockTask(deathChestBlock);
			}
		}
	}


	// new deployChest prototype
	public final Result deployChest(final Player player, final Collection<ItemStack> droppedItems) {

		// combine stacks of same items where possible
		List<ItemStack> chestItems = ChestUtilities.consolidateItemStacks(droppedItems);

		// get required chest size
		ChestSize chestSize = ChestSize.SINGLE;
		if (chestItems.size() > 27) {
			chestSize = ChestSize.DOUBLE;
		}

		// if require-chest option is enabled
		// if player does not have a chest in inventory
		// and player does not have permission override
		if (plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest")
				&& !hasChest(chestItems)) {

			// create result object
			Result result = new Result(ResultCode.NO_CHEST);

			// add all dropped items to result object
			result.setRemainingItems(droppedItems);

			// return result object
			return result;
		}

		// search for valid chest location
		Result result = findValidChestLocation(player, chestSize);

		// if search failed, return result
		if (!result.getResultCode().equals(ResultCode.SUCCESS)
				&& !result.getResultCode().equals(ResultCode.PARTIAL_SUCCCESS)) {
			if (plugin.debug) {
				plugin.getLogger().info("Left chest search failed.");
			}
			return result;
		}

		// if require-chest option is enabled remove one chest from chest items
		if (plugin.getConfig().getBoolean("require-chest")) {
			removeOneChest(chestItems);
		}

		// place chest at result location and place dropped items in chest inventory
		List<ItemStack> remainingItems = placeChest(result.getLocation(), chestItems);
		if (plugin.debug) {
			plugin.getLogger().info("Left chest placed at " + result.getLocation().toString());
		}

		// create DeathChestBlock object
		DeathChestBlock deathChestBlock = new DeathChestBlock(player, result.getLocation().getBlock());

		// put DeathChestBlock in datastore
		plugin.dataStore.putRecord(deathChestBlock);

		// create expire task for deathChestBlock
		taskManager.createExpireBlockTask(deathChestBlock);

		// place sign on left chest
		placeSign(player, result.getLocation().getBlock());

		// if chest size is single and result is success, return
		if (chestSize.equals(ChestSize.SINGLE) && result.getResultCode().equals(ResultCode.SUCCESS)) {
			if (plugin.debug) {
				plugin.getLogger().info("Single chest placement successful! Exiting deploy method.");
			}
			result.setRemainingItems(remainingItems); // just in case somehow items were left over, they will get dropped
			return result;
		}

		// if result is partial success, return result with remaining items
		if (result.getResultCode().equals(ResultCode.PARTIAL_SUCCCESS)) {
			if (plugin.debug) {
				plugin.getLogger().info("Double chest placement partial success! Exiting deploy method.");
			}
			result.setRemainingItems(remainingItems);
			return result;
		}

		// if player does not have double chest permission, return result with remaining items
		if (!player.hasPermission("deathchest.doublechest")) {
			result.setRemainingItems(remainingItems);
			return result;
		}

		// if second chest needed
		// and require-chest option is enabled
		// and player does not have permission override
		// and player does not have a chest in inventory
		// return result with remaining items
		if (remainingItems.size() > 0
				&& plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest")
				&& !hasChest(chestItems)) {

			result.setRemainingItems(remainingItems);
			return result;
		}

		// if second chest needed
		// and require-chest option is enabled
		// and player does not have permission override
		// and player does have chest in inventory
		// remove one chest from remaining items
		if (remainingItems.size() > 0
				&& plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest")
				&& hasChest(remainingItems)) {
			removeOneChest(remainingItems);
		}

		// if second chest needed and valid double chest location was found,
		// place second chest at location to right of result location and place remaining items in inventory
		if (chestSize.equals(ChestSize.DOUBLE)
				&& result.getResultCode().equals(ResultCode.SUCCESS)
				&& remainingItems.size() > 0) {

			remainingItems = placeChest(getLocationToRight(result.getLocation()), remainingItems);
		}

		// put remaining items in result
		result.setRemainingItems(remainingItems);

		// create DeathChestBlock object
		DeathChestBlock rightChestBlock = new DeathChestBlock(player,getLocationToRight(result.getLocation()).getBlock());

		// put DeathChestBlock in datastore
		plugin.dataStore.putRecord(rightChestBlock);

		// create expire task for deathChestBlock
		taskManager.createExpireBlockTask(rightChestBlock);

		return result;
	}

	
	private List<ItemStack> placeChest(final Location location,
									   final List<ItemStack> chestItems) {

		// get current block at location
		Block chestBlock = location.getBlock();

		// get block state
		BlockState chestState = chestBlock.getState();

		// set material to chest
		chestState.setType(Material.CHEST);

		// set direction
		chestState.setData(new org.bukkit.material.Chest(getCardinalDirection(location)));

		// update chest BlockState
		chestState.update(true, false);

		// get updated BlockState
		chestState = chestBlock.getState();

		// cast to Chest
		org.bukkit.block.Chest chest = (org.bukkit.block.Chest) chestState;

		// put items into chest inventory, items that don't fit are returned as List of ItemStack
		return fillChest(chest, chestItems);

//		// create DeathChestBlock object
//		DeathChestBlock deathChestBlock = new DeathChestBlock(player,chestBlock);
//
//		// put DeathChestBlock in datastore
//		plugin.dataStore.putRecord(deathChestBlock);
//
//		// create expire task for deathChestBlock
//		taskManager.createExpireBlockTask(deathChestBlock);

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
		
		// get player facing direction (yaw)
		float yaw = player.getLocation().getYaw();

		// get block adjacent to chest facing player direction
		Block signblock = chestBlock.getRelative(getCardinalDirection(player));
		
		// if chest face is valid location, create wall sign
		if (isValidSignLocation(player,signblock.getLocation())) {
			signblock.setType(Material.WALL_SIGN);
		}
		else {
			// create sign post on top of chest if chest face was invalid location
			signblock = chestBlock.getRelative(BlockFace.UP);
			if (isValidSignLocation(player,signblock.getLocation())) {
				signblock.setType(Material.SIGN);
			}
			else {
				// if top of chest is also an invalid location, do nothing and return
				return false;
			}
		}
		
		// get block state of sign block
		BlockState signblockState = signblock.getState();
		
		// if block has not been successfully transformed into a sign, return false
		if (!(signblockState instanceof Sign)) {
			return false;
		}
		
		// Place text on sign with player name and death date
		Sign sign = (Sign)signblockState;
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
					line = line.replace("%date%", dateString);
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
		signData.setFacingDirection(getCardinalDirection(yaw));
		sign.setData(signData);
		
		// update sign block with text and direction
		sign.update();
		
		// create DeathChestBlock object for sign
		DeathChestBlock deathChestBlock = new DeathChestBlock(player,signblock);

		// insert deathChestBlock in datastore
		plugin.dataStore.putRecord(deathChestBlock);
		
		// create expire task for deathChestBlock
		taskManager.createExpireBlockTask(deathChestBlock);

		// return success
		return true;
	}


	private List<ItemStack> fillChest(final Chest chest, final Collection<ItemStack> itemStacks) {

		// convert itemStacks list to array
		ItemStack[] stackArray = new ItemStack[itemStacks.size()];
		stackArray = itemStacks.toArray(stackArray);

		// return list of items that did not fit in chest
		return new ArrayList<>(chest.getInventory().addItem(stackArray).values());
	}


	/**
	 * Search for a valid location to place a single chest,
	 * taking into account replaceable blocks, grass path blocks and
	 * restrictions from other block protection plugins if configured
	 * @param player Player that deathchest is being deployed for
	 * @return SearchResult
	 */
	private Result findValidChestLocation(final Player player, final ChestSize chestSize) {

		// count number of tests performed, for debugging purposes
		int testCount = 0;

		// get distance to search from config
		int radius = plugin.getConfig().getInt("search-distance");

		// get clone of player death location
		Location testLocation = player.getLocation().clone();

		// if player died in the void, start search at y=1 if place-above-void configured true
		if (testLocation.getY() < 1 && plugin.getConfig().getBoolean("place-above-void")) {
			testLocation.setY(1);
		}

		// print player death location in log
		if (plugin.debug) {
			plugin.getLogger().info("Death location: "
					+ testLocation.getBlockX() + ","
					+ testLocation.getBlockY() + ","
					+ testLocation.getBlockZ());
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


	private Result validateChestLocation(final Player player,
										 final Location location,
										 final ChestSize chestSize) {

		// declare result
		Result result;

		// test left chest block location
		result = validateChestLocation(player,
				location,
				ChestElement.LEFT_CHEST);
		if (!result.getResultCode().equals(ResultCode.SUCCESS)) {
			return result;
		}

		// test sign block location
		result = validateChestLocation(player,
				LocationUtilities.getLocationToFront(location),
				ChestElement.SIGN);
		if (!result.getResultCode().equals(ResultCode.SUCCESS)) {
			return result;
		}

		// if chest is to be a double chest, test right chest location
		if (chestSize.equals(ChestSize.DOUBLE)) {

			// test right chest block location
			result = validateChestLocation(player,
					LocationUtilities.getLocationToRight(location),
					ChestElement.RIGHT_CHEST);
			if (!result.getResultCode().equals(ResultCode.SUCCESS)) {
				return result;
			}
		}

		// return successful search result with location
		result.setResultCode(ResultCode.SUCCESS);
		return result;
	}


	private Result validateChestLocation(final Player player,
											   final Location location,
											   final ChestElement chestElement) {

		// get block at passed location
		Block block = location.getBlock();

		// if block at location is not replaceable block, return negative result
		if (!getReplaceableBlocks().contains(block.getType())) {
			return new Result(ResultCode.NON_REPLACEABLE_BLOCK);
		}

		// if block at location is above grass path, return negative result
		if (isAboveGrassPath(block)) {
			return new Result(ResultCode.ABOVE_GRASS_PATH);
		}

		// if left chest, check for adjacent chest to left
		if (chestElement.equals(ChestElement.LEFT_CHEST)) {
			if (getBlockToLeft(location).getType().equals(Material.CHEST)) {
				return new Result(ResultCode.ADJACENT_CHEST);
			}
		}

		// if right chest, check for adjacent chest to right
		if (chestElement.equals(ChestElement.RIGHT_CHEST)) {
			if (getBlockToRight(location).getType().equals(Material.CHEST)) {
				return new Result(ResultCode.ADJACENT_CHEST);
			}
		}

//		// if block at location is protected by plugin, return negative result
//		ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestPlacement(player, block);
//		if (blockingPlugin != null) {
//			Result result = new Result(ResultCode.PROTECTION_PLUGIN);
//			result.setProtectionPlugin(blockingPlugin);
//			return result;
//		}

		Result result = new Result(ResultCode.SUCCESS);
		result.setLocation(location);
		return result;
	}


	/** Check if sign can be placed at location
	 *
	 * @param player	Player to check permissions
	 * @param location	Location to check permissions
	 * @return boolean
	 */
	private boolean isValidSignLocation(final Player player, final Location location) {

		Block block = location.getBlock();

		// check if block at location is a ReplaceableBlock
		return getReplaceableBlocks().contains(block.getType());
	}

}
