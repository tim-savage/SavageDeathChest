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

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.chests.DeathChest;
import com.winterhavenmc.deathchest.messages.Macro;
import com.winterhavenmc.deathchest.messages.MessageId;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionCheckResult;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionCheckResultCode;
import com.winterhavenmc.deathchest.sounds.SoundId;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;


/**
 * A class of helper methods for event listeners, mostly conditional statements
 */
final public class PermissionCheck {

	// reference to plugin main class
	final PluginMain plugin;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	public PermissionCheck(final PluginMain plugin) {
		this.plugin = plugin;
	}


	/**
	 * Send debug message to log if debugging is enabled in configuration file
	 *
 	 * @param message the debug message to log
	 */
	@SuppressWarnings("unused")
	public void logDebugMessage(final String message) {
		if (plugin.getConfig().getBoolean("debug")) {
			plugin.getLogger().info(message);
		}
	}


	/**
	 * Check if chest access is blocked by another plugin
	 *
	 * @param result the access check result
	 * @return true if protectionPlugin is a valid plugin, false if it is null
	 */
	public boolean isPluginBlockingAccess(final ProtectionCheckResult result) {
		return result.getResultCode().equals(ProtectionCheckResultCode.BLOCKED);
	}


	/**
	 * Check if player is in create mode and deployment is configured disabled
	 * and player does not have overriding permission
	 *
	 * @param player the player on whose behalf a chest is being deployed
	 * @return true if deployment should be denied, false if not
	 */
	public boolean isCreativeDeployDisabled(final Player player) {
		return player.getGameMode().equals(GameMode.CREATIVE)
				&& !plugin.getConfig().getBoolean("creative-deploy")
				&& !player.hasPermission("deathchest.creative-deploy");
	}


	/**
	 * Check if player is in creative mode and access is configured disabled
	 * and player does not have overriding permission
	 *
	 * @param player the player attempting to access a chest
	 * @return true if player should be denied access, false if not
	 */
	private boolean isCreativeAccessDisabled(final Player player) {
		return player.getGameMode().equals(GameMode.CREATIVE)
				&& !plugin.getConfig().getBoolean("creative-access")
				&& !player.hasPermission("deathchest.creative-access");
	}


	/**
	 * Check if a chest inventory is already open and being viewed by another player
	 * @param deathChest the deathchest to check
	 *
	 * @return true if chest is already open, false if not
	 */
	private boolean isCurrentlyOpen(final DeathChest deathChest) {
		return deathChest.getViewerCount() > 0;
	}


	/**
	 * check if chest protection is enabled in configuration file
	 *
	 * @return true if chest protection is enabled, false if not
	 */
	private boolean isProtectionDisabled() {
		return !plugin.getConfig().getBoolean("chest-protection");
	}


	/**
	 * Check if chest protection is enabled and has expired for death chest
	 *
	 * @param deathChest the death chest to check expiration
	 * @return true if protection is enabled and chest has expired, false if not
	 */
	private boolean isProtectionExpired(final DeathChest deathChest) {
		return plugin.getConfig().getBoolean("chest-protection") &&
				deathChest.protectionExpired();
	}


	/**
	 * Check if chest protection is enabled and has not expired for death chest
	 *
	 * @param deathChest the death chest to check expiration
	 * @return true if protection is enabled and chest has not expired, otherwise false
	 */
	private boolean isProtectionNotExpired(final DeathChest deathChest) {
		return plugin.getConfig().getBoolean("chest-protection") &&
				!deathChest.protectionExpired();
	}


	/**
	 * Check if chest protection is enabled and player has permission to loot other's chests
	 *
	 * @param player the player to check for permission
	 * @return true if player has permission, false if not
	 */
	private boolean hasLootOtherPermission(final Player player) {
		return plugin.getConfig().getBoolean("chest-protection") &&
				player.hasPermission("deathchest.loot.other");
	}


