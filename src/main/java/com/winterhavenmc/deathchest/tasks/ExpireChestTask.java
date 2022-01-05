package com.winterhavenmc.deathchest.tasks;

import com.winterhavenmc.deathchest.chests.DeathChest;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * A class that implements a death chest expire task to be run at the appropriate time
 */
public final class ExpireChestTask extends BukkitRunnable {

	// death chest block to expire
	private final DeathChest deathChest;


	/**
	 * Class constructor
	 * @param deathChest the death chest to expire
	 */
	public ExpireChestTask(final DeathChest deathChest) {

		// set death chest block field
		this.deathChest = deathChest;
	}


	@Override
	public void run() {

		// check for null death chest
		if (this.deathChest != null) {

			// expire death chest
			this.deathChest.expire();
		}
	}

}
