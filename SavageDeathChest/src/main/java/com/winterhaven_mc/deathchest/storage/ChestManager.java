package com.winterhaven_mc.deathchest.storage;

import com.winterhaven_mc.deathchest.DeathChestBlock;
import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.SearchResult;
import com.winterhaven_mc.deathchest.util.LocationUtilities;
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

    // ItemStack of Chest for comparisons
    private static final ItemStack CHEST_STACK = new ItemStack(Material.CHEST);
    
	// DeathChestBlock material types
	private static final Set<Material> deathChestMaterials = 
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
					Material.CHEST,
					Material.SIGN_POST,
					Material.WALL_SIGN
				)));
	

	/**
	 * constructor method for <code>ChestManager</code>
	 * 
	 * @param	plugin		A reference to this plugin's main class
	 */
	public ChestManager(final PluginMain plugin) {
		
		// set reference to main class
		this.plugin = plugin;
		
		// load death chest blocks from datastore
		loadAllDeathChestBlocks();
	}

	
	/**
	 * Loads death chest blocks from datastore<br>
	 * expires death chest blocks whose time has passed<br>
	 * schedules tasks to expire remaining blocks 
	 */
	private void loadAllDeathChestBlocks() {
		
		Long currentTime = System.currentTimeMillis();

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
				plugin.taskManager.createExpireBlockTask(deathChestBlock);
			}
		}
	}


	/**
	 * Attempt to deploy a death chest at player's death location containing dropped items
	 * @param player				Player that died
	 * @param droppedItems			Items player had in inventory on death
	 * @return List of ItemStack	items that didn't fit in chest
	 */
	public final List<ItemStack> deployChest(final Player player, final List<ItemStack> droppedItems) {
		
		List<ItemStack> remainingItems;

		// combine stacks of same items where possible
		List<ItemStack> chestItems = DeathChestBlock.consolidateItems(droppedItems);

		// check if require-chest option is enabled
		// and player does not have permission override
		if (plugin.getConfig().getBoolean("require-chest") &&
				!player.hasPermission("deathchest.freechest")) {

			// if player does not have a chest in their inventory
			// output message and return, allowing inventory items to drop on ground
			if (!hasChest(chestItems)) {
				plugin.messageManager.sendPlayerMessage(player, "no-chest-in-inventory");
				return droppedItems;
			}
			// remove one chest from players inventory
			chestItems = removeOneChest(chestItems);

			// if only one chest required to hold all dropped items	
			// or player doesn't have doublechest permission
			// or player doesn't have a second chest in inventory
			if (chestItems.size() <= 27 
					|| !player.hasPermission("deathchest.doublechest") 
					|| !hasChest(chestItems)) {
				if (plugin.debug) {
					plugin.getLogger().info("Deploying single chest...");
				}
				remainingItems = deploySingleChest(player, chestItems);
				return remainingItems;
			}
			// deploy double chest
			else {
				chestItems.remove(new ItemStack(Material.CHEST,1));
				if (plugin.debug) {
					plugin.getLogger().info("Deploying Double Chest...");
				}
				remainingItems = deployDoubleChest(player, chestItems);
				return remainingItems;
			}
		}
		// require-chest option not enabled or player has permission override
		else {
			// if only one chest required to hold all dropped items	
			// or player doesn't have doublechest permission
			if (chestItems.size() <= 27 
					|| !player.hasPermission("deathchest.doublechest")) {
				if (plugin.debug) {
					plugin.getLogger().info("Deploying Single Chest...");
				}
				remainingItems = deploySingleChest(player, chestItems);
				return remainingItems;
			}
			else {
				// deploy double chest
				if (plugin.debug) {
					plugin.getLogger().info("Deploying Double Chest...");
				}
				remainingItems = plugin.chestManager.deployDoubleChest(player, chestItems);
				return remainingItems;
			}
		}
	}


	/**
	 * Deploy a single chest
	 * 
	 * @param player			player to deploy chest for
	 * @param droppedItems		items to place in chest
	 * @return Items that did not fit in chest, as List of ItemStacks
	 */
	private List<ItemStack> deploySingleChest(final Player player, final List<ItemStack> droppedItems) {
		
		SearchResult result = LocationUtilities.findValidSingleChestLocation(player);

		// search result returned gives reason if valid location could not be found
		if (result == null || !result.equals(SearchResult.SUCCESS)) {
			
			// use try..catch block here so a messaging error does not cause item duplication
			try {
				if (SearchResult.PROTECTION_PLUGIN.equals(result)) {
					plugin.messageManager.sendPlayerMessage(player,	
							"chest-denied-plugin", result.getProtectionPlugin());
					if (plugin.debug) {
						plugin.getLogger().info("Chest deployment prevented by "
							+ result.getProtectionPlugin().getPluginName() + ".");
					}
				}
				else if (SearchResult.ADJACENT_CHEST.equals(result)) {
					plugin.messageManager.sendPlayerMessage(player,	"chest-denied-adjacent");
					if (plugin.debug) {
						plugin.getLogger().info("Chest deployment prevented by adjacent chest.");
					}				
				}
				else if (SearchResult.NON_REPLACEABLE_BLOCK.equals(result)) {
					plugin.messageManager.sendPlayerMessage(player,	"chest-denied-block");
					if (plugin.debug) {
						plugin.getLogger().info("Chest deployment prevented by non-replaceable block.");
					}
				}
			} catch (Exception e) {
				plugin.getLogger().info("An error occurred while sending a chest denied message.");
				if (plugin.debug) {
					e.printStackTrace();
				}
			}
			return droppedItems;
		}
		
		// actual chest creation
		Block block = result.getLocation().getBlock();
		block.setType(Material.CHEST);
		BlockState state = block.getState();
		Chest chest = (Chest)state;
		
		// set chest direction
		org.bukkit.material.Chest chestData = (org.bukkit.material.Chest) chest.getData();
		chestData.setFacingDirection(LocationUtilities.getCardinalDirection(result.getLocation().getYaw()));
		
		chest.update();
		
		// put items into chest, items that don't fit are put in noFit list
		List<ItemStack> noFit = fillChest(chest,droppedItems);
		
		// create DeathChestBlock object
		DeathChestBlock deathChestBlock = new DeathChestBlock(player,block);
		
		// put DeathChestBlock in datastore
		plugin.dataStore.putRecord(deathChestBlock);
		
		// create expire task for deathChestBlock
		plugin.taskManager.createExpireBlockTask(deathChestBlock);

		// place sign on chest
		placeChestSign(player,block);

		// send success message to player
		plugin.messageManager.sendPlayerMessage(player, "chest-success");
		
		// return list of items that did not fit in chest
		return noFit;
	}
	
	
	/**
	 * Deploy a double chest
	 * 
	 * @param player			Player for whom to deploy chest
	 * @param droppedItems		items to place in chest
	 * @return Any items that could not be placed in chest, as List of ItemStack
	 */
	private List<ItemStack> deployDoubleChest(final Player player, final List<ItemStack> droppedItems) {
		
		// get chest facing direction based on player yaw
		BlockFace chestDirection = LocationUtilities.getCardinalDirection(player.getLocation().getYaw());
		
		// try to find a valid double chest location
		SearchResult result = LocationUtilities.findValidDoubleChestLocation(player);
		
		if (plugin.debug) {
			plugin.getLogger().info("First Chest Search Result: " + result.toString());
		}
		
		// if no valid double chest location can be found, try to find a valid single chest location
		if (result == null || result != SearchResult.SUCCESS) {
			result = LocationUtilities.findValidSingleChestLocation(player);
			if (plugin.debug) {
				plugin.getLogger().info("Double Chest (single) Search Result: " + result.toString());
			}
		}

		// if no valid single chest location, return droppedItems
		if (result == null || result != SearchResult.SUCCESS) {
			
			// use try..catch block here so a messaging error does not cause item duplication
			try {
				if (SearchResult.PROTECTION_PLUGIN.equals(result)) {
					plugin.messageManager.sendPlayerMessage(player,
							"chest-denied-plugin", result.getProtectionPlugin());
					if (plugin.debug) {
						plugin.getLogger().info("Chest deployment prevented by "
							+ result.getProtectionPlugin().getPluginName() + ".");
					}
				}
				else if (SearchResult.ADJACENT_CHEST.equals(result)) {
					plugin.messageManager.sendPlayerMessage(player,
							"chest-denied-adjacent");
					if (plugin.debug) {
						plugin.getLogger().info("Chest deployment prevented by adjacent chest.");
					}				
				}
				else if (SearchResult.NON_REPLACEABLE_BLOCK.equals(result)) {
					plugin.messageManager.sendPlayerMessage(player,
							"chest-denied-block");
					if (plugin.debug) {
						plugin.getLogger().info("Chest deployment prevented by non-replaceable blocks.");
					}
				}
			} catch (Exception e) {
				plugin.getLogger().info("An error occurred while sending a chest denied message.");
				if (plugin.debug) {
					e.printStackTrace();
				}
			}
			return droppedItems;
		}
		
		// actual chest creation
		Block block = result.getLocation().getBlock();
		
		// set block to chest material
		block.setType(Material.CHEST);
		
		// get blockstate of chest
		Chest chest = (Chest)block.getState();
		
		// set chest direction
		org.bukkit.material.Chest chestData = (org.bukkit.material.Chest) chest.getData();
		chestData.setFacingDirection(chestDirection);

		// update chest to set direction
		chest.update();
		
		// fill chest with dropped items
		List<ItemStack> remainingItems = fillChest(chest,droppedItems);

		// place sign on chest
		placeChestSign(player,block);
	
		// create DeathChestBlock object
		DeathChestBlock deathChestBlock = new DeathChestBlock(player,block);
		
		// put deathChestBlock in datastore
		plugin.dataStore.putRecord(deathChestBlock);
		
		// create expire task for deathChestBlock
		plugin.taskManager.createExpireBlockTask(deathChestBlock);

		// get location one block to right of first chest
		Location location = LocationUtilities.locationToRight(result.getLocation());

		// check that second chest location is valid
		SearchResult result2 = LocationUtilities.isValidRightChestLocation(player, location);
		
		// if block at second chest location is not valid, send message and return remaining_items
		if (result2 == null || result2 != SearchResult.SUCCESS) {
			plugin.messageManager.sendPlayerMessage(player, "doublechest-partial-success");
			return remainingItems;
		}
		
		// get block at location to the right of first chest
		block = location.getBlock();
	
		// set block to chest material
		block.setType(Material.CHEST);
		
		// get blockstate of chest
		chest = (Chest)block.getState();
		
		// set chest direction
		chestData = (org.bukkit.material.Chest) chest.getData();
		chestData.setFacingDirection(chestDirection);
		
		// update chest blockstate
		chest.update();
	
		// fill chest with dropped items
		List<ItemStack> noFitItems = fillChest(chest,remainingItems);
		
		// create DeathChestBlock object
		DeathChestBlock deathChestBlock2 = new DeathChestBlock(player,block);
		
		// insert deathChestBlock in datastore
		plugin.dataStore.putRecord(deathChestBlock2);
		
		// create expire task for deathChestBlock
		plugin.taskManager.createExpireBlockTask(deathChestBlock2);

		// send success message to player
		plugin.messageManager.sendPlayerMessage(player, "chest-success");
		
		// return list of items that did not fit in chest
		return noFitItems;
	}

	
	/**
	 * Place sign on chest
	 * @param player		Chest owner
	 * @param chestblock	Chest block
	 * @return boolean		Success or failure to place sign
	 */
	private boolean placeChestSign(final Player player, final Block chestblock) {
		
		// if chest-signs are not enabled in configuration, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-signs")) {
			return false;
		}
		
		// get player facing direction (yaw)
		float yaw = player.getLocation().getYaw();
		
		// get block adjacent to chest facing player direction
		Block signblock = chestblock.getRelative(LocationUtilities.getCardinalDirection(yaw));
		
		// if chest face is valid location, create wall sign
		if (LocationUtilities.isValidSignLocation(player,signblock.getLocation())) {
			signblock.setType(Material.WALL_SIGN);
		}
		else {
			// create sign post on top of chest if chest face was invalid location
			signblock = chestblock.getRelative(BlockFace.UP);
			if (LocationUtilities.isValidSignLocation(player,signblock.getLocation())) {
				signblock.setType(Material.SIGN_POST);
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
					line = line.replace("%playername%", player.getName());
					line = line.replace("%date%", dateString);
					line = line.replace("%worldname%", plugin.worldManager.getWorldName(player.getWorld()));
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
		signData.setFacingDirection(LocationUtilities.getCardinalDirection(yaw));
		sign.setData(signData);
		
		// update sign block with text and direction
		sign.update();
		
		// create DeathChestBlock object for sign
		DeathChestBlock deathChestBlock = new DeathChestBlock(player,signblock);

		// insert deathChestBlock in datastore
		plugin.dataStore.putRecord(deathChestBlock);
		
		// create expire task for deathChestBlock
		plugin.taskManager.createExpireBlockTask(deathChestBlock);

		// return success
		return true;
	}


	/**
	 * Check if Collection of ItemStack contains at least one chest
	 * @param itemStacks Collection of ItemStack to check for chest
	 * @return boolean
	 */
	private boolean hasChest(final Collection<ItemStack> itemStacks) {
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
	 * Remove one chest from list of item stacks
	 * @param itemStacks	List of ItemStack to remove chest
	 * @return List of ItemStack with one chest removed
	 */
	private List<ItemStack> removeOneChest(final List<ItemStack> itemStacks) {
		
		for (ItemStack stack : itemStacks) {
			if (stack.isSimilar(CHEST_STACK)) {
				itemStacks.remove(stack);
				stack.setAmount(stack.getAmount() - 1);
				if (stack.getAmount() > 0) {
					itemStacks.add(stack);
				}
			break;
			}
		}
		return itemStacks;
	}
	
	
	private List<ItemStack> fillChest(final Chest chest, final List<ItemStack> itemStacks) {
		
		// convert itemStacks list to array
		ItemStack[] stackArray = new ItemStack[itemStacks.size()];
		stackArray = itemStacks.toArray(stackArray);
		
		// return list of items that did not fit in chest
		return new ArrayList<>(chest.getInventory().addItem(stackArray).values());
	}
	
}
