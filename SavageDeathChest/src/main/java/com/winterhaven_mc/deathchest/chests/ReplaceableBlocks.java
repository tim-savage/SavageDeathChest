package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.PluginMain;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A class to manage the configured list of material types that can be replaced by a death chest
 */
public final class ReplaceableBlocks {

	// reference to main class
	private final PluginMain plugin;

	// material types that can be replaced by death chests
	private final Set<Material> replaceableBlocks;


	/**
	 * Class Constructor<br>
	 * populates set of replaceable blocks from config file
	 */
	ReplaceableBlocks(final PluginMain plugin) {

		this.plugin = plugin;

		this.replaceableBlocks = new HashSet<>();

		this.reload();
	}


	/**
	 * Load list of replaceable blocks from config file
	 */
	public final void reload() {

		// clear replaceable blocks
		replaceableBlocks.clear();

		// get string list of materials from config file
		List<String> materialStringList = plugin.getConfig().getStringList("replaceable-blocks");

		// iterate over string list
		for (String materialString : materialStringList) {

			// if material string matches a valid material type, add to replaceableBlocks set
			if (Material.matchMaterial(materialString) != null) {
				replaceableBlocks.add(Material.matchMaterial(materialString));
			}
		}
	}


	/**
	 * Check if replaceableBlocks set contains passed material
	 *
	 * @param material the material the test for
	 * @return true if replaceBlocks set contains material, false if it does not
	 */
	final boolean contains(final Material material) {

		// check for null parameter
		if (material == null) {
			return false;
		}

		return this.replaceableBlocks.contains(material);
	}


	/**
	 * Get string representation of replaceableBlocks set
	 *
	 * @return Formatted string list of materials in replaceableBlocks set
	 */
	@Override
	public final String toString() {
		return this.replaceableBlocks.toString();
	}
}
