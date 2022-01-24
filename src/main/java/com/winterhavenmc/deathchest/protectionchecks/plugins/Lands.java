package com.winterhavenmc.deathchest.protectionchecks.plugins;

import com.winterhavenmc.deathchest.protectionchecks.ProtectionPlugin;
import com.winterhavenmc.deathchest.protectionchecks.ProtectionPluginAbstract;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.Material;
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

		landsIntegration = new LandsIntegration(plugin);
	}


	@Override
	public boolean allowChestPlacement(final Player player, final Location location) {

		try {
			final @Nullable Area area = landsIntegration.getAreaByLoc(location);
			if (area != null) {
				// this query should return true if placement is allowed, false if placement is denied NOTE: unconfirmed for paid plugin
				return area.hasFlag(player, me.angeschossen.lands.api.flags.Flags.BLOCK_PLACE, Material.CHEST, false);
			}
		}
		catch (Exception e) {
			logPlaceError();
		}
		return true;
	}


	@Override
	public boolean allowChestAccess(final Player player, final Location location) {

		try {
			final @Nullable Area area = landsIntegration.getAreaByLoc(location);
			if (area != null) {
				// this query should return true if access is allowed, false if access is denied NOTE: unconfirmed for paid plugin
				return area.hasFlag(player, me.angeschossen.lands.api.flags.Flags.INTERACT_CONTAINER, false);
			}
		}
		catch (Exception e) {
			logAccessError();
		}
		return true;
	}

}
