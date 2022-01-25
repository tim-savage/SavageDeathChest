package com.winterhavenmc.deathchest.permissions;

import com.winterhavenmc.deathchest.chests.DeathChest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;


/**
 * Interface for action to be taken on permission result checks
 */
public interface ResultAction {

	/**
	 * Execute action for permission check
	 *
	 * @param event the event where the permission check occurred
	 * @param player the player involved in the event
	 * @param deathChest the deathchest involved in the event
	 */
	void execute(final Cancellable event, final Player player, final DeathChest deathChest);

}
