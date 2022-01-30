/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
