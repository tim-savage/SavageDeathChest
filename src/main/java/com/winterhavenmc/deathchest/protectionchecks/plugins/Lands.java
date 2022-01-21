package com.winterhavenmc.deathchest.protectionchecks.plugins;

import com.winterhavenmc.deathchest.protectionchecks.ProtectionPlugin;
import com.winterhavenmc.deathchest.protectionchecks.ProtectionPluginAbstract;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Lands extends ProtectionPluginAbstract implements ProtectionPlugin {

	LandsIntegration landsIntegration;

	public Lands(final JavaPlugin plugin, final String name, final String version) {
		this.plugin = plugin;
		this.name = name;
		this.version = version;

		landsIntegration = new LandsIntegration(plugin);
	}


	@Override
	public boolean allowChestPlacement(final Player player, final Location location) {

		try {
			final Area area = landsIntegration.getAreaByLoc(location);
			// this query returns true if placement is allowed, false if placement is denied NOTE: unconfirmed for pay plugin
			return (Objects.requireNonNull(area).hasFlag(player, me.angeschossen.lands.api.flags.Flags.BLOCK_PLACE, false));
		}
		catch (Exception e) {
			logPlaceError();
		}
		return true;
	}


	@Override
	public boolean allowChestAccess(final Player player, final Location location) {

		try {
			final Area area = landsIntegration.getAreaByLoc(location);
			// this query return true if access is allowed, false if access is denied NOTE: unconfirmed for pay plugin
			return Objects.requireNonNull(area).hasFlag(player, me.angeschossen.lands.api.flags.Flags.INTERACT_CONTAINER, false);
		}
		catch (Exception e) {
			logAccessError();
		}
		return true;
	}

}
