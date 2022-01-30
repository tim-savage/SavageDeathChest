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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;


public final class ProtectionPluginRegistry {

	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private final JavaPlugin plugin;
	private final Set<ProtectionPlugin> protectionPluginSet;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class
	 */
	public ProtectionPluginRegistry(final JavaPlugin plugin) {

		this.plugin = plugin;

		// initialize set
		this.protectionPluginSet = new LinkedHashSet<>();

		// populate set with enabled protection plugins
		registerEnabledPlugins(plugin);
	}


	/**
	 * Insert a protection plugin check instance in the registry
	 *
	 * @param protectionPlugin the protection plugin check to insert in the registry
	 */
	private void register(final ProtectionPlugin protectionPlugin) {
		protectionPluginSet.add(protectionPlugin);
	}


	/**
	 * Iterate protection plugin check instances in registry and test if chest placement is allowed for player at location
	 *
	 * @param player the player for whom to perform check
	 * @param location the location to perform check
	 * @return ProtectionCheckResult - result object with result code and blocking protection plugin if applicable
	 */
	public ProtectionCheckResult placementAllowed(final Player player, final Location location) {

		ProtectionCheckResult result = new ProtectionCheckResult();

		// iterate through active protection plugins in set
		for (ProtectionPlugin protectionPlugin : protectionPluginSet) {

			// if protection plugin is configured ignore on place: skip check
			if (protectionPlugin.isIgnoredOnPlace()) {
				continue;
			}

			// if allow chest placement check returns false, set result code to BLOCKED and break loop
			if (!protectionPlugin.allowChestPlacement(player, location)) {
				result.setResultCode(ProtectionCheckResultCode.BLOCKED);
				result.setProtectionPlugin(protectionPlugin);
				break;
			}
		}
		return result;
	}


	/**
	 * Iterate protection plugin check instances in registry and test if chest access is allowed for player at location
	 *
	 * @param player the player for whom to perform check
	 * @param location the location to perform check
	 * @return ProtectionCheckResult - result object with result code and blocking protection plugin if applicable
	 */
	public ProtectionCheckResult AccessAllowed(final Player player, final Location location) {

		ProtectionCheckResult result = new ProtectionCheckResult();

		// iterate through active protection plugins in set
		for (ProtectionPlugin protectionPlugin : protectionPluginSet) {

			// if protection plugin is configured ignore on access: skip check
			if (protectionPlugin.isIgnoredOnAccess()) {
				continue;
			}

			// if allow chest access check returns false, set result code to BLOCKED and break loop
			if (!protectionPlugin.allowChestAccess(player, location)) {
				result.setResultCode(ProtectionCheckResultCode.BLOCKED);
				result.setProtectionPlugin(protectionPlugin);
				break;
			}
		}
		return result;
	}


	/**
	 * Get all plugin check instances from registry
	 *
	 * @return collection of plugin check instances from registry
	 */
	public Collection<ProtectionPlugin> getAll() {
		return protectionPluginSet;
	}


	/**
	 * Iterate ProtectionPluginTypes and insert check instances for installed plugins
	 *
	 * @param plugin reference to plugin main class
	 */
	private void registerEnabledPlugins(final JavaPlugin plugin) {

		// iterate over all plugins defined in ProtectionPluginType enum
		for (ProtectionPluginType protectionPluginType : ProtectionPluginType.values()) {

			// get reference to plugin
			Plugin pluginInstance = plugin.getServer().getPluginManager().getPlugin(protectionPluginType.getName());

			// if plugin is installed and enabled
			if (pluginInstance != null && pluginInstance.isEnabled()) {

				// get plugin name as string
				String name = pluginInstance.getName();

				// get plugin version as string
				String version = pluginInstance.getDescription().getVersion();

				// register plugin type in map
				register(protectionPluginType.create(plugin, version));

				// log detected plugins
				plugin.getLogger().info("Detected " + name + " v" + version);
			}
		}
	}

}
