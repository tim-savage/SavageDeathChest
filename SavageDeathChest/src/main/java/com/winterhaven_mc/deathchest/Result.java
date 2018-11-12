package com.winterhaven_mc.deathchest;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

public class Result {

	private ResultCode resultCode;
	private Location location;
	private ProtectionPlugin protectionPlugin;
	private Collection<ItemStack> remainingItems = new ArrayList<>();


	/**
	 * Class constructor
	 * @param resultCode initial result code for result
	 */
	public Result(final ResultCode resultCode) {
		this.resultCode = resultCode;
	}


	public ResultCode getResultCode() { return resultCode; }

	public void setResultCode(final ResultCode resultCode) { this.resultCode = resultCode; }

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

	public Collection<ItemStack> getRemainingItems() { return remainingItems; }

	public void setRemainingItems(Collection<ItemStack> remainingItems) { this.remainingItems = remainingItems; }

}
