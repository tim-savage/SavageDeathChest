package com.winterhaven_mc.deathchest.listeners;

import com.winterhaven_mc.deathchest.DeathChestBlock;
import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.material.Sign;


public class BlockEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	
	/**
	 * Class constructor
	 * @param plugin
	 */
	public BlockEventListener(final PluginMain plugin) {
		
		// set reference to main class
		this.plugin = plugin;
		
		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	
	/**
	 * Block break event handler<br>
	 * checks for ownership of death chests and prevents breakage by non-owners
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOW)
	//TODO: Make sure we're using the right priority. Use NORMAL if possible.
	public void onBlockBreak(final BlockBreakEvent event) {
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		// if block is not a DeathChestBlock, we're not concerned with it, so do nothing and return
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}
		
		// if block is a death sign, get attached block
		if (DeathChestBlock.isDeathSign(block)) {
			block = DeathChestBlock.getSignAttachedBlock(block);
		}
		
		// confirm block is a death chest, in case original block was a sign
		if (!DeathChestBlock.isDeathChest(block)) {
			return;
		}
			
		// if access is blocked by a protection plugin, do nothing and return (allow protection plugin to handle event)
		ProtectionPlugin blockingPlugin = ProtectionPlugin.allowChestAccess(player, block);
		if (blockingPlugin != null) {
			if (plugin.debug) {
				plugin.getLogger().info(blockingPlugin.getPluginName() + " prevented access to a chest.");
			}
			return;
		}
		
		// if player is in creative mode 
		// and creative-access is configured false
		// and player does not have override permission:
		// cancel event, send message and return
		if (player.getGameMode().equals(GameMode.CREATIVE) 
				&& !plugin.getConfig().getBoolean("creative-access")
				&& !player.hasPermission("deathchest.creative-access")) {
			plugin.messageManager.sendPlayerMessage(player, "no-creative-access");
			event.setCancelled(true);
			return;
		}
		
		// if chest-protection option is not enabled, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}

		// cancel event
		event.setCancelled(true);
		
		// if chest is already open, disallow breakage; send message and return
		if (plugin.chestManager.getChestViewerCount(block) > 0) {

			// send player message
			plugin.messageManager.sendPlayerMessage(player, "chest-currently-open");

			// if sound effects are enabled, play denied access sound
			if (plugin.getConfig().getBoolean("sound-effects")) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			}
			return;
		}
		
		// if player is owner or has deathchest.loot.other permission, break chest and return
		if (DeathChestBlock.isDeathChestOwner(player, block) || player.hasPermission("deathchest.loot.other")) {
			plugin.chestManager.destroyDeathChestBlock(block);
			return;
		}
		
		// if killer looting is enabled  and player is killer, break chest and return
		if (plugin.getConfig().getBoolean("killer-looting") 
				&& DeathChestBlock.isDeathChestKiller(player, block)) {
			plugin.chestManager.destroyDeathChestBlock(block);
			return;
		}
		
		// send player not-owner message
		plugin.messageManager.sendPlayerMessage(player, "not-owner");

		// if sound effects are enabled, play denied access sound
		if (plugin.getConfig().getBoolean("sound-effects")) {
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
		}
	}


	/**
	 * Entity explode event handler<br>
	 * Make death chests explosion proof if chest-protection is enabled
	 * @param event
	 */
	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent event) {
		
		// if chest-protection is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}
		
		// iterate through all blocks in explosion event and remove those that are DeathChestBlocks
		ArrayList<Block> blocks = new ArrayList<Block>(event.blockList());
		for (Block block : blocks) {
			if (DeathChestBlock.isDeathChestBlock(block)) {
				event.blockList().remove(block);
			}
		}
	}


	/**
	 * Block explode event handler<br>
	 * Make death chests explosion proof if chest-protection is enabled
	 * @param event
	 */
	@EventHandler
	public void onBlockExplode(final BlockExplodeEvent event) {
		
		// if chest-protection is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}
		
		// iterate through all blocks in explosion event and remove those that are DeathChestBlocks
		ArrayList<Block> blocks = new ArrayList<Block>(event.blockList());
		for (Block block : blocks) {
			if (DeathChestBlock.isDeathChestBlock(block)) {
				event.blockList().remove(block);
			}
		}
	}

	
	/**
	 * Block physics event handler<br>
	 * remove detached death chest signs from game to prevent players gaining additional signs
	 * @param event
	 */
	@EventHandler
	public void signDetachCheck(final BlockPhysicsEvent event) {
	
		Block block = event.getBlock();
	
		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		// if block is not a DeathSign, do nothing and return
		if (!DeathChestBlock.isDeathSign(block)) {
			return;
		}
		
	    Sign sign = (Sign)block.getState().getData();
		Block attached_block = block.getRelative(sign.getAttachedFace());
	    
		// if attached block is still there, do nothing and return
		if (attached_block.getType() != Material.AIR) {
			return;
		}
		
		// cancel event
		event.setCancelled(true);
		
		// destroy the block
		plugin.chestManager.destroyDeathChestBlock(block);
	}
	
}
