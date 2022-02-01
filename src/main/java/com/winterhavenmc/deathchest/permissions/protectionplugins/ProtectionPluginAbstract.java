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
import org.bukkit.plugin.java.JavaPlugin;


public abstract class ProtectionPluginAbstract implements ProtectionPlugin {

	protected JavaPlugin plugin;
	protected String name;
	protected String version;

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public abstract boolean allowChestPlacement(final Player player, final Location location);

	@Override
	public abstract boolean allowChestAccess(final Player player, final Location location);

	@Override
	public String getPluginName() {
		return this.name;
	}

	@Override
	public String getPluginVersion() {
		return this.version;
	}


	/**
	 * Log errors that occur when checking for block place permission
	 */
	@Override
	public void logPlaceError() {
		plugin.getLogger().warning("An error occurred checking for block place permission with " + this);
	}


	/**
	 * Log errors that occur when checking for block place permission
	 */
	@Override
	public void logPlaceError(final String message) {
		plugin.getLogger().warning("An error occurred checking for block place permission with " + this);
		plugin.getLogger().warning(message);
	}


	/**
	 * Log errors that occur when checking for chest access permission
	 */
	@Override
	public void logAccessError() {
		plugin.getLogger().warning("An error occurred checking for chest access permission with " + this);
	}


	/**
	 * Log errors that occur when checking for chest access permission
	 */
	@Override
	public void logAccessError(final String message) {
		plugin.getLogger().warning("An error occurred checking for chest access permission with " + this);
		plugin.getLogger().warning(message);
	}


	/**
	 * Check if protection plugin is configured to be ignored on death chest placement
	 *
	 * @return {@code true} if the protection plugin is enabled for check on placement, {@code false} if not
	 */
	@Override
	public boolean isIgnoredOnPlace() {

		// if plugin is not enabled in config, return false
		return (plugin.getConfig().getBoolean("protection-plugins." + this + ".ignore-on-place"));
	}


	/**
	 * Check if protection plugin is configured to be ignored on death chest access
	 *
	 * @return {@code true} if the protection plugin is enabled for check on access, {@code false} if not
	 */
	@Override
	public boolean isIgnoredOnAccess() {
		// if plugin is not enabled in config, return false
		return (plugin.getConfig().getBoolean("protection-plugins." + this + ".ignore-on-access"));
	}

}
