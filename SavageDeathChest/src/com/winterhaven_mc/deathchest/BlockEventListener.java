package com.winterhaven_mc.deathchest;

import com.winterhaven_mc.deathchest.DeathChestMain;
import java.util.ArrayList;
import org.bukkit.Material;
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

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (!block.hasMetadata("deathchest")) {
			return;
		}
		event.setCancelled(true);
		if (this.plugin.getConfig().getBoolean("chest-protection") &&
				((block.getMetadata("deathchest").get(0)).asString().equals(player.getUniqueId().toString()) ||
				player.hasPermission("deathchest.loot.others"))) {
			plugin.messagemanager.sendPlayerMessage(player, "not-owner");
			return;
		}
		block.setType(Material.AIR);
		plugin.chestmanager.removeDeathChestItem(block);
	}

	@EventHandler
	public void signDetachCheck(BlockPhysicsEvent event) {

		Block block = event.getBlock();

		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		// if block does not have death chest metadata, do nothing and return
		if (!block.hasMetadata("deathchest")) {
			return;
		}

		// if block is not a sign, do nothing and return
		if (block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
			return;
		}
		
		// Sign sign = (Sign)block.getState().getData(); // decompiler output this. replaced with below
		Sign sign = (Sign) block.getState();
		Block attached_block = block.getRelative(sign.getAttachedFace());

		// if attached block is not air, do nothing and return
		if (attached_block.getType() != Material.AIR) {
			return;
		}
		// cancel event
		event.setCancelled(true);
		
		// remove metadata from block
		block.removeMetadata("deathchest", plugin);
		
		// remove deathchest item from hashmap
		plugin.chestmanager.removeDeathChestItem(block);
		
		// delete block by setting to air
		block.setType(Material.AIR);
		if (plugin.debug) {
			plugin.getLogger().info("Removed attached sign from broken deathchest item.");
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		
		// if chest-protection is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}
		// iterate through all blocks in explosion event and remove those that have deathchest metadata
		ArrayList<Block> blocks = new ArrayList<Block>(event.blockList());
		for (Block block : blocks) {
			if (block.hasMetadata("deathchest")) {
				event.blockList().remove((Object)block);
			}
		}
	}

	/**
	 * Auto-loot chest on sneak-punch if player is owner or has override permission
	 * @param event
	 */
	@EventHandler(priority=EventPriority.HIGHEST)
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
		
		// if block does not have deathchest metadata, do nothing and return
		if (!block.hasMetadata("deathchest")) {
			return;
		}

		// if chest-protection is enabled in config, test for ownership or override permission
		if (plugin.getConfig().getBoolean("chest-protection", true)) {
			
			// if player is not block owner and does not have override permisssion, cancel event, output message and return
			if	(!block.getMetadata("deathchest").get(0).asString().equals(player.getUniqueId().toString()) &&
					!player.hasPermission("deathchest.loot.other")) {
				event.setCancelled(true);
				plugin.messagemanager.sendPlayerMessage(player, "not-owner");
				return;
			}
		}
		
		// if player clicked sign, set block to attached block
		if (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN_POST)) {
			Sign sign = (Sign)block.getState();
			block = block.getRelative(sign.getAttachedFace());
		}
		plugin.chestmanager.lootChest(player, block);
	}
}
