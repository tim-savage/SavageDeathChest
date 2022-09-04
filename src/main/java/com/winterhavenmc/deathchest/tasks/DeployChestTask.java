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

package com.winterhavenmc.deathchest.tasks;

import com.winterhavenmc.deathchest.PluginMain;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;


public class DeployChestTask extends BukkitRunnable {

	private final PluginMain plugin;
	private final Player player;
	private final Collection<ItemStack> droppedItems;


	public DeployChestTask(final PluginMain plugin, final Player player, final Collection<ItemStack> droppedItems) {
		this.plugin = plugin;
		this.player = player;
		this.droppedItems = droppedItems;
	}

	public void run() {
		plugin.chestManager.getDeploymentFactory().createDeployment(plugin, player, droppedItems).deploy();
	}

}
