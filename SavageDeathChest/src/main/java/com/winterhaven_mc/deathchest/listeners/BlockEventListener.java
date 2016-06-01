package com.winterhaven_mc.deathchest.listeners;

import com.winterhaven_mc.deathchest.DeathChestBlock;
import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.ProtectionPlugin;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;


public final class BlockEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	
	/**
	 * Class constructor
	 * @param plugin reference to main class
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
	 * @param event the event being handled by this method
	 */
	@EventHandler(priority = EventPriority.LOW)
	//TODO: Make sure we're using the right priority. Use NORMAL if possible.
	public final void onBlockBreak(final BlockBreakEvent event) {
		
		final Block block = event.getBlock();
		final Player player = event.getPlayer();
		
		// get instance of DeathChestBlock from event block
		final DeathChestBlock deathChestBlock = DeathChestBlock.getChestInstance(block);

		// if event block is not a DeathChestBlock, do nothing and return
		if (deathChestBlock == null) {
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
		if (deathChestBlock.getViewerCount() > 0) {

			// send player message
			plugin.messageManager.sendPlayerMessage(player, "chest-currently-open");

			// play denied access sound
			plugin.soundManager.playerSound(player,"CHEST_DENIED_ACCESS");
			return;
		}
		
		// if player is owner or has deathchest.loot.other permission, break chest and return
		if (deathChestBlock.isOwner(player) || player.hasPermission("deathchest.loot.other")) {
			deathChestBlock.destroy();
			return;
		}
		
		// if killer looting is enabled  and player is killer, break chest and return
		// TODO: this will need to be removed when items taken limit is implemented
		if (plugin.getConfig().getBoolean("killer-looting") 
				&& deathChestBlock.isKiller(player)) {
			deathChestBlock.destroy();
			return;
		}
		
		// send player not-owner message
		plugin.messageManager.sendPlayerMessage(player, "not-owner");

		// play denied access sound
		plugin.soundManager.playerSound(player,"CHEST_DENIED_ACCESS");
	}


	/**
	 * Entity explode event handler<br>
	 * Make death chests explosion proof if chest-protection is enabled
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onEntityExplode(final EntityExplodeEvent event) {
		
		// if chest-protection is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}
		
		// iterate through all blocks in explosion event and remove those that are DeathChestBlocks
		ArrayList<Block> blocks = new ArrayList<>(event.blockList());
		for (Block block : blocks) {
			if (DeathChestBlock.isDeathChestBlock(block)) {
				event.blockList().remove(block);
			}
		}
	}


	/**
	 * Block explode event handler<br>
	 * Make death chests explosion proof if chest-protection is enabled
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onBlockExplode(final BlockExplodeEvent event) {
		
		// if chest-protection is not enabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-protection")) {
			return;
		}
		
		// iterate through all blocks in explosion event and remove those that are DeathChestBlocks
		ArrayList<Block> blocks = new ArrayList<>(event.blockList());
		for (Block block : blocks) {
			if (DeathChestBlock.isDeathChestBlock(block)) {
				event.blockList().remove(block);
			}
		}
	}

	
	/**
	 * Block physics event handler<br>
	 * remove detached death chest signs from game to prevent players gaining additional signs
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void signDetachCheck(final BlockPhysicsEvent event) {
	
		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// get instance of DeathChestBlock representing the event block
		final DeathChestBlock deathSign = DeathChestBlock.getSignInstance(event.getBlock());

		// if block is not a death chest sign, do nothing and return
		if (deathSign == null) {
			return;
		}
		
		// get block that sign is attached to
		final Block attachedBlock = deathSign.getAttachedBlock();
	    
		// if attached block is still there, do nothing and return
		if (attachedBlock != null && attachedBlock.getType() != Material.AIR) {
			return;
		}
		
		// cancel event
		event.setCancelled(true);
		
		// destroy the death sign
		deathSign.destroy();
	}

}
