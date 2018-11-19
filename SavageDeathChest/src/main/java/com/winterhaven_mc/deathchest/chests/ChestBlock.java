package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.sounds.SoundId;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Sign;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class ChestBlock {

	// static reference to main class
	private static PluginMain plugin = PluginMain.instance;

	// chest UUID
	private UUID chestUUID;

	// chest block location
	private Location location;

	// chest block type
	private ChestBlockType chestBlockType;


	/**
	 * Class constructor
	 */
	public ChestBlock() { }


	/**
	 * Class constructor
	 * @param deathChest DeathChest object that this ChestBlock is member
	 * @param block in game block this ChestBlock represents
	 */
	ChestBlock(final DeathChest deathChest, final Block block, final ChestBlockType chestBlockType) {

		// set ChestUUID for this ChestBlock
		this.chestUUID = deathChest.getChestUUID();

		// set location for this ChestBlock
		this.location = block.getLocation();

		// set ChestBlockType
		this.chestBlockType = chestBlockType;

		// set block metadata
		this.setMetadata(deathChest);

		// add this ChestBlock to map
		plugin.chestManager.addChestBlock(this);

		// add this ChestBlock to passed DeathChest
		deathChest.addChestBlock(this.chestBlockType, this);
	}


	/**
	 * Getter method for chest block location
	 * @return location - the location of this chest block
	 */
	public Location getLocation() {
		return location;
	}


	/**
	 * Setter method for chest block location
	 * @param location the location to set for this chest block
	 */
	public void setLocation(final Location location) {
		this.location = location;
	}


	/**
	 * Getter method for chest block chestUUID
	 * @return the chestUUID for this chest block
	 */
	public UUID getChestUUID() {
		return chestUUID;
	}


	/**
	 * Setter method for chest block chestUUID
	 * @param chestUUID the chestUUID to set for this chest block
	 */
	public void setChestUUID(final UUID chestUUID) {
		this.chestUUID = chestUUID;
	}


	/**
	 * Getter method for ChestBlockType of this chest block
	 * @return the ChestBlockType of this chest block
	 */
	public ChestBlockType getType() {
		return chestBlockType;
	}


	/**
	 * Setter method for ChestBlockType of this chest block
	 * @param chestBlockType the ChestBlockType to set for this chest block
	 */
	public void setType(final ChestBlockType chestBlockType) {
		this.chestBlockType = chestBlockType;
	}


	/**
	 * Get DeathChest chest block that DeathSign is attached to
	 * @return DeathChest chest block; returns null if sign is not a DeathSign or attached block is not a DeathChest
	 */
	private Block getAttachedBlock() {

		// get block represented by this DeathChestBlock
		final Block block = this.getLocation().getBlock();

		// if block is null return null
		if (block == null) {
			return null;
		}

		// if block is not a DeathSign, return null
		if (!plugin.chestManager.isDeathChestSignBlock(block)) {
			return null;
		}

		// initialize return block
		Block returnBlock = null;

		// if block is wall sign, set block to attached block
		if (block.getType().equals(Material.WALL_SIGN)) {
			Sign sign = (Sign)block.getState().getData();
			returnBlock = block.getRelative(sign.getAttachedFace());
		}
		// else if block is sign post, set block to one block below
		else if (block.getType().equals(Material.SIGN)) {
			returnBlock = block.getRelative(0, -1, 0);
		}

		// if attached block is not a DeathChest, return null
		if (!plugin.chestManager.isDeathChestChestBlock(returnBlock)) {
			return null;
		}

		return returnBlock;
	}


	/**
	 * Open the inventory of this DeathChest for player
	 * @param player the player for whom to open the DeathChest inventory
	 */
	public final void openInventory(final Player player) {

		// get the block state of block represented by this ChestBlock
		BlockState blockState = this.getLocation().getBlock().getState();

		// if block is a sign or wall sign, get attached block
		if (blockState.getType().equals(Material.SIGN) || blockState.getType().equals((Material.WALL_SIGN))) {

			// get attached block
			Block block = this.getAttachedBlock();

			// if attached block returned null, do nothing and return
			if (block != null) {
				blockState = this.getAttachedBlock().getState();
			}
			else return;
		}

		// if block state is not a chest block, do nothing and return
		if (!blockState.getType().equals(Material.CHEST)) {
			return;
		}

		// open chest inventory
		final Chest chest = (Chest)blockState;
		final Inventory inventory = chest.getInventory();
		player.openInventory(inventory);
	}


	/**
	 * Transfer contents of chest block to player inventory
	 * @param player the player whose inventory chest items will be placed
	 */
	void transferContents(final Player player) {

		// get in game block at deathBlock location
		Block block = this.getLocation().getBlock();

		// confirm block is still death chest block
		if (block.getType().equals(Material.CHEST)
				&& plugin.chestManager.isChestBlock(block)) {

			// get player inventory object
			final PlayerInventory playerinventory = player.getInventory();

			// get chest object
			final Chest chest = (Chest)block.getState();

			// get array of ItemStack for chest inventory
			final List<ItemStack> chestInventory = new ArrayList<>(Arrays.asList(chest.getInventory().getContents()));

			// iterate through all inventory slots in chest inventory
			for (ItemStack itemStack : chestInventory) {

				// if inventory slot item is not null...
				if (itemStack != null) {

					// remove item from chest inventory
					chest.getInventory().removeItem(itemStack);

					// add item to player inventory
					playerinventory.addItem(itemStack);

					// play inventory add sound
					plugin.soundConfig.playSound(player, SoundId.INVENTORY_ADD_ITEM);
				}
			}
		}
	}


	/**
	 * Set block metadata
	 */
	private void setMetadata(final DeathChest deathChest) {

		// get in game block at chest block location
		Block block = this.getLocation().getBlock();

		// if block is not death chest material, do nothing and return
		if (!ChestManager.deathChestMaterials.contains(block.getType())) {
			return;
		}

		// set chest uuid metadata
		if (deathChest.getChestUUID() != null) {
			block.setMetadata("deathchest-uuid", new FixedMetadataValue(plugin, deathChest.getChestUUID()));
		}

		// set owner uuid metadata
		if (deathChest.getOwnerUUID() != null) {
			block.setMetadata("deathchest-owner", new FixedMetadataValue(plugin, deathChest.getOwnerUUID()));
		}

		// set killer uuid metadata
		if (deathChest.getKillerUUID() != null) {
			block.setMetadata("deathchest-killer", new FixedMetadataValue(plugin, deathChest.getKillerUUID()));
		}
	}


	/**
	 * Remove metadata from block
	 */
	private void removeMetadata() {

		// get in game block at this chestBlock location
		Block block = this.getLocation().getBlock();

		block.removeMetadata("deathchest-uuid", plugin);
		block.removeMetadata("deathchest-owner", plugin);
		block.removeMetadata("deathchest-killer", plugin);
	}


	/**
	 * Destroy chest block, dropping any contents on ground.
	 * Removes block metadata and deletes corresponding block record from datastore.
	 */
	void destroy() {

		// get in game block at this chestBlock location
		Block block = this.getLocation().getBlock();

		// load chunk if necessary
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load();
		}

		// remove metadata from block
		this.removeMetadata();

		// set block material to air; this will drop chest contents, but not the block itself
		block.setType(Material.AIR);

		// remove ChestBlock record from datastore
		plugin.dataStore.deleteBlockRecord(this);

		// remove ChestBlock from chest block map
		plugin.chestManager.removeChestBlock(this);
	}

}
