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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;


/**
 * A utility class that implements static methods for various location manipulations
 */
public final class LocationUtilities {

	// set of path block material names as strings
	private static final Collection<String> PATH_MATERIAL_NAMES = Set.of(
			"GRASS_PATH",
			"LEGACY_GRASS_PATH",
			"DIRT_PATH"	);


	/**
	 * Private constructor to prevent instantiation this class
	 */
	private LocationUtilities() {
		throw new AssertionError();
	}


	/**
	 * Get the cardinal compass direction.<br>
	 * Converts direction in degrees to BlockFace cardinal direction (N,E,S,W)
	 *
	 * @param yaw Direction in degrees
	 * @return BlockFace of cardinal direction
	 */
	private static BlockFace getCardinalBlockFace(final float yaw) {

		// ensure yaw is between 0 and 360 (in case of negative yaw)
		double rotation = (yaw + 360) % 360;

		if (45 <= rotation && rotation < 135) {
			return BlockFace.EAST;
		}
		else if (135 <= rotation && rotation < 225) {
			return BlockFace.SOUTH;
		}
		else if (225 <= rotation && rotation < 315) {
			return BlockFace.WEST;
		}
		else {
			return BlockFace.NORTH;
		}
	}


	/**
	 * Get the cardinal compass direction.<br>
	 * Converts direction in degrees to BlockFace cardinal direction (N,E,S,W)
	 *
	 * @param location location to determine cardinal direction
	 * @return BlockFace of cardinal direction
	 */
	public static BlockFace getCardinalBlockFace(final Location location) {
		return getCardinalBlockFace(location.getYaw());
	}


	/**
	 * Get the cardinal compass direction.<br>
	 * Converts direction in degrees to BlockFace cardinal direction (N,E,S,W)
	 *
	 * @param player player to determine cardinal direction
	 * @return BlockFace of cardinal direction
	 */
	public static BlockFace getCardinalBlockFace(final Player player) {
		return getCardinalBlockFace(player.getLocation().getYaw());
	}


	/**
	 * Get location to right of location based on yaw
	 *
	 * @param location initial location
	 * @return location one block to right, preserving original yaw
	 */
	public static Location getLocationToRight(final Location location) {

		Location resultLocation = getBlockToRight(location).getLocation();

		// set new location yaw to match original
		resultLocation.setYaw(location.getYaw());
		return resultLocation;
	}


	/**
	 * Get block to left of location based on yaw
	 *
	 * @param location initial location
	 * @return block to left of location
	 */
	public static Block getBlockToLeft(final Location location) {
		float yaw = location.getYaw() + 90;
		return location.getBlock().getRelative(getCardinalBlockFace(yaw));
	}


	/**
	 * Get block to right of location based on yaw
	 *
	 * @param location initial location
	 * @return block to right of initial location
	 */
	public static Block getBlockToRight(final Location location) {
		float yaw = location.getYaw() - 90;
		return location.getBlock().getRelative(getCardinalBlockFace(yaw));
	}


	public static boolean isAbovePath(final Block block) {

		// get string for block material type at location below block
		String materialType = block.getRelative(0, -1, 0).getType().toString();

		// if block at location is above grass path, return negative result
		return PATH_MATERIAL_NAMES.contains(materialType);
	}

}
