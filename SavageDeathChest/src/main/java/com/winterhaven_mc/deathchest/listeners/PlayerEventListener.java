package com.winterhaven_mc.deathchest.listeners;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.messages.Macro;
import com.winterhaven_mc.deathchest.chests.search.ProtectionPlugin;
import com.winterhaven_mc.deathchest.chests.ChestBlock;
import com.winterhaven_mc.deathchest.chests.DeathChest;
import com.winterhaven_mc.deathchest.chests.Deployment;
import com.winterhaven_mc.deathchest.sounds.SoundId;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static com.winterhaven_mc.deathchest.messages.Macro.*;
import static com.winterhaven_mc.deathchest.messages.Macro.VIEWER;
import static com.winterhaven_mc.deathchest.messages.MessageId.*;


/**
 * A class that contains {@code EventHandler} methods to process player related events
 */

public final class PlayerEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;


	/**
	 * class constructor
	 *
	 * @param plugin reference to main class
	 */
	public PlayerEventListener(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

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
			plugin.messageBuilder.build(player, CHEST_DENIED_WORLD_DISABLED)
					.setMacro(LOCATION, player.getLocation())
					.send(plugin.languageHandler);
			return;
		}

		// if player does not have permission for death chest creation,
		// do nothing and allow inventory items to drop on ground
		if (!player.hasPermission("deathchest.chest")) {
			plugin.messageBuilder.build(player, CHEST_DENIED_PERMISSION)
					.setMacro(Macro.LOCATION, player.getLocation())
					.send(plugin.languageHandler);
			return;
		}

		// if player is in creative mode,
		// and creative-deploy is configured false,
		// and player does not have creative-deploy permission override:
		// output message and return
		if (player.getGameMode().equals(GameMode.CREATIVE)
				&& !plugin.getConfig().getBoolean("creative-deploy")
				&& !player.hasPermission("deathchest.creative-deploy")) {
			plugin.messageBuilder.build(player, CREATIVE_MODE)
					.setMacro(Macro.LOCATION, player.getLocation())
					.send(plugin.languageHandler);
			return;
		}

		// if player inventory is empty, output message and return
		if (event.getDrops().isEmpty()) {
			plugin.messageBuilder.build(player, INVENTORY_EMPTY)
					.setMacro(Macro.LOCATION, player.getLocation())
					.send(plugin.languageHandler);
			return;
		}

		// if configured true, output player inventory to log
		if (plugin.getConfig().getBoolean("log-inventory-on-death")) {
			plugin.getLogger().info(player.getDisplayName() + " death inventory:");
			plugin.getLogger().info(event.getDrops().toString());
		}

		// copy event drops to new list
		List<ItemStack> droppedItems = new ArrayList<>(event.getDrops());

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
	 * Prevent deathchest opening by non-owners or creative players.<br>
	 * Listens at EventPriority.LOW to handle event before protection plugins
	 *
	 * @param event PlayerInteractEvent
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(final PlayerInteractEvent event) {

		// get player
		final Player player = event.getPlayer();

		// get block
		final Block block = event.getClickedBlock();

		// if block is not DeathChest block, do nothing and return
		if (!plugin.chestManager.isChestBlock(block)) {
			return;
		}

		// get ChestBlock at clicked block location
		ChestBlock chestBlock = plugin.chestManager.getBlock(block.getLocation());

		// if chest block returned null, do nothing and return
		if (chestBlock == null) {
			return;
		}

		// get DeathChest from ChestBlock
		DeathChest deathChest = plugin.chestManager.getChest(chestBlock.getChestUid());

		// if DeathChest returned null, do nothing and return
		if (deathChest == null) {
			return;
		}

		// if access is blocked by a protection plugin, do nothing and return (allow protection plugin to handle event)
		final ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestAccess(player, block);
		if (blockingPlugin != null) {
			if (plugin.debug) {
				plugin.getLogger().info("Death chest playerInteractEvent was blocked by "
						+ blockingPlugin.getPluginName());
			}
			return;
		}

		// if player is in creative mode,
		// and creative-access is configured false,
		// and player does not have override permission,
		// then cancel event, send message and return
		if (player.getGameMode().equals(GameMode.CREATIVE)
				&& !plugin.getConfig().getBoolean("creative-access")
				&& !player.hasPermission("deathchest.creative-access")) {
			event.setCancelled(true);
			plugin.messageBuilder.build(player, NO_CREATIVE_ACCESS)
					.setMacro(LOCATION, player.getLocation()
					).send(plugin.languageHandler);
			return;
		}

		// get owner name from uuid
		String ownerName = "-";
		if (deathChest.hasValidOwnerUid()) {
			ownerName = plugin.getServer().getOfflinePlayer(deathChest.getOwnerUid()).getName();
		}

		// get killer name from uuid
		String killerName = "-";
		if (deathChest.hasValidKillerUid()) {
			killerName = plugin.getServer().getOfflinePlayer(deathChest.getKillerUid()).getName();
		}

		// if chest inventory is already being viewed: cancel event, send message and return
		if (deathChest.getViewerCount() > 0) {

			// cancel event
			event.setCancelled(true);

			// only one chest viewer allowed, so get name viewer at index 0
			String viewerName = deathChest.getInventory().getViewers().get(0).getName();

			// send player message
			plugin.messageBuilder.build(player, CHEST_CURRENTLY_OPEN)
					.setMacro(LOCATION, deathChest.getLocation())
					.setMacro(OWNER, ownerName)
					.setMacro(KILLER, killerName)
					.setMacro(VIEWER, viewerName)
					.send(plugin.languageHandler);

			// play denied access sound
			plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
			return;
		}

		// if player sneak punched block and quick-loot is enabled, try auto-loot
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)
				&& player.isSneaking()
				&& plugin.getConfig().getBoolean("quick-loot")
				&& player.hasPermission("deathchest.loot")) {

			// cancel event
			event.setCancelled(true);

			// if chest protection is not enabled or has expired, loot chest and return
			if (!plugin.getConfig().getBoolean("chest-protection") || deathChest.protectionExpired()) {
				deathChest.autoLoot(player);
				return;
			}

			// if player is owner or has deathchest.loot.other permission, loot chest and return
			if (deathChest.isOwner(player) || player.hasPermission("deathchest.loot.other")) {
				deathChest.autoLoot(player);
				return;
			}

			// if killer looting is enabled and player is killer and has permission, loot chest and return
			if (plugin.getConfig().getBoolean("killer-looting")
					&& deathChest.isKiller(player)
					&& player.hasPermission("deathchest.loot.killer")) {
				deathChest.autoLoot(player);
				return;
			}

			// if chest protection is enabled and has not expired, send message and return
			if (plugin.getConfig().getBoolean("chest-protection") && !deathChest.protectionExpired()) {
				long remainingProtectionTime = System.currentTimeMillis() - deathChest.getProtectionExpirationTime();
				plugin.messageBuilder.build(player, CHEST_ACCESSED_PROTECTION_TIME)
						.setMacro(Macro.OWNER, deathChest.getOwnerName())
						.setMacro(Macro.DURATION, remainingProtectionTime)
						.setMacro(LOCATION, deathChest.getLocation())
						.send(plugin.languageHandler);
				// play denied access sound
				plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
				return;
			}

			// send player not-owner message
			plugin.messageBuilder.build(player, NOT_OWNER)
					.setMacro(LOCATION, deathChest.getLocation())
					.setMacro(OWNER, ownerName)
					.setMacro(KILLER, killerName)
					.send(plugin.languageHandler);

			// play denied access sound
			plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
			return;
		}

		// if player did not right click block, do nothing and return
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}

		// if chest-protection option is not enabled or protection has expired, allow chest to open normally
		if (!plugin.getConfig().getBoolean("chest-protection") || deathChest.protectionExpired()) {
			return;
		}

		// if player is owner or has deathchest.loot.other permission, allow chest to open normally
		if (deathChest.isOwner(player) || player.hasPermission("deathchest.loot.other")) {
			return;
		}

		// if killer looting is enabled and player is killer and has permission, allow chest to open normally
		if (plugin.getConfig().getBoolean("killer-looting")
				&& deathChest.isKiller(player)
				&& player.hasPermission("deathchest.loot.killer")) {
			return;
		}

		// cancel event
		event.setCancelled(true);

		// send player not-owner message
		plugin.messageBuilder.build(player, NOT_OWNER)
				.setMacro(LOCATION, deathChest.getLocation())
				.setMacro(OWNER, ownerName)
				.setMacro(KILLER, killerName)
				.send(plugin.languageHandler);

		// if chest protection is enabled and has not expired, send message and return
		if (plugin.getConfig().getBoolean("chest-protection") && !deathChest.protectionExpired()) {
			long remainingProtectionTime = System.currentTimeMillis() - deathChest.getProtectionExpirationTime();
			plugin.messageBuilder.build(player, CHEST_ACCESSED_PROTECTION_TIME)
					.setMacro(Macro.OWNER, deathChest.getOwnerName())
					.setMacro(Macro.DURATION, remainingProtectionTime)
					.setMacro(LOCATION, deathChest.getLocation())
					.send(plugin.languageHandler);
		}

		// play denied access sound
		plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
	}

}
