package com.winterhavenmc.deathchest.protectionplugins;

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

		this.protectionPluginSet = new LinkedHashSet<>();

		// register installed protection plugins in map
		for (ProtectionPluginType protectionPluginType : ProtectionPluginType.values()) {

			// get reference to plugin
			Plugin protectionPlugin = plugin.getServer().getPluginManager().getPlugin(protectionPluginType.getName());

			if (protectionPlugin != null && protectionPlugin.isEnabled()) {
				register(protectionPluginType.enable(plugin));
				String version = protectionPlugin.getDescription().getVersion();
				plugin.getLogger().info("Detected " + protectionPluginType.getName() + " v" + version);
			}
		}
	}


	private void register(ProtectionPlugin protectionPlugin) {
		protectionPluginSet.add(protectionPlugin);
	}


	public ProtectionPlugin placementAllowed(final Player player, final Location location) {
		for (ProtectionPlugin protectionPlugin : protectionPluginSet) {
			if (!protectionPlugin.allowChestPlacement(player, location)) {
				return protectionPlugin;
			}
		}
		return null;
	}


	public ProtectionPlugin AccessAllowed(final Player player, final Location location) {
		for (ProtectionPlugin protectionPlugin : protectionPluginSet) {
			if (!protectionPlugin.allowChestAccess(player, location)) {
				return protectionPlugin;
			}
		}
		return null;
	}

	public Collection<ProtectionPlugin> getAll() {
		return protectionPluginSet;
	}

}
