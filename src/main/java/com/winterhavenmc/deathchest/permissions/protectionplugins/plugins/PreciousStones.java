package com.winterhavenmc.deathchest.permissions.protectionplugins.plugins;

import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPluginAbstract;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones.API;


/**
 * Class that implements protection plugin checks for PreciousStones
 */
public class PreciousStones extends ProtectionPluginAbstract {


	/**
	 * Class constructor
	 *
	 * @param plugin reference to SavageDeathChest plugin main class instance
	 * @param name name of the protection plugin
	 * @param version version of the protection plugin
	 */
	public PreciousStones(final JavaPlugin plugin, final String name, final String version) {
		this.plugin = plugin;
		this.name = name;
		this.version = version;
	}


	@Override
	public boolean allowChestPlacement(final Player player, final Location location) {
		try {
			return API().canPlace(player, location);
		}
		catch (Exception e) {
			logPlaceError();
			// allow placement on error
			return true;
		}
	}


	@Override
	public boolean allowChestAccess(final Player player, final Location location) {
		try {
			return !API().flagAppliesToPlayer(player, FieldFlag.PROTECT_INVENTORIES, location);
		}
		catch (Exception e) {
			logAccessError();
			// allow access on error
			return true;
		}
	}

}
