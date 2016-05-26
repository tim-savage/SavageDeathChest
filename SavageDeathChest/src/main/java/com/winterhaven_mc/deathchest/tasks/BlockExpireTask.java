package com.winterhaven_mc.deathchest.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import com.winterhaven_mc.deathchest.DeathChestBlock;


final class BlockExpireTask extends BukkitRunnable {

	// death chest block to expire
	private final DeathChestBlock deathChestBlock;
	
	/**
	 * Class constructor
	 */
	BlockExpireTask(final DeathChestBlock deathChestBlock) {
		
		// set death chest block field
		this.deathChestBlock = deathChestBlock;		
	}

	@Override
	public void run() {
		deathChestBlock.expire();
	}
	
}
