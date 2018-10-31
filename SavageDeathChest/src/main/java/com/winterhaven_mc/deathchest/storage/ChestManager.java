package com.winterhaven_mc.deathchest.storage;


import com.winterhaven_mc.deathchest.DeathChestBlock;
import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.SearchResult;
import com.winterhaven_mc.deathchest.tasks.TaskManager;
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
					Material.SIGN
				)));
	

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
	public final SearchResult deployChest(final Player player, final List<ItemStack> droppedItems) {

		// combine stacks of same items where possible
		List<ItemStack> chestItems = DeathChestBlock.consolidateItemStacks(droppedItems);

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
			SearchResult result = SearchResult.NO_CHEST;

			// add all dropped items to result object
			result.setRemainingItems(droppedItems);

			// return result object
			return result;
		}

		// search for valid chest location
		SearchResult result = findValidChestLocation(player, chestSize);

		// if search failed, return result
		if (!result.equals(SearchResult.SUCCESS) && !result.equals(SearchResult.PARTIAL_SUCCCESS)) {
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
		if (chestSize.equals(ChestSize.SINGLE) && result.equals(SearchResult.SUCCESS)) {
			if (plugin.debug) {
				plugin.getLogger().info("Single chest placement successful! Exiting deploy method.");
			}
			result.setRemainingItems(remainingItems); // just in case somehow items were left over, they will get dropped
			return result;
		}

		// if result is partial success, return result with remaining items
		if (result.equals(SearchResult.PARTIAL_SUCCCESS)) {
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
		// and player does not have a chest in inventory
		// and player does not have permission override
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
		// and player has chest in inventory
		// and player does not have permission override
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
				&& result.equals(SearchResult.SUCCESS)
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


//	/**
//	 * Attempt to deploy a death chest at player's death location containing dropped items
//	 * @param player				Player that died
//	 * @param droppedItems			Items player had in inventory on death
//	 * @return List of ItemStack	items that didn't fit in chest
//	 */
//	public final List<ItemStack> deployChest(final Player player, final List<ItemStack> droppedItems) {
//
//		List<ItemStack> remainingItems;
//
//		// combine stacks of same items where possible
//		List<ItemStack> chestItems = DeathChestBlock.consolidateItemStacks(droppedItems);
//
//		// check if require-chest option is enabled
//		// and player does not have permission override
//		if (plugin.getConfig().getBoolean("require-chest") &&
//				!player.hasPermission("deathchest.freechest")) {
//
//			// if player does not have a chest in their inventory
//			// output message and return, allowing inventory items to drop on ground
//			if (!hasChest(chestItems)) {
//				plugin.messageManager.sendMessage(player, MessageId.NO_CHEST_IN_INVENTORY);
//				return droppedItems;
//			}
//
//			// remove one chest from players inventory
//			chestItems = removeOneChest(chestItems);
//
//			// if only one chest required to hold all dropped items
//			// or player doesn't have doublechest permission
//			// or player doesn't have a second chest in inventory
//			if (chestItems.size() <= 27
//					|| !player.hasPermission("deathchest.doublechest")
//					|| !hasChest(chestItems)) {
//				if (plugin.debug) {
//					plugin.getLogger().info("Deploying single chest...");
//				}
//				remainingItems = deploySingleChest(player, chestItems);
//				return remainingItems;
//			}
//			// deploy double chest
//			else {
//				chestItems = removeOneChest(chestItems);
//				if (plugin.debug) {
//					plugin.getLogger().info("Deploying Double Chest...");
//				}
//				remainingItems = deployDoubleChest(player, chestItems);
//				return remainingItems;
//			}
//		}
//		// require-chest option not enabled or player has permission override
//		else {
//			// if only one chest required to hold all dropped items
//			// or player doesn't have doublechest permission
//			if (chestItems.size() <= 27
//					|| !player.hasPermission("deathchest.doublechest")) {
//				if (plugin.debug) {
//					plugin.getLogger().info("Deploying Single Chest...");
//				}
//				return deploySingleChest(player, chestItems);
//			}
//			else {
//				// deploy double chest
//				if (plugin.debug) {
//					plugin.getLogger().info("Deploying Double Chest...");
//				}
//				return deployDoubleChest(player, chestItems);
//			}
//		}
//	}


//	/**
//	 * Deploy a single chest
//	 *
//	 * @param player			player to deploy chest for
//	 * @param droppedItems		items to place in chest
//	 * @return Items that did not fit in chest, as List of ItemStacks
//	 */
//	private List<ItemStack> deploySingleChest(final Player player, final List<ItemStack> droppedItems) {
//
//		// get chest facing direction based on player yaw
//		BlockFace chestDirection = LocationUtilities.getCardinalDirection(player);
//
//		SearchResult result = findValidChestLocation(player, ChestSize.SINGLE);
//
//		// search result returned gives reason if valid location could not be found
//		if (result == null || !result.equals(SearchResult.SUCCESS)) {
//
//			// use try..catch block here so a messaging error does not cause item duplication
//			try {
//				if (SearchResult.PROTECTION_PLUGIN.equals(result)) {
//					plugin.messageManager.sendMessage(player,
//							MessageId.CHEST_DENIED_PLUGIN, result.getProtectionPlugin());
//					if (plugin.debug) {
//						plugin.getLogger().info("Chest deployment prevented by "
//							+ result.getProtectionPlugin().getPluginName() + ".");
//					}
//				}
//				else if (SearchResult.ADJACENT_CHEST.equals(result)) {
//					plugin.messageManager.sendMessage(player,	MessageId.CHEST_DENIED_ADJACENT);
//					if (plugin.debug) {
//						plugin.getLogger().info("Chest deployment prevented by adjacent chest.");
//					}
//				}
//				else if (SearchResult.NON_REPLACEABLE_BLOCK.equals(result)) {
//					plugin.messageManager.sendMessage(player,	MessageId.CHEST_DENIED_BLOCK);
//					if (plugin.debug) {
//						plugin.getLogger().info("Chest deployment prevented by non-replaceable block.");
//					}
//				}
//			} catch (Exception e) {
//				plugin.getLogger().info("An error occurred while sending a chest denied message.");
//				if (plugin.debug) {
//					e.printStackTrace();
//				}
//			}
//			return droppedItems;
//		}
//
//		// actual chest creation
//		Block chestBlock = result.getLocation().getBlock();
//		BlockState chestState = chestBlock.getState();
//
//		// set material to chest
//		chestState.setType(Material.CHEST);
//
//		// set direction
//		chestState.setData(new org.bukkit.material.Chest(chestDirection));
//
//		// update chest BlockState
//		chestState.update(true, false);
//
//		// get updated BlockState
//		chestState = chestBlock.getState();
//
//		// cast to Chest
//		org.bukkit.block.Chest chest = (org.bukkit.block.Chest) chestState;
//
//		// get chest inventory
//		Inventory inventory = chest.getBlockInventory();
//
//		// put items into chest inventory, items that don't fit are returned as List of ItemStack
//		List<ItemStack> noFit = fillChest(inventory,droppedItems);
//
//		// create DeathChestBlock object
//		DeathChestBlock deathChestBlock = new DeathChestBlock(player,chestBlock);
//
//		// put DeathChestBlock in datastore
//		plugin.dataStore.putRecord(deathChestBlock);
//
//		// create expire task for deathChestBlock
//		taskManager.createExpireBlockTask(deathChestBlock);
//
//		// place sign on chest
//		placeSign(player,chestBlock);
//
//		// send success message to player
//		plugin.messageManager.sendMessage(player, MessageId.CHEST_SUCCESS);
//
//		// return list of items that did not fit in chest
//		return noFit;
//	}
	
	
//	/**
//	 * Deploy a double chest
//	 *
//	 * @param player Player for whom to deploy chest
//	 * @param droppedItems items to place in chest
//	 * @return Any items that could not be placed in chest, as List of ItemStack
//	 */
//	private List<ItemStack> deployDoubleChest(final Player player, final List<ItemStack> droppedItems) {
//
//		// get chest facing direction based on player yaw
//		BlockFace chestDirection = LocationUtilities.getCardinalDirection(player.getLocation().getYaw());
//
//		// try to find a valid double chest location
//		SearchResult result = findValidChestLocation(player, ChestSize.DOUBLE);
//
//		// if no valid double chest location can be found, deploy single chest
//		if (result == null || !result.equals(SearchResult.SUCCESS)) {
//			result = findValidChestLocation(player, ChestSize.SINGLE);
//			if (result.equals(SearchResult.SUCCESS)) {
//				result = SearchResult.PARTIAL_SUCCCESS;
//			}
//		}
//
//		// if no valid single chest location, return droppedItems
//		if (!result.equals(SearchResult.SUCCESS)|| !result.equals(SearchResult.PARTIAL_SUCCCESS)) {
//
//			// use try..catch block here so a messaging error does not cause item duplication
//			try {
//				if (SearchResult.PROTECTION_PLUGIN.equals(result)) {
//					plugin.messageManager.sendMessage(player,
//							MessageId.CHEST_DENIED_PLUGIN, result.getProtectionPlugin());
//					if (plugin.debug) {
//						plugin.getLogger().info("Chest deployment prevented by "
//							+ result.getProtectionPlugin().getPluginName() + ".");
//					}
//				}
//				else if (SearchResult.ADJACENT_CHEST.equals(result)) {
//					plugin.messageManager.sendMessage(player,
//							MessageId.CHEST_DENIED_ADJACENT);
//					if (plugin.debug) {
//						plugin.getLogger().info("Chest deployment prevented by adjacent chest.");
//					}
//				}
//				else if (SearchResult.NON_REPLACEABLE_BLOCK.equals(result)) {
//					plugin.messageManager.sendMessage(player,
//							MessageId.CHEST_DENIED_BLOCK);
//					if (plugin.debug) {
//						plugin.getLogger().info("Chest deployment prevented by non-replaceable blocks.");
//					}
//				}
//			} catch (Exception e) {
//				plugin.getLogger().info("An error occurred while sending a chest denied message.");
//				if (plugin.debug) {
//					e.printStackTrace();
//				}
//			}
//			return droppedItems;
//		}
//
//		// actual chest creation
//		Block leftChestBlock = result.getLocation().getBlock();
//		BlockState leftChestState = leftChestBlock.getState();
//
//		// set material to chest
//		leftChestState.setType(Material.CHEST);
//
//		// set direction
//		leftChestState.setData(new org.bukkit.material.Chest(chestDirection));
//
//		// update chest BlockState
//		leftChestState.update(true, false);
//
//		// get updated BlockState
//		leftChestState = leftChestBlock.getState();
//
//		// cast to Chest
//		org.bukkit.block.Chest leftChest = (org.bukkit.block.Chest) leftChestState;
//
//		// get chest inventory
//		Inventory inventory = leftChest.getBlockInventory();
//
//		// put items into chest inventory, items that don't fit are returned as List of ItemStack
//		List<ItemStack> noFit = fillChest(inventory,droppedItems);
//
//		// create DeathChestBlock object
//		DeathChestBlock deathChestBlock = new DeathChestBlock(player,leftChestBlock);
//
//		// put DeathChestBlock in datastore
//		plugin.dataStore.putRecord(deathChestBlock);
//
//		// create expire task for deathChestBlock
//		taskManager.createExpireBlockTask(deathChestBlock);
//
//		// place sign on chest
//		placeSign(player,leftChestBlock);
//
//		// PLACE RIGHT CHEST
//
//		// if search returned PARTIAL_SUCCESS, return remaining items
//		if (result.equals(SearchResult.PARTIAL_SUCCCESS)) {
//			// return remaining items
//			return noFit;
//		}
//
//		Block rightChestBlock = blockToRight(leftChestBlock.getLocation());
//		BlockState rightChestState = rightChestBlock.getState();
//
//		// set material to chest
//		rightChestState.setType(Material.CHEST);
//
//		// set direction
//		rightChestState.setData(new org.bukkit.material.Chest(chestDirection));
//
//		// update chest BlockState
//		rightChestState.update(true, false);
//
//		// get updated BlockState
//		rightChestState = leftChestBlock.getState();
//
//
//
//
//
//		// get location one block to right of first chest
//		Location location = locationToRight(result.getLocation());
//
//		// actual chest creation
//		Block block = result.getLocation().getBlock();
//
//		// set block to chest material
//		block.setType(Material.CHEST);
//
//		// get blockstate of chest
//		Chest chest = (Chest)block.getState();
//
//		// set chest direction
//		org.bukkit.material.Chest chestData = (org.bukkit.material.Chest) chest.getData();
//		chestData.setFacingDirection(chestDirection);
//
//		// update chest to set direction
//		chest.update();
//
//		// fill chest with dropped items
//		List<ItemStack> remainingItems = fillChest(chest,droppedItems);
//
//		// place sign on chest
//		placeSign(player,block);
//
//		// create DeathChestBlock object
//		DeathChestBlock deathChestBlock = new DeathChestBlock(player,block);
//
//		// put deathChestBlock in datastore
//		plugin.dataStore.putRecord(deathChestBlock);
//
//		// create expire task for deathChestBlock
//		taskManager.createExpireBlockTask(deathChestBlock);
//
//		// get location one block to right of first chest
//		Location location = locationToRight(result.getLocation());
//
//		// check that second chest location is valid
//		SearchResult result2 = validateChestLocation(player, location, ChestSize.DOUBLE);
//
//		// if block at second chest location is not valid, send message and return remaining_items
//		if (result2 == null || result2 != SearchResult.SUCCESS) {
//			plugin.messageManager.sendMessage(player, MessageId.DOUBLECHEST_PARTIAL_SUCCESS);
//			return remainingItems;
//		}
//
//		// get block at location to the right of first chest
//		block = location.getBlock();
//
//		// set block to chest material
//		block.setType(Material.CHEST);
//
//		// get blockstate of chest
//		chest = (Chest)block.getState();
//
//		// set chest direction
//		chestData = (org.bukkit.material.Chest) chest.getData();
//		chestData.setFacingDirection(chestDirection);
//
//		// update chest blockstate
//		chest.update();
//
//		// fill chest with dropped items
//		List<ItemStack> noFitItems = fillChest(chest,remainingItems);
//
//		// create DeathChestBlock object
//		DeathChestBlock deathChestBlock2 = new DeathChestBlock(player,block);
//
//		// insert deathChestBlock in datastore
//		plugin.dataStore.putRecord(deathChestBlock2);
//
//		// create expire task for deathChestBlock
//		taskManager.createExpireBlockTask(deathChestBlock2);
//
//		// send success message to player
//		plugin.messageManager.sendMessage(player, MessageId.CHEST_SUCCESS);
//
//		// return list of items that did not fit in chest
//		return noFitItems;
//	}

	
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


	private List<ItemStack> fillChest(final Chest chest, final List<ItemStack> itemStacks) {

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
	private SearchResult findValidChestLocation(final Player player, final ChestSize chestSize) {

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
		SearchResult result;

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
					if (result.equals(SearchResult.SUCCESS)) {
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
					if (result.equals(SearchResult.SUCCESS)) {
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
					if (result.equals(SearchResult.SUCCESS)) {
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
					if (result.equals(SearchResult.SUCCESS)) {
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
		return SearchResult.NON_REPLACEABLE_BLOCK;
	}


	private SearchResult validateChestLocation(final Player player,
											   final Location testLocation,
											   final ChestSize chestSize) {

		// declare search result
		SearchResult result;

		// test left chest block location
		result = validateChestLocation(player, testLocation, ChestElement.LEFT_CHEST);
		if (!result.equals(SearchResult.SUCCESS)) {
			return result;
		}

		// test sign block location
		result = validateChestLocation(player, LocationUtilities.getLocationToFront(testLocation), ChestElement.SIGN);
		if (!result.equals(SearchResult.SUCCESS)) {
			return result;
		}

		// if test is to be a double chest, test right chest location
		if (chestSize.equals(ChestSize.DOUBLE)) {

			// test right chest block location
			result = validateChestLocation(player, LocationUtilities.getLocationToRight(testLocation), ChestElement.SIGN);
			if (!result.equals(SearchResult.SUCCESS)) {
				return result;
			}
		}

		// return successful search result with location
		result = SearchResult.SUCCESS;
		result.setLocation(testLocation);
		return SearchResult.SUCCESS;
	}


	private SearchResult validateChestLocation(final Player player,
											   final Location testLocation,
											   final ChestElement chestElement) {

		// get block at passed location
		Block block = testLocation.getBlock();

		// if block at location is not replaceable block, return negative result
		if (!getReplaceableBlocks().contains(block.getType())) {
			return SearchResult.NON_REPLACEABLE_BLOCK;
		}

		// if block at location is above grass path, return negative result
		if (isAboveGrassPath(block)) {
			return SearchResult.ABOVE_GRASS_PATH;
		}

		// if left chest, check for adjacent chest to left
		if (chestElement.equals(ChestElement.LEFT_CHEST)) {
			if (getBlockToLeft(testLocation).getType().equals(Material.CHEST)) {
				return SearchResult.ADJACENT_CHEST;
			}
		}

		// if right chest, check for adjacent chest to right
		if (chestElement.equals(ChestElement.RIGHT_CHEST)) {
			if (getBlockToRight(testLocation).getType().equals(Material.CHEST)) {
				return SearchResult.ADJACENT_CHEST;
			}
		}

//		// if block at location is protected by plugin, return negative result
//		ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestPlacement(player, block);
//		if (blockingPlugin != null) {
//			SearchResult result = SearchResult.PROTECTION_PLUGIN;
//			result.setProtectionPlugin(blockingPlugin);
//			return result;
//		}

		SearchResult result = SearchResult.SUCCESS;
		result.setLocation(testLocation);
		return SearchResult.SUCCESS;
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
