package com.winterhavenmc.deathchest.chests.search;

import com.winterhavenmc.deathchest.protectionchecks.ProtectionPlugin;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.LinkedList;


/**
 * A class that encapsulates fields to be returned
 * as the result of a search for a valid chest location
 */
public final class SearchResult {

	private SearchResultCode searchResultCode;
	private Location location;
	private ProtectionPlugin protectionPlugin;
	private Collection<ItemStack> remainingItems;


	/**
	 * Class constructor
	 *
	 * @param searchResultCode initial result code for result
	 */
	SearchResult(final SearchResultCode searchResultCode) {
		this.searchResultCode = searchResultCode;
		this.location = null;
		this.protectionPlugin = null;
		this.remainingItems = new LinkedList<>();
	}


	/**
	 * Class constructor
	 *
	 * @param searchResultCode initial result code for result
	 * @param remainingItems player dropped items remaining
	 *
	 */
	public SearchResult(final SearchResultCode searchResultCode, final Collection<ItemStack> remainingItems) {
		this.searchResultCode = searchResultCode;
		this.location = null;
		this.protectionPlugin = null;
		this.remainingItems = remainingItems;
	}


	/**
	 * Getter method for searchResultCode
	 *
	 * @return SearchResultCode - the result code currently set for this result object
	 */
	public SearchResultCode getResultCode() {
		return searchResultCode;
	}


	/**
	 * Setter method for searchResultCode
	 * @param searchResultCode - the result code to set for this result object
	 */
	public void setResultCode(final SearchResultCode searchResultCode) {
		this.searchResultCode = searchResultCode;
	}


	/**
	 * Getter method for location
	 *
	 * @return Location - the location currently set for this result object
	 */
	public Location getLocation() {
		return location;
	}


	/**
	 * Setter method for location
	 * @param location - the location to set for this result object
	 */
	void setLocation(final Location location) {
		this.location = location;
	}


	/**
	 * Getter method for protectionPlugin
	 *
	 * @return ProtectionPlugin - the protection plugin enum value currently set for this result object
	 */
	public ProtectionPlugin getProtectionPlugin() {
		return protectionPlugin;
	}


	/**
	 * Setter method for protectionPlugin
	 * @param protectionPlugin - the protection plugin enum value to set for this result object
	 */
	@SuppressWarnings("unused")
	void setProtectionPlugin(final ProtectionPlugin protectionPlugin) {
		this.protectionPlugin = protectionPlugin;
	}


	/**
	 * Getter method for remainingItems
	 *
	 * @return Collection of ItemStack - the remaining items currently set for this result object
	 */
	public Collection<ItemStack> getRemainingItems() {
		return remainingItems;
	}


	/**
	 * Setter method for remainingItems
	 * @param remainingItems Collection of ItemStack - the remaining items to set for this result object
	 */
	public void setRemainingItems(final Collection<ItemStack> remainingItems) {
		this.remainingItems = remainingItems;
	}

}
