package com.winterhaven_mc.deathchest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WGBukkit;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;

public class PermissionManager {

	DeathChestMain plugin;
	
	Boolean wg_enabled = false;
	Boolean gp_enabled = false;
	Boolean towny_enabled = false;
	
	PermissionManager(DeathChestMain plugin) {
		
		this.plugin = plugin;
		
		// check if WorldGuard plugin is enabled
		Plugin wg = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		if (wg != null && wg.isEnabled()) {
			plugin.getLogger().info("WorldGuard detected.");
			wg_enabled = true;
		}

		// check if GriefPrevention plugin is enabled
		Plugin gp = plugin.getServer().getPluginManager().getPlugin("GriefPrevention");
		if (gp != null && gp.isEnabled()) {
			plugin.getLogger().info("GriefPrevention detected.");
			gp_enabled = true;
		}

		// check if Towny plugin is enabled
		Plugin towny = plugin.getServer().getPluginManager().getPlugin("Towny");
		if (towny != null && towny.isEnabled()) {
			plugin.getLogger().info("Towny detected.");
			towny_enabled = true;
		}
	}

	
	/**
	 *  Check if player has GriefPrevention chest access at location
	 * @param player	Player to check permission
	 * @param location	Location to check permission
	 * @return boolean	true/false player has chest access at location
	 */
	boolean gpPermission(Player player, Location location) {

		// if GriefPrevention config option is enabled and GriefPrevention plugin is enabled
		if (plugin.getConfig().getBoolean("griefprevention-enabled", true) && gp_enabled) {
			// if player does not have Grief Prevention chest access, spill inventory
			Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
			if (claim != null) {
				String gpErrorMessage = claim.allowContainers(player);
				if (gpErrorMessage != null) {
					plugin.getLogger().info(gpErrorMessage);
					if (plugin.debug) {
						plugin.getLogger().info("Chest placement prevented by GriefPrevention.");
					}
					return false;
				}
			}
		}
		return true;
	}


	/**
	 *  Check if player has WorldGuard build permission at location
	 * @param player	Player to check permissions
	 * @param location	Location to check permissions
	 * @return boolean	true/false player has build permission at location
	 */
	boolean wgPermission(Player player, Location location) {
		// if WorldGuard config option is enabled and WorldGuard plugin is enabled, check for chest access
		if (plugin.getConfig().getBoolean("worldguard-enabled", true) && WGBukkit.getPlugin().isEnabled()) {
			if (!WGBukkit.getPlugin().canBuild(player, location)) {
				if (plugin.debug) {
					plugin.getLogger().info("Chest placement prevented by WorldGuard.");
				}
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Check if player has Towny chest permission at location
	 * @param player
	 * @param location
	 * @return boolean true/false player has chest usage permission at location
	 */
	@SuppressWarnings("deprecation")
	boolean townyPermission(Player player, Location location) {
		// if Towny config option is enabled and Towny plugin is enabled
		if (plugin.getConfig().getBoolean("towny-enabled", true) && towny_enabled) {
			if (!PlayerCacheUtil.getCachePermission(player, location, Material.CHEST.getId(), (byte)0, TownyPermission.ActionType.SWITCH)) {
				if (plugin.debug) {
					plugin.getLogger().info("Chest placement prevented by Towny.");
				}
				return false;
			}
		}
		return true;
	}
	
}
