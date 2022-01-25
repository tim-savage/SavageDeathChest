package com.winterhavenmc.deathchest.permissions;

import com.winterhavenmc.deathchest.chests.DeathChest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;


/**
 * A class that defines the action to be taken for a break chest permission check
 */
public class BreakChestAction implements ResultAction {

	@Override
	public void execute(final Cancellable event, final Player player, final DeathChest deathChest) {
		event.setCancelled(true);
		deathChest.destroy();
	}

}
