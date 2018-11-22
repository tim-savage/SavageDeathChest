package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.ProtectionPlugin;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;


/**
 * A class that encapsulates fields to be returned as the result of a chest search
 */
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

	/**
	 * Getter method for resultCode
	 * @return ResultCode - the result code currently set for this result object
	 */
	ResultCode getResultCode() {
		return resultCode;
	}

	/**
	 * Setter method for resultCode
	 * @param resultCode the result code to set for this result object
	 */
	void setResultCode(final ResultCode resultCode) {
		this.resultCode = resultCode;
	}

	/**
	 * Getter method for location
	 * @return Location - the location currently set for this result object
	 */
	Location getLocation() {
		return location;
	}

	/**
	 * Setter method for location
	 * @param location the location to set for this result object
	 */
	void setLocation(final Location location) {
		this.location = location.clone();
	}

	/**
	 * Getter method for protectionPlugin
	 * @return ProtectionPlugin - the protection plugin enum value currently set for this result object
	 */
	ProtectionPlugin getProtectionPlugin() {
		return protectionPlugin;
	}

	/**
	 * Setter method for protectionPlugin
	 * @param protectionPlugin the protection plugin enum value to set for this result object
	 */
	@SuppressWarnings("unused")
	void setProtectionPlugin(final ProtectionPlugin protectionPlugin) {
		this.protectionPlugin = protectionPlugin;
	}

	/**
	 * Getter method for remainingItems
	 * @return Collection of ItemStack - the remaining items currently set for this result object
	 */
	Collection<ItemStack> getRemainingItems() {
		return remainingItems;
	}

	/**
	 * Setter method for remainingItems
	 * @param remainingItems a Collection of ItemStacks to set as the the remaining items for this result object
	 */
	void setRemainingItems(final Collection<ItemStack> remainingItems) {
		this.remainingItems = remainingItems;
	}

}
