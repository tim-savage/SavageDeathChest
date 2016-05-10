package com.winterhaven_mc.deathchest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.World;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.winterhaven_mc.deathchest.PluginMain;

public class WorldManager {

	// reference to main class
	private final PluginMain plugin;
	
	// list of enabled world names
	private List<UUID> enabledWorldUIDs;
	
	// reference to MultiverseCore
	private final MultiverseCore mvCore;

	
	/**
	 * Class constructor
	 * @param plugin
	 */
	public WorldManager(PluginMain plugin) {
		
		// set reference to main class
		this.plugin = plugin;
		
		// get reference to Multiverse-Core if installed
		mvCore = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
		if (mvCore != null && mvCore.isEnabled()) {
			plugin.getLogger().info("Multiverse-Core detected.");
		}
		
		// populate enabled world UID field
		this.reload();
	}

	/**
	 * update enabledWorlds ArrayList field from config file settings
	 */
	public void reload() {
		
		// create empty list for enabled world UIDs
		List<UUID> list = new ArrayList<UUID>();
		
		// if configured list is empty, add all worlds to list
		if (plugin.getConfig().getStringList("enabled-worlds").isEmpty()) {
			for (World world : plugin.getServer().getWorlds()) {
				list.add(world.getUID());
			}
		}
		else {
			// add all configured worlds to list
			for (String worldName : plugin.getConfig().getStringList("enabled-worlds")) {
				
				// get world by name
				World world = plugin.getServer().getWorld(worldName);
				
				// only add world UID if it is not already in list and world is loaded
				if (world != null && !list.contains(world.getUID())) {
					list.add(world.getUID());
				}
			}
		}
		
		// remove configured list of disabled worlds from list
		for (String worldName : plugin.getConfig().getStringList("disabled-worlds")) {
			
			// get world by name
			World world = plugin.getServer().getWorld(worldName);
			
			// if world is not null remove UID from list
			if (world != null) {
				list.remove(world.getUID());
			}
		}
		
		// set enabledWorldUIDs field to list
		this.enabledWorldUIDs = list;
	}
	
	
	/**
	 * get list of enabled world names
	 * @return ArrayList of String enabledWorlds
	 */
	public List<String> getEnabledWorldNames() {
		
		// create empty list of string for return
		List<String> resultList = new ArrayList<String>();
		
		// iterate through list of enabled world UIDs
		for (UUID worldUID : this.enabledWorldUIDs) {
			
			// get world by UID
			World world = plugin.getServer().getWorld(worldUID);
			
			// if world is not null, add name to return list
			if (world != null) {
				resultList.add(world.getName());
			}
		}

		// return result list
		return resultList;
	}

	
	/**
	 * Check if a world is enabled by UID
	 * @param worldUID
	 * @return
	 */
	public boolean isEnabled(UUID worldUID) {
		
		// if worldUID is null return false
		if (worldUID == null) {
			return false;
		}
		
		return this.enabledWorldUIDs.contains(worldUID);
	}
	

	/**
	 * Check if a world is enabled by world object
	 * @param world
	 * @return
	 */
	public boolean isEnabled(World world) {
		
		// if world is null return false
		if (world == null) {
			return false;
		}
		
		return this.enabledWorldUIDs.contains(world.getUID());		
	}

	
	/**
	 * Check if a world is enabled by name
	 * @param worldName
	 * @return
	 */
	public boolean isEnabled(String worldName) {
		
		// if worldName is null or empty, return false
		if (worldName == null || worldName.isEmpty()) {
			return false;
		}
		
		// get world by name
		World world = plugin.getServer().getWorld(worldName);
		
		// if world is null, return false
		if (world == null) {
			return false;
		}

		return (this.enabledWorldUIDs.contains(world.getUID()));
	}
	
	
	/**
	 * Get world name from, using Multiverse alias if available
	 * @param world
	 * @return world name or multiverse alias as String
	 */
	public String getWorldName(World world) {
		
		// get bukkit world name
		String worldName = world.getName();
		
		// if Multiverse is enabled, get MultiverseWorld object
		if (mvCore != null && mvCore.isEnabled()) {
			
			MultiverseWorld mvWorld = mvCore.getMVWorldManager().getMVWorld(world);

			// if Multiverse alias is not null or empty, set world name to alias
			if (mvWorld != null  && mvWorld.getAlias() != null && !mvWorld.getAlias().isEmpty()) {
				worldName = mvCore.getMVWorldManager().getMVWorld(worldName).getAlias();
			}
		}

		// return the bukkit world name or Multiverse world alias
		return worldName;
	}
}
