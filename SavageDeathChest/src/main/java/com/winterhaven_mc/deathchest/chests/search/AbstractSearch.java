package com.winterhaven_mc.deathchest.chests.search;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.chests.ChestSize;
import com.winterhaven_mc.deathchest.chests.Deployment;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import static com.winterhaven_mc.deathchest.util.LocationUtilities.getLocationToRight;


public abstract class AbstractSearch implements Search {

	protected final PluginMain plugin;
	protected final Player player;
	protected final ChestSize chestSize;
	protected final int searchDistance;
	protected boolean placeAboveVoid;
	protected SearchResult result;


	/**
	 * Class constructor
	 * @param plugin reference to main class instance
	 * @param player player for whom death chest is being placed
	 * @param chestSize double or single chest
	 */
	protected AbstractSearch(final PluginMain plugin,
							 final Player player,
							 final ChestSize chestSize) {

		this.plugin = plugin;
		this.player = player;
		this.chestSize = chestSize;
		this.searchDistance = plugin.getConfig().getInt("search-distance");
		this.placeAboveVoid = plugin.getConfig().getBoolean("place-above-void");

		// initialize default result
		result = new SearchResult(ResultCode.NON_REPLACEABLE_BLOCK);
		result.setLocation(player.getLocation());
	}


	@Override
	public abstract void execute();


	/**
	 * Get search result
	 *
	 * @return SearchResult object
	 */
	@Override
	public SearchResult getResult() {
		return result;
	}


	/**
	 * Validate chest location for chest size
	 *
	 * @param player    the player for whom the chest is being placed
	 * @param location  the location to test
	 * @param chestSize the size of the chest to be placed (single, double)
	 * @return SearchResult - the result object for the tested location
	 */
	SearchResult validateChestLocation(final Player player,
									   final Location location,
									   final ChestSize chestSize) {

		// test right chest location
		SearchResult result = validateChestLocation(player, location);

		// if right chest is not successful, return result
		if (!result.getResultCode().equals(ResultCode.SUCCESS)) {
			return result;
		}

		// if chest is to be a double chest, test left chest location
		if (chestSize.equals(ChestSize.DOUBLE)) {

			// test left chest block location (to player's right)
			result = validateChestLocation(player, getLocationToRight(location));
			result.setLocation(location);
		}

		return result;
	}


	/**
	 * Validate chest location for chest type
	 *
	 * @param player    the player for whom the chest is being placed
	 * @param location  the location to test
	 * @return SearchResult - the result object for the tested location
	 */
	private SearchResult validateChestLocation(final Player player, final Location location) {

		Block block = location.getBlock();

		// if block at location is not replaceable block, return negative result
		if (!plugin.chestManager.isReplaceableBlock(block)) {
			result.setResultCode(ResultCode.NON_REPLACEABLE_BLOCK);
			return result;
		}

		// if block at location is above grass path, return negative result
		if (Deployment.isAbovePath(block)) {
			result.setResultCode(ResultCode.ABOVE_GRASS_PATH);
			return result;
		}

		// if block at location is protected by plugin, return negative result
		ProtectionPlugin protectionPlugin = ProtectionPlugin.allowChestPlacement(player, block);
		if (protectionPlugin != null) {
			result.setResultCode(ResultCode.PROTECTION_PLUGIN);
			result.setProtectionPlugin(protectionPlugin);
			return result;
		}

		// if block at location is within spawn protection radius, return negative result
		if (isSpawnProtected(location)) {
			result.setResultCode(ResultCode.SPAWN_RADIUS);
			return result;
		}

		// return successful result with location
		result.setResultCode(ResultCode.SUCCESS);
		result.setLocation(location);
		return result;
	}


	/**
	 * Check if location is within world spawn protection radius
	 *
	 * @param location the location to check
	 * @return {@code true) if passed location is within world spawn protection radius, {@code false) if not
	 */
	private boolean isSpawnProtected(final Location location) {

		// check for null parameter
		if (location == null) {
			return false;
		}

		// get world spawn location
		World world = location.getWorld();

		if (world == null) {
			return false;
		}

		Location worldSpawn = plugin.worldManager.getSpawnLocation(location.getWorld());

		// get spawn protection radius
		int spawnRadius = plugin.getServer().getSpawnRadius();

		// if location is within spawn radius of world spawn location, return true; else return false
		return location.distanceSquared(worldSpawn) < (spawnRadius ^ 2);
	}

}
