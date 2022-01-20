package com.winterhavenmc.deathchest.protectionplugins;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


final class WorldGuard extends ProtectionPluginAbstract implements ProtectionPlugin {


	WorldGuard(final JavaPlugin plugin, final String name) {
		this.plugin = plugin;
		this.name = name;
	}


	@Override
	public boolean allowChestPlacement(Player player, Location location) {
		final RegionContainer regionContainer = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();

		// use try..catch block to gracefully handle exceptions thrown by protection plugin
		try {
			RegionQuery query = regionContainer.createQuery();
			return query.testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
		}
		catch (Exception e) {
			logPlaceError();
		}
		return true;
	}


	@Override
	public boolean allowChestAccess(Player player, Location location) {
		final RegionContainer regionContainer = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();

		// use try..catch block to gracefully handle exceptions thrown by protection plugin
		try {
			RegionQuery query = regionContainer.createQuery();
			return query.testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.CHEST_ACCESS);
		}
		catch (Exception e) {
			logAccessError();
		}
		return true;
	}

}
