package com.winterhavenmc.deathchest.permissions.protectionplugins.plugins;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPlugin;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPluginAbstract;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Class that implements protection plugin checks for WorldGuard
 */
public final class WorldGuard extends ProtectionPluginAbstract implements ProtectionPlugin {

	// reference to worldguard region container
	final RegionContainer regionContainer;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to SavageDeathChest plugin main class instance
	 * @param name name of the protection plugin
	 * @param version version of the protection plugin
	 */
	public WorldGuard(final JavaPlugin plugin, final String name, final String version) {
		this.plugin = plugin;
		this.name = name;
		this.version = version;

		this.regionContainer = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
	}


	@Override
	public boolean allowChestPlacement(final Player player, final Location location) {
		try {
			RegionQuery query = regionContainer.createQuery();
			// this query returns true if placement is allowed, false if placement denied
			return query.testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
		}
		catch (Exception e) {
			logPlaceError();
			// if error occurred, allow placement
			return true;
		}
	}


	@Override
	public boolean allowChestAccess(final Player player, final Location location) {
		try {
			RegionQuery query = regionContainer.createQuery();
			// this query returns true if access allowed, false if access denied
			return query.testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.CHEST_ACCESS);
		}
		catch (Exception e) {
			logAccessError();
			// if error occurred, allow access
			return true;
		}
	}

}
