package com.winterhavenmc.deathchest.permissions.protectionplugins.plugins;

import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPlugin;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPluginAbstract;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Class that implements protection plugin checks for RoadBlock
 */
public final class RoadBlock extends ProtectionPluginAbstract implements ProtectionPlugin {


	/**
	 * Class constructor
	 *
	 * @param plugin reference to SavageDeathChest plugin main class instance
	 * @param name name of the protection plugin
	 * @param version version of the protection plugin
	 */
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
		catch (Error | Exception e) {
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
