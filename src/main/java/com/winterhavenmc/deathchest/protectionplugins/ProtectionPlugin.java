package com.winterhavenmc.deathchest.protectionplugins;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ProtectionPlugin {

	boolean allowChestPlacement(final Player player, final Location location);

	boolean allowChestAccess(final Player player, final Location location);

	void logPlaceError();

	void logAccessError();

	String getPluginName();

}
