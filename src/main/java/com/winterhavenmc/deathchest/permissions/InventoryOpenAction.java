package com.winterhavenmc.deathchest.permissions;

import com.winterhavenmc.deathchest.chests.DeathChest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;


/**
 * A class that defines the action to be taken for an inventory open permission check
 */
public class InventoryOpenAction implements ResultAction {

	@Override
	public void execute(final Cancellable event, final Player player, final DeathChest deathChest) {
		event.setCancelled(true);
		player.openInventory(deathChest.getInventory());
	}

}
