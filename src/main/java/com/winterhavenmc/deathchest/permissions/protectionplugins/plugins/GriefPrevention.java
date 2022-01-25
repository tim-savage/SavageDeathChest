package com.winterhavenmc.deathchest.permissions.protectionplugins.plugins;

import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPlugin;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPluginAbstract;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Class that implements protection plugin checks for GriefPrevention
 */
public final class GriefPrevention extends ProtectionPluginAbstract implements ProtectionPlugin {


	/**
	 * Class constructor
	 *
	 * @param plugin reference to SavageDeathChest plugin main class instance
	 * @param name name of the protection plugin
	 * @param version version of the protection plugin
	 */
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
