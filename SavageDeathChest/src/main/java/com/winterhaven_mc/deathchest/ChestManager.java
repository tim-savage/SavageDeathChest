package com.winterhaven_mc.deathchest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

public class ChestManager {

	private DeathChestMain plugin;

	// datastore object
	private Datastore datastore;
	
	// chest utilities object
	private ChestUtilities chestutilities;
	
	// protected item materials
	private Set<Material> protecteditems = new HashSet<Material>();
	
	// materials that can be replaced by chests
	private HashSet<Material> replaceableblocks = new HashSet<Material>();
	
	private ConcurrentHashMap<String, Boolean> expire_message_cooldown = new ConcurrentHashMap<String, Boolean>();


	/**
	 * constructor method for <code>ChestManager</code>
	 * 
	 * @param	plugin		A reference to this plugin's main class
	 */
	public ChestManager(DeathChestMain plugin) {
		
		// create pointer to main class
		this.plugin = plugin;
		
		// instantiate chestutilities
        chestutilities = new ChestUtilities(plugin);
		
		// instantiate datastore
		if (plugin.getConfig().getString("storage-type","sqlite").equalsIgnoreCase("file")) {
			datastore = new DatastoreYML();
		}
		else {
			datastore = new DatastoreSQLite();
		}
		
		// initialize datastore
		try {
			datastore.initialize();
		} catch (Exception e) {
			plugin.getLogger().warning("An error occured while initializing the datastore.");
			if (plugin.debug) {
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}
		
		// load material types that chests can replace from config file
		loadReplaceableBlocks();
		
		// get protected items (chests and signs)
		getProtectedItemsList();
		
		// load death chest blocks from datastore
		loadDeathChestBlocks();
		
	}

	
	private void loadDeathChestBlocks() {
		
		Long currentTime = System.currentTimeMillis();

		for (DeathChestBlock deathChestBlock : datastore.getAllRecords()) {
			
			// get current block at deathChestBlock location
			Block block = deathChestBlock.getLocation().getBlock();
			
			// if block is not a death chest block, remove from datastore
			if (!protecteditems.contains(block.getType())) {
				datastore.deleteRecord(deathChestBlock.getLocation());
				
				// send debug message to log
				if (plugin.debug) {
					plugin.getLogger().info("Block at loaded location is not a chest or sign. Removed from datastore.");
				}
				return;
			}
			
			// if expiration time has passed, expire deathChestBlock now
			if (deathChestBlock.getExpiration() < currentTime) {
				expireDeathChestItem(deathChestBlock.getLocation().getBlock());
				return;
			}
			
			// set block metadata
			block.setMetadata("deathchest", new FixedMetadataValue(plugin, deathChestBlock.getOwnerUUID().toString()));

			// schedule task to expire at appropriate time
			createItemExpireTask(deathChestBlock);
		}
	}

	
	/**
	 * Create task to expire deathchest item at appropriate time in the future
	 * @param block
	 */
	private void createItemExpireTask(final DeathChestBlock deathChestBlock) {
		Long currentTime = System.currentTimeMillis();
		Long expireTime = deathChestBlock.getExpiration();
		Long ticksRemaining = (expireTime - currentTime) / 50;
		if (ticksRemaining < 1) {
			ticksRemaining = (long) 1;
		}
		if (plugin.debug) {
			plugin.getLogger().info("Scheduling item to expire in " + ticksRemaining + " ticks.");
		}
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

			@Override
			public void run() {
				expireDeathChestItem(deathChestBlock.getLocation().getBlock());
			}
		}, ticksRemaining);
	}
	

	private void expireDeathChestItem(Block block) {

		Player player;
		Player[] arrplayer;

		// if block does not have deathchest metadata, do nothing and return
		if (!block.hasMetadata("deathchest")) {
			if (plugin.debug) {
				plugin.getLogger().info("Item to expire does not have deathchest metadata.");
			}
			return;
		}

		// get player UUID from block metadata
		final String playeruuid = block.getMetadata("deathchest").get(0).asString();
		
		// remove deathChestBlock from datastore
		datastore.deleteRecord(block.getLocation());
		
		// remove death chest block from in game
		breakDeathchestItem(block);
		if (plugin.debug) {
			plugin.getLogger().info("Expired chest item removed from in game.");
		}
		
		// if player is in expire_message_cooldown hashmap, do nothing and return (so only one message is sent to player)
		if (expire_message_cooldown.containsKey(playeruuid) ||
				(arrplayer = Bukkit.getOnlinePlayers()).length == 0 ||
				!(player = arrplayer[0]).getUniqueId().toString().equals(playeruuid)) {
			return;
		}
		
		// send chest expired message to player
		plugin.messagemanager.sendPlayerMessage(player, "chest-expired");
		
		// put player in expire_message_cooldown hashmap, and remove after 20 ticks (1 second)
		expire_message_cooldown.put(playeruuid, true);
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override
			public void run() {
				expire_message_cooldown.remove(playeruuid);
			}
		}, 20);
	}

	
	/**
	 * Load list of replaceable blocks from config file
	 */
	private void loadReplaceableBlocks() {
		List<String> blockstringlist = plugin.getConfig().getStringList("replaceable-blocks");
		for (String blocktype : blockstringlist) {
			if (Material.matchMaterial(blocktype) == null || replaceableblocks.contains(blocktype)) {
				continue;
			}
			replaceableblocks.add(Material.matchMaterial(blocktype));
		}
	}

	
	/**
	 * Get set list of replaceable blocks
	 * @return
	 */
	public HashSet<Material> getReplaceableBlocks() {
		return replaceableblocks;
	}

	
	public DeathChestBlock getDeathChestBlock(Location location) {
		return datastore.getRecord(location);
	}
	
	
	public void removeDeathChestItem(Block block) {

		// delete record from datastore
		datastore.deleteRecord(block.getLocation());

		// send debug message
		if (plugin.debug) {
			plugin.getLogger().info("Deathchest item removed from datastore.");
		}
	}


	/**
	 * Remove deathchest item from datastore, remove metadata, and delete from world, dropping items
	 * 
	 * @param block
	 */
	private void breakDeathchestItem(Block block) {
		
		// if block type is not in protected items list, do nothing and return
		if (!protecteditems.contains(block.getType())) {
			if (plugin.debug) {
				this.plugin.getLogger().info("Block at " + block.getLocation().toString() + " is not a deathchest item.");
			}
			return;
		}
		// if block does not have deathchest metadata, do nothing and return
		if (!block.hasMetadata("deathchest")) {
			if (plugin.debug) {
				this.plugin.getLogger().info("Block at " + block.getLocation().toString() + " does not have deathchest metadata!");
			}
			return;
		}
		// if chunk containing death chest is not loaded, load it so items will drop
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load();
		}
		
		// remove block from death chest datastore
		removeDeathChestItem(block);
		
		// remove metadata from block
		block.removeMetadata("deathchest", plugin);
		
		// replace block with air, dropping chest contents but not block itself
		block.setType(Material.AIR);
	}

	
