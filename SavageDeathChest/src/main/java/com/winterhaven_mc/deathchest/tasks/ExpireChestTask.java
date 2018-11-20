package com.winterhaven_mc.deathchest.tasks;

import com.winterhaven_mc.deathchest.chests.DeathChest;
import org.bukkit.scheduler.BukkitRunnable;


final public class ExpireChestTask extends BukkitRunnable {

	// death chest block to expire
	private final DeathChest deathChest;
	

	/**
	 * Class constructor
	 */
	public ExpireChestTask(final DeathChest deathChest) {
		
		// set death chest block field
		this.deathChest = deathChest;
	}


	@Override
	public void run() {
		deathChest.expire();
	}
	
}
