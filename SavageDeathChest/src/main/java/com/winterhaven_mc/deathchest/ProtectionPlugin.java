package com.winterhaven_mc.deathchest;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.winterhaven_mc.roadblock.PublicAPI;

public enum ProtectionPlugin {
	
	GRIEFPREVENTION("GriefPrevention") {
		@Override
		public boolean hasPermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
				if (claim != null) {
					String gpErrorMessage = claim.allowContainers(player);
					if (gpErrorMessage != null) {
						return false;
					}
				}
			}
			return true;
		}
	},
	
	PRECIOUSSTONES("PreciousStones") {
		@Override
		public boolean hasPermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				if (!PreciousStones.API().canPlace(player, location)) {
					return false;
				}
			}
			return true;
		}
	},
	
	ROADBLOCK("RoadBlock") {
		@Override
		public boolean hasPermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				
				int maxDepth = plugin.getConfig().getInt("search-distance");
				
				if (PublicAPI.isRoadBelow(location, maxDepth)) {
					return false;
				}
			}
			return true;
		}
	},
	
	TOWNY("Towny") {
		@SuppressWarnings("deprecation")
		@Override
		public boolean hasPermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				if (!PlayerCacheUtil.getCachePermission(player, location, Material.CHEST.getId(),
						(byte)0, TownyPermission.ActionType.SWITCH)) {
					return false;
				}
			}
			return true;
		}
	},
	
	WORLDGUARD("WorldGuard") {
		@Override
		public boolean hasPermission(Player player, Location location) {
			
			// only perform check if plugin is enabled
			if (this.isEnabled()) {
				
				// get reference to worldguard plugin
				WorldGuardPlugin wg = WGBukkit.getPlugin();

				// get worldguard version string
				String wgVersion = wg.getDescription().getVersion();

				// if worldguard version 5, use canBuild method
				if (wgVersion.startsWith("5.")) {

					if (!WGBukkit.getPlugin().canBuild(player, location)) {
						return false;
					}
				}
				// if worldguard version 6, use testBlockPlace method
				else if (wgVersion.startsWith("6.")) {

					if (!wg.createProtectionQuery().testBlockPlace(player, location, Material.CHEST)) {
						return false;
					}
				}
			}
			return true;
		}
	};

	// static reference to main class
	private static PluginMain plugin = PluginMain.instance;
	
	// protection plugin name
	private String name;
	
	private boolean enabled = false;
	
	/**
	 * Enum constructor
	 * @param name
	 */
	private ProtectionPlugin(String name) {
		
		// set plugin name field
		this.name = name;
		
	}
	
	
	/**
	 * Check if player has chest place or access permission at location as allowed by plugin
	 * @param player
	 * @param location
	 * @return
	 */
	public abstract boolean hasPermission(Player player, Location location);

	
	/**
	 * Get plugin name
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	
	/**
	 * Check if protection plugin is listed in config file
	 * @return
	 */
	public boolean isIgnored() {
		if (plugin.getConfig().getStringList("ignored-plugins").contains(this.getName())) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Check if protection plugin is enabled
	 * @return
	 */
	public boolean isEnabled() {
		return this.enabled;
	}
	
	
	/**
	 * Set enabled field
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	
	/**
	 * Check if protection plugin is enabled
	 * @return
	 */
	public boolean testEnabled() {
		Plugin testPlugin = plugin.getServer().getPluginManager().getPlugin(this.getName());
		if (testPlugin != null && testPlugin.isEnabled()) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Set protection plugin enabled status and output to log
	 */
	public static void detectEnabled() {
		
		for (ProtectionPlugin pp : ProtectionPlugin.values()) {
			if (!pp.isIgnored()) {
				if (pp.testEnabled()) {
					pp.setEnabled(true);
					plugin.getLogger().info("Respecting " + pp.getName() + " protected areas.");
				}
				else {
					pp.setEnabled(false);
				}
			}
			else {
				pp.setEnabled(false);
				plugin.getLogger().info("Ignoring " + pp.getName() + " protected areas.");
			}
		}
	}

}
