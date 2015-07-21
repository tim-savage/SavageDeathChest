package com.winterhaven_mc.deathchest;

import org.bukkit.Location;

public enum SearchResult {

	SUCCESS,
	PROTECTION_PLUGIN,
	NON_REPLACEABLE_BLOCK,
	ADJACENT_CHEST;
	

	private Location location;
	private ProtectionPlugin protectionPlugin;

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public ProtectionPlugin getProtectionPlugin() {
		return protectionPlugin;
	}

	public void setProtectionPlugin(ProtectionPlugin protectionPlugin) {
		this.protectionPlugin = protectionPlugin;
	}
	
}
