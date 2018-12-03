package com.winterhaven_mc.deathchest.tasks;

import com.winterhaven_mc.deathchest.chests.DeathChest;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * A class that implements a death chest expire task to be run at the appropriate time
 */
public final class ExpireChestTask extends BukkitRunnable {

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
