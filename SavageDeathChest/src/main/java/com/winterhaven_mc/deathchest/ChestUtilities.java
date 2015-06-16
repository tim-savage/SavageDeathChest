package com.winterhaven_mc.deathchest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ChestUtilities {
	
    final PluginMain plugin;
    
    public ChestUtilities(PluginMain plugin) {
        this.plugin = plugin;
        
    }


    /**
     * Get the cardinal compass direction.<br>
     * Converts direction in degrees to BlockFace direction (N,E,S,W)
     * 
     * @param yaw	Direction in degrees
     * @return BlockFace of cardinal direction
     */
    public BlockFace getDirection(float yaw) {
    	double rot = yaw % 360;
    	if (rot < 0) {
    		rot += 360.0;
    	}
    	if (45 <= rot && rot < 135) {
    		return BlockFace.EAST;
    	}
    	else if (135 <= rot && rot < 225) {
    		return BlockFace.SOUTH;
    	}
    	else if (225 <= rot && rot < 315) {
    		return BlockFace.WEST;
    	}
    	else {
    		return BlockFace.NORTH;
    	}
    }

	
	/**
	 * Combine item stacks of same material up to max stack size
	 * @param itemlist	List of itemstacks to combine
	 * @return List of ItemStack with same materials combined
	 */
	public List<ItemStack> consolidateItems(List<ItemStack> itemlist) {

		List<ItemStack> returnlist = new ArrayList<ItemStack>();
		
		for (ItemStack itemstack : itemlist) {
			if (itemstack == null) {
				continue;
			}
			
			for (ItemStack checkstack : returnlist) {
				if (checkstack == null) {
					continue;
				}
				if (checkstack.isSimilar(itemstack)) {
					int transfer = Math.min(itemstack.getAmount(),checkstack.getMaxStackSize() - checkstack.getAmount());
					itemstack.setAmount(itemstack.getAmount() - transfer);
					checkstack.setAmount(checkstack.getAmount()	+ transfer);
				}
			}
			if (itemstack.getAmount() > 0) {
				returnlist.add(itemstack);
			}
		}
		if (plugin.debug) {
			plugin.getLogger().info("There are " + returnlist.size() + " consolidated item stacks.");
		}
		return returnlist;
	}

	
	/**
	 * Remove one chest from list of item stacks
	 * @param list	List of itemstacks to remove chest
	 * @return List of itemstacks with one chest removed
	 */
	public List<ItemStack> removeOneChest(List<ItemStack> list) {
		
		for (ItemStack stack : list) {
			if (stack.isSimilar(new ItemStack(Material.CHEST))) {
				list.remove(stack);
				stack.setAmount(stack.getAmount() - 1);
				if (stack.getAmount() > 0) {
					list.add(stack);
				}
			break;
			}
		}
		return list;
	}


	/**
	 * Check if list of item stacks contains at least one chest
	 * @param list List of itemstacks to check for chest
	 * @return boolean
	 */
	public boolean hasChest(List<ItemStack> list) {
		boolean haschest = false;
		for (ItemStack item : list) {
			if (item.getType().equals(Material.CHEST)) {
				haschest = true;
				break;
			}
		}
		return haschest;	
	}


	/**
	 * Get location to left of location based on yaw
	 * @param location initial location
	 * @return location one block to left
	 */
	public Location locationToLeft(Location location) {
		float yaw = location.getYaw() + 90;
		Location resultLocation = location.getBlock().getRelative(getDirection(yaw)).getLocation();
		
		// set new location yaw to match original
		resultLocation.setYaw(location.getYaw());
		return resultLocation;
	}


	/**
	 * Get location to right of location based on yaw
	 * @param location initial location
	 * @return location one block to right
	 */
	public Location locationToRight(Location location) {
		float yaw = location.getYaw() - 90;
		Location resultLocation = location.getBlock().getRelative(getDirection(yaw)).getLocation();

		// set new location yaw to match original
		resultLocation.setYaw(location.getYaw());
		return resultLocation;
	}


	/**
	 * Get block to left of location based on yaw
	 * @param location initial location
	 * @return block to left of location
	 */
	public Block blockToLeft(Location location) {
		float yaw = location.getYaw() + 90;
		return location.getBlock().getRelative(getDirection(yaw));
	}


	/**
	 * Get block to right of location based on yaw
	 * @param location inital location
	 * @return block to right of initial location
	 */
	public Block blockToRight(Location location) {
		float yaw = location.getYaw() - 90;
		return location.getBlock().getRelative(getDirection(yaw));
	}


	/**
	 * Get block in front of location based on yaw
	 * @param location initial location
	 * @return block in front of initial location
	 */
	public Block blockInFront(Location location) {
		float yaw = location.getYaw() + 180;
		return location.getBlock().getRelative(getDirection(yaw));
	}

	
	/**
	 * Get block to rear of location based on yaw
	 * @param location initial location
	 * @return block behind inital location
	 */
	public Block blockToRear(Location location) {
		float yaw = location.getYaw();
		return location.getBlock().getRelative(getDirection(yaw));
	}


	/**
	 * Search for a valid location to place a single chest,
	 * taking into account replaceable blocks, as well as 
	 * WorldGuard regions and GriefPrevention claims if configured
	 * @param player Player that deathchest is being deployed for
	 * @return location that is valid for a single chest, or null if valid location cannot be found
	 */
	public Location findValidSingleChestLocation(Player player) {

		Location origin = player.getLocation();
		
		int radius = plugin.getConfig().getInt("search-distance");

		int ox = origin.getBlockX();
		int oy = origin.getBlockY();
		int oz = origin.getBlockZ();
		float oyaw = origin.getYaw();
		float opitch = origin.getPitch();
		World world = origin.getWorld();

		Location location = new Location(world,ox,oy,oz,oyaw,opitch);
		
		for (int y = 0; y < radius; y++) {
			for (int x = 0; x < radius; x++) {
				for (int z = 0; z < radius; z++) {
					location = new Location(world,ox+x,oy+y,oz+z,oyaw,opitch);
					if (isValidSingleLocation(player,location)) {
						return location;
					}
					if (x == 0 && z == 0) {
						continue;
					}
					location = new Location(world,ox-x,oy+y,oz+z,oyaw,opitch);
					if (isValidSingleLocation(player,location)) {
						return location;
					}
					location = new Location(world,ox-x,oy+y,oz-z,oyaw,opitch);
					if (isValidSingleLocation(player,location)) {
						return location;
					}
					location = new Location(world,ox+x,oy+y,oz-z,oyaw,opitch);
					if (isValidSingleLocation(player,location)) {
						return location;
					}
				}
			}
		}
		// no valid location could be found for a single chest, so return null
		return null;
	}

	/**
	 * Search for valid location to place a double chest, 
	 * taking into account replaceable blocks, as well as 
	 * WorldGuard regions and GriefPrevention claims if configured
	 * and adjacent existing chests
	 * @param player Player that deathchest is being deployed for
	 * @return location that is valid for double chest deployment, or null if valid location cannot be found
	 */
	public Location findValidDoubleChestLocation(Player player) {
	
		Location origin = player.getLocation();
		
		int radius = plugin.getConfig().getInt("search-distance");
	
		int ox = origin.getBlockX();
		int oy = origin.getBlockY();
		int oz = origin.getBlockZ();
		float oyaw = origin.getYaw();
		float opitch = origin.getPitch();
		World world = origin.getWorld();
	
		Location location = new Location(world,ox,oy,oz,oyaw,opitch);
		
		for (int y = 0; y < radius; y++) {
			for (int x = 0; x < radius; x++) {
				for (int z = 0; z < radius; z++) {
					location = new Location(world,ox+x,oy+y,oz+z,oyaw,opitch);
					if (isValidSingleLocation(player,location) &&
							isValidSingleLocation(player,locationToRight(location))) {
						return location;
					}
					if (x == 0 && z == 0) {
						continue;
					}
					location = new Location(world,ox-x,oy+y,oz-z,oyaw,opitch);
					if (isValidSingleLocation(player,location) &&
							isValidSingleLocation(player,locationToRight(location))) {
						return location;
					}
					if (x == 0 || z == 0) {
						continue;
					}
					location = new Location(world,ox-x,oy+y,oz+z,oyaw,opitch);
					if (isValidSingleLocation(player,location) &&
							isValidSingleLocation(player,locationToRight(location))) {
						return location;
					}
					location = new Location(world,ox+x,oy+y,oz-z,oyaw,opitch);
					if (isValidSingleLocation(player,location) &&
							isValidSingleLocation(player,locationToRight(location))) {
						return location;
					}
				}
			}
		}
		return null;
	}


	/** Check if sign can be placed at location
     * 
     * @param player	Player to check permissions
     * @param location	Location to check permissions
     * @return boolean
     */
    public boolean isValidSignLocation(Player player, Location location) {
    	
    	Block block = location.getBlock();
    	
    	// check if block at location is a ReplaceableBlock
    	if(!plugin.chestManager.getReplaceableBlocks().contains(block.getType())) {
    		return false;
    	}
    	
    	// check all enabled protection plugins for player permission at location
    	for (ProtectionPlugin pp : ProtectionPlugin.values()) {    		
    		if (!pp.hasPermission(player, location)) {
    			if (plugin.debug) {
    				plugin.getLogger().info("DeathChest sign prevented by " + pp.getName());
    			}
    			return false;
    		}
    	}
    	return true;
    }
    
    
    /** Check if single chest can be placed at location
     * 
     * @param player	Player to check permissions
     * @param location	Location to check permissions
     * @return boolean
     */
    public boolean isValidSingleLocation(Player player, Location location) {
    	
    	Block block = location.getBlock();
    	
    	// check if block at location is a ReplaceableBlock
    	if(!plugin.chestManager.getReplaceableBlocks().contains(block.getType())) {
    		return false;
    	}
    	// check if location is adjacent to an existing chest
    	if (adjacentChest(location,true)) {
    		return false;
    	}
    	// check all enabled protection plugins for player permission at location
    	for (ProtectionPlugin pp : ProtectionPlugin.values()) {    		
    		if (!pp.hasPermission(player, location)) {
    			if (plugin.debug) {
    				plugin.getLogger().info("DeathChest prevented by " + pp.getName());
    			}
    			return false;
    		}
    	}
    	return true;
    }
    
    /** Check if second of double chest can be placed at location
     * 
     * @param player	Player to check permissions
     * @param location	Location to check permissions
     * @return boolean
     */
    public boolean isValidDoubleLocation(Player player, Location location) {
    	
    	Block block = location.getBlock();
    	
    	// check if block at location is a ReplaceableBlock
    	if(!plugin.chestManager.getReplaceableBlocks().contains(block.getType())) {
    		return false;
    	}
    	// check if location is adjacent to an existing chest, ignoring first placed chest
    	if (adjacentChest(location,false)) {
    		return false;
    	}
    	// check all enabled protection plugins for player permission at location
    	for (ProtectionPlugin pp : ProtectionPlugin.values()) {    		
    		if (!pp.hasPermission(player, location)) {
    			if (plugin.debug) {
    				plugin.getLogger().info("DeathChest prevented by " + pp.getName());
    			}
    			return false;
    		}
    	}
    	return true;
    }
    
    boolean adjacentChest(Location location, Boolean firstChest) {
    	
    	if (firstChest) {
    		if (blockToLeft(location).getType().equals(Material.CHEST)) {
    			return true;
    		}
    	}
    	if (blockToRight(location).getType().equals(Material.CHEST)) {
    		return true;
    	}
    	if (blockInFront(location).getType().equals(Material.CHEST)) {
    		return true;
    	}
    	if (blockToRear(location).getType().equals(Material.CHEST)) {
    		return true;
    	}
    	return false;
    }

}

