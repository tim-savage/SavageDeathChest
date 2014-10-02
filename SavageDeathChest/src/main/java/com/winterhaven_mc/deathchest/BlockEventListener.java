package com.winterhaven_mc.deathchest;

import com.winterhaven_mc.deathchest.DeathChestMain;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		// if block is not a DeathChestBlock, do nothing and return
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}
		
		// cancel event
		event.setCancelled(true);
		
		// if chest-protection is enabled in config, test for ownership
		if (plugin.getConfig().getBoolean("chest-protection",true)) {
			
			// if block is not owned by player, test for override permission or killer-looting enabled
			if (!block.getMetadata("deathchest-owner").get(0).asString().equals(player.getUniqueId().toString())) {

				// if player does not have deathchest.loot.other permission,
				if (!player.hasPermission("deathchest.loot.other")) {

					// if killer-looting is enabled and player is not killer
					if (plugin.getConfig().getBoolean("killer-looting",false) &&
							!block.getMetadata("deathchest-killer").get(0).asString().equals(player.getUniqueId().toString())) {

						// send not-owner player message and return
						plugin.messagemanager.sendPlayerMessage(player,"not-owner");
						return;
					}
				}
			}
		}
		
		// destroy DeathChestBlock
		plugin.chestmanager.destroyDeathChestBlock(block);
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
		
		// if quick-loot is not enabled in configuration, do nothing and return
		if (!plugin.getConfig().getBoolean("quick-loot", true)) {
			return;
		}
		
		// if player is not sneaking, do nothing and return
		if (!player.isSneaking()) {
			return;
		}
		
		// if block is not a DeathChestBlock, do nothing and return
		if (!DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}
		
		// if chest-protection is enabled in config, test for ownership
		if (plugin.getConfig().getBoolean("chest-protection",true)) {
			
			// if player is not block owner, test for override permisssion or killer-looting enabled
			if	(!block.getMetadata("deathchest-owner").get(0).asString().equals(player.getUniqueId().toString())) {
				if (plugin.debug) {
					plugin.getLogger().info("Sneak-puncher is not chest owner.");
				}
	
				// if player does not have deathchest.loot.other permission
				if (!player.hasPermission("deathchest.loot.other")) {
	
					// if killer-looting is enabled and player is not killer
					if (plugin.getConfig().getBoolean("killer-looting",false) &&
							!block.getMetadata("deathchest-killer").get(0).asString().equals(player.getUniqueId().toString())) {
					
						// cancel event, output message and return
						event.setCancelled(true);
						plugin.messagemanager.sendPlayerMessage(player, "not-owner");
						return;
					}
				}
			}
		}
		
		// if player clicked sign, set block to attached block
		if (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN_POST)) {
			Sign sign = (Sign)block.getState().getData();
			block = block.getRelative(sign.getAttachedFace());
		}
		
		// loot chest
		plugin.chestmanager.lootChest(player, block);
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
		plugin.chestmanager.destroyDeathChestBlock(block);
	}
}
