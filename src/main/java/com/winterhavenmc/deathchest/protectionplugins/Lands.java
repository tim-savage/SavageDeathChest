package com.winterhavenmc.deathchest.protectionplugins;

import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

final class Lands extends ProtectionPluginAbstract implements ProtectionPlugin {


	public Lands(final JavaPlugin plugin, final String name) {
		this.plugin = plugin;
		this.name = name;
	}


	@Override
	public boolean allowChestPlacement(Player player, Location location) {

		try {
			LandsIntegration landsIntegration = new LandsIntegration(plugin);
			final Area area = landsIntegration.getAreaByLoc(location);
			return (Objects.requireNonNull(area).hasFlag(player, me.angeschossen.lands.api.flags.Flags.BLOCK_PLACE, false));
		}
		catch (Exception e) {
			logPlaceError();
		}
		return true;
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
		}
		return true;
	}

}
