package com.winterhavenmc.deathchest.chests.search;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.chests.ChestSize;
import com.winterhavenmc.deathchest.chests.Deployment;
import com.winterhavenmc.deathchest.chests.LocationUtilities;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionCheckResult;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionCheckResultCode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;


abstract class AbstractSearch implements Search {

	protected final PluginMain plugin;
	protected final Player player;
	protected final ChestSize chestSize;
	protected final int searchDistance;
	protected boolean placeAboveVoid;
	protected SearchResult searchResult;


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
		searchResult = new SearchResult(SearchResultCode.NON_REPLACEABLE_BLOCK);
		searchResult.setLocation(player.getLocation());
	}


	@Override
	public abstract void execute();


	/**
	 * Get search result
	 *
	 * @return SearchResult object
	 */
	@Override
	public SearchResult getSearchResult() {
		return searchResult;
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
		if (!result.getResultCode().equals(SearchResultCode.SUCCESS)) {
			return result;
		}

		// if chest is to be a double chest, test left chest location
		if (chestSize.equals(ChestSize.DOUBLE)) {

			// test left chest block location (to player's right)
			result = validateChestLocation(player, LocationUtilities.getLocationToRight(location));
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
			searchResult.setResultCode(SearchResultCode.NON_REPLACEABLE_BLOCK);
			return searchResult;
		}

		// if block at location is above grass path, return negative result
		if (Deployment.isAbovePath(block)) {
			searchResult.setResultCode(SearchResultCode.ABOVE_GRASS_PATH);
			return searchResult;
		}

		// if block at location is protected by plugin, return negative result
		ProtectionCheckResult protectionCheckResult = plugin.protectionPluginRegistry.placementAllowed(player, location);
		if (protectionCheckResult.getResultCode().equals(ProtectionCheckResultCode.BLOCKED)) {
			searchResult.setResultCode(SearchResultCode.PROTECTION_PLUGIN);
			searchResult.setProtectionPlugin(protectionCheckResult.getProtectionPlugin());
			return searchResult;
		}

		// if block at location is within spawn protection radius, return negative result
		if (isSpawnProtected(location)) {
			searchResult.setResultCode(SearchResultCode.SPAWN_RADIUS);
			return searchResult;
		}

		// return successful result with location
		searchResult.setResultCode(SearchResultCode.SUCCESS);
		searchResult.setLocation(location);
		return searchResult;
	}


	/**
	 * Check if location is within world spawn protection radius
	 *
	 * @param location the location to check
	 * @return {@code true} if passed location is within world spawn protection radius, {@code false} if not
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
