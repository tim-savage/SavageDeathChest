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
import com.winterhavenmc.deathchest.chests.ChestSize;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.LinkedList;


public class DeploymentFactory {

	public Deployment createDeployment(final PluginMain plugin, final Player player, final Collection<ItemStack> droppedItems) {

		// combine stacks of same items where possible
		Collection<ItemStack> consolidatedItems = consolidateItemStacks(droppedItems);

		// get required chest size
		ChestSize chestSize = ChestSize.selectFor(consolidatedItems.size());

		// deploy appropriately sized chest
		if (chestSize.equals(ChestSize.SINGLE) || !player.hasPermission("deathchest.doublechest")) {
			return new SingleChestDeployment(plugin, player, consolidatedItems);
		}
		else {
			return new DoubleChestDeployment(plugin, player, consolidatedItems);
		}
	}


	/**
	 * Combine ItemStacks of same material up to max stack size
	 *
	 * @param itemStacks Collection of ItemStacks to combine
	 * @return Collection of ItemStack with same materials combined
	 */
	private Collection<ItemStack> consolidateItemStacks(final Collection<ItemStack> itemStacks) {

		final Collection<ItemStack> returnList = new LinkedList<>();

		for (ItemStack itemStack : itemStacks) {
			if (itemStack == null) {
				continue;
			}

			for (ItemStack checkStack : returnList) {
				if (checkStack == null) {
					continue;
				}
				if (checkStack.isSimilar(itemStack)) {
					int transferAmount = Math.min(itemStack.getAmount(), checkStack.getMaxStackSize() - checkStack.getAmount());
					itemStack.setAmount(itemStack.getAmount() - transferAmount);
					checkStack.setAmount(checkStack.getAmount() + transferAmount);
				}
			}
			if (itemStack.getAmount() > 0) {
				returnList.add(itemStack);
			}
		}
		return returnList;
	}

}
