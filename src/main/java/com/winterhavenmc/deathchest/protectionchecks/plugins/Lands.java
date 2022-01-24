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

import static me.angeschossen.lands.api.flags.Flags.BLOCK_PLACE;
import static me.angeschossen.lands.api.flags.Flags.INTERACT_CONTAINER;


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

		// get reference to LandsIntegration instance
		landsIntegration = new LandsIntegration(plugin);
	}


	/**
	 * Check if plugin will allow chest placement at location by player
	 *
	 * @param player the player whose death chest will be placed
	 * @param location the location where the death chest will be placed
	 * @return boolean - true if placement is allowed by plugin, false if not
	 */
	@Override
	public boolean allowChestPlacement(final Player player, final Location location) {

		try {
			final @Nullable Area area = landsIntegration.getAreaByLoc(location);
			if (area != null) {
				// this query should return true if placement is allowed, false if placement is denied
				return area.hasFlag(player, BLOCK_PLACE, Material.CHEST, false);
			}
		}
		catch (Exception e) {
			logPlaceError();
		}
		// if all else fails, allow chest placement
		return true;
	}


	/**
	 * Check if plugin will allow chest access at location by player
	 *
	 * @param player the player who is trying to access a death chest
	 * @param location the location where the death chest is being accessed
	 * @return boolean - true if access is allowed by plugin, false if not
	 */
	@Override
	public boolean allowChestAccess(final Player player, final Location location) {

		try {
			final @Nullable Area area = landsIntegration.getAreaByLoc(location);
			if (area != null) {
				// this query should return true if access is allowed, false if access is denied
				return area.hasFlag(player, INTERACT_CONTAINER, false);
			}
		}
		catch (Exception e) {
			logAccessError();
		}
		// if all else fails, allow chest access
		return true;
	}

}
