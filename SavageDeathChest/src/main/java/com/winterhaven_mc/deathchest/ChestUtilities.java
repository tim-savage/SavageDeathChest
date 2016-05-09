package com.winterhaven_mc.deathchest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ChestUtilities {
	
	// reference to main class
    private final PluginMain plugin;
    
    
    /**
     * Class constructor
     * @param plugin
     */
    public ChestUtilities(final PluginMain plugin) {
    	
    	// set reference to main class
        this.plugin = plugin;
    }


    /**
     * Get the cardinal compass direction.<br>
     * Converts direction in degrees to BlockFace cardinal direction (N,E,S,W)
     * 
     * @param yaw	Direction in degrees
     * @return BlockFace of cardinal direction
     */
    public BlockFace getCardinalDirection(final float yaw) {
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
	 * @param itemlist	Collection of itemstacks to combine
	 * @return List of ItemStack with same materials combined
	 */
	public List<ItemStack> consolidateItems(final Collection<ItemStack> itemlist) {

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
	 * @param list Collection of ItemStack to check for chest
	 * @return boolean
	 */
	public boolean hasChest(final Collection<ItemStack> list) {
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
	public Location getLocationToLeft(final Location location) {
		
		float yaw = location.getYaw() + 90;

		// clone location so original isn't modified
		Location resultLocation = location.clone();

		// get location that is one block to left of current location
		resultLocation = resultLocation.getBlock().getRelative(getCardinalDirection(yaw)).getLocation();
		
		// set result location yaw to match original
		resultLocation.setYaw(location.getYaw());
		return resultLocation;
	}


	/**
	 * Get location to right of location based on yaw
	 * @param location initial location
	 * @return location one block to right
	 */
	public Location locationToRight(final Location location) {
		
		float yaw = location.getYaw() - 90;
		
		// clone location so original isn't modified
		Location resultLocation = location.clone();
		
		// get location that is one block to right of current location
		resultLocation = resultLocation.getBlock().getRelative(getCardinalDirection(yaw)).getLocation();

		// set new location yaw to match original
		resultLocation.setYaw(location.getYaw());
		return resultLocation;
	}


	/**
	 * Get location in front of location based on yaw
	 * @param location initial location
	 * @return location one block to right
	 */
	public Location locationToFront(final Location location) {
		
		float yaw = location.getYaw();
		
		// clone location so original isn't modified
		Location resultLocation = location.clone();
		
		// get location that is one block in front of current location
		resultLocation = resultLocation.getBlock().getRelative(getCardinalDirection(yaw)).getLocation();

		// set new location yaw to match original
		resultLocation.setYaw(location.getYaw());
		return resultLocation;
	}


	/**
	 * Get block to left of location based on yaw
	 * @param location initial location
	 * @return block to left of location
	 */
	public Block blockToLeft(final Location location) {
		float yaw = location.getYaw() + 90;
		return location.getBlock().getRelative(getCardinalDirection(yaw));
	}


	/**
	 * Get block to right of location based on yaw
	 * @param location inital location
	 * @return block to right of initial location
	 */
	public Block blockToRight(final Location location) {
		float yaw = location.getYaw() - 90;
		return location.getBlock().getRelative(getCardinalDirection(yaw));
	}


	/**
	 * Get block in front of location based on yaw
	 * @param location initial location
	 * @return block in front of initial location
	 */
	public Block blockInFront(final Location location) {
		float yaw = location.getYaw() + 180;
		return location.getBlock().getRelative(getCardinalDirection(yaw));
	}

	
	/**
	 * Get block to rear of location based on yaw
	 * @param location initial location
	 * @return block behind inital location
	 */
	public Block blockToRear(final Location location) {
		float yaw = location.getYaw();
		return location.getBlock().getRelative(getCardinalDirection(yaw));
	}


	/**
	 * Search for a valid location to place a single chest,
	 * taking into account replaceable blocks, grass path blocks and 
	 * restrictions from other block protection plugins if configured
	 * @param player Player that deathchest is being deployed for
	 * @return SearchResult
	 */
	public SearchResult findValidSingleChestLocation(final Player player) {

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
		
		// initialize search result object
		SearchResult result = null;

		// iterate over all locations with search distance until a valid location is found
		for (int y = 0; y < radius; y = y + 1) {
			for (int x = 0; x < radius; x = x + 1) {
				for (int z = 0; z < radius; z = z + 1) {
					
					// set new test location
					testLocation.add(x,y,z);
					
					// get result for test location
					result = isValidLeftChestLocation(player,testLocation);
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
					result = isValidLeftChestLocation(player,testLocation);
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
					result = isValidLeftChestLocation(player,testLocation);
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
					result = isValidLeftChestLocation(player,testLocation);
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
		// no valid location could be found for a single chest, so return null
		if (plugin.debug) {
			plugin.getLogger().info("Locations tested: " + testCount);
		}
		return result;
	}

	/**
	 * Search for valid location to place a double chest, 
	 * taking into account replaceable blocks, as well as 
	 * protection plugin regions if configured
	 * and adjacent existing chests
	 * @param player Player that deathchest is being deployed for
	 * @return location that is valid for double chest deployment, or null if valid location cannot be found
	 */
	public SearchResult findValidDoubleChestLocation(final Player player) {
	
		// count number of tests performed, for debugging purposes
		int testCount = 0;
		
		// get search distance from config
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
		
		// initialize search result objects
		SearchResult result1 = null;
		SearchResult result2 = null;

		for (int y = 0; y < radius; y = y + 1) {
			for (int x = 0; x < radius; x = x + 1) {
				for (int z = 0; z < radius; z = z + 1) {
					
					// set new test location
					testLocation.add(x,y,z);
					
					// get result for test location and adjacent location for second chest
					result1 = isValidLeftChestLocation(player,testLocation);
					result2 = isValidRightChestLocation(player,locationToRight(testLocation));
					testCount = testCount + 2;
					
					// if both results are valid, return test result object
					if (result1.equals(SearchResult.SUCCESS) &&
							result2.equals(SearchResult.SUCCESS)) {
						if (plugin.debug) {
							plugin.getLogger().info("Locations tested: " + testCount);
						}
						return result1;
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
					testLocation.add(-x,y,-z);
					
					// get result for test location and adjacent location for second chest
					result1 = isValidLeftChestLocation(player,testLocation);
					result2 = isValidRightChestLocation(player,locationToRight(testLocation));
					testCount = testCount + 2;

					// if both test locations are valid, return search result object
					if (result1.equals(SearchResult.SUCCESS) &&
							result2.equals(SearchResult.SUCCESS)) {
						if (plugin.debug) {
							plugin.getLogger().info("Locations tested: " + testCount);
						}
						return result1;
					}
					else {
						// reset test location
						testLocation.add(x,-y,z);
					}
					
					// locations 0,y,z and x,y,0 had already been checked, so skip ahead
					if (x == 0 || z == 0) {
						continue;
					}
					
					// set new test location
					testLocation.add(-x,y,z);
					
					// get result for test location and adjacent location for second chest
					result1 = isValidLeftChestLocation(player,testLocation);
					result2 = isValidRightChestLocation(player,locationToRight(testLocation));
					testCount = testCount + 2;

					// if both test locations are valid, return search result object
					if (result1.equals(SearchResult.SUCCESS) &&
							result2.equals(SearchResult.SUCCESS)) {
						if (plugin.debug) {
							plugin.getLogger().info("Locations tested: " + testCount);
						}
						return result1;
					}
					else {
						// reset test location
						testLocation.add(x,-y,-z);
					}
					
					// set new test location
					testLocation.add(x,y,-z);
					
					// get result for test location and adjacent location for second chest
					result1 = isValidLeftChestLocation(player,testLocation);
					result2 = isValidRightChestLocation(player,locationToRight(testLocation));
					testCount = testCount + 2;

					// if both test locations are valid, return search result object
					if (result1.equals(SearchResult.SUCCESS) &&
							result2.equals(SearchResult.SUCCESS)) {
						if (plugin.debug) {
							plugin.getLogger().info("Locations tested: " + testCount);
						}
						return result1;
					}
					else {
						// reset test location
						testLocation.add(-x,-y,z);
					}
				}
			}
		}
		if (plugin.debug) {
			plugin.getLogger().info("Locations tested: " + testCount);
		}
		return result1;
	}


	/** Check if sign can be placed at location
	 * 
	 * @param player	Player to check permissions
	 * @param location	Location to check permissions
	 * @return boolean
	 */
	public boolean isValidSignLocation(final Player player, final Location location) {

		Block block = location.getBlock();

		// check if block at location is a ReplaceableBlock
		if (!plugin.chestManager.getReplaceableBlocks().contains(block.getType())) {
			return false;
		}
		
		// check all enabled protection plugins for player permission at location
		ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestPlacement(player, block);
		if (blockingPlugin != null) {
			return false;
		}
		return true;
	}


	/** Check if single chest can be placed at location
	 * 
	 * @param player	Player to check permissions
	 * @param location	Location to check permissions
	 * @return boolean
	 */
	public SearchResult isValidLeftChestLocation(final Player player, final Location location) {

		Block block = location.getBlock();
		SearchResult result = null;

		// check if block at location is a ReplaceableBlock
		if(!plugin.chestManager.getReplaceableBlocks().contains(block.getType())) {
			return SearchResult.NON_REPLACEABLE_BLOCK;
		}
		// check if location is adjacent to an existing chest
		if (adjacentChest(location,true)) {
			return SearchResult.ADJACENT_CHEST;
		}
		
		// check if chest or sign would be above grass path
		if (isAboveGrassPath(block)
				|| isAboveGrassPath(blockToRear(location))) {
			return SearchResult.ABOVE_GRASS_PATH;
		}

		// check all enabled protection plugins for player permission at location
		ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestPlacement(player, block);
		if (blockingPlugin != null) {
			result = SearchResult.PROTECTION_PLUGIN;
			result.setProtectionPlugin(blockingPlugin);
			return result;
		}
		result = SearchResult.SUCCESS;
		result.setLocation(location.clone());
		return result;
	}


	/** Check if second of double chest can be placed at location
	 * 
	 * @param player	Player to check permissions
	 * @param location	Location to check permissions
	 * @return boolean
	 */
	public SearchResult isValidRightChestLocation(final Player player, final Location location) {

		Block block = location.getBlock();
		SearchResult result = null;
		
		// check if block at location is a ReplaceableBlock
		if(!plugin.chestManager.getReplaceableBlocks().contains(block.getType())) {
			return SearchResult.NON_REPLACEABLE_BLOCK;
		}
		
		// check if location is adjacent to an existing chest, ignoring left chest location
		if (adjacentChest(location,false)) {
			return SearchResult.ADJACENT_CHEST;
		}

		// check if block is above grass path
		if (isAboveGrassPath(block)) {
			return SearchResult.ABOVE_GRASS_PATH;
		}
		
		// check all enabled protection plugins for player permission at location
		ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestPlacement(player, block);
		if (blockingPlugin != null) {
			result = SearchResult.PROTECTION_PLUGIN;
			result.setProtectionPlugin(blockingPlugin);
			return result;
		}
		// don't need to set location in search result, only left chest location is used
		return SearchResult.SUCCESS;
	}


	boolean adjacentChest(final Location location, final Boolean firstChest) {

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
	

	boolean isAboveGrassPath(final Block block) {
		
		if (block.getRelative(0, -1, 0).getType().equals(Material.GRASS_PATH)) {
			return true;
		}
		return false;
	}

}

