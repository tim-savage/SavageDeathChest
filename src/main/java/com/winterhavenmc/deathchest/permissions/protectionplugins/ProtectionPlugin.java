/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathchest.permissions.protectionplugins;

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
	 * Get the name of the protection plugin
	 *
	 * @return String - the name of the protection plugin
	 */
	@SuppressWarnings("unused")
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
