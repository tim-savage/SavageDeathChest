package com.winterhavenmc.deathchest.protectionchecks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;


public final class ProtectionPluginRegistry {

	private final Set<ProtectionPlugin> protectionPluginSet;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class
	 */
	public ProtectionPluginRegistry(final JavaPlugin plugin) {

		// initialize set
		this.protectionPluginSet = new LinkedHashSet<>();

		// populate set with enabled protection plugins
		scanPlugins(plugin);
	}


	private void register(final ProtectionPlugin protectionPlugin) {
		protectionPluginSet.add(protectionPlugin);
	}


	public ProtectionPlugin placementAllowed(final Player player, final Location location) {

		// iterate through active protection plugins in set
		for (ProtectionPlugin protectionPlugin : protectionPluginSet) {

			// if protection plugin is configured ignore on place, skip check
			if (protectionPlugin.isIgnoredOnPlace()) {
				continue;
			}

			// if allow chest placement check returns false, return protection plugin
			if (!protectionPlugin.allowChestPlacement(player, location)) {
				return protectionPlugin;
			}
		}
		return null;
	}


	public ProtectionPlugin AccessAllowed(final Player player, final Location location) {

		// iterate through active protection plugins in set
		for (ProtectionPlugin protectionPlugin : protectionPluginSet) {

			// if protection plugin is configured ignore on access, skip check
			if (protectionPlugin.isIgnoredOnAccess()) {
				continue;
			}

			// if allow chest access check returns false, return protection plugin
			if (!protectionPlugin.allowChestAccess(player, location)) {
				return protectionPlugin;
			}
		}
		return null;
	}


	public Collection<ProtectionPlugin> getAll() {
		return protectionPluginSet;
	}


	private void scanPlugins(final JavaPlugin plugin) {

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
