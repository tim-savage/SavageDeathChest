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

import com.winterhavenmc.deathchest.PluginMain;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.List;

public class ChestSign {

	private final PluginMain plugin;
	private final Player player;
	private final DeathChest deathChest;


	// new chestSign(plugin, player, deathChest).place();
	public ChestSign(final PluginMain plugin, final Player player, final DeathChest deathChest) {
		this.plugin = plugin;
		this.player = player;
		this.deathChest = deathChest;
	}

	public void place() {

		// if chest-signs are not enabled in configuration, do nothing and return
		if (!plugin.getConfig().getBoolean("chest-signs")) {
			return;
		}

		// get chest block location
		Location chestBlockLocation = deathChest.getLocation();

		if (chestBlockLocation == null) {
			return;
		}

		// get chest block at location
		Block chestBlock = chestBlockLocation.getBlock();

		// get chest block state
		BlockState chestState = chestBlock.getState();

		// if block state is not chest, do nothing and return
		if (!(chestState.getBlockData() instanceof Chest)) {
			return;
		}

		// get chest block face direction from block data
		BlockFace blockFace = ((Chest) chestState.getBlockData()).getFacing();

		// if chest face is valid location, create wall sign
		if (isValidSignLocation(chestBlock.getRelative(blockFace).getLocation())) {
			placeFrontSign(chestBlock, player, deathChest);
		}
		// if front sign could not be placed and holograms are not enabled, place top sign
		else if (!plugin.getConfig().getBoolean("holograms-enabled")) {
			placeTopSign(chestBlock, player, deathChest);
		}
	}


	private void placeFrontSign(final Block chestBlock, final Player player, final DeathChest deathChest) {

		// get block adjacent to chest facing player direction
		Block signBlock = chestBlock.getRelative(LocationUtilities.getCardinalBlockFace(player));

		// set block type to wall sign
		signBlock.setType(Material.OAK_WALL_SIGN);

		BlockState blockState = signBlock.getState();
		org.bukkit.block.data.type.WallSign signBlockDataType = (org.bukkit.block.data.type.WallSign) blockState.getBlockData();
		signBlockDataType.setFacing(LocationUtilities.getCardinalBlockFace(player));
		blockState.setBlockData(signBlockDataType);
		blockState.update();

		finalizeSign(signBlock, player, deathChest);
	}


	private void placeTopSign(final Block chestBlock, final Player player, final DeathChest deathChest) {

		// get block on top of chest
		Block signBlock = chestBlock.getRelative(BlockFace.UP);

		// set block type to sign post
		signBlock.setType(Material.OAK_SIGN);

		BlockState blockState = signBlock.getState();
		org.bukkit.block.data.type.Sign signBlockDataType = (org.bukkit.block.data.type.Sign) blockState.getBlockData();
		signBlockDataType.setRotation(LocationUtilities.getCardinalBlockFace(player));
		blockState.setBlockData(signBlockDataType);
		blockState.update();

		finalizeSign(signBlock, player, deathChest);
	}


	private void finalizeSign(final Block signBlock, final Player player, final DeathChest deathChest) {

		// put configured text on sign
		setSignText(signBlock, player);

		// create ChestBlock for this sign block
		ChestBlock signChestBlock = new ChestBlock(deathChest.getChestUid(), signBlock.getLocation());

		// add this ChestBlock to block map
		plugin.chestManager.putBlock(ChestBlockType.SIGN, signChestBlock);

		// set sign block metadata
		signChestBlock.setMetadata(deathChest);
	}


	private void setSignText(final Block signBlock, final Player player) {

		// get block state of sign block
		BlockState signBlockState = signBlock.getState();

		// if block has not been successfully transformed into a sign, return false
		if (!(signBlockState instanceof org.bukkit.block.Sign)) {
			return;
		}

		// Place text on sign with player name and death date
		// cast signBlockState to org.bukkit.block.Sign type object
		org.bukkit.block.Sign sign = (org.bukkit.block.Sign) signBlockState;

		// get configured date format from config file
		String dateFormat = plugin.getConfig().getString("DATE_FORMAT");

		// if configured date format is null or empty, use default format
		if (dateFormat == null || dateFormat.isEmpty()) {
			dateFormat = "MMM d, yyyy";
		}

		// create formatted date string from current time
		String dateString = new SimpleDateFormat(dateFormat).format(System.currentTimeMillis());

		// get sign text from config file
		List<String> lines = plugin.getConfig().getStringList("SIGN_TEXT");

		int lineCount = 0;
		for (String line : lines) {

			// stop after four lines (zero indexed)
			if (lineCount > 3) {
				break;
			}

			// do string replacements
			line = line.replace("%PLAYER%", player.getName());
			line = line.replace("%DATE%", dateString);
			line = line.replace("%WORLD%", plugin.worldManager.getWorldName(player.getWorld()));
			line = ChatColor.translateAlternateColorCodes('&', line);

			// set sign text
			sign.setLine(lineCount, line);
			lineCount++;
		}

		// update sign block with text and direction
		sign.update();
	}


	/**
	 * Check if sign can be placed at location
	 *
	 * @param location Location to check
	 * @return boolean {@code true} if location is valid for sign placement, {@code false} if not
	 */
	private boolean isValidSignLocation(final Location location) {

		// check for null parameter
		if (location == null) {
			return false;
		}

		// get block at location
		Block block = location.getBlock();

		// check if block is above path
		if (LocationUtilities.isAbovePath(block)) {
			return false;
		}

		// check if block at location is a ReplaceableBlock
		return plugin.chestManager.isReplaceableBlock(block);
	}

}
