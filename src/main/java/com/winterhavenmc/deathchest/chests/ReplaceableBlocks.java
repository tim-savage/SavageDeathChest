package com.winterhavenmc.deathchest.chests;

import com.winterhavenmc.deathchest.PluginMain;
import org.bukkit.Material;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * A class to manage the configured list of material types that can be replaced by a death chest
 */
final class ReplaceableBlocks {

	// reference to main class
	private final PluginMain plugin;

	// material types that can be replaced by death chests
	private final Set<Material> materialSet;


	/**
	 * Class Constructor<br>
	 * populates set of replaceable blocks from config file
	 */
	ReplaceableBlocks(final PluginMain plugin) {

		this.plugin = plugin;

		this.materialSet = Collections.synchronizedSet(new LinkedHashSet<>());

		this.reload();
	}


	/**
	 * Load list of replaceable blocks from config file
	 */
	void reload() {

		// clear replaceable blocks
		materialSet.clear();

		// get string list of materials from config file
		Collection<String> materialStringList = plugin.getConfig().getStringList("replaceable-blocks");

		// iterate over string list
		for (String materialString : materialStringList) {

			// if material string matches a valid material type, add to replaceableBlocks set
			if (Material.matchMaterial(materialString) != null) {
				materialSet.add(Material.matchMaterial(materialString));
			}
		}
	}


	/**
	 * Check if replaceableBlocks set contains passed material
	 *
	 * @param material the material the test for
	 * @return true if replaceBlocks set contains material, false if it does not
	 */
	boolean contains(final Material material) {

		// check for null parameter
		if (material == null) {
			return false;
		}

		return this.materialSet.contains(material);
	}


	/**
	 * Get string representation of replaceableBlocks set
	 *
	 * @return Formatted string list of materials in replaceableBlocks set
	 */
	@Override
	public String toString() {
		return this.materialSet.toString();
	}
}
