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

package com.winterhavenmc.deathchest.permissions;

import com.winterhavenmc.deathchest.chests.DeathChest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;


/**
 * A class that defines the action to be taken for an inventory open permission check
 */
public class InventoryOpenAction implements ResultAction {

	@Override
	public void execute(final Cancellable event, final Player player, final DeathChest deathChest) {
		event.setCancelled(true);
		player.openInventory(deathChest.getInventory());
	}

}
