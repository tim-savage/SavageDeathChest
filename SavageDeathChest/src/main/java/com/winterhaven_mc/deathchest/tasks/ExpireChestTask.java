package com.winterhaven_mc.deathchest.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import com.winterhaven_mc.deathchest.DeathChestBlock;


final public class ExpireChestTask extends BukkitRunnable {

	// death chest block to expire
	private final DeathChestBlock deathChestBlock;
	
	/**
	 * Class constructor
	 */
	public ExpireChestTask(final DeathChestBlock deathChestBlock) {
		
		// set death chest block field
		this.deathChestBlock = deathChestBlock;		
	}

	@Override
	public void run() {
		deathChestBlock.expire();
	}
	
}
