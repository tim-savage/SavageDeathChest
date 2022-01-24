package com.winterhavenmc.deathchest.listeners;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.messages.Macro;
import com.winterhavenmc.deathchest.chests.ChestBlock;
import com.winterhavenmc.deathchest.chests.DeathChest;
import com.winterhavenmc.deathchest.chests.Deployment;
import com.winterhavenmc.deathchest.protectionchecks.ProtectionCheckResult;
import com.winterhavenmc.deathchest.sounds.SoundId;

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

import java.util.Collection;
import java.util.LinkedList;

import static com.winterhavenmc.deathchest.messages.Macro.*;
import static com.winterhavenmc.deathchest.messages.MessageId.*;
import static com.winterhavenmc.deathchest.protectionchecks.ProtectionCheckResultCode.*;


/**
 * A class that contains {@code EventHandler} methods to process player related events
 */

public final class PlayerEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// reference to helper class
	private final Helper helper;

	/**
	 * class constructor
	 *
	 * @param plugin reference to main class
	 */
	public PlayerEventListener(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// create instance of helper class
		this.helper = new Helper(plugin);

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
					.send();
			return;
		}

		// if player does not have permission for death chest creation,
		// do nothing and allow inventory items to drop on ground
		if (!player.hasPermission("deathchest.chest")) {
			plugin.messageBuilder.build(player, CHEST_DENIED_PERMISSION)
					.setMacro(Macro.LOCATION, player.getLocation())
					.send();
			return;
		}

		// if player is in creative mode,
		// and creative-deploy is configured false,
		// and player does not have creative-deploy permission override:
		// output message and return
		if (helper.creativeModeDeployDisabled(player)) {
			plugin.messageBuilder.build(player, CREATIVE_MODE)
					.setMacro(Macro.LOCATION, player.getLocation())
					.send();
			return;
		}

		// if player inventory is empty, output message and return
		if (event.getDrops().isEmpty()) {
			plugin.messageBuilder.build(player, INVENTORY_EMPTY)
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

		// if no-sneak left-click, do nothing and return (allow event to be handled by block break event)
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && !player.isSneaking()) {
			return;
		}

		// if chest inventory is already being viewed: cancel event, send message and return
		if (helper.chestCurrentlyOpen(deathChest)) {
			event.setCancelled(true);
			String viewerName = deathChest.getInventory().getViewers().iterator().next().getName();
			plugin.messageBuilder.build(player, CHEST_CURRENTLY_OPEN)
					.setMacro(LOCATION, deathChest.getLocation())
					.setMacro(OWNER, deathChest.getOwnerName())
					.setMacro(KILLER, deathChest.getKillerName())
					.setMacro(VIEWER, viewerName)
					.send();
			plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
			return;
		}

		// if player is in creative mode, and creative-access is configured false,
		// and player does not have override permission: cancel event, send message and return
		if (helper.creativeModeAccessDisabled(player)) {
			event.setCancelled(true);
			plugin.messageBuilder.build(player, NO_CREATIVE_ACCESS)
					.setMacro(LOCATION, player.getLocation()
					).send();
			plugin.soundConfig.playSound(player, SoundId.CHEST_DENIED_ACCESS);
			return;
		}

		// get result of all protection plugin checks
		final ProtectionCheckResult result = plugin.protectionPluginRegistry.AccessAllowed(player, block.getLocation());

		// if access blocked by protection plugin, do nothing and return (allow protection plugin to handle)
		if (result.getResultCode().equals(BLOCKED)) {
			// do not cancel event - allow protection plugin to handle it
			return;
		}

		// if no-sneak right-click, try to open chest inventory
		if (helper.isPlayerOpeningInventory(event, player)) {
			helper.performOpenInventoryOperations(event, player, deathChest);
			return;
		}

		// if player sneak punched chest and quick-loot is enabled, try auto-loot
		if (helper.isPlayerQuickLooting(event, player)) {
			helper.performQuickLoot(event, player, deathChest);
		}
	}

}
