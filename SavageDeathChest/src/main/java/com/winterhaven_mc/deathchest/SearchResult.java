package com.winterhaven_mc.deathchest;

import org.bukkit.Location;

public enum SearchResult {

	SUCCESS,
	PROTECTION_PLUGIN,
	NON_REPLACEABLE_BLOCK,
	ABOVE_GRASS_PATH,
	ADJACENT_CHEST;
	

	private Location location;
	private ProtectionPlugin protectionPlugin;

	public Location getLocation() {
		return location;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public ProtectionPlugin getProtectionPlugin() {
		return protectionPlugin;
	}

	public void setProtectionPlugin(final ProtectionPlugin protectionPlugin) {
		this.protectionPlugin = protectionPlugin;
	}
	
}
