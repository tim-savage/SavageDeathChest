package com.winterhavenmc.deathchest.protectionchecks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public abstract class ProtectionPluginAbstract implements ProtectionPlugin {

	protected JavaPlugin plugin;
	protected String name;
	protected String version;

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
		plugin.getLogger().warning("An error occurred checking for block place permission "
				+ "with " + this.getPluginName());
	}


	/**
	 * Log errors that occur when checking for chest access permission
	 */
	@Override
	public void logAccessError() {
		plugin.getLogger().warning("An error occurred checking for chest access permission "
				+ "with " + this.getPluginName());
	}


	/**
	 * Check if protection plugin is configured to be ignored on death chest placement
	 *
	 * @return {@code true} if the protection plugin is enabled for check on placement, {@code false} if not
	 */
	@Override
	public boolean isIgnoredOnPlace() {

		// if plugin is not enabled in config, return false
		return (plugin.getConfig().getBoolean("protection-plugins." + this.getPluginName() + ".ignore-on-place"));
	}


	/**
	 * Check if protection plugin is configured to be ignored on death chest access
	 *
	 * @return {@code true} if the protection plugin is enabled for check on access, {@code false} if not
	 */
	@Override
	public boolean isIgnoredOnAccess() {

		// if plugin is not enabled in config, return false
		return (plugin.getConfig().getBoolean("protection-plugins." + this.getPluginName() + ".ignore-on-access"));
	}

	@Override
	public String toString() {
		return this.name;
	}

}
