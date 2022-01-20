package com.winterhavenmc.deathchest.protectionplugins;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

final class RoadBlock extends ProtectionPluginAbstract implements ProtectionPlugin {

	RoadBlock(final JavaPlugin plugin, final String name) {
		this.plugin = plugin;
		this.name = name;
	}


	@Override
	public boolean allowChestPlacement(Player player, Location location) {
		// use try..catch block to gracefully handle exceptions thrown by protection plugin
		try {
			return com.winterhavenmc.roadblock.SimpleAPI.canPlace(location);
		}
		catch (Exception e) {
			logPlaceError();
		}
		return true;
	}


	@Override
	public boolean allowChestAccess(Player player, Location location) {
		// RoadBlock is not concerned with chest access permissions; always return true
		return true;
	}

}