	/**
	 * Check if chest protection is enabled and killer looting is enabled
	 * and player is killer of death chest owner and player has killer looting permission
	 *
	 * @param player the player to check for killer looting
	 * @param deathChest the death chest being looted
	 * @return true if all killer looting checks pass, false if not
	 */
	private boolean isKillerLooting(final Player player, final DeathChest deathChest) {
		return plugin.getConfig().getBoolean("chest-protection") &&
				plugin.getConfig().getBoolean("killer-looting") &&
				deathChest.isKiller(player) &&
				player.hasPermission("deathchest.loot.killer");
	}


	/**
	 * Perform permission checks and take action for a player interacting with death chest
	 *
	 * @param event the PlayerInteractEvent being checked
	 * @param player the player who is attempting to open a chest
	 * @param deathChest the deathchest that is being quick-looted
	 */
	public void performChecks(final Cancellable event, final Player player,
	                          final DeathChest deathChest, final ResultAction resultAction) {

		// get protectionCheckResult of all protection plugin checks
		final ProtectionCheckResult protectionCheckResult = plugin.protectionPluginRegistry.AccessAllowed(player, deathChest.getLocation());

		// if access blocked by protection plugin, do nothing and return (allow protection plugin to handle)
		if (isPluginBlockingAccess(protectionCheckResult)) {
			// do not cancel event - allow protection plugin to handle it
			return;
		}

		// if chest inventory is already being viewed: cancel event, send message and return
		if (isCurrentlyOpen(deathChest)) {
			event.setCancelled(true);
			String viewerName = deathChest.getInventory().getViewers().iterator().next().getName();
			plugin.messageBuilder.build(player, MessageId.CHEST_CURRENTLY_OPEN)
					.setMacro(Macro.LOCATION, deathChest.getLocation())
					.setMacro(Macro.OWNER, deathChest.getOwnerName())
					.setMacro(Macro.KILLER, deathChest.getKillerName())
					.setMacro(Macro.VIEWER, viewerName)
					.send();
			plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
			return;
		}

		// if player is in creative mode, and creative-access is configured false,
		// and player does not have override permission: cancel event, send message and return
		if (isCreativeAccessDisabled(player)) {
			event.setCancelled(true);
			plugin.messageBuilder.build(player, MessageId.NO_CREATIVE_ACCESS)
					.setMacro(Macro.LOCATION, player.getLocation()
					).send();
			plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
			return;
		}

		// if player is chest owner, perform result action and return
		if (deathChest.isOwner(player)) {
			resultAction.execute(event, player, deathChest);
			return;
		}

		// if chest protection is not enabled, perform result action and return
		if (isProtectionDisabled()) {
			resultAction.execute(event, player, deathChest);
			return;
		}

		// if chest protection is not enabled or chest protection has expired, perform result action and return
		if (isProtectionExpired(deathChest)) {
			resultAction.execute(event, player, deathChest);
			return;
		}

		// if player has loot other permission, perform result action and return
		if (hasLootOtherPermission(player)) {
			resultAction.execute(event, player, deathChest);
			return;
		}

		// if player is killer and killer looting enabled, perform result action and return
		if (isKillerLooting(player, deathChest)) {
			resultAction.execute(event, player, deathChest);
			return;
		}

		// cancel event
		event.setCancelled(true);

		// if chest protection is enabled and has not expired, send message and return
		if (isProtectionNotExpired(deathChest)) {
			long protectionTimeRemainingMillis = deathChest.getProtectionTime() - System.currentTimeMillis();
			plugin.messageBuilder.build(player, MessageId.CHEST_ACCESSED_PROTECTION_TIME)
					.setMacro(Macro.OWNER, deathChest.getOwnerName())
					.setMacro(Macro.PROTECTION_DURATION, protectionTimeRemainingMillis)
					.setMacro(Macro.PROTECTION_DURATION_MINUTES, protectionTimeRemainingMillis)
					.setMacro(Macro.LOCATION, deathChest.getLocation())
					.send();
		}
		// else send player not-owner message
		else {
			plugin.messageBuilder.build(player, MessageId.NOT_OWNER)
					.setMacro(Macro.LOCATION, deathChest.getLocation())
					.setMacro(Macro.OWNER, deathChest.getOwnerName())
					.setMacro(Macro.KILLER, deathChest.getKillerName())
					.send();
		}

		// play denied access sound
		plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
	}

}
