package com.winterhaven_mc.deathchest;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

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
import com.winterhaven_mc.proclaim.SimpleAPI;
import com.winterhaven_mc.roadblock.PublicAPI;

public enum ProtectionPlugin {
	
	FACTIONS("Factions") {
		
		@Override
		public boolean hasPlacePermission(Player player, Location location) {

			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				try {
					if (!EngineMain.canPlayerBuildAt(player, PS.valueOf(location), false)) {
						return false;
					}

				}
				catch (Exception e) {
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for place permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}
		
		@Override
		public boolean hasChestPermission(Player player, Location location) {

			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				try {
					if (!EngineMain.playerCanUseItemHere(player, PS.valueOf(location), Material.CHEST, false)) {
						return false;
					}
				}
				catch (Exception e) {
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for access permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}
		
	},
	
	GRIEFPREVENTION("GriefPrevention") {
		
		@Override
		public boolean hasPlacePermission(Player player, Location location) {

			// only perform check if plugin is enabled
			if (this.isEnabled()) {
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
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for place permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}
		
		@Override
		public boolean hasChestPermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
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
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for access permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}
		
	},
	
	PRECIOUSSTONES("PreciousStones") {
	
		@Override
		public boolean hasPlacePermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				try {
					if (! PreciousStones.API().canPlace(player, location)) {
						return false;
					}
				}
				catch (Exception e) {
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for place permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}
		
		@Override
		public boolean hasChestPermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				try {
					if (PreciousStones.API().flagAppliesToPlayer(player, FieldFlag.PROTECT_INVENTORIES, location)) {
						return false;
					}
				}
				catch (Exception e) {
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for access permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}
		
	},
	
	PROCLAIM("ProClaim") {
		
		@Override
		public boolean hasPlacePermission(Player player, Location location) {

			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				try {
					return SimpleAPI.hasBuildTrust(player, location);
				}
				catch (Exception e) {
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for place permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}
		
		@Override
		public boolean hasChestPermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				try {
					return SimpleAPI.hasContainerTrust(player, location);
				}
				catch (Exception e) {
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for access permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}
		
	},
	
	ROADBLOCK("RoadBlock") {
		
		@Override
		public boolean hasPlacePermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				
				try {
					int maxDepth = plugin.getConfig().getInt("search-distance");
					
					if (PublicAPI.isRoadBelow(location, maxDepth)) {
						return false;
					}
				}
				catch (Exception e) {
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for place permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}
		
		@Override
		public boolean hasChestPermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				
				try {
					int maxDepth = plugin.getConfig().getInt("search-distance");
					
					if (PublicAPI.isRoadBelow(location, maxDepth)) {
						return false;
					}
				}
				catch (Exception e) {
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for access permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}
		
	},
	
	TOWNY("Towny") {
		
		@SuppressWarnings("deprecation")
		@Override
		public boolean hasPlacePermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				try {
					if (!PlayerCacheUtil.getCachePermission(player, location, Material.CHEST.getId(),
							(byte)0, TownyPermission.ActionType.BUILD)) {
						return false;
					}
				}
				catch (Exception e) {
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for place permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}

		@SuppressWarnings("deprecation")
		@Override
		public boolean hasChestPermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				try {
					if (!PlayerCacheUtil.getCachePermission(player, location, Material.CHEST.getId(),
							(byte)0, TownyPermission.ActionType.SWITCH)) {
						return false;
					}
				}
				catch (Exception e) {
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for access permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}

	},
	
	WORLDGUARD("WorldGuard") {
		
		@Override
		public boolean hasPlacePermission(Player player, Location location) {

			// only perform check if plugin is enabled
			if (this.isEnabled()) {

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
					if (plugin.debug) {
						plugin.getLogger().warning("An error occured checking for place permission "
								+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
			}
			return true;
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public boolean hasChestPermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				
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
					if (plugin.debug) {
					plugin.getLogger().warning("An error occured checking for access permission "
							+ "with " + this.getPluginName() + " v" + this.getVersion());
					}
				}
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
	 * @param pluginName
	 */
	private ProtectionPlugin(String pluginName) {
		
		// set plugin name field
		this.pluginName = pluginName;
		
	}
	
	/**
	 * Check if player has block place permission at location as allowed by plugin
	 * @param player
	 * @param location
	 * @return
	 */
	public abstract boolean hasPlacePermission(Player player, Location location);
	
	
	/**
	 * Check if player has chest access permission at location as allowed by plugin
	 * @param player
	 * @param location
	 * @return
	 */
	public abstract boolean hasChestPermission(Player player, Location location);	
	
	/**
	 * Get plugin name
	 * @return
	 */
	public String getPluginName() {
		return this.pluginName;
	}

	
	/**
	 * Check if protection plugin is enabled
	 * @return
	 */
	public boolean isEnabled() {
		
		Plugin testPlugin = plugin.getServer().getPluginManager().getPlugin(this.getPluginName());
		if (testPlugin != null && testPlugin.isEnabled()) {
			return true;
		}
		return false;
	}

	
	public static ProtectionPlugin allowChestPlacement(Player player, Block block) {
		
		// iterate through protection plugins
		for (ProtectionPlugin pp : ProtectionPlugin.values()) {
			
			// if plugin is in check-on-place list, check required permission
			if (plugin.getConfig().getStringList("check-on-place").contains(pp.getPluginName())) { 
			
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

	
	public static ProtectionPlugin allowChestAccess(Player player, Block block) {
		
		// iterate through protection plugins
		for (ProtectionPlugin pp : ProtectionPlugin.values()) {

			// if plugin is in check-on-access list, and plugin denies access, set allow false
			if (plugin.getConfig().getStringList("check-on-access").contains(pp.getPluginName()) 
					&& !pp.hasChestPermission(player, block.getLocation())) {
				return pp;
			}
		}
		return null;
	}
	
	public static void reportInstalled() {
		
		for (ProtectionPlugin pp : ProtectionPlugin.values()) {
			if (pp.isEnabled()) {
				plugin.getLogger().info(pp.getPluginName() + " v" + pp.getVersion() + " detected.");
			}
		}		
	}
	
	public String getVersion() {
		
		if (plugin.getServer().getPluginManager().getPlugin(this.getPluginName()) != null) {
			return plugin.getServer().getPluginManager().getPlugin(this.getPluginName()).getDescription().getVersion();
		}
		return "unknown";
	}

}
