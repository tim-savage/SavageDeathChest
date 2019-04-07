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

import javax.annotation.concurrent.Immutable;
import java.util.*;


/**
 * A class that represents a single block that is a component of a death chest
 */
@Immutable
public final class ChestBlock {

	// static reference to main class
	private final PluginMain plugin = PluginMain.instance;

	// chest UUID
	private final UUID chestUUID;

	// chest block location
	private final Location location;


	/**
	 * Class constructor
	 *
	 * @param chestUUID the UUID of the chest that this ChestBlock is member
	 * @param location  the location of the in game block this ChestBlock object represents
	 */
	public ChestBlock(final UUID chestUUID, final Location location) {

		// set ChestUUID for this ChestBlock
		this.chestUUID = chestUUID;

		// set location for this ChestBlock with defensive copy of passed location
		this.location = new Location(location.getWorld(),
				location.getX(),
				location.getY(),
				location.getZ(),
				location.getYaw(),
				location.getPitch());
	}


	/**
	 * Getter method for chest block location
	 *
	 * @return Location - the in game location of this chest block
	 */
	public final Location getLocation() {

		// return defensive copy of location
		return new Location(this.location.getWorld(),
				this.location.getX(),
				this.location.getY(),
				this.location.getZ(),
				this.location.getYaw(),
				this.location.getPitch());
	}


	/**
	 * Getter method for chest block chestUUID
	 *
	 * @return UUID - the UUID of the chest that this chest block is a member
	 */
	public final UUID getChestUUID() {
		return chestUUID;
	}


	/**
	 * Get DeathChest chest block that DeathChest sign is attached
	 *
	 * @return Block - DeathChest chest block;
	 * returns null if sign is not a DeathChest sign or attached block is not a DeathChest chest block
	 */
	private Block getAttachedBlock() {

		// get block represented by this DeathChestBlock
		final Block block = this.getLocation().getBlock();

		// if block is not a DeathSign, return null
		if (!plugin.chestManager.isChestBlockSign(block)) {
			return null;
		}

		// get block state cast to Sign
		Sign sign = (Sign) block.getState().getData();

		// get attached block
		Block returnBlock = block.getRelative(sign.getAttachedFace());

		// if attached block is not a DeathChest, return null
		if (!plugin.chestManager.isChestBlockChest(returnBlock)) {
			return null;
		}

		return returnBlock;
	}


	/**
	 * Get the inventory of this ChestBlock
	 *
	 * @return Inventory - the inventory of this ChestBlock;
	 * if ChestBlock is a sign, return inventory of attached ChestBlock;
	 * returns null if this ChestBlock (or attached block) is not a chest
	 */
	final Inventory getInventory() {

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
			else {
				return null;
			}
		}

		// if blockState is a chest object, open inventory for player
		if (blockState instanceof Chest) {
			return ((Chest) blockState).getInventory();
		}

		return null;
	}


	/**
	 * Open the inventory of this ChestBlock for player
	 *
	 * @param player the player for whom to open the ChestBlock inventory
	 */
	public final void openInventory(final Player player) {

		// check for null player object
		if (player == null) {
			return;
		}

		// get inventory for this chest block
		Inventory inventory = this.getInventory();

		// if inventory is not null, open inventory for player
		if (inventory != null) {
			player.openInventory(inventory);
		}
	}


	/**
	 * Transfer the contents of this chest block to player inventory
	 *
	 * @param player the player whose inventory chest items will be placed
	 */
	final Collection<ItemStack> transferContents(final Player player) {

		// create empty list to contain items that did not fit in chest
		List<ItemStack> remainingItems = new ArrayList<>();

		// check for null object
		if (player != null) {

			// get in game block at deathBlock location
			Block block = this.getLocation().getBlock();

			// confirm block is still death chest block
			if (plugin.chestManager.isChestBlockChest(block)) {

				// get player inventory object
				final PlayerInventory playerinventory = player.getInventory();

				// get chest object
				final Chest chest = (Chest) block.getState();

				// get array of ItemStack for chest inventory
				final List<ItemStack> chestInventory = new ArrayList<>(Arrays.asList(chest.getInventory().getContents()));

				// iterate through all inventory slots in chest inventory
				for (ItemStack itemStack : chestInventory) {

					// if inventory slot item is not null...
					if (itemStack != null) {

						// remove item from chest inventory
						chest.getInventory().removeItem(itemStack);

						// add item to player inventory
						remainingItems.addAll(playerinventory.addItem(itemStack).values());

						// play inventory add sound
						plugin.soundConfig.playSound(player, SoundId.INVENTORY_ADD_ITEM);
					}
				}
			}
		}
		return remainingItems;
	}


	/**
	 * Set block metadata
	 *
	 * @param deathChest the DeathChest whose metadata will be set on this chest block
	 */
	final void setMetadata(final DeathChest deathChest) {

		// check for null object
		if (deathChest == null || deathChest.getChestUUID() == null) {
			return;
		}

		// get in game block at chest block location
		Block block = this.getLocation().getBlock();

		// if block is not death chest material, do nothing and return
		if (!ChestManager.deathChestMaterials.contains(block.getType())) {
			return;
		}

		// set chest uuid metadata
		block.setMetadata("deathchest-uuid", new FixedMetadataValue(plugin, deathChest.getChestUUID()));

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
	 * Remove metadata from this chest block
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
	 * Removes block metadata and deletes corresponding block record from block index and datastore.
	 */
	final void destroy() {

		// get in game block at this chestBlock location
		Block block = this.getLocation().getBlock();

		// load chunk if necessary
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load();
		}

		// remove metadata from block
		this.removeMetadata();

		// remove ChestBlock record from datastore
		plugin.dataStore.deleteBlockRecord(this);

		// remove ChestBlock from block map
		plugin.chestManager.removeChestBlock(this);

		// set block material to air; this will drop chest contents, but not the block itself
		// this must be performed last, because above methods do checks for valid in-game chest material block
		block.setType(Material.AIR);
	}

}
