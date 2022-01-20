package com.winterhavenmc.deathchest.protectionplugins;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

final class Towny extends ProtectionPluginAbstract implements ProtectionPlugin {


	public Towny(final JavaPlugin plugin, final String name) {
		this.plugin = plugin;
		this.name = name;
	}


	@Override
	public boolean allowChestPlacement(Player player, Location location) {

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
	public boolean allowChestAccess(Player player, Location location) {

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
