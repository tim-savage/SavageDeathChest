package com.winterhavenmc.deathchest.protectionplugins;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public abstract class ProtectionPluginAbstract implements ProtectionPlugin {

	protected JavaPlugin plugin;
	protected String name;

	@Override
	public abstract boolean allowChestPlacement(Player player, Location location);

	@Override
	public abstract boolean allowChestAccess(Player player, Location location);

	@Override
	public String getPluginName() {
		return this.name;
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

}
