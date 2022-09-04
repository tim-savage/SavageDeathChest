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

package com.winterhavenmc.deathchest.chests;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;


/**
 * An enum whose values represent the different types of chest blocks
 */
public enum ChestBlockType {

	SIGN,
	LEFT_CHEST,
	RIGHT_CHEST;


	/**
	 * Get chest block type from passed block
	 *
	 * @param block block to determine chest type
	 * @return ChestBlockType enum value, or null if block is not a chest or sign
	 */
	static ChestBlockType getType(final Block block) {

		// check for null parameter
		if (block == null) {
			return null;
		}

		// if block material is SIGN or WALL_SIGN, return ChestBlockType.SIGN
		if (block.getType().equals(Material.OAK_SIGN) || block.getType().equals(Material.OAK_WALL_SIGN)) {
			return ChestBlockType.SIGN;
		}

		// if block material is CHEST, determine if it is LEFT or RIGHT chest (single chest returns RIGHT)
		else if (block.getType().equals(Material.CHEST)) {

			// cast block state to chest
			Chest chest = (Chest) block.getState();

			// get block data
			BlockData blockData = chest.getBlockData();

			// if chest is left chest, return LEFT_CHEST
			if (((org.bukkit.block.data.type.Chest) blockData).getType()
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
