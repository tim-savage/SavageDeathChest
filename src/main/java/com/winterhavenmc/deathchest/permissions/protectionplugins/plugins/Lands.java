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

package com.winterhavenmc.deathchest.permissions.protectionplugins.plugins;

import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPlugin;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPluginAbstract;

import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.flags.Flags;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;


/**
 * Class that implements protection plugin checks for Lands
 */
public final class Lands extends ProtectionPluginAbstract implements ProtectionPlugin {

	// reference to LandsIntegration instance
	LandsIntegration landsIntegration;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to SavageDeathChest plugin main class instance
	 * @param name name of the protection plugin
	 * @param version version of the protection plugin
	 */
	public Lands(final JavaPlugin plugin, final String name, final String version) {
		this.plugin = plugin;
		this.name = name;
		this.version = version;

		// get reference to LandsIntegration instance
		landsIntegration = new LandsIntegration(plugin);
	}


	/**
	 * Check if Lands plugin will allow chest placement at location by player
	 *
	 * @param player the player whose death chest will be placed
	 * @param location the location where the death chest will be placed
	 * @return boolean - true if placement is allowed by plugin, false if not
	 */
	@Override
	public boolean allowChestPlacement(final Player player, final Location location) {

		try {
			final @Nullable Area area = landsIntegration.getAreaByLoc(location);
			if (area != null) {
				// this query should return true if placement is allowed, false if placement is denied
				return area.hasFlag(player, Flags.BLOCK_PLACE, false);
			}
		}
		catch (Error | Exception e) {
			logPlaceError();
		}
		// if all else fails, allow chest placement
		return true;
	}


	/**
	 * Check if Lands plugin will allow chest access at location by player
	 *
	 * @param player the player who is trying to access a death chest
	 * @param location the location where the death chest is being accessed
	 * @return boolean - true if access is allowed by plugin, false if not
	 */
	@Override
	public boolean allowChestAccess(final Player player, final Location location) {

		try {
			final @Nullable Area area = landsIntegration.getAreaByLoc(location);
			if (area != null) {
				// this query should return true if access is allowed, false if access is denied
				return area.hasFlag(player, Flags.INTERACT_CONTAINER, false);
			}
		}
		catch (Error | Exception e) {
			logAccessError();
		}
		// if all else fails, allow chest access
		return true;
	}

}
