package com.winterhavenmc.deathchest.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;


/**
 * A utility class that implements static methods for various location manipulations
 */
public final class LocationUtilities {

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
	private static BlockFace getCardinalDirection(final float yaw) {

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
	public static BlockFace getCardinalDirection(final Location location) {
		return getCardinalDirection(location.getYaw());
	}


	/**
	 * Get the cardinal compass direction.<br>
	 * Converts direction in degrees to BlockFace cardinal direction (N,E,S,W)
	 *
	 * @param player player to determine cardinal direction
	 * @return BlockFace of cardinal direction
	 */
	public static BlockFace getCardinalDirection(final Player player) {
		return getCardinalDirection(player.getLocation().getYaw());
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
		return location.getBlock().getRelative(getCardinalDirection(yaw));
	}


	/**
	 * Get block to right of location based on yaw
	 *
	 * @param location inital location
	 * @return block to right of initial location
	 */
	public static Block getBlockToRight(final Location location) {
		float yaw = location.getYaw() - 90;
		return location.getBlock().getRelative(getCardinalDirection(yaw));
	}

}
