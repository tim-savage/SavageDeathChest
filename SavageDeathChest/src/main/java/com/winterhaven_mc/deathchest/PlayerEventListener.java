package com.winterhaven_mc.deathchest;

import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerEventListener implements Listener {

	// reference to main class
	private PluginMain plugin;

	
	/** class constructor
	 * 
	 * @param plugin reference to main class
	 */
	public PlayerEventListener(PluginMain plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	

	/**
	 * Event listener for PlayerDeathEvent<p>
	 * Attempt to deploy a death chest on player death
	 * @param event	PlayerDeathEvent
	 */
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		Player player = (Player)event.getEntity();
		List<ItemStack> dropped_items = event.getDrops();
		
		// If player's current world is not enabled in config, do nothing
		// and allow inventory items to drop on ground
		if (!playerWorldEnabled(player)) {
			return;
		}
		
		// if player does not have permission for death chest creation,
		// do nothing and allow inventory items to drop on ground
		if (!player.hasPermission("deathchest.chest")) {
			plugin.messageManager.sendPlayerMessage(player, "permission-denied");
			return;
		}
		
		// if player is in creative mode, output message and return
		if (player.getGameMode().equals(GameMode.CREATIVE)) {
			plugin.messageManager.sendPlayerMessage(player, "creative-mode");
			return;
		}
		
		// if player inventory is empty, output message and return
		if (dropped_items.isEmpty()) {
			plugin.messageManager.sendPlayerMessage(player, "inventory-empty");
			return;
		}
		
		// deploy chest, putting items that don't fit in chest into dropped_items list
		dropped_items = plugin.chestManager.deployChest(player, dropped_items);
		
		// clear dropped items
		event.getDrops().clear();
		
		// drop any items that couldn't be placed in a death chest
		event.getDrops().addAll(dropped_items);
		return;
	}

	
	/** prevent deathchest opening by non-owners or creative players
	 * 
	 * @param	event	PlayerInteractEvent
	 * @return	void
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		final Player player = event.getPlayer();
		final Block block = event.getClickedBlock();
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		// if clicked block is not a chest, do nothing and return
		if (!block.getType().equals(Material.CHEST)) {
			return;
		}
		
		// if block is not a DeathChestBlock, do nothing and return
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}
		
		// if player did not right click chest, do nothing and return 
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		// if player is in creative mode and does not have override permission, cancel event, send message and return
		if (player.getGameMode().equals(GameMode.CREATIVE) && !player.hasPermission("deathchest.creative-access")) {
			event.setCancelled(true);
			plugin.messageManager.sendPlayerMessage(player, "no-creative-access");
			return;
		}
		
		// if chest-protection option is not enabled, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}
		
		// if player is chest owner, check if killer is currently looting chest
		if (block.getMetadata("deathchest-owner").get(0).asString().equals(player.getUniqueId().toString())) {

			// if killer already has chest open, cancel event and output message and return
			if (plugin.chestManager.getChestViewerCount(block) > 0) {

				// cancel event
				event.setCancelled(true);
				
				// send player message
				plugin.messageManager.sendPlayerMessage(player, "chest-currently-open");

				// if sound effects are enabled, play denied access sound
				if (plugin.getConfig().getBoolean("sound-effects")) {
					player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
				}
			}
			return;
		}

		// if player has deathchest.loot.other permission, do nothing and return
		if (player.hasPermission("deathchest.loot.other")) {
			return;
		}
		
		// if killer-looting is enabled check if player is killer
		if (plugin.getConfig().getBoolean("killer-looting")) {
			
			// if player is killer, check that chest owner does not already have chest open
			if (block.hasMetadata("deathchest-killer") && block.getMetadata("deathchest-killer").get(0).asString().equals(player.getUniqueId().toString())) {

				// if chest owner already has chest open, cancel event and output message 
				if (plugin.chestManager.getChestViewerCount(block) > 0) {

					// cancel event
					event.setCancelled(true);
					
					// send player message
					plugin.messageManager.sendPlayerMessage(player, "chest-currently-open");

					// if sound effects are enabled, play denied access sound
					if (plugin.getConfig().getBoolean("sound-effects")) {
						player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
					}
				}
				return;
			}
		}
		
		// cancel event
		event.setCancelled(true);
		
		// send player not-owner message
		plugin.messageManager.sendPlayerMessage(player, "not-owner");
	}

	
	/**
	 * Test if plugin is enabled in player's current world.
	 * 
	 * @param player	Player to test world enabled.
	 * @return true if player world is enabled, false if not enabled 
	 */
	private boolean playerWorldEnabled(Player player) {
		
		if (plugin.commandManager.getEnabledWorlds().contains(player.getWorld().getName())) {
			return true;
		}
		return false;
	}

}
