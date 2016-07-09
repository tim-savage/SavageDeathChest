package com.winterhaven_mc.deathchest.util;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;
import com.winterhaven_mc.deathchest.SearchResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public final class LocationUtilities {

	// reference to main class
	private static final PluginMain plugin = PluginMain.instance;

	// material types that can be replaced by death chests
	private static Set<Material> replaceableBlocks = new HashSet<>(loadReplaceableBlocks());


	/**
	 * Private constructor to prevent instantiation this class
	 */
	private LocationUtilities() {
		throw new AssertionError();
	}


	/**
	 * Get the cardinal compass direction.<br>
	 * Converts direction in degrees to BlockFace cardinal direction (N,E,S,W)
	 * 
	 * @param yaw	Direction in degrees
	 * @return BlockFace of cardinal direction
	 */
	public static BlockFace getCardinalDirection(final float yaw) {

		// ensure yaw is between 0 and 360 (in case of negative yaw)
		double rotation = (yaw + 360) % 360;

		if (45 <= rotation && rotation < 135) {
			return BlockFace.EAST;
		}
		else if (135 <= rotation && rotation < 225) {
			return BlockFace.SOUTH;
		}
		else if (225 <= rotation && rotation < 315) {
			return BlockFace.WEST;
		}
		else {
			return BlockFace.NORTH;
		}
	}


	/**
	 * Get location to right of location based on yaw
	 * @param location initial location
	 * @return location one block to right, preserving original yaw
	 */
	public static Location locationToRight(final Location location) {

		Location resultLocation = blockToRight(location).getLocation();

		// set new location yaw to match original
		resultLocation.setYaw(location.getYaw());
		return resultLocation;
	}


	/**
	 * Get block to left of location based on yaw
	 * @param location initial location
	 * @return block to left of location
	 */
	public static Block blockToLeft(final Location location) {
		float yaw = location.getYaw() + 90;
		return location.getBlock().getRelative(getCardinalDirection(yaw));
	}


	/**
	 * Get block to right of location based on yaw
	 * @param location inital location
	 * @return block to right of initial location
	 */
	public static Block blockToRight(final Location location) {
		float yaw = location.getYaw() - 90;
		return location.getBlock().getRelative(getCardinalDirection(yaw));
	}


	/**
	 * Get block in front of location based on yaw
	 * @param location initial location
	 * @return block in front of initial location
	 */
	public static Block blockInFront(final Location location) {
		float yaw = location.getYaw() + 180;
		return location.getBlock().getRelative(getCardinalDirection(yaw));
	}


	/**
	 * Get block to rear of location based on yaw
	 * @param location initial location
	 * @return block behind inital location
	 */
	public static Block blockToRear(final Location location) {
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
	public static SearchResult findValidSingleChestLocation(final Player player) {

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
	 * taking into account replaceable blocks, adjacent chests, 
	 * and protection plugin regions if configured
	 * @param player Player that deathchest is being deployed for
	 * @return location that is valid for double chest deployment, or null if no valid location found
	 */
	public static SearchResult findValidDoubleChestLocation(final Player player) {

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
		SearchResult result2;

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
	public static boolean isValidSignLocation(final Player player, final Location location) {

		Block block = location.getBlock();

		// check if block at location is a ReplaceableBlock
		if (!getReplaceableBlocks().contains(block.getType())) {
			return false;
		}

		// check all enabled protection plugins for player permission at location
		ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestPlacement(player, block);
		return (blockingPlugin == null);
	}


	/** Check if single chest can be placed at location
	 * 
	 * @param player	Player to check permissions
	 * @param location	Location to check permissions
	 * @return boolean
	 */
	private static SearchResult isValidLeftChestLocation(final Player player, final Location location) {

		Block block = location.getBlock();
		SearchResult result;

		// check if block at location is a ReplaceableBlock
		if(!getReplaceableBlocks().contains(block.getType())) {
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
	public static SearchResult isValidRightChestLocation(final Player player, final Location location) {

		Block block = location.getBlock();
		SearchResult result;

		// check if block at location is a ReplaceableBlock
		if(!getReplaceableBlocks().contains(block.getType())) {
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


	private static boolean adjacentChest(final Location location, final Boolean isFirstChest) {

		// if this is the first chest, check block to left; else skip checking block to left
		if (isFirstChest && blockToLeft(location).getType().equals(Material.CHEST)) {
			if (plugin.debug) {
				plugin.getLogger().info("Block to left is an adjacent chest.");
			}
			return true;
		}
		else {
			if (plugin.debug) {
				plugin.getLogger().info("Skipping adjacent chest check for block to left.");
			}
		}
		if (blockToRight(location).getType().equals(Material.CHEST)) {
			if (plugin.debug) {
				plugin.getLogger().info("Block to right is an adjacent chest.");
			}
			return true;
		}
		if (blockInFront(location).getType().equals(Material.CHEST)) {
			if (plugin.debug) {
				plugin.getLogger().info("Block to front is an adjacent chest.");
			}
			return true;
		}
		if (blockToRear(location).getType().equals(Material.CHEST)) {
			if (plugin.debug) {
				plugin.getLogger().info("Block to rear is an adjacent chest.");
			}
			return true;
		}
		return false;
	}


	private static boolean isAboveGrassPath(final Block block) {

		return block.getRelative(0, -1, 0).getType().equals(Material.GRASS_PATH);
	}


	/**
	 * Get Set of replaceable blocks
	 * @return Set of replaceable blocks
	 */
	private static Set<Material> getReplaceableBlocks() {
		return replaceableBlocks;
	}


	/**
	 * Load list of replaceable blocks from config file
	 */
	public static Set<Material> loadReplaceableBlocks() {

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

		if (plugin.debug) {
			plugin.getLogger().info("Replaceable blocks: " + returnSet.toString());
		}
		
		return returnSet;
	}

}
