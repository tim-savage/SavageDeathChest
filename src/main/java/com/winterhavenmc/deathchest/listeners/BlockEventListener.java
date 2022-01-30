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

package com.winterhavenmc.deathchest.listeners;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.permissions.BreakChestAction;
import com.winterhavenmc.deathchest.permissions.PermissionCheck;
import com.winterhavenmc.deathchest.permissions.ResultAction;
import com.winterhavenmc.deathchest.chests.DeathChest;

import com.winterhavenmc.deathchest.chests.LocationUtilities;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Collection;
import java.util.LinkedList;


/**
 * A class that contains {@code EventHandler} methods to process block related events
 */
public final class BlockEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;
	
	// reference to permissionCheck class
	private final PermissionCheck permissionCheck;

	final ResultAction breakChestAction = new BreakChestAction();


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public BlockEventListener(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// create instance of permissionCheck class
		this.permissionCheck = new PermissionCheck(plugin);

		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Block place event handler<br>
	 * prevent placing chests adjacent to existing death chest
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {

		final Block block = event.getBlock();
		final Location location = block.getLocation();

		// if placed block is not a chest, do nothing and return
		if (!block.getType().equals(Material.CHEST)) {
			return;
		}

		// check for adjacent death chests and cancel event if found
		if (plugin.chestManager.isChestBlockChest(LocationUtilities.getBlockToLeft(location))
				|| plugin.chestManager.isChestBlockChest(LocationUtilities.getBlockToRight(location))) {
			event.setCancelled(true);
		}
	}


	/**
	 * Block break event handler<br>
	 * Checks for ownership of death chests and prevents breakage by non-owners.<br>
	 * Listens at EventPriority.LOW to handle event before protection plugins
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(final BlockBreakEvent event) {

		// get instance of DeathChest from event block
		final DeathChest deathChest = plugin.chestManager.getChest(event.getBlock());

		// if death chest is null, do nothing and return
		if (deathChest == null) {
			return;
		}

		// get player from event
		final Player player = event.getPlayer();

		// do permissions checks and take appropriate action
		permissionCheck.performChecks(event, player, deathChest, breakChestAction);
	}


	/**
	 * Entity explode event handler<br>
	 * Make death chests explosion proof if chest-protection is enabled
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent event) {

		// if chest-protection is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}

		// iterate through all blocks in explosion event and remove those that are DeathChest chests or signs
		Collection<Block> blocks = new LinkedList<>(event.blockList());
		for (Block block : blocks) {
			if (plugin.chestManager.isChestBlock(block)) {
				// remove death chest block from blocks exploded list if protection has not expired
				DeathChest deathChest = plugin.chestManager.getChest(block);
				if (deathChest != null && !deathChest.protectionExpired()) {
					event.blockList().remove(block);
				}
			}
		}
	}


	/**
	 * Block explode event handler<br>
	 * Make death chests explosion proof if chest-protection is enabled
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public void onBlockExplode(final BlockExplodeEvent event) {

		// if chest-protection is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}

		// iterate through all blocks in explosion event and remove those that are DeathChest chests or signs
		Collection<Block> blocks = new LinkedList<>(event.blockList());
		for (Block block : blocks) {
			if (plugin.chestManager.isChestBlock(block)) {
				// remove death chest block from blocks exploded list if protection has not expired
				DeathChest deathChest = plugin.chestManager.getChest(block);
				if (deathChest != null && !deathChest.protectionExpired()) {
					event.blockList().remove(block);
				}
			}
		}
	}


	/**
	 * Block physics event handler<br>
	 * remove detached death chest signs from game to prevent players gaining additional signs
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public void signDetachCheck(final BlockPhysicsEvent event) {

		// if event block is a DeathChest component, cancel event
		if (plugin.chestManager.isChestBlockSign(event.getBlock())) {
			event.setCancelled(true);
		}
	}

}
