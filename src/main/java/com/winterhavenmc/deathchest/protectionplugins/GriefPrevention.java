package com.winterhavenmc.deathchest.protectionplugins;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

final class GriefPrevention extends ProtectionPluginAbstract implements ProtectionPlugin {


	public GriefPrevention(final JavaPlugin plugin, final String name) {
		this.plugin = plugin;
		this.name = name;
	}


	@Override
	public boolean allowChestPlacement(Player player, Location location) {
		// use try...catch block to gracefully handle exceptions thrown by protection plugin
		try {
			Claim claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
			if (claim != null) {
				String gpErrorMessage = claim.allowBuild(player, Material.CHEST);
				if (gpErrorMessage != null) {
					return false;
				}
			}
		}
		catch (Exception e) {
			logPlaceError();
		}
		return true;
	}


	@Override
	public boolean allowChestAccess(Player player, Location location) {
		// use try..catch block to gracefully handle exceptions thrown by protection plugin
		try {
			Claim claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
			if (claim != null) {
				String gpErrorMessage = claim.allowContainers(player);
				if (gpErrorMessage != null) {
					return false;
				}
			}
		}
		catch (Exception e) {
			logAccessError();
		}
		return true;
	}

}
