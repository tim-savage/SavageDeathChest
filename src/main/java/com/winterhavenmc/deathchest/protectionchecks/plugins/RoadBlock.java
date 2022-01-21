package com.winterhavenmc.deathchest.protectionchecks.plugins;

import com.winterhavenmc.deathchest.protectionchecks.ProtectionPlugin;
import com.winterhavenmc.deathchest.protectionchecks.ProtectionPluginAbstract;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class RoadBlock extends ProtectionPluginAbstract implements ProtectionPlugin {

	public RoadBlock(final JavaPlugin plugin, final String name, final String version) {
		this.plugin = plugin;
		this.name = name;
		this.version = version;
	}


	@Override
	public boolean allowChestPlacement(final Player player, final Location location) {
		try {
			// this query returns true if placement is allowed, false if placement is denied
			return com.winterhavenmc.roadblock.SimpleAPI.canPlace(location);
		}
		catch (Exception e) {
			logPlaceError();
			// if error occurred, allow placement
			return true;
		}
	}


	@Override
	public boolean allowChestAccess(final Player player, final Location location) {
		// RoadBlock is not concerned with chest access permissions; always return true
		return true;
	}

}
