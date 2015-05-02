package com.winterhaven_mc.deathchest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ChestManager {

	private DeathChestMain plugin;

	// datastore object
	private Datastore datastore;
	
	// chest utilities object
	private ChestUtilities chestUtilities;
	
	// DeathChestBlock material types
	private HashSet<Material> deathChestMaterials = new HashSet<Material>();
	
	// material types that can be replaced by death chests
	private HashSet<Material> replaceableBlocks = new HashSet<Material>();

	// HashSet of player uuids (as strings) that have been sent expire message in last 1 second
	private HashSet<String> expireMessageCooldown = new HashSet<String>();


	/**
	 * constructor method for <code>ChestManager</code>
	 * 
	 * @param	plugin		A reference to this plugin's main class
	 */
	public ChestManager(DeathChestMain plugin) {
		
		// create pointer to main class
		this.plugin = plugin;
		
		// instantiate chestutilities
        chestUtilities = new ChestUtilities(plugin);
		
		// instantiate datastore
        datastore = getNewDatastore();
        
		// convert any old datastore files to new datastore
		convertDatastores();
 		
		// load material types that chests can be replace from config file
		loadReplaceableBlocks();
		
		// get death chest block types (chests and signs)
		setDeathChestBlockTypes();
		
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

		for (DeathChestBlock deathChestBlock : datastore.getAllRecords()) {
			
			// get current block at deathChestBlock location
			Block block = deathChestBlock.getLocation().getBlock();
			
			// if block at location is not a DeathChestBlock type, remove from datastore
			if (!deathChestMaterials.contains(block.getType())) {
				datastore.deleteRecord(deathChestBlock.getLocation());
				
				// send debug message to log
				if (plugin.debug) {
					plugin.getLogger().info("Block at loaded location is not a DeathChestBlock type. Removed from datastore.");
				}
				continue;
			}
			
			// set block metadata
			deathChestBlock.setMetadata();
			
			// if expiration time has passed, expire deathChestBlock now
			if (deathChestBlock.getExpiration() < currentTime) {
				
				// if death chest expiration is greater than 0, expire it (zero or less means never expire)
				if (deathChestBlock.getExpiration() > 0) {
					expireDeathChestBlock(deathChestBlock.getLocation().getBlock());
				}
			}
			else {
				// schedule task to expire at appropriate time
				createItemExpireTask(deathChestBlock);
			}
		}
	}

	
	/**
	 * Create task to expire death chest block at appropriate time in the future
	 * @param block
	 */
	private void createItemExpireTask(final DeathChestBlock deathChestBlock) {
		
		// if DeathChestBlock expiration is zero or less, it is set to never expire; output debug message and return.
		if (deathChestBlock.getExpiration() < 1) {
			if (plugin.debug) {
				plugin.getLogger().info("DeathChestBlock is set to never expire.");
			}
			return;
		}
		
		Long currentTime = System.currentTimeMillis();
		Long expireTime = deathChestBlock.getExpiration();
		Long ticksRemaining = (expireTime - currentTime) / 50;
		if (ticksRemaining < 1) {
			ticksRemaining = (long) 1;
		}
		
		if (plugin.debug) {
			plugin.getLogger().info("Scheduling block to expire in " + ticksRemaining + " ticks.");
		}
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

			@Override
			public void run() {
				expireDeathChestBlock(deathChestBlock.getLocation().getBlock());
			}
		}, ticksRemaining);
	}
	

	private void expireDeathChestBlock(Block block) {

		// if block is not a DeathChestBlock, do nothing and return
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}
		
		// get owner UUID from block metadata
		final String playeruuid = block.getMetadata("deathchest-owner").get(0).asString();
		
		// destroy DeathChestBlock
		destroyDeathChestBlock(block);
		
		// if player is in expireMessageCooldown HashSet, do nothing and return (so only one message is sent to player)
		if (expireMessageCooldown.contains(playeruuid)) {
			return;
		}
		
		// iterate through online players and match uuid to DeathChestBlock owner to send expire message
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			if (player.getUniqueId().toString().equals(playeruuid)) {
				plugin.messageManager.sendPlayerMessage(player, "chest-expired");
			}
		}
		
		// put player in expireMessageCooldown HashSet, and remove after 20 ticks (1 second)
		expireMessageCooldown.add(playeruuid);
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override
			public void run() {
				expireMessageCooldown.remove(playeruuid);
			}
		}, 20);
	}

	
	/**
	 * Remove DeathChestBlock from datastore, remove block metadata, and delete from world, dropping items
	 * @param block
	 */
	public void destroyDeathChestBlock(Block block) {

		// delete record from datastore
		getCurrentDatastore().deleteRecord(block.getLocation());
		
		// if block is indeed a DeathChestBlock, break block and drop contents
		if (deathChestMaterials.contains(block.getType()) && DeathChestBlock.isDeathChestBlock(block)) {

			// if chunk containing death chest is not loaded, load it so items will drop
			if (!block.getChunk().isLoaded()) {
				block.getChunk().load();
			}
			
			// destroy block by setting to AIR, dropping contents but not block itself
			block.setType(Material.AIR);
		}
		
		// remove block metadata
		DeathChestBlock.removeMetadata(block);
	}

	
	/**
	 * Public interface to lootChest
	 * @param player	Player doing the looting
	 * @param block		block to attempt to loot
	 */
	public void lootChest(Player player, Block block) {
		lootChest(player,block,0);
	}

	
	/**
	 * 
	 * @param player		player doing the looting
	 * @param block			block to attempt to loot
	 * @param iterations	depth of recursion
	 */
	private void lootChest(Player player, Block block, int iterations) {
		
		// increment iteration count for recursion
		iterations++;
		
		if (block.getType().equals(Material.CHEST)) {
			PlayerInventory playerinventory = player.getInventory();
			BlockState state = block.getState();
			Chest chest = (Chest)state;
			ItemStack[] chestinventory = chest.getInventory().getContents();
			for(int i = 0; i < chestinventory.length && playerinventory.firstEmpty() != -1; i++) {
				if(chestinventory[i] != null) {
					playerinventory.addItem(chestinventory[i]);
					chest.getInventory().removeItem(chestinventory[i]);
					if (plugin.getConfig().getBoolean("sound-effects",true)) {
						player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
					}
				}
			}
		}

		// destroy DeathChestBlock
		destroyDeathChestBlock(block);
		
		// if less than two chests have been detected,
		// check surrounding blocks for another chest
		if (iterations < 2) {
			
			BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
		
			for (BlockFace face : faces) {
				Block testblock = block.getRelative(face);
				if (testblock.getType().equals(Material.CHEST) &&
						DeathChestBlock.isDeathChestBlock(testblock)) {
					lootChest(player,testblock,iterations);
				}
			}
		}
	}


	/**
	 * Attempt to deploy a death chest at player's death location containing dropped items
	 * @param player				Player that died
	 * @param droppedItems			Items player had in inventory on death
	 * @return List of ItemStack	items that didn't fit in chest
	 */
	public List<ItemStack> deployChest(Player player, List<ItemStack> droppedItems) {
		
		List<ItemStack> remainingItems = new ArrayList<ItemStack>();
		List<ItemStack> chestItems = new ArrayList<ItemStack>();
				
		// combine stacks of same items where possible
		chestItems = DeathChestBlock.consolidateItems(droppedItems);

		// if require-chest option is enabled
		// and player does not have permission override
		if(plugin.getConfig().getBoolean("require-chest", false) &&
				!player.hasPermission("deathchest.freechest")) {

			// if player does not have a chest in their inventory
			// output message and return, allowing inventory items to drop on ground
			if (!chestUtilities.hasChest(chestItems)) {
				plugin.messageManager.sendPlayerMessage(player, "no-chest-in-inventory");
				return droppedItems;
			}
			// remove one chest from players inventory
			chestItems = chestUtilities.removeOneChest(chestItems);

			// if only one chest required to hold all dropped items	
			// or player doesn't have doublechest permission
			// or player doesn't have a second chest in inventory
			if (chestItems.size() <= 27 ||
					!player.hasPermission("deathchest.doublechest") ||
					!chestUtilities.hasChest(chestItems)) {
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
			if (chestItems.size() <= 27 ||
					!player.hasPermission("deathchest.doublechest")) {
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
	private List<ItemStack> deploySingleChest(Player player, List<ItemStack> droppedItems) {
		
		Location location = chestUtilities.findValidSingleChestLocation(player);

		// null location is returned if valid location could not be found
		if (location == null) {
			return droppedItems;
		}
		
		// actual chest creation
		Block block = location.getBlock();
		block.setType(Material.CHEST);
		BlockState state = block.getState();
		Chest chest = (Chest)state;
		
		// set chest direction
		org.bukkit.material.Chest chestData = (org.bukkit.material.Chest) chest.getData();
		chestData.setFacingDirection(chestUtilities.getDirection(location.getYaw()));
		
		chest.update();
		
		// put items into chest, items that don't fit are put in remaining_items list
		List<ItemStack> remainingItems = new ArrayList<ItemStack>(droppedItems);
		int chestsize = chest.getInventory().getSize();
		int itemcount = 0;
		for (ItemStack item : droppedItems) {
			chest.getInventory().addItem(item);
			remainingItems.remove(item);
			itemcount++;
			if (itemcount >= chestsize) {
				break;
			}
		}

		// create DeathChestBlock object
		DeathChestBlock deathChestBlock = new DeathChestBlock(player,block);
		
		// put DeathChestBlock in datastore
		getCurrentDatastore().putRecord(deathChestBlock);
		
		// create expire task for deathChestBlock
		createItemExpireTask(deathChestBlock);

		// place sign on chest
		placeChestSign(player,block);

		// send success message to player
		plugin.messageManager.sendPlayerMessage(player, "chest-success");
		
		// return list of items that did not fit in chest
		return remainingItems;
	}
	
	
	/**
	 * Deploy a double chest
	 * 
	 * @param player			Player for whom to deploy chest
	 * @param droppedItems		items to place in chest
	 * @return Any items that could not be placed in chest, as List of ItemStack
	 */
	private List<ItemStack> deployDoubleChest(Player player, List<ItemStack> droppedItems) {
		
		// try to find a valid double chest location
		Location location = chestUtilities.findValidDoubleChestLocation(player);
		
		// if no valid double chest location can be found, try to find a valid single chest location
		if (location == null) {
			location = chestUtilities.findValidSingleChestLocation(player);
		}

		// if no valid single chest location, return droppedItems
		if (location == null) {
			return droppedItems;
		}
		
		// actual chest creation
		Block block = location.getBlock();
		
		// set block to chest material
		block.setType(Material.CHEST);
		
		// get blockstate of chest
		Chest chest = (Chest)block.getState();
		
		// set chest direction
		org.bukkit.material.Chest chestData = (org.bukkit.material.Chest) chest.getData();
		chestData.setFacingDirection(chestUtilities.getDirection(location.getYaw()));

		chest.update();
		
		// put items into first chest, items that don't fit are put in remaining_items list
		List<ItemStack> remaining_items = new ArrayList<ItemStack>(droppedItems);
		int chestsize = chest.getInventory().getSize();
		int itemcount = 0;
		for (ItemStack item : droppedItems) {
			chest.getInventory().addItem(item);
			remaining_items.remove(item);
			itemcount++;
			if (itemcount >= chestsize) {
				break;
			}
		}
		
		// place sign on chest
		placeChestSign(player,block);
	
		// create DeathChestBlock object
		DeathChestBlock deathChestBlock = new DeathChestBlock(player,block);
		
		// put deathChestBlock in datastore
		getCurrentDatastore().putRecord(deathChestBlock);
		
		// create expire task for deathChestBlock
		createItemExpireTask(deathChestBlock);

		// get location one block to right of first chest
		location = chestUtilities.locationToRight(location);
		
		// if block at second chest location is not valid, send message and return remaining_items
		if (!chestUtilities.isValidDoubleLocation(player,location)) {
			plugin.messageManager.sendPlayerMessage(player, "doublechest-partial-success");
			return remaining_items;
		}
		
		// get block at location to the right of first chest
		block = location.getBlock();
	
		// set block to chest material
		block.setType(Material.CHEST);
		
		// get blockstate of chest
		chest = (Chest)block.getState();
		
		// set chest direction
		chestData = (org.bukkit.material.Chest) chest.getData();
		chestData.setFacingDirection(chestUtilities.getDirection(location.getYaw()));
		
		// update blockstate
		chest.update();
	
		// put items into chest, items that don't fit are put in remaining_items list
		List<ItemStack> remaining_items2 = new ArrayList<ItemStack>(remaining_items);
		chestsize = chest.getInventory().getSize();
		itemcount = 0;
		for (ItemStack item : remaining_items) {
			chest.getInventory().addItem(item);
			remaining_items2.remove(item);
			itemcount++;
			if (itemcount >= chestsize) {
				break;
			}
		}
	
		// create DeathChestBlock object
		DeathChestBlock deathChestBlock2 = new DeathChestBlock(player,block);
		
		// insert deathChestBlock in datastore
		getCurrentDatastore().putRecord(deathChestBlock2);
		
		// create expire task for deathChestBlock
		createItemExpireTask(deathChestBlock2);

		// send success message to player
		plugin.messageManager.sendPlayerMessage(player, "chest-success");
		
		// return list of items that did not fit in chest
		return remaining_items2;
	}

	
	/**
	 * Place sign on chest
	 * @param player		Chest owner
	 * @param chestblock	Chest block
	 * @return boolean		Success or failure to place sign
	 */
	private boolean placeChestSign(Player player, Block chestblock) {
		
		// if chest-signs are turned off in configuration, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-signs")) {
			return false;
		}
		
		// get player facing direction (yaw)
		float yaw = player.getLocation().getYaw();
		
		// get block adjacent to chest facing player direction
		Block signblock = chestblock.getRelative(chestUtilities.getDirection(yaw));
		
		// if chest face is valid location, create wall sign
		if (chestUtilities.isValidSignLocation(player,signblock.getLocation())) {
			signblock.setType(Material.WALL_SIGN);
		}
		else {
			// create sign post on top of chest if chest face was invalid location
			signblock = chestblock.getRelative(BlockFace.UP);
			if (chestUtilities.isValidSignLocation(player,signblock.getLocation())) {
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
		String datestring = new SimpleDateFormat("MMM d, yyyy").format(System.currentTimeMillis());
		sign.setLine(0, ChatColor.BOLD + "R.I.P.");
		sign.setLine(1, ChatColor.RESET + player.getName());
		sign.setLine(3, "D: " + datestring);
		
		// set sign facing direction
		org.bukkit.material.Sign signData = (org.bukkit.material.Sign) signblockState.getData();
		signData.setFacingDirection(chestUtilities.getDirection(yaw));
		sign.setData(signData);
		
		// update sign block with text and direction
		sign.update();
		
		// create DeathChestBlock object for sign
		DeathChestBlock deathChestBlock = new DeathChestBlock(player,signblock);

		// insert deathChestBlock in datastore
		getCurrentDatastore().putRecord(deathChestBlock);
		
		// create expire task for deathChestBlock
		createItemExpireTask(deathChestBlock);

		// return success
		return true;
	}
	

	/**
	 * Get count of chest inventory viewers
	 * @param block
	 * @return
	 */
	public int getChestViewerCount(Block block) {
		
		int count = 0;
		
		// confirm block is a chest
		if (block.getType().equals(Material.CHEST)) {
			
			// get chest inventory object
			BlockState state = block.getState();
			Chest chest = (Chest)state;
			
			// get count of inventory viewers
			count = chest.getInventory().getViewers().size();
		}
		
		// return number of chest inventory viewers
		return count;
	}


	/**
	 * Assign DeathChestBlock material types to hash set
	 */
	private void setDeathChestBlockTypes() {
		deathChestMaterials.add(Material.CHEST);
		deathChestMaterials.add(Material.WALL_SIGN);
		deathChestMaterials.add(Material.SIGN_POST);
	}

	
	
	/**
	 * Get HashSet of replaceable blocks
	 * @return
	 */
	public HashSet<Material> getReplaceableBlocks() {
		return replaceableBlocks;
	}


	/**
	 * Load list of replaceable blocks from config file
	 */
	private void loadReplaceableBlocks() {

		// get string list of materials from config file
		List<String> materialStringList = plugin.getConfig().getStringList("replaceable-blocks");
		
		// iterate over string list
		for (String materialString : materialStringList) {
			
			// if material string matches a valid material type, add to replaceableBlocks HashSet
			if (Material.matchMaterial(materialString) != null) {
				replaceableBlocks.add(Material.matchMaterial(materialString));
			}
		}
	}


	/**
	 * Public wrapper for datastore.close() method
	 */
	public void closeDatastore() {
		getCurrentDatastore().close();
	}
	
	/**
	 * Get new datastore, converting records from old datastore if provided
	 * @param oldDatastore
	 * @return
	 */
	public Datastore getNewDatastore() {
		
		Datastore newDatastore;
		
		if (plugin.getConfig().getString("storage-type","sqlite").equals("yaml")) {
			// instantiate yaml datastore
			newDatastore = new DatastoreYAML();
		}
		else {
			// instantiate sqlite datastore
			newDatastore = new DatastoreSQLite();
		}

		// initialize new datastore
		try {
			newDatastore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().warning("Could not initialize "
					+ newDatastore.getDatastoreName() + " datastore. ");
			if (plugin.debug) {
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
			
			// if new datastore that failed to initialize was not a yaml datastore, try and fallback to that
			if (!(newDatastore instanceof DatastoreYAML)) {
				newDatastore = new DatastoreYAML();
				try {
					newDatastore.initialize();
				}
				catch (Exception e2) {
					plugin.getLogger().warning("Could not initialize "
							+ newDatastore.getDatastoreName() + " datastore. "
							+ "Death chests will not persist after server restart.");
					return null;
				}
			}
		}
		plugin.getLogger().info(newDatastore.getDatastoreName() + " datastore initialized.");

		// return new datastore
		return newDatastore;
	}

	
	/**
	 * Get reference to current datastore
	 * @return Datastore
	 */
	public Datastore getCurrentDatastore() {
		return this.datastore;
	}

	
	/**
	 * Set new datastore
	 * @param newDatastore
	 */
	public void setCurrentDatastore(Datastore newDatastore) {
		this.datastore = newDatastore;
	}

	
	/**
	 * Convert existing datastore files to new datastore
	 */
	public void convertDatastores() {

		Datastore currentDatastore = getCurrentDatastore();
		Datastore oldDatastore;

		// if current datastore is yaml, check for sqlite file to convert 
		if (currentDatastore instanceof DatastoreYAML) {
			oldDatastore = new DatastoreSQLite();
		}
		// otherwise, check for yaml file to convert
		else {
			oldDatastore = new DatastoreYAML();
		}

		// if old datastore is not null and filename is not null or blank...
		if (oldDatastore != null && oldDatastore.getFilename() != null && !oldDatastore.getFilename().isEmpty()) {

			// try to convert old datastore to current datastore 
			File oldDatastoreFile = new File(plugin.getDataFolder() + File.separator + oldDatastore.getFilename());
			if (oldDatastoreFile.exists()) {
				try {
					oldDatastore.initialize();
				} catch (Exception e) {
					plugin.getLogger().warning("Could not initialize existing " 
							+ oldDatastore.getDatastoreName() + "  datastore for conversion.");
					return;
				}

				// counter for records converted
				int blockCount = 0;

				// get all old datastore records
				ArrayList<DeathChestBlock> allOldRecords = new ArrayList<DeathChestBlock>(oldDatastore.getAllRecords());

				// copy each record to new datastore
				for (DeathChestBlock deathChestBlock : allOldRecords) {
					currentDatastore.putRecord(deathChestBlock);
					blockCount++;
				}

				// output number of records converted to log
				plugin.getLogger().info(blockCount + " DeathChestBlocks converted from "
						+ oldDatastore.getDatastoreName() + " datastore.");

				// close old datastore
				oldDatastore.close();

				// delete old data file
				oldDatastoreFile.delete();
			}
			// dereference old datastore file
			oldDatastoreFile = null;
			
			// dereference old datastore
			oldDatastore = null;
		}
	}

}
