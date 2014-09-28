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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

public class ChestManager {

	private DeathChestMain plugin;

	// instantiate ConfigAccessor class
	private ConfigAccessor deathchestsfile;

	// instantiate chest utilities class
	private ChestUtilities chestutilities;
	
	// protected item materials
	private Set<Material> protecteditems = new HashSet<Material>();
	
	// materials that can be replaced by chests
	private HashSet<Material> replaceableblocks = new HashSet<Material>();
	
	//  hashmap of death chests indexed by location, storing their deployed time as systime
	private ConcurrentHashMap<Block, Long> deathchestitems = new ConcurrentHashMap<Block, Long>();

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
        
		// instantiate ConfigAccessor for saved chests
		deathchestsfile = new ConfigAccessor(plugin, "deathchests.yml");
		
		// load material types that chests can replace from config file
		loadReplaceableBlocks();
		
		// get protected items (chests and signs)
		getProtectedItemsList();
		
		// load deathchests from save file into hashmap
		loadDeathChestItems();
		
		scheduleExpiredChests();
	}


	/**
	 * Iterate through all items in deathchest hashmap and expire items who's expire time is past
	 * and set a task to expire the the rest at the appropriate time
	 */
	private void scheduleExpiredChests() {
		Long currenttime = System.currentTimeMillis();
		for (Block block : deathchestitems.keySet()) {
			Long itemexpiretime  = deathchestitems.get(block);
			if (itemexpiretime < currenttime) {
				this.expireDeathChestItem(block);
				continue;
			}
			createItemExpireTask(block);
		}
	}


	/**
	 * Create task to expire deathchest item at appropriate time in the future
	 * @param block
	 */
	private void createItemExpireTask(final Block block) {
		Long currenttime = System.currentTimeMillis();
		Long expiretime = this.deathchestitems.get((Object)block);
		Long ticks_remaining = (expiretime - currenttime) / 50;
		if (ticks_remaining < 1) {
			ticks_remaining = (long) 1;
		}
		if (plugin.debug) {
			plugin.getLogger().info("Scheduling item to expire in " + ticks_remaining + " ticks.");
		}
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

			@Override
			public void run() {
				expireDeathChestItem(block);
			}
		}, ticks_remaining);
	}

	private void expireDeathChestItem(Block block) {
		Player player;
		Player[] arrplayer;

		// if block is not in deathchest item hashmap, do nothing and return
		if (!deathchestitems.containsKey(block)) {
			if (plugin.debug) {
				plugin.getLogger().info("Item is no longer in hashmap. It was probably retrieved by player.");
			}
			return;
		}
		// remove block from deathchest item hashmap
		deathchestitems.remove(block);
		
		// if block does not have deathchest metadata, do nothing and return
		if (!block.hasMetadata("deathchest")) {
			if (plugin.debug) {
				plugin.getLogger().info("Item to expire does not have deathchest metadata.");
			}
			return;
		}
		
		// remove deathchest item from persistent storage file
		final String playeruuid = block.getMetadata("deathchest").get(0).asString();
		deathchestsfile.getConfig().set(playeruuid + "." + chestutilities.makeKey(block.getState()), null);
		
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

	public Long getDeathChestItem(Block block) {
		return deathchestitems.get(block);
	}

	private void putDeathChestItem(Block block) {
		
		if (!protecteditems.contains((Object)block.getType())) {
			if (plugin.debug) {
				this.plugin.getLogger().info("Block was not a deathchest item, so not inserted into hash set.");
			}
			return;
		}
		
		Long expiretime = System.currentTimeMillis() + this.plugin.getConfig().getLong("expire-time", 60) * 60000;
		deathchestitems.put(block, expiretime);

		if (plugin.debug) {
			plugin.getLogger().info("Deathchest item inserted into hash set. Expire time: " + expiretime);
		}
		createItemExpireTask(block);
	}

	public void removeDeathChestItem(Block block) {
		if (!deathchestitems.containsKey(block)) {
			return;
		}
		deathchestitems.remove(block);
		if (plugin.debug) {
			plugin.getLogger().info("Deathchest item removed from hash set.");
		}
	}


	/** Load deathchest locations from file into private deathchestitems hash set
	 *  currently only used in chestmanager class constructor, but could be made public
	 */
	private void loadDeathChestItems() {
		// get player uuids from save file
		for (String playeruuid : deathchestsfile.getConfig().getKeys(false)) {
			// get player chest record keys from save file
			Set<String> keys = deathchestsfile.getConfig().getConfigurationSection(playeruuid).getKeys(false);
			// remove player uuid from save file if there are no records
			if (keys.isEmpty()) {
				deathchestsfile.getConfig().set(playeruuid, null);
				if (plugin.debug) {
					plugin.getLogger().info("Configuration section " + playeruuid + " was removed because it had no entries.");
				}
				continue;
			}
			// get death chest item expire times from save file
			for (String key : keys) {
				plugin.getLogger().info("Loading key: " + key);
				String[] temp = key.split("\\|");
				String worldname = temp[0];
				Double locX = Double.parseDouble(temp[1]);
				Double locY = Double.parseDouble(temp[2]);
				Double locZ = Double.parseDouble(temp[3]);
				Long expiretime = deathchestsfile.getConfig().getLong(playeruuid + "." + key);
				World world = Bukkit.getWorld(worldname);
				Location location = new Location(world,locX,locY,locZ);
				Block block = location.getBlock();
				// remove record if block is not a death chest item
				if (!protecteditems.contains(block.getType())) {
					plugin.getLogger().info("Block at loaded location is not a chest or sign. Removed from save file object.");
					deathchestsfile.getConfig().set(playeruuid + "." + key, null);
				}
				else {
					// put record into hashmap
					deathchestitems.put(block, expiretime);
					// set metadata on death chest item blocks
					block.setMetadata("deathchest", new FixedMetadataValue(plugin, playeruuid));
					if (plugin.debug) {
						plugin.getLogger().info("Inserted deathchest item in hash set.");
						plugin.getLogger().info("Material: " + block.getType().toString());
						plugin.getLogger().info("World: " + worldname);
						plugin.getLogger().info("X: " + locX);
						plugin.getLogger().info("Y:" + locY);
						plugin.getLogger().info("Z: " + locZ);
						plugin.getLogger().info("expire time: " + expiretime);
					}
				}
			}
		}
		// write updated save file object to disk
		deathchestsfile.saveConfig();
	}

	/** Save deathchest locations from deathchestitems hash set to file
	 *
	 * called by onDisable() in main
	 * 
	 */
	public void saveDeathChestItems() {
		for (Block block : deathchestitems.keySet()) {
			BlockState blockstate = block.getState();
			String playerID = blockstate.getMetadata("deathchest").get(0).asString();
			String key = chestutilities.makeKey(blockstate);
			if (!protecteditems.contains(block.getType())) {
				deathchestsfile.getConfig().set(key, null);
				if (plugin.debug) {
					plugin.getLogger().info("Block is not a deathchest item. Removed from save file object.");
				}
				continue;
			}
			Long expiretime = deathchestitems.get(block);
			if (expiretime != null) {
				deathchestsfile.getConfig().set(playerID + "." + key, expiretime);
			}
		}
		// write updated save file object to disk
		deathchestsfile.saveConfig();
		plugin.getLogger().info("Saved deathchests to file.");
		return;
	}

	
	/**
	 * Remove deathchest item from hashmap, remove metadata, and delete from world, dropping items
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
		
		// remove block from death chest hashmap
		removeDeathChestItem(block);
		
		// remove metadata from block
		block.removeMetadata("deathchest", plugin);
		
		// replace block with air, dropping chest contents but not block itself
		block.setType(Material.AIR);
	}

	
	/**
	 * List currently tracked deathchest items in hashmap, mainly for debugging purposes
	 * @param sender
	 */
	public void listDeathChestItems(CommandSender sender) {
		sender.sendMessage("Tracked Deathchests: " + this.deathchestitems.keySet().size());
		for (Block block : deathchestitems.keySet()) {
			BlockState blockstate = block.getState();
			Location location = blockstate.getLocation();
			Long expiretime = (deathchestitems.get(block) - System.currentTimeMillis()) / 60000;
			sender.sendMessage("----------");
			sender.sendMessage("Owner: " + (block.getMetadata("deathchest").get(0)).asString());
			sender.sendMessage("Material: " + blockstate.getType().toString());
			sender.sendMessage("Location: " + location.getWorld().getName() + " x: " + location.getBlockX() + " y: " + location.getBlockY() + " z: " + location.getBlockZ());
			sender.sendMessage("Expires: " + expiretime + " minutes.");
		}
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
			if (plugin.debug) {
				plugin.getLogger().info(itemcount + ": " + item.toString());
			}
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

		// put chest in deathchest hashmap
		putDeathChestItem(block);
		plugin.messagemanager.sendPlayerMessage(player, "chest-success");
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
	
		// put chest in deathchest hashmap
		putDeathChestItem(block);
		
		
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
	
		// put chest in deathchest hashmap
		putDeathChestItem(block);
		plugin.messagemanager.sendPlayerMessage(player, "chest-success");
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
		putDeathChestItem(signblock);
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

}

