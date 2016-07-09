package com.winterhaven_mc.deathchest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.massivecraft.factions.engine.EngineMain;
import com.massivecraft.massivecore.ps.PS;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;

public enum ProtectionPlugin {

	FACTIONS("Factions") {

		@Override
		public final boolean hasPlacePermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				if (!EngineMain.canPlayerBuildAt(player, PS.valueOf(location), false)) {
					return false;
				}

			}
			catch (Exception e) {
				logPlaceError();
			}

			return true;
		}

		@Override
		public final boolean hasChestPermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				if (!EngineMain.playerCanUseItemHere(player, PS.valueOf(location), Material.CHEST, false)) {
					return false;
				}
			}
			catch (Exception e) {
				logAccessError();
			}

			return true;
		}

	},

	GRIEFPREVENTION("GriefPrevention") {

		@Override
		public final boolean hasPlacePermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
				if (claim != null) {
					String gpErrorMessage = claim.allowBuild(player,Material.CHEST);
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
		public final boolean hasChestPermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
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

	},

	PRECIOUSSTONES("PreciousStones") {

		@Override
		public final boolean hasPlacePermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				if (! PreciousStones.API().canPlace(player,location)) {
					return false;
				}
			}
			catch (Exception e) {
				logPlaceError();
			}

			return true;
		}

		@Override
		public final boolean hasChestPermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				if (PreciousStones.API().flagAppliesToPlayer(player,FieldFlag.PROTECT_INVENTORIES,location)) {
					return false;
				}
			}
			catch (Exception e) {
				logAccessError();
			}

			return true;
		}

	},

	PROCLAIM("ProClaim") {

		@Override
		public final boolean hasPlacePermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				return com.winterhaven_mc.proclaim.SimpleAPI.hasBuildTrust(player, location);
			}
			catch (Exception e) {
				logPlaceError();
			}

			return true;
		}

		@Override
		public final boolean hasChestPermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				return com.winterhaven_mc.proclaim.SimpleAPI.hasContainerTrust(player, location);
			}
			catch (Exception e) {
				logAccessError();
			}

			return true;
		}
	},

	ROADBLOCK("RoadBlock") {

		@Override
		public final boolean hasPlacePermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {

				if (com.winterhaven_mc.roadblock.SimpleAPI.canPlace(location)) {
					return false;
				}
			}
			catch (Exception e) {
				logPlaceError();
			}

			return true;
		}

		@Override
		public final boolean hasChestPermission(final Player player, final Location location) {

			// RoadBlock is not concerned with chest access permissions, check chest place permission instead

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {

				if (com.winterhaven_mc.roadblock.SimpleAPI.canPlace(location)) {
					return false;
				}
			}
			catch (Exception e) {
				logAccessError();
			}

			return true;
		}

	},

	TOWNY("Towny") {

		@SuppressWarnings("deprecation")
		@Override
		public final boolean hasPlacePermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				if (!PlayerCacheUtil.getCachePermission(player, location, Material.CHEST.getId(),
						(byte)0, TownyPermission.ActionType.BUILD)) {
					return false;
				}
			}
			catch (Exception e) {
				logPlaceError();
			}

			return true;
		}

		@SuppressWarnings("deprecation")
		@Override
		public final boolean hasChestPermission(final Player player, final Location location) {

			// only perform check if plugin is installed
			if (this.isInstalled()) {
				try {
					if (!PlayerCacheUtil.getCachePermission(player, location, Material.CHEST.getId(),
							(byte)0, TownyPermission.ActionType.SWITCH)) {
						return false;
					}
				}
				catch (Exception e) {
					logAccessError();
				}
			}
			return true;
		}

	},

	WORLDGUARD("WorldGuard") {

		@Override
		public final boolean hasPlacePermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				// get worldguard version string
				String wgVersion = WGBukkit.getPlugin().getDescription().getVersion();

				// if worldguard version 5, use canBuild method
				if (wgVersion.startsWith("5.")) {

					if (!WGBukkit.getPlugin().canBuild(player, location)) {
						return false;
					}
				}
				// if worldguard version 6, use testBlockPlace method
				else if (wgVersion.startsWith("6.")) {

					if (!WGBukkit.getPlugin().createProtectionQuery()
							.testBlockPlace(player, location, Material.CHEST)) {
						return false;
					}
				}
			}
			catch (Exception e) {
				logPlaceError();
			}

			return true;
		}

		@SuppressWarnings("deprecation")
		@Override
		public final boolean hasChestPermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				// get worldguard version string
				String wgVersion = WGBukkit.getPlugin().getDescription().getVersion();

				// if worldguard version 5, use region manager method
				if (wgVersion.startsWith("5.")) {

					RegionManager regionManager = WGBukkit.getPlugin().getRegionManager(location.getWorld());
					ApplicableRegionSet set = regionManager.getApplicableRegions(location);
					LocalPlayer localPlayer = WGBukkit.getPlugin().wrapPlayer(player);
					if (! set.allows(DefaultFlag.CHEST_ACCESS,localPlayer)) {
						return false;
					}
				}
				// if worldguard version 6 use protection query method
				else if (wgVersion.startsWith("6.")) {

					if (! WGBukkit.getPlugin().createProtectionQuery()
							.testBlockInteract(player, location.getBlock())) {
						return false;
					}
				}
			}
			catch (Exception e) {
				logAccessError();
			}

			return true;
		}

	};

	// static reference to main class
	private final static PluginMain plugin = PluginMain.instance;

	// protection plugin name
	private final String pluginName;


	/**
	 * Enum constructor
	 * @param pluginName the official case-sensitive plugin name
	 */
	ProtectionPlugin(final String pluginName) {

		// set plugin name field
		this.pluginName = pluginName;
	}


	/**
	 * Log errors that occur when checking for block place permission
	 */
	void logPlaceError() {
		plugin.getLogger().warning("An error occured checking for place permission "
				+ "with " + this.getPluginName() + " v" + this.getVersion());
	}


	/**
	 * Log errors that occur when checking for chest access permission
	 */
	void logAccessError() {
		plugin.getLogger().warning("An error occured checking for access permission "
				+ "with " + this.getPluginName() + " v" + this.getVersion());
	}


	/**
	 * Check if player has block place permission at location as allowed by plugin
	 * @param player the player to test for block place permission
	 * @param location the location to test for block place permission
	 * @return {@code true} if this plugin allows the player to place blocks at the given location,
	 * 			else {@code false}
	 */
	public abstract boolean hasPlacePermission(final Player player, final Location location);


	/**
	 * Check if player has chest access permission at location as allowed by plugin
	 * @param player the player to test for chest access permission
	 * @param location the location to test for chest access permisssion
	 * @return {@code true} if this plugin allows the player chest access permission at the given location
	 */
	public abstract boolean hasChestPermission(final Player player, final Location location);	


	/**
	 * Get plugin name
	 * @return the name of the plugin
	 */
	public final String getPluginName() {
		return this.pluginName;
	}


	public final String getVersion() {

		if (plugin.getServer().getPluginManager().getPlugin(this.getPluginName()) != null) {
			return plugin.getServer().getPluginManager().getPlugin(this.getPluginName()).getDescription().getVersion();
		}
		return " (unknown version)";
	}


	/**
	 * Check if protection plugin is enabled for check on place in config
	 */
	private boolean isConfigEnabledPlace() {

		// if plugin is not enabled in config, return false
		return (plugin.getConfig().getBoolean("protection-plugins." + this.getPluginName() + ".check-on-place"));
	}


	/**
	 * Check if protection plugin is enabled for check on access in config
	 * @return {@code true} if the protection plugin is enabled for check on access, {@code false} if not
	 */
	public final boolean isConfigEnabledAccess() {

		// if plugin is not enabled in config, return false
		return (plugin.getConfig().getBoolean("protection-plugins." + this.getPluginName() + ".check-on-access"));
	}


	/**
	 * Check if protection plugin is installed and operational
	 * @return {@code true} if the protection plugin is installed, {@code false} if it is not
	 */
	public final boolean isInstalled() {

		// get reference to plugin
		Plugin testPlugin = plugin.getServer().getPluginManager().getPlugin(this.getPluginName());

		// if plugin reference is not null and plugin is enabled, return true; else false
		return (testPlugin != null && testPlugin.isEnabled());
	}


	/**
	 * Output the detected installed plugins to the log on plugin start
	 */
	public static void reportInstalled() {

		for (ProtectionPlugin pp : ProtectionPlugin.values()) {
			if (pp.isInstalled()) {
				plugin.getLogger().info(pp.getPluginName() + " v" + pp.getVersion() + " detected.");
			}
		}		
	}


	public static ProtectionPlugin allowChestPlacement(final Player player, final Block block) {

		// iterate through protection plugins
		for (ProtectionPlugin pp : ProtectionPlugin.values()) {

			// if plugin is installed and in check-on-place list, check required permission
			if (pp.isInstalled() && pp.isConfigEnabledPlace()) {

				// if require build is configured true, check for build permission
				if (plugin.getConfig().getBoolean("require-build-permission")) {

					// if build permission is denied, return ProtectionPlugin object
					if (!pp.hasPlacePermission(player, block.getLocation())) {
						return pp;
					}
				}

				// otherwise if chest access permission is denied, return ProtectionPlugin object
				else if (!pp.hasChestPermission(player, block.getLocation())) {
					return pp;
				}
			}
		}

		// if placement is allowed by all protection plugins, return null
		return null;
	}


	public static ProtectionPlugin allowChestAccess(final Player player, final Block block) {

		// iterate through protection plugins
		for (ProtectionPlugin pp : ProtectionPlugin.values()) {

			// if plugin is installed and in check-on-access list, check for access permission
			if (pp.isInstalled() && pp.isConfigEnabledAccess()) {

				// if access permission is denied, return ProtectionPlugin object
				if (!pp.hasChestPermission(player, block.getLocation())) {
					return pp;
				}
			}
		}

		// if access is allowed by all protection plugins, return null
		return null;
	}

}
