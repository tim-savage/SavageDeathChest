package com.winterhavenmc.deathchest.permissions.protectionplugins.plugins;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPlugin;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPluginAbstract;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;


/**
 * Class that implements protection plugin checks for Towny
 */
public final class Towny extends ProtectionPluginAbstract implements ProtectionPlugin {


	/**
	 * Class constructor
	 *
	 * @param plugin reference to SavageDeathChest plugin main class instance
	 * @param name name of the protection plugin
	 * @param version version of the protection plugin
	 */
	public Towny(final JavaPlugin plugin, final String name, final String version) {
		this.plugin = plugin;
		this.name = name;
		this.version = version;
	}


	@Override
	public boolean allowChestPlacement(final Player player, final Location location) {

		// use try..catch block to gracefully handle exceptions thrown by protection plugin
		try {
			return PlayerCacheUtil.getCachePermission(player, location,
					Material.CHEST, TownyPermission.ActionType.BUILD);
		}
		catch (Exception e) {
			logPlaceError();
			return true;
		}
	}


	@Override
	public boolean allowChestAccess(final Player player, final Location location) {

		try {
			LandsIntegration landsIntegration = new LandsIntegration(plugin);
			final Area area = landsIntegration.getAreaByLoc(location);
			return Objects.requireNonNull(area).hasFlag(player, me.angeschossen.lands.api.flags.Flags.INTERACT_CONTAINER, false);
		}
		catch (Exception e) {
			logAccessError();
			return true;
		}
	}

}
