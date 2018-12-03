package com.winterhaven_mc.deathchest.chests;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;


/**
 * An enum whose values represent the different types of chest blocks
 */
enum ChestBlockType {

	SIGN,
	LEFT_CHEST,
	RIGHT_CHEST;


	/**
	 * Get chest block type from passed block
	 * @param block block to determine chest type
	 * @return ChestBlockType enum value, or null if block is not a chest or sign
	 */
	public static ChestBlockType getType(final Block block) {

		// check for null
		if (block == null) {
			return null;
		}

		// if block material is SIGN or WALL_SIGN, return ChestBlockType.SIGN
		if (block.getType().equals(Material.SIGN) || block.getType().equals(Material.WALL_SIGN)) {
			return ChestBlockType.SIGN;
		}

		// if block material is CHEST, determine if it is LEFT or RIGHT chest (single chest returns RIGHT)
		else if (block.getType().equals(Material.CHEST)) {

			// cast block state to chest
			Chest chest = (Chest) block.getState();

			// get block data
			BlockData blockData = chest.getBlockData();

			// if chest is left chest, return LEFT_CHEST
			if (((org.bukkit.block.data.type.Chest)blockData).getType()
					.equals(org.bukkit.block.data.type.Chest.Type.LEFT)) {
				return ChestBlockType.LEFT_CHEST;
			}

			// if chest is single chest or right chest, return RIGHT_CHEST
			return ChestBlockType.RIGHT_CHEST;
		}

		// if block is not a sign or chest, return null
		return null;
	}

}
