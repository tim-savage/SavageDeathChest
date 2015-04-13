package com.winterhaven_mc.deathchest;

import com.winterhaven_mc.deathchest.DeathChestMain;

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
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.material.Sign;


public class BlockEventListener implements Listener {

	DeathChestMain plugin;  // pointer to main class

	
	/**
	 * Class constructor
	 * @param plugin
	 */
	public BlockEventListener(DeathChestMain plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	
	/**
	 * Block break event handler<br>
	 * checks for ownership of death chests and prevents breakage by non-owners
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		// if block is not a DeathChestBlock, we're not concerned with it, so do nothing and return
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}
		
		// cancel event
		event.setCancelled(true);
		
		// if player is in creative mode and does not have override permission, send message and return
		if (player.getGameMode().equals(GameMode.CREATIVE) && !player.hasPermission("deathchest.creative-access")) {
			plugin.messageManager.sendPlayerMessage(player, "no-creative-access");
			return;
		}
		
		// if chest-protection is enabled in config, test for ownership
		if (plugin.getConfig().getBoolean("chest-protection",true)) {
			
			// if block is not owned by player, test for override permission or killer-looting enabled
			if (!block.getMetadata("deathchest-owner").get(0).asString().equals(player.getUniqueId().toString())) {
				
				// if player does not have deathchest.loot.other permission,
				if (!player.hasPermission("deathchest.loot.other")) {

					// if killer-looting is enabled, check if player is killer
					if (plugin.getConfig().getBoolean("killer-looting",false)) { 
						
						// if player is not killer send message and return
						if (!block.hasMetadata("deathchest-killer") || !block.getMetadata("deathchest-killer").get(0).asString().equals(player.getUniqueId().toString())) {

							// player is not killer, so send not-owner player message and return
							plugin.messageManager.sendPlayerMessage(player,"not-owner");
							return;
						}
					}
					else {
						// killer-looting is not enabled, so send not-owner player message and return
						plugin.messageManager.sendPlayerMessage(player,"not-owner");
						return;						
					}
				}
			}
		}
				
		// if block is wall sign, set block to attached block
		if (block.getType().equals(Material.WALL_SIGN)) {
		    Sign sign = (Sign)block.getState().getData();
		    block = block.getRelative(sign.getAttachedFace());
		}
		// if block is sign post, set block to one block below
		else if (block.getType().equals(Material.SIGN_POST)) {
			block = block.getRelative(0, 1, 0);
		}
		
		// confirm block is a death chest 
		if (block.getType().equals(Material.CHEST) && DeathChestBlock.isDeathChestBlock(block)) {
			
			// if chest inventory has any viewers, output message and return
			// this is to prevent breaking death chests that are already open by another player
			if (plugin.chestManager.getChestViewerCount(block) > 0) {

				// send player message
				plugin.messageManager.sendPlayerMessage(player, "chest-currently-open");

				// play denied access sound
				if (plugin.getConfig().getBoolean("sound-effects",true)) {
					player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
				}
			}
			else {
				// destroy DeathChestBlock
				plugin.chestManager.destroyDeathChestBlock(block);
			}
		}
	}

	
	/**
	 * Block damage event handler<br>
	 * Auto-loot chest on sneak-punch if player is owner or has override permission
	 * @param event
	 */
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
	
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		// if block is not a DeathChestBlock, do nothing and return
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}
		
		// if quick-loot is not enabled in configuration, do nothing and return
		if (!plugin.getConfig().getBoolean("quick-loot", true)) {
			return;
		}
		
		// if player is not sneaking, do nothing and return
		if (!player.isSneaking()) {
			return;
		}
		
		// cancel event
		event.setCancelled(true);
		
		// if player is in creative mode and does not have override permission, send message and return
		if (player.getGameMode().equals(GameMode.CREATIVE) && !player.hasPermission("deathchest.creative-access")) {
			plugin.messageManager.sendPlayerMessage(player, "no-creative-access");
			return;
		}
		
		// if chest-protection is enabled in config, test for ownership
		if (plugin.getConfig().getBoolean("chest-protection",true)) {
			
			// if player is not block owner, test for override permission or killer-looting enabled
			if (!block.getMetadata("deathchest-owner").get(0).asString().equals(player.getUniqueId().toString())) {
	
				// if player does not have deathchest.loot.other permission, check if killer looting is enabled
				if (!player.hasPermission("deathchest.loot.other")) {
	
					// if killer-looting is enabled check if player is killer
					if (plugin.getConfig().getBoolean("killer-looting",false)) {
						
						// if killer metadata is not set or doesn't match player uuid, player is not killer
						if (!block.hasMetadata("deathchest-killer") ||
								!block.getMetadata("deathchest-killer").get(0).asString().equals(player.getUniqueId().toString())) {
					
							// player is not killer, so output message and return
							plugin.messageManager.sendPlayerMessage(player, "not-owner");
							return;
						}
					}
					else {
						// killer looting is not enabled, so output message and return
						plugin.messageManager.sendPlayerMessage(player, "not-owner");
						return;						
					}
				}
			}
		}
		
		// if player clicked attached wall sign, set block to attached block
		if (block.getType().equals(Material.WALL_SIGN)) {
			Sign sign = (Sign)block.getState().getData();
			block = block.getRelative(sign.getAttachedFace());
		}
		// if player clicked sign post, set block to block below sign post
		else if (block.getType().equals(Material.SIGN_POST)) {
			block = block.getRelative(0, -1, 0);
		}
		
		// confirm block is a chest
		if (block.getType().equals(Material.CHEST)) {
			
			// if chest inventory has any viewers, output message and return
			// this is to prevent quick-looting death chests that are already open by another player
			if (plugin.chestManager.getChestViewerCount(block) > 0) {

				// send player message
				plugin.messageManager.sendPlayerMessage(player, "chest-currently-open");

				// play denied access sound
				if (plugin.getConfig().getBoolean("sound-effects",true)) {
					player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
				}
			}
			else {
				// loot chest
				plugin.chestManager.lootChest(player, block);
			}
		}
	}


	/**
	 * Entity explode event handler<br>
	 * Make death chests explosion proof if chest-protection is enabled
	 * @param event
	 */
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		
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
	public void signDetachCheck(BlockPhysicsEvent event) {
	
		Block block = event.getBlock();
	
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		// if block is not a DeathChestBlock, do nothing and return
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}
		
		// if block is not a sign, do nothing and return
		if (block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
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
		
		// destroy DeathChestBlock
		plugin.chestManager.destroyDeathChestBlock(block);
	}
	
}
