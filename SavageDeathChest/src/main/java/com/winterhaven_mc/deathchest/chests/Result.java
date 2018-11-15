package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.ProtectionPlugin;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

class Result {

	private ResultCode resultCode;
	private Location location;
	private ProtectionPlugin protectionPlugin;
	private Collection<ItemStack> remainingItems = new ArrayList<>();


	/**
	 * Class constructor
	 */
	Result() { }


	/**
	 * Class constructor
	 * @param resultCode initial result code for result
	 */
	Result(final ResultCode resultCode) {
		this.resultCode = resultCode;
	}

	ResultCode getResultCode() { return resultCode; }

	void setResultCode(final ResultCode resultCode) { this.resultCode = resultCode; }

	Location getLocation() {
		return location;
	}

	void setLocation(final Location location) {
		this.location = location.clone();
	}

	ProtectionPlugin getProtectionPlugin() {
		return protectionPlugin;
	}

	@SuppressWarnings("unused")
	void setProtectionPlugin(final ProtectionPlugin protectionPlugin) {
		this.protectionPlugin = protectionPlugin;
	}

	Collection<ItemStack> getRemainingItems() { return remainingItems; }

	void setRemainingItems(Collection<ItemStack> remainingItems) { this.remainingItems = remainingItems; }

}
