package com.winterhaven_mc.deathchest.chests.search;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;


/**
 * A class that encapsulates fields to be returned
 * as the result of a search for a valid chest location
 */
public final class SearchResult {

	private ResultCode resultCode;
	private Location location;
	private ProtectionPlugin protectionPlugin;
	private Collection<ItemStack> remainingItems;


	/**
	 * Class constructor
	 *
	 * @param resultCode initial result code for result
	 */
	SearchResult(final ResultCode resultCode) {
		this.resultCode = resultCode;
		this.location = null;
		this.protectionPlugin = null;
		this.remainingItems = new ArrayList<>();
	}


	/**
	 * Class constructor
	 *
	 * @param resultCode initial result code for result
	 * @param remainingItems player dropped items remaining
	 *
	 */
	public SearchResult(final ResultCode resultCode, final Collection<ItemStack> remainingItems) {
		this.resultCode = resultCode;
		this.location = null;
		this.protectionPlugin = null;
		this.remainingItems = remainingItems;
	}


	/**
	 * Class constructor
	 *
	 * @param resultCode initial result code for result
	 * @param location chest location
	 */
	SearchResult(final ResultCode resultCode, final Location location) {
		this.resultCode = resultCode;
		this.location = location;
		this.protectionPlugin = null;
		this.remainingItems = new ArrayList<>();
	}


	/**
	 * Class constructor
	 *
	 * @param resultCode initial result code for result
	 * @param protectionPlugin plugin that prevented chest placement
	 */
	SearchResult(final ResultCode resultCode, final ProtectionPlugin protectionPlugin) {
		this.resultCode = resultCode;
		this.location = null;
		this.protectionPlugin = protectionPlugin;
		this.remainingItems = new ArrayList<>();
	}


	/**
	 * Getter method for resultCode
	 *
	 * @return ResultCode - the result code currently set for this result object
	 */
	public final ResultCode getResultCode() {
		return resultCode;
	}


	/**
	 * Setter method for resultCode
	 * @param resultCode - the result code to set for this result object
	 */
	public final void setResultCode(ResultCode resultCode) {
		this.resultCode = resultCode;
	}


	/**
	 * Getter method for location
	 *
	 * @return Location - the location currently set for this result object
	 */
	public final Location getLocation() {
		return location;
	}


	/**
	 * Setter method for location
	 * @param location - the locatin to set for this result object
	 */
	final void setLocation(Location location) {
		this.location = location;
	}


	/**
	 * Getter method for protectionPlugin
	 *
	 * @return ProtectionPlugin - the protection plugin enum value currently set for this result object
	 */
	public final ProtectionPlugin getProtectionPlugin() {
		return protectionPlugin;
	}


	/**
	 * Setter method for protectionPlugin
	 * @param protectionPlugin - the protection plugin enum value to set for this result object
	 */
	@SuppressWarnings("unused")
	final void setProtectionPlugin(ProtectionPlugin protectionPlugin) {
		this.protectionPlugin = protectionPlugin;
	}


	/**
	 * Getter method for remainingItems
	 *
	 * @return Collection of ItemStack - the remaining items currently set for this result object
	 */
	public final Collection<ItemStack> getRemainingItems() {
		return remainingItems;
	}


	/**
	 * Setter method for remainingItems
	 * @param remainingItems Collection of ItemStack - the remaining items to set for this result object
	 */
	public final void setRemainingItems(Collection<ItemStack> remainingItems) {
		this.remainingItems = remainingItems;
	}

}
