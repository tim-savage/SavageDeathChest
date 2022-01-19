package com.winterhavenmc.deathchest.listeners;

import com.winterhavenmc.deathchest.chests.DeathChest;
import com.winterhavenmc.deathchest.chests.search.ProtectionPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * A class of helper methods for event listeners, mostly conditional statements
 */
final class Helper {

	JavaPlugin plugin;

	Helper(JavaPlugin plugin) {
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
	 * @param protectionPlugin a reference to a blocking plugin
	 * @return true if protectionPlugin is a valid plugin, false if it is null
	 */
	boolean pluginBlockedAccess(final ProtectionPlugin protectionPlugin) {
		// if access is blocked by a protection plugin, do nothing and return (allow protection plugin to handle event)
		return protectionPlugin != null;
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
	 * Check if a chest is already open
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
	 * @param event the event to cancel
	 * @param deathChest the death chest to auto-loot
	 * @param player the player whose inventory the chest contents will be transferred
	 */
	void cancelEventAndAutoLootChest(final PlayerInteractEvent event, final DeathChest deathChest, final Player player) {
		event.setCancelled(true);
		deathChest.autoLoot(player);
	}
}
