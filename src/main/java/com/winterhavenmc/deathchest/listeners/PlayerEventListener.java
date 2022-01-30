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
import com.winterhavenmc.deathchest.chests.DeathChest;
import com.winterhavenmc.deathchest.chests.Deployment;
import com.winterhavenmc.deathchest.messages.Macro;
import com.winterhavenmc.deathchest.messages.MessageId;
import com.winterhavenmc.deathchest.permissions.InventoryOpenAction;
import com.winterhavenmc.deathchest.permissions.PermissionCheck;
import com.winterhavenmc.deathchest.permissions.QuickLootAction;
import com.winterhavenmc.deathchest.permissions.ResultAction;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.LinkedList;


/**
 * A class that contains {@code EventHandler} methods to process player related events
 */

public final class PlayerEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// reference to permissionCheck class
	private final PermissionCheck permissionCheck;

	private final ResultAction inventoryOpenAction = new InventoryOpenAction();
	private final ResultAction quickLootAction = new QuickLootAction();


	/**
	 * class constructor
	 *
	 * @param plugin reference to main class
	 */
	public PlayerEventListener(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// create instance of permissionCheck class
		this.permissionCheck = new PermissionCheck(plugin);

		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Event listener for PlayerDeathEvent<p>
	 * Attempt to deploy a death chest on player death.<br>
	 * Listens at EventPriority.HIGH to allow other plugins to process event first,
	 * in order to manipulate player's dropped items on death before placement in chest
	 *
	 * @param event PlayerDeathEvent
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(final PlayerDeathEvent event) {

		// get event player
		Player player = event.getEntity();

		// if player's current world is not enabled in config,
		// do nothing and allow inventory items to drop on ground
		if (!plugin.worldManager.isEnabled(player.getWorld())) {
			plugin.messageBuilder.build(player, MessageId.CHEST_DENIED_WORLD_DISABLED)
					.setMacro(Macro.LOCATION, player.getLocation())
					.send();
			return;
		}

		// if player does not have permission for death chest creation,
		// do nothing and allow inventory items to drop on ground
		if (!player.hasPermission("deathchest.chest")) {
			plugin.messageBuilder.build(player, MessageId.CHEST_DENIED_PERMISSION)
					.setMacro(Macro.LOCATION, player.getLocation())
					.send();
			return;
		}

		// if player is in creative mode,
		// and creative-deploy is configured false,
		// and player does not have creative-deploy permission override:
		// output message and return
		if (permissionCheck.isCreativeDeployDisabled(player)) {
			plugin.messageBuilder.build(player, MessageId.CREATIVE_MODE)
					.setMacro(Macro.LOCATION, player.getLocation())
					.send();
			return;
		}

		// if player inventory is empty, output message and return
		if (event.getDrops().isEmpty()) {
			plugin.messageBuilder.build(player, MessageId.INVENTORY_EMPTY)
					.setMacro(Macro.LOCATION, player.getLocation())
					.send();
			return;
		}

		// if configured true, output player inventory to log
		if (plugin.getConfig().getBoolean("log-inventory-on-death")) {
			plugin.getLogger().info(player.getDisplayName() + " death inventory:");
			plugin.getLogger().info(event.getDrops().toString());
		}

		// copy event drops to new collection
		Collection<ItemStack> droppedItems = new LinkedList<>(event.getDrops());

		// remove all items from event drops
		event.getDrops().clear();

		// deploy DeathChest after configured delay
		new BukkitRunnable() {

			@Override
			public void run() {
				new Deployment(plugin, player, droppedItems);

			}
		}.runTaskLater(plugin, plugin.getConfig().getInt("chest-deployment-delay"));
	}


	/**
	 * Event listener for PlayerInteractEvent<p>
	 * Performs permission checks when a player attempts to interact with a death chest.
	 * Listens at EventPriority.LOW to handle event before protection plugins
	 *
	 * @param event PlayerInteractEvent
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(final PlayerInteractEvent event) {

		// get DeathChest from event clicked block
		final DeathChest deathChest = plugin.chestManager.getChest(event.getClickedBlock());

		// if DeathChest is null, do nothing and return
		if (deathChest == null) {
			return;
		}

		// get player from event
		final Player player = event.getPlayer();

		// if player sneak-clicked chest, try auto-loot
		if (isPlayerQuickLooting(event, player)) {
			permissionCheck.performChecks(event, player, deathChest, quickLootAction);
			return;
		}

		// if right-click chest, try to open chest inventory
		if (isPlayerOpeningInventory(event)) {
			permissionCheck.performChecks(event, player, deathChest, inventoryOpenAction);
		}
	}


	/**
	 * Test if player is attempting to quick loot chest if allowed
	 *
	 * @param event the PlayerInteractEvent being checked
	 * @param player the player being checked
	 * @return true if player is sneak-punching a chest and configuration and permissions allows
	 */
	private boolean isPlayerQuickLooting(final PlayerInteractEvent event, final Player player) {
		return (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
				&& player.isSneaking()
				&& plugin.getConfig().getBoolean("quick-loot")
				&& player.hasPermission("deathchest.loot");
	}


	/**
	 * Test if player is attempting to open a chest by right-clicking
	 *
	 * @param event the PlayerInteractEvent being checked
	 * @return true if player is opening a chest by right-clinking
	 */
	private boolean isPlayerOpeningInventory(final PlayerInteractEvent event) {
		return event.getAction().equals(Action.RIGHT_CLICK_BLOCK);
	}

}
