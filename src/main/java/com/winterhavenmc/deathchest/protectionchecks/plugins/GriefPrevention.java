package com.winterhavenmc.deathchest.protectionchecks.plugins;

import com.winterhavenmc.deathchest.protectionchecks.ProtectionPlugin;
import com.winterhavenmc.deathchest.protectionchecks.ProtectionPluginAbstract;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class GriefPrevention extends ProtectionPluginAbstract implements ProtectionPlugin {


	public GriefPrevention(final JavaPlugin plugin, final String name, final String version) {
		this.plugin = plugin;
		this.name = name;
		this.version = version;
	}


	@Override
	public boolean allowChestPlacement(final Player player, final Location location) {
		try {
			Claim claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
			if (claim != null) {
				// this query returns a string error message if placement denied, null if placement allowed
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
	public boolean allowChestAccess(final Player player, final Location location) {
		try {
			Claim claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
			if (claim != null) {
				// this query returns a string error message if container access denied, null if container access allowed
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
