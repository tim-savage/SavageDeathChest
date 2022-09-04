/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathchest.chests.deployment;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.chests.*;
import com.winterhavenmc.deathchest.chests.search.QuadrantSearch;
import com.winterhavenmc.deathchest.chests.search.SearchResult;
import com.winterhavenmc.deathchest.chests.search.SearchResultCode;

import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.LinkedList;


public class DoubleChestDeployment extends AbstractDeployment implements Deployment {

	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 * @param player the player for whom a death chest is being deployed
	 * @param droppedItems the player's death drops
	 */
	public DoubleChestDeployment(final PluginMain plugin, final Player player, final Collection<ItemStack> droppedItems) {
		super(plugin, player, droppedItems);
	}


	/**
	 * Execute the deployment of death chest
	 *
	 * @return the result of the attempted death chest deployment
	 */
	@Override
	public SearchResult deploy() {

		// make copy of dropped items
		Collection<ItemStack> remainingItems = new LinkedList<>(droppedItems);

		// search for valid chest location
		SearchResult searchResult = new QuadrantSearch(plugin, player, ChestSize.DOUBLE).execute();

		// if only single chest location found, deploy single chest
		if (searchResult.getResultCode().equals(SearchResultCode.PARTIAL_SUCCESS)) {
			searchResult = new SingleChestDeployment(plugin, player, remainingItems).deploy();

			// if single chest deployment was successful, set PARTIAL_SUCCESS result
			if (searchResult.getResultCode().equals(SearchResultCode.SUCCESS)) {
				searchResult.setResultCode(SearchResultCode.PARTIAL_SUCCESS);
			}

			// return result
			return searchResult;
		}

		// if search failed, return result with remaining items
		if (!searchResult.getResultCode().equals(SearchResultCode.SUCCESS)) {
			searchResult.setRemainingItems(remainingItems);
			return searchResult;
		}

		// if require-chest option is enabled
		// and player does not have permission override
		if (chestRequired()) {

			// check that player has chest in inventory
			if (containsChest(remainingItems)) {

				// if consume-required-chest configured true: remove one chest from remaining items
				if (plugin.getConfig().getBoolean("consume-required-chest")) {
					remainingItems = removeOneChest(remainingItems);
				}
			}
			// else return NO_CHEST result
			else {
				searchResult.setResultCode(SearchResultCode.NO_CHEST);
				searchResult.setRemainingItems(remainingItems);
				return searchResult;
			}
		}

		// create new deathChest object for player
		DeathChest deathChest = new DeathChest(player);

		// place chest at result location
		placeChest(player, deathChest, searchResult.getLocation(), ChestBlockType.RIGHT_CHEST);

		// attempt to place second chest

		// if require-chest option is enabled
		// and player does not have permission override
		if (plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest")) {

			// check that player has chest in inventory
			if (containsChest(remainingItems)) {
				// if consume-required-chest configured true: remove one chest from remaining items
				if (plugin.getConfig().getBoolean("consume-required-chest")) {
					remainingItems = removeOneChest(remainingItems);
				}
			}
			// else return new PARTIAL_SUCCESS result with location and remaining items after filling chest
			else {
				searchResult.setResultCode(SearchResultCode.PARTIAL_SUCCESS);
				searchResult.setRemainingItems(deathChest.fill(remainingItems));
				return searchResult;
			}
		}

		// place chest at result location
		placeChest(player, deathChest, LocationUtilities.getLocationToRight(searchResult.getLocation()), ChestBlockType.LEFT_CHEST);

		// set chest type to left/right for double chest

		// set chest block state
		setChestBlockState(searchResult.getLocation().getBlock(), Chest.Type.RIGHT);
		setChestBlockState(LocationUtilities.getLocationToRight(searchResult.getLocation()).getBlock(), Chest.Type.LEFT);

		// put remaining items in result after filling chest
		searchResult.setRemainingItems(deathChest.fill(remainingItems));

		// place sign on chest
		new ChestSign(plugin, player, deathChest).place();

		// place hologram above chest
//		UUID hologramUid = new Hologram(plugin, player, searchResult.getLocation(), ChestSize.DOUBLE).place();

		// put hologramUid in search result
//		searchResult.setHologramUid(hologramUid);

		// finish deployment
		this.finish(searchResult, deathChest);

		// return result
		return searchResult;
	}

}