//	/**
//	 * List currently tracked deathchest items in hashmap, mainly for debugging purposes
//	 * @param sender
//	 */
//	public void listDeathChestItems(CommandSender sender) {
//		sender.sendMessage("Tracked Deathchests: " + this.deathchestitems.keySet().size());
//		for (Block block : deathchestitems.keySet()) {
//			BlockState blockstate = block.getState();
//			Location location = blockstate.getLocation();
//			Long expiretime = (deathchestitems.get(block) - System.currentTimeMillis()) / 60000;
//			sender.sendMessage("----------");
//			sender.sendMessage("Owner: " + (block.getMetadata("deathchest").get(0)).asString());
//			sender.sendMessage("Material: " + blockstate.getType().toString());
//			sender.sendMessage("Location: " + location.getWorld().getName() + " x: " + location.getBlockX() + " y: " + location.getBlockY() + " z: " + location.getBlockZ());
//			sender.sendMessage("Expires: " + expiretime + " minutes.");
//		}
//	}

	
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
				}
			}
		}

		// remove deathchest item from hashmap
		plugin.chestmanager.removeDeathChestItem(block);
		
		// remove deathchest metadata from block
		block.removeMetadata("deathchest", plugin);
		
		// destroy chest by setting to AIR, dropping contents but not block itself
		block.setType(Material.AIR);
		
		// if less than two chests have been detected,
		// check surrounding blocks for another chest
		if (iterations < 2) {
			
			BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
		
			for (BlockFace face : faces) {
				Block testblock = block.getRelative(face);
				if (testblock.getType().equals(Material.CHEST) &&
						testblock.hasMetadata("deathchest")) {
					lootChest(player,testblock,iterations);
				}
			}
		}
	}


	/**
	 * Attempt to deploy a death chest at player's death location containing dropped items
	 * @param player				Player that died
	 * @param dropped_items			Items player had in inventory on death
	 * @return List of ItemStack	items that didn't fit in chest
	 */
	public List<ItemStack> deployChest(Player player, List<ItemStack> dropped_items) {
		
		List<ItemStack> remaining_items = new ArrayList<ItemStack>();
		List<ItemStack> chest_items = new ArrayList<ItemStack>();
				
		// combine stacks of same items where possible
		chest_items = chestutilities.consolidateItems(dropped_items);

		// if require-chest option is enabled
		// and player does not have permission override
		if(plugin.getConfig().getBoolean("require-chest", false) &&
				!player.hasPermission("deathchest.freechest")) {

			// if player does not have a chest in their inventory
			// output message and return, allowing inventory items to drop on ground
			if (!chestutilities.hasChest(chest_items)) {
				plugin.messagemanager.sendPlayerMessage(player, "no-chest-in-inventory");
				return dropped_items;
			}
			// remove one chest from players inventory
			chest_items = chestutilities.removeOneChest(chest_items);

			// if only one chest required to hold all dropped items	
			// or player doesn't have doublechest permission
			// or player doesn't have a second chest in inventory
			if (chest_items.size() <= 27 ||
					!player.hasPermission("deathchest.doublechest") ||
					!chestutilities.hasChest(chest_items)) {
				if (plugin.debug) {
					plugin.getLogger().info("Deploying single chest...");
				}
				remaining_items = deploySingleChest(player, chest_items);
				return remaining_items;
			}
			// deploy double chest
			else {
				chest_items.remove(new ItemStack(Material.CHEST,1));
				if (plugin.debug) {
					plugin.getLogger().info("Deploying Double Chest...");
				}
				remaining_items = deployDoubleChest(player, chest_items);
				return remaining_items;
			}
		}
		// require-chest option not enabled or player has permission override
		else {
			// if only one chest required to hold all dropped items	
			// or player doesn't have doublechest permission
			if (chest_items.size() <= 27 ||
					!player.hasPermission("deathchest.doublechest")) {
				if (plugin.debug) {
					plugin.getLogger().info("Deploying Single Chest...");
				}
				remaining_items = deploySingleChest(player, chest_items);
				return remaining_items;
			}
			else {
				// deploy double chest
				if (plugin.debug) {
					plugin.getLogger().info("Deploying Double Chest...");
				}
				remaining_items = plugin.chestmanager.deployDoubleChest(player, chest_items);
				return remaining_items;
			}
		}
	}


	/**
	 * Deploy a single chest
	 * 
	 * @param player			player to deploy chest for
	 * @param dropped_items		items to place in chest
	 * @return Items that did not fit in chest, as List of ItemStacks
	 */
	@SuppressWarnings("deprecation")
	private List<ItemStack> deploySingleChest(Player player, List<ItemStack> dropped_items) {
		
		Location location = chestutilities.findValidSingleLocation(player);

		if (!chestutilities.validLocation(player,location)) {
			if (plugin.debug) {
				plugin.getLogger().info("Block at death location is not a replaceable block. Trying one block up...");
			}
			return dropped_items;
		}
		if (plugin.debug) {
			plugin.getLogger().info("Chest can be placed at death location.");
		}
		
		// actual chest creation
		Block block = location.getBlock();
		block.setType(Material.CHEST);
		BlockState state = block.getState();
		Chest chest = (Chest)state;
		chest.setRawData(chestutilities.getChestDirectionByte(player.getLocation().getYaw()));
		chest.update();
		
		// put items into chest, items that don't fit are put in remaining_items list
		List<ItemStack> remaining_items = new ArrayList<ItemStack>(dropped_items);
		int chestsize = chest.getInventory().getSize();
		int itemcount = 0;
		for (ItemStack item : dropped_items) {
			chest.getInventory().addItem(item);
			remaining_items.remove(item);
			itemcount++;
			if (itemcount >= chestsize) {
				break;
			}
		}
		if (plugin.debug) {
			plugin.getLogger().info("Remaining Items: " + remaining_items.size());
		}
		
		// set metadata to identify chest as a deathchest
		block.setMetadata("deathchest", new FixedMetadataValue(plugin, player.getUniqueId().toString()));

		// place sign on chest
		placeChestSign(player,block);

		// create DeathChestBlock object
		Long expiration = System.currentTimeMillis() + this.plugin.getConfig().getLong("expire-time", 60) * 60000;

		DeathChestBlock deathChestBlock = new DeathChestBlock();
		deathChestBlock.setOwnerUUID(player.getUniqueId());
		deathChestBlock.setLocation(block.getLocation());
		deathChestBlock.setExpiration(expiration);

		// put DeathChestBlock in datastore
		datastore.putRecord(deathChestBlock);
		
		// create expire task for deathChestBlock
		createItemExpireTask(deathChestBlock);

		// send success message to player
		plugin.messagemanager.sendPlayerMessage(player, "chest-success");
		
		// return list of items that did not fit in chest
		return remaining_items;
	}
	
	
	/**
	 * Deploy a double chest
	 * 
	 * @param player			Player to deploy chest for
	 * @param dropped_items		items to place in chest
	 * @return Any items that could not be placed in chest, as List of ItemStack
	 */
	@SuppressWarnings("deprecation")
	private List<ItemStack> deployDoubleChest(Player player, List<ItemStack> dropped_items) {
		
		Location location = chestutilities.findValidDoubleLocation(player);
		
		if (!chestutilities.validLocation(player,location)) {
			if (plugin.debug) {
				plugin.getLogger().info("Block at death location 1 is not a replaceable block.");
			}
			return dropped_items;
		}
		if (plugin.debug) {
			plugin.getLogger().info("Chest can be placed at death location 1.");
		}
		
		// actual chest creation
		Block block = location.getBlock();
		block.setType(Material.CHEST);
		BlockState state = block.getState();
		Chest chest = (Chest)state;
		chest.setRawData(chestutilities.getChestDirectionByte(location.getYaw()));
		chest.update();
		
		// put items into chest, items that don't fit are put in remaining_items list
		List<ItemStack> remaining_items = new ArrayList<ItemStack>(dropped_items);
		int chestsize = chest.getInventory().getSize();
		int itemcount = 0;
		for (ItemStack item : dropped_items) {
			chest.getInventory().addItem(item);
			remaining_items.remove(item);
			itemcount++;
			if (itemcount >= chestsize) {
				break;
			}
		}
		
		// set metadata to identify chest as a deathchest
		block.setMetadata("deathchest", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
	
		// place sign on chest
		placeChestSign(player,block);
	
		// create DeathChestBlock object
		Long expiration = System.currentTimeMillis() + this.plugin.getConfig().getLong("expire-time", 60) * 60000;
		DeathChestBlock deathChestBlock = new DeathChestBlock();
		deathChestBlock.setOwnerUUID(player.getUniqueId());
		deathChestBlock.setLocation(block.getLocation());
		deathChestBlock.setExpiration(expiration);
		
		// put deathChestBlock in datastore
		datastore.putRecord(deathChestBlock);
		
		// create expire task for deathChestBlock
		createItemExpireTask(deathChestBlock);

		// get block to the right of first chest
		block = chestutilities.blockToRight(location);
	
		// if not a valid location, try block to left of first chest
		if (!chestutilities.validLocation(player,block.getLocation())) {
			block = chestutilities.blockToLeft(location);
		}
		
		if (!chestutilities.validLocation(player,block.getLocation())) {
			if (plugin.debug) {
				plugin.getLogger().info("Block at second chest location is not a replaceable block.");
			}
			plugin.messagemanager.sendPlayerMessage(player, "doublechest-partial-success");
			return remaining_items;
		}
		if (plugin.debug) {
			plugin.getLogger().info("Second chest can be placed at death location.");
		}
		block.setType(Material.CHEST);
		state = block.getState();
		chest = (Chest)state;
		chest.setRawData(chestutilities.getChestDirectionByte(location.getYaw()));
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
	
		// set metadata to identify chest as a deathchest
		block.setMetadata("deathchest", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
	
		// create DeathChestBlock object
		DeathChestBlock deathChestBlock2 = new DeathChestBlock();
		deathChestBlock2.setOwnerUUID(player.getUniqueId());
		deathChestBlock2.setLocation(block.getLocation());
		deathChestBlock2.setExpiration(expiration);
		
		// insert deathChestBlock in datastore
		datastore.putRecord(deathChestBlock2);
		
		// create expire task for deathChestBlock
		createItemExpireTask(deathChestBlock2);

		// send success message to player
		plugin.messagemanager.sendPlayerMessage(player, "chest-success");
		
		// return list of items that did not fit in chest
		return remaining_items2;
	}


	
	/**
	 * Place sign on chest
	 * @param player	Chest owner
	 * @param block		Chest block
	 * @return boolean	Success or fail to place sign
	 */
	
	@SuppressWarnings("deprecation")
	private boolean placeChestSign(Player player, Block block) {
		if (!plugin.getConfig().getBoolean("chest-signs")) {
			return false;
		}
		float yaw = player.getLocation().getYaw();
		Block signblock = block.getRelative(chestutilities.getChestDirectionFace(yaw));
		if (chestutilities.validLocation(player,signblock.getLocation())) {
			signblock.setType(Material.WALL_SIGN);
			signblock.setData(chestutilities.getChestDirectionByte(yaw));
		}
		else {
			signblock = block.getRelative(BlockFace.UP);
			if (chestutilities.validLocation(player,signblock.getLocation())) {
				signblock.setType(Material.SIGN_POST);
				signblock.setData(chestutilities.getSignPostDirectionByte(yaw));
			}
			else {
				if (plugin.debug) {
					plugin.getLogger().info("Could not place sign on chest.");
				}
				return false;
			}
		}
		signblock.setMetadata("deathchest", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
		
		// create DeathChestBlock object for sign
		Long expiration = System.currentTimeMillis() + this.plugin.getConfig().getLong("expire-time", 60) * 60000;
		DeathChestBlock deathChestBlock = new DeathChestBlock();
		deathChestBlock.setOwnerUUID(player.getUniqueId());
		deathChestBlock.setLocation(signblock.getLocation());
		deathChestBlock.setExpiration(expiration);
		
		// insert deathChestBlock in datastore
		datastore.putRecord(deathChestBlock);
		
		// create expire task for deathChestBlock
		createItemExpireTask(deathChestBlock);

		// create actual sign with player name and death date
		Sign sign = (Sign)signblock.getState();
		String datestring = new SimpleDateFormat("MMM d, yyyy").format(System.currentTimeMillis());
		sign.setLine(0, ChatColor.BOLD + "R.I.P.");
		sign.setLine(1, ChatColor.RESET + player.getName());
		sign.setLine(3, "D: " + datestring);
		sign.update();
		return true;
	}
	
	/**
	 * Assign protected items to hash set
	 */
	private void getProtectedItemsList() {
		protecteditems.add(Material.CHEST);
		protecteditems.add(Material.WALL_SIGN);
		protecteditems.add(Material.SIGN_POST);
	}

	public void close() {
		datastore.close();
	}
	
}

