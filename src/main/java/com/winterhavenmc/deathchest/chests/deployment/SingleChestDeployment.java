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
import com.winterhavenmc.deathchest.chests.ChestBlockType;
import com.winterhavenmc.deathchest.chests.ChestSign;
import com.winterhavenmc.deathchest.chests.ChestSize;
import com.winterhavenmc.deathchest.chests.DeathChest;
import com.winterhavenmc.deathchest.chests.search.QuadrantSearch;
import com.winterhavenmc.deathchest.chests.search.SearchResult;
import com.winterhavenmc.deathchest.chests.search.SearchResultCode;

import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.LinkedList;


public class SingleChestDeployment extends AbstractDeployment implements Deployment {

	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 * @param player the player for whom a death chest is being deployed
	 * @param droppedItems the player's death drops
	 */
	public SingleChestDeployment(PluginMain plugin, Player player, Collection<ItemStack> droppedItems) {
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

		SearchResult searchResult;

		// if require-chest option is enabled and player does not have permission override
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
				searchResult = new SearchResult(SearchResultCode.NO_REQUIRED_CHEST, remainingItems);
				this.finish(searchResult, new DeathChest(player));
				return searchResult;
			}
		}

		// search for valid chest location
		searchResult = new QuadrantSearch(plugin, player, ChestSize.SINGLE).execute();

		// create new deathChest object for player
		DeathChest deathChest = new DeathChest(player);

		// if search successful, place chest
		if (searchResult.getResultCode().equals(SearchResultCode.SUCCESS)) {

			// place chest at result location
			placeChest(player, deathChest, searchResult.getLocation(), ChestBlockType.RIGHT_CHEST);

			// set chest block state
			setChestBlockState(searchResult.getLocation().getBlock(), Chest.Type.SINGLE);

			// fill chest
			remainingItems = deathChest.fill(remainingItems);

			// place sign on chest
			new ChestSign(plugin, player, deathChest).place();
		}

		// set remaining items in result
		searchResult.setRemainingItems(remainingItems);

		// finish deployment
		this.finish(searchResult, deathChest);

		// return result
		return searchResult;
	}

}
