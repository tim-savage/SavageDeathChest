package com.winterhaven_mc.deathchest;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public enum SearchResult {

	SUCCESS,
	PARTIAL_SUCCCESS,
	PROTECTION_PLUGIN,
	NON_REPLACEABLE_BLOCK,
	ABOVE_GRASS_PATH,
	ADJACENT_CHEST,
	NO_CHEST;
	

	private Location location;
	private ProtectionPlugin protectionPlugin;
	private List<ItemStack> remainingItems = new ArrayList<>();

	public Location getLocation() {
		return location;
	}

	public void setLocation(final Location location) {
		this.location = location.clone();
	}

	public ProtectionPlugin getProtectionPlugin() {
		return protectionPlugin;
	}

	public void setProtectionPlugin(final ProtectionPlugin protectionPlugin) {
		this.protectionPlugin = protectionPlugin;
	}

	public List<ItemStack> getRemainingItems() { return remainingItems; }

	public void setRemainingItems(List<ItemStack> remainingItems) { this.remainingItems = remainingItems; }

}
