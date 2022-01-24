package com.winterhavenmc.deathchest.listeners;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.chests.DeathChest;
import com.winterhavenmc.deathchest.messages.Macro;
import com.winterhavenmc.deathchest.protectionchecks.ProtectionCheckResult;
import com.winterhavenmc.deathchest.protectionchecks.ProtectionCheckResultCode;
import com.winterhavenmc.deathchest.sounds.SoundId;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.winterhavenmc.deathchest.messages.Macro.*;
import static com.winterhavenmc.deathchest.messages.MessageId.CHEST_ACCESSED_PROTECTION_TIME;
import static com.winterhavenmc.deathchest.messages.MessageId.NOT_OWNER;


/**
 * A class of helper methods for event listeners, mostly conditional statements
 */
final class Helper {

	PluginMain plugin;

	Helper(PluginMain plugin) {
		this.plugin = plugin;
	}

	/**
	 * Send debug message to log if debugging is enabled in configuration file
	 *
 	 * @param message the debug message to log
	 */
	void logDebugMessage(final String message) {
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
	boolean pluginBlockedAccess(final ProtectionCheckResult result) {
		return result.getResultCode().equals(ProtectionCheckResultCode.BLOCKED);
	}

	/**
	 * Check if player is in creative mode and access is configured disabled
	 * and player does not have overriding permission
	 *
	 * @param player the player attempting to access a chest
	 * @return true if player should be denied access, false if not
	 */
	boolean creativeModeAccessDisabled(final Player player) {
		return player.getGameMode().equals(GameMode.CREATIVE)
				&& !plugin.getConfig().getBoolean("creative-access")
				&& !player.hasPermission("deathchest.creative-access");
	}

	/**
	 * Check if player is in create mode and deployment is configured disabled
	 * and player does not have overriding permission
	 *
	 * @param player the player on whose behalf a chest is being deployed
	 * @return true if deployment should be denied, false if not
	 */
	boolean creativeModeDeployDisabled(final Player player) {
		return player.getGameMode().equals(GameMode.CREATIVE)
				&& !plugin.getConfig().getBoolean("creative-deploy")
				&& !player.hasPermission("deathchest.creative-deploy");
	}

	/**
	 * Check if a chest inventory is already open and being viewed by another player
	 * @param deathChest the deathchest to check
	 *
	 * @return true if chest is already open, false if not
	 */
	boolean chestCurrentlyOpen(final DeathChest deathChest) {
		return deathChest.getViewerCount() > 0;
	}

	/**
	 * check if chest protection is enabled in configuration file
	 *
	 * @return true if chest protection is enabled, false if not
	 */
	boolean chestProtectionDisabled() {
		return !plugin.getConfig().getBoolean("chest-protection");
	}

	/**
	 * Check if chest protection is enabled and has expired for a particular death chest
	 *
	 * @param deathChest the death chest to check expiration
	 * @return true if protection is enabled and chest has expired, false if not
	 */
	boolean chestProtectionExpired(final DeathChest deathChest) {
		return plugin.getConfig().getBoolean("chest-protection") &&
				deathChest.protectionExpired();
	}

	/**
	 * Check if chest protection is enabled and has not expired for a particular death chest
	 *
	 * @param deathChest the death chest to check expiration
	 * @return true if protection is enabled and chest has not expired, otherwise false
	 */
	boolean chestProtectionNotExpired(final DeathChest deathChest) {
		return plugin.getConfig().getBoolean("chest-protection") &&
				!deathChest.protectionExpired();
	}

	/**
	 * Check if chest protection is enabled and player has permission to loot other's chests
	 *
	 * @param player the player to check for permission
	 * @return true if player has permission, false if not
	 */
	boolean playerHasLootOtherPermission(final Player player) {
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
	boolean playerIsKillerLooting(final Player player, final DeathChest deathChest) {
		return plugin.getConfig().getBoolean("chest-protection") &&
				plugin.getConfig().getBoolean("killer-looting") &&
				deathChest.isKiller(player) &&
				player.hasPermission("deathchest.loot.killer");
	}


	/**
	 * Cancel the event and destroy chest, dropping chest contents
	 *
	 * @param event the event to cancel
	 * @param deathChest the chest to destroy
	 */
	void cancelEventAndDestroyChest(final BlockBreakEvent event, final DeathChest deathChest) {
		event.setCancelled(true);
		deathChest.destroy();
	}


	/**
	 * Cancel the event and auto-loot chest, placing contents in a player inventory
	 *
	 *  @param event the event to cancel
	 * @param player the player whose inventory the chest contents will be transferred
	 * @param deathChest the death chest to auto-loot
	 */
	void cancelEventAndAutoLootChest(final Cancellable event, final Player player, final DeathChest deathChest) {
		event.setCancelled(true);
		deathChest.autoLoot(player);
	}


	/**
	 * Cancel the event and open chest inventory for player
	 *
	 * @param event the event to cancel
	 * @param player the player for whom the inventory will be opened
	 * @param deathChest the chest whose inventory will be opened
	 */
	void cancelEventAndOpenInventory(final Cancellable event, final Player player, final DeathChest deathChest) {
		event.setCancelled(true);
		player.openInventory(deathChest.getInventory());
	}


	/**
	 * Test if player is attempting to quick loot chest if allowed
	 *
	 * @param event the PlayerInteractEvent being checked
	 * @param player the player being checked
	 * @return true if player is sneak-punching a chest and configuration and permissions allows
	 */
	boolean isPlayerQuickLooting(final PlayerInteractEvent event, final Player player) {
		return (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
				&& player.isSneaking()
				&& plugin.getConfig().getBoolean("quick-loot")
				&& player.hasPermission("deathchest.loot");
	}


	/**
	 * Perform the sequence of checks and actions for a player to quick loot a chest
	 *  @param event the PlayerInteractEvent being checked
	 * @param player the player who is attempting to quick-loot the chest
	 * @param deathChest the deathchest that is being quick-looted
	 */
	void performQuickLoot(final Cancellable event, final Player player, final DeathChest deathChest) {

		// if chest protection is not enabled, loot chest and return
		if (chestProtectionDisabled()) {
			cancelEventAndAutoLootChest(event, player, deathChest);
			return;
		}

		// if chest protection has expired, loot chest and return
		if (chestProtectionExpired(deathChest)) {
			cancelEventAndAutoLootChest(event, player, deathChest);
			return;
		}

		// if player is owner, loot chest and return
		if (deathChest.isOwner(player)) {
			cancelEventAndAutoLootChest(event, player, deathChest);
			return;
		}

		// if player has deathchest.loot.other permission, loot chest and return
		if (playerHasLootOtherPermission(player)) {
			cancelEventAndAutoLootChest(event, player, deathChest);
			return;
		}

		// if killer looting is enabled and player is killer and has permission, loot chest and return
		if (playerIsKillerLooting(player, deathChest)) {
			cancelEventAndAutoLootChest(event, player, deathChest);
			return;
		}

		// if chest protection is enabled and has not expired, send message and return
		if (chestProtectionNotExpired(deathChest)) {
			long protectionTimeRemainingMillis = deathChest.getProtectionTime() - System.currentTimeMillis();
			plugin.messageBuilder.build(player, CHEST_ACCESSED_PROTECTION_TIME)
					.setMacro(Macro.OWNER, deathChest.getOwnerName())
					.setMacro(PROTECTION_DURATION, protectionTimeRemainingMillis)
					.setMacro(PROTECTION_DURATION_MINUTES, protectionTimeRemainingMillis)
					.setMacro(LOCATION, deathChest.getLocation())
					.send();
		}
		else {
			// send player not-owner message
			plugin.messageBuilder.build(player, NOT_OWNER)
					.setMacro(LOCATION, deathChest.getLocation())
					.setMacro(OWNER, deathChest.getOwnerName())
					.setMacro(KILLER, deathChest.getKillerName())
					.send();
		}

		// play denied access sound
		plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
	}


	/**
	 * Test if player is attempting to open a chest by right-clicking while not sneaking
	 *
	 * @param event the PlayerInteractEvent being checked
	 * @param player the player being checked
	 * @return true if player is opening a chest by right-clinking while not sneaking, else false
	 */
	boolean isPlayerOpeningInventory(final PlayerInteractEvent event, final Player player) {
		return event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !player.isSneaking();
	}


	/**
	 * Perform the sequence of checks and actions for a player to open a chest
	 *
	 * @param event the PlayerInteractEvent being checked
	 * @param player the player who is attempting to open a chest
	 * @param deathChest the deathchest that is being quick-looted
	 */
	void performOpenInventoryOperations(final Cancellable event, final Player player, final DeathChest deathChest) {

		// if chest protection is not enabled, open inventory for player and return
		if (chestProtectionDisabled()) {
			cancelEventAndOpenInventory(event, player, deathChest);
			return;
		}

		// if chest protection is not enabled or chest protection has expired, open inventory for player and return
		if (chestProtectionExpired(deathChest)) {
			cancelEventAndOpenInventory(event, player, deathChest);
			return;
		}

		// if player is chest owner, open inventory for player and return
		if (deathChest.isOwner(player)) {
			cancelEventAndOpenInventory(event, player, deathChest);
			return;
		}

		// if player has loot other permission, open inventory for player and return
		if (player.hasPermission("deathchest.loot.other")) {
			cancelEventAndOpenInventory(event, player, deathChest);
			return;
		}

		// if player is killer and killer looting enabled, open inventory for player and return
		if (playerIsKillerLooting(player, deathChest)) {
			cancelEventAndOpenInventory(event, player, deathChest);
			return;
		}

		// if chest protection is enabled and has not expired, send message and return
		if (chestProtectionNotExpired(deathChest)) {
			long protectionTimeRemainingMillis = deathChest.getProtectionTime() - System.currentTimeMillis();
			plugin.messageBuilder.build(player, CHEST_ACCESSED_PROTECTION_TIME)
					.setMacro(Macro.OWNER, deathChest.getOwnerName())
					.setMacro(PROTECTION_DURATION, protectionTimeRemainingMillis)
					.setMacro(PROTECTION_DURATION_MINUTES, protectionTimeRemainingMillis)
					.setMacro(LOCATION, deathChest.getLocation())
					.send();
		}
		// else send player not-owner message
		else {
			plugin.messageBuilder.build(player, NOT_OWNER)
					.setMacro(LOCATION, deathChest.getLocation())
					.setMacro(OWNER, deathChest.getOwnerName())
					.setMacro(KILLER, deathChest.getKillerName())
					.send();
		}

		// play denied access sound
		plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
	}

}
