package com.winterhavenmc.deathchest.chests.search;

import com.winterhavenmc.deathchest.PluginMain;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * An enum whose values represent supported block protection plugins and includes methods to
 * query player permission to place blocks or access chests at a given location.
 */
public enum ProtectionPlugin {

//	FACTIONS("Factions") {
//
//		@Override
//		public final boolean hasPlacePermission(final Player player, final Location location) {
//
//			// use try..catch block to gracefully handle exceptions thrown by protection plugin
//			try {
//				if (!EnginePermBuild.canPlayerBuildAt(player, PS.valueOf(location), false)) {
//					return false;
//				}
//
//			}
//			catch (Exception e) {
//				logPlaceError();
//			}
//
//			return true;
//		}
//
//		@Override
//		public final boolean hasChestPermission(final Player player, final Location location) {
//
//			// use try..catch block to gracefully handle exceptions thrown by protection plugin
//			try {
//				if (!EnginePermBuild.useBlock(player, location.getBlock(), Material.CHEST, false)) {
////				if (!EngineMain.playerCanUseItemHere(player, PS.valueOf(location), Material.CHEST, false)) {
//					return false;
//				}
//			}
//			catch (Exception e) {
//				logAccessError();
//			}
//
//			return true;
//		}
//
//	},

	GRIEFPREVENTION("GriefPrevention") {
		@Override
		public final boolean hasPlacePermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
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
				if (PreciousStones.API().flagAppliesToPlayer(player, FieldFlag.PROTECT_INVENTORIES,location)) {
					return false;
				}
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
		@Override
		public final boolean hasPlacePermission(final Player player, final Location location) {

			// use try..catch block to gracefully handle exceptions thrown by protection plugin
			try {
				return PlayerCacheUtil.getCachePermission(player, location,
						Material.CHEST, TownyPermission.ActionType.BUILD);
			}
			catch (Exception e) {
				logPlaceError();
			}

			return true;
		}

		@Override
		public final boolean hasChestPermission(final Player player, final Location location) {

			// only perform check if plugin is installed
			if (this.isInstalled()) {
				try {
					return PlayerCacheUtil.getCachePermission(player, location,
							Material.CHEST, TownyPermission.ActionType.SWITCH);
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

			final RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();

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
		public final boolean hasChestPermission(final Player player, final Location location) {

			final RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();

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
	};

	// static reference to main class
	private final static PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	// protection plugin name
	private final String pluginName;


	/**
	 * Enum constructor
	 *
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
		plugin.getLogger().warning("An error occurred checking for block place permission "
				+ "with " + this.getPluginName() + " v" + this.getVersion());
	}


	/**
	 * Log errors that occur when checking for chest access permission
	 */
	void logAccessError() {
		plugin.getLogger().warning("An error occurred checking for chest access permission "
				+ "with " + this.getPluginName() + " v" + this.getVersion());
	}


	/**
	 * Check if player has block place permission at location as allowed by plugin
	 *
	 * @param player   the player to test for block place permission
	 * @param location the location to test for block place permission
	 * @return {@code true} if this plugin allows the player to place blocks at the given location,
	 * else {@code false}
	 */
	public abstract boolean hasPlacePermission(final Player player, final Location location);


	/**
	 * Check if player has chest access permission at location as allowed by plugin
	 *
	 * @param player   the player to test for chest access permission
	 * @param location the location to test for chest access permisssion
	 * @return {@code true} if this plugin allows the player chest access permission at the given location
	 */
	public abstract boolean hasChestPermission(final Player player, final Location location);


	/**
	 * Get plugin name
	 *
	 * @return the name of the plugin
	 */
	public final String getPluginName() {
		return this.pluginName;
	}


	/**
	 * Override toString() method to print plugin name
	 * @return String - the plugin name
	 */
	@Override
	public String toString() {
		return this.pluginName;
	}


	/**
	 * Get plugin version
	 *
	 * @return String - plugin version
	 */
	private String getVersion() {

		Plugin thisPlugin = plugin.getServer().getPluginManager().getPlugin(this.getPluginName());

		if (thisPlugin != null) {
			return thisPlugin.getDescription().getVersion();
		}
		return " (unknown version)";
	}


	/**
	 * Check if protection plugin is configured to be ignored on death chest placement
	 *
	 * @return {@code true} if the protection plugin is enabled for check on placement, {@code false} if not
	 */
	private boolean isIgnoredOnPlace() {

		// if plugin is not enabled in config, return false
		return (plugin.getConfig().getBoolean("protection-plugins." + this.getPluginName() + ".ignore-on-place"));
	}


	/**
	 * Check if protection plugin is configured to be ignored on death chest access
	 *
	 * @return {@code true} if the protection plugin is enabled for check on access, {@code false} if not
	 */
	private boolean isIgnoredOnAccess() {

		// if plugin is not enabled in config, return false
		return (plugin.getConfig().getBoolean("protection-plugins." + this.getPluginName() + ".ignore-on-access"));
	}


	/**
	 * Check if protection plugin is installed and operational
	 *
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

			// if plugin is installed and ignore-on-place is false, check required permission
			if (pp.isInstalled() && !pp.isIgnoredOnPlace()) {

				// if build permission is denied, return ProtectionPlugin object
				if (!pp.hasPlacePermission(player, block.getLocation())) {
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

			// if plugin is installed and ignore-on-access is false, check for access permission
			if (pp.isInstalled() && !pp.isIgnoredOnAccess()) {

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
