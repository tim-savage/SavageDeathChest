package com.winterhavenmc.deathchest.protectionchecks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ProtectionPlugin {


	/**
	 * Check if plugin will allow chest placement at location by player
	 *
	 * @param player the player whose death chest will be placed
	 * @param location the location where the death chest will be placed
	 * @return boolean - true if placement is allowed by plugin, false if not
	 */
	boolean allowChestPlacement(final Player player, final Location location);


	/**
	 * Check if plugin will allow chest access at location by player
	 *
	 * @param player the player who is trying to access a death chest
	 * @param location the location where the death chest is being accessed
	 * @return boolean - true if access is allowed by plugin, false if not
	 */
	boolean allowChestAccess(final Player player, final Location location);


	/**
	 * Log error if chest placement failed
	 */
	void logPlaceError();


	/**
	 * Log error if chest access failed
	 */
	void logAccessError();


	/**
	 * Get the name of the protection plugin
	 *
	 * @return String - the name of the protection plugin
	 */
	String getPluginName();


	/**
	 * Get the version of the protection plugin
	 *
	 * @return String - the version of the protection plugin
	 */
	@SuppressWarnings("unused")
	String getPluginVersion();


	/**
	 * Check if the protection plugin is configured to be ignored on chest placement
	 *
	 * @return boolean - true if plugin is configured ignore on place, otherwise false
	 */
	boolean isIgnoredOnPlace();


	/**
	 * Check if the protection plugin is configured to be ignored on chest access
	 *
	 * @return boolean - true if plugin is configured ignore on access, otherwise false
	 */
	boolean isIgnoredOnAccess();

}
