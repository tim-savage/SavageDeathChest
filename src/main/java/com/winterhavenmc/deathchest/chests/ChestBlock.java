package com.winterhavenmc.deathchest.chests;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.sounds.SoundId;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


/**
 * A class that represents a single block that is a component of a death chest.
 * Block may be a left chest, a right chest, or an attached sign
 */
public final class ChestBlock {

	// reference to main class
	private final PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	// chest UUID
	private final UUID chestUUID;

	private final String worldName;
	private final UUID worldUid;

	private final int x;
	private final int y;
	private final int z;
	private final float yaw;
	private final float pitch;


	/**
	 * Class constructor
	 *
	 * @param chestUUID the UUID of the chest that this ChestBlock is member
	 * @param location  the location of the in game block this ChestBlock object represents
	 */
	public ChestBlock(final UUID chestUUID, final Location location) {

		// set ChestUUID for this ChestBlock
		this.chestUUID = chestUUID;

		World world = location.getWorld();

		if (world == null) {
			this.worldName = "unknown";
			this.worldUid = null;
		}
		else {
			this.worldName = world.getName();
			this.worldUid = world.getUID();
		}

		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}


	/**
	 * Class constructor
	 *
	 * @param chestUUID the UUID of the chest that this ChestBlock is member
	 * @param worldName the string name of the world for this chest location
	 * @param worldUid the Uid of the world for this chest location
	 * @param x int the x block location for this chest location
	 * @param y int the y block location for this chest location
	 * @param z int the z block location for this chest location
	 * @param yaw float the yaw for this chest location location
	 * @param pitch flaot the pitch for this chest location
	 */
	public ChestBlock(final UUID chestUUID,
	                  final String worldName,
	                  final UUID worldUid,
	                  final int x,
	                  final int y,
	                  final int z,
	                  final float yaw,
	                  final float pitch) {

		// set ChestUUID for this ChestBlock
		this.chestUUID = chestUUID;

		World world = plugin.getServer().getWorld(worldUid);

		// if world is invalid, set name unknown
		if (world == null) {
			this.worldName = worldName;
		}
		else {
			this.worldName = world.getName();
		}

		this.worldUid = worldUid;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}


	/**
	 * Getter method for chest block location
	 *
	 * @return Location - the in game location of this chest block
	 */
	public Location getLocation() {

		World world = plugin.getServer().getWorld(this.worldUid);

		if (world == null) {
			return null;
		}

		// return new location object
		return new Location(world,
				this.x,
				this.y,
				this.z,
				this.yaw,
				this.pitch);
	}


	/**
	 * Getter method for chest block world name
	 *
	 * @return String - the name of the world for the location of this chest block
	 */
	public String getWorldName() {
		return this.worldName;
	}

	public UUID getWorldUid() {
		return this.worldUid;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getZ() {
		return this.z;
	}

	/**
	 * Getter method for chest block chestUUID
	 *
	 * @return UUID - the UUID of the chest that this chest block is a member
	 */
	public UUID getChestUid() {
		return chestUUID;
	}


	/**
	 * Get DeathChest chest block that DeathChest sign is attached
	 *
	 * @return Block - DeathChest chest block;
	 * returns null if sign is not a DeathChest sign or attached block is not a DeathChest chest block
	 */
	private Block getAttachedBlock() {

		// if DeathChestBlock location is null, return null
		if (this.getLocation() == null) {
			return null;
		}

		// get block represented by this DeathChestBlock
		final Block block = this.getLocation().getBlock();

		// if block is not a DeathSign, return null
		if (!plugin.chestManager.isChestBlockSign(block)) {
			return null;
		}

		Block returnBlock = null;

		// if block is a wall sign, get block behind
		if (block.getBlockData() instanceof WallSign) {
			WallSign wallSign = (WallSign) block.getBlockData();
			returnBlock = block.getRelative(wallSign.getFacing().getOppositeFace());
		}

		// else if block is a sign post, get block below
		else if (block.getBlockData() instanceof Sign) {
			returnBlock = block.getRelative(0, 1, 0);
		}

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
	Inventory getInventory() {

		// if this ChestBlock location is null, return null
		if (this.getLocation() == null) {
			return null;
		}

		// get the block state of block represented by this ChestBlock
		BlockState blockState = this.getLocation().getBlock().getState();

		// if block is a sign or wall sign, get attached block
		if (blockState.getType().equals(Material.OAK_SIGN) || blockState.getType().equals((Material.OAK_WALL_SIGN))) {

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
	 * Transfer the contents of this chest block to player inventory
	 *
	 * @param player the player whose inventory chest items will be placed
	 */
	Collection<ItemStack> transferContents(final Player player) {

		// create empty list to contain items that did not fit in chest
		Collection<ItemStack> remainingItems = new LinkedList<>();

		// if player is null, return empty list
		if (player == null) {
			return remainingItems;
		}

		// if DeathBlock location is null, return empty list
		if (this.getLocation() == null) {
			return remainingItems;
		}

		// get in game block at deathBlock location
		Block block = this.getLocation().getBlock();

		// confirm block is still death chest block
		if (plugin.chestManager.isChestBlockChest(block)) {

			// get player inventory object
			final PlayerInventory playerInventory = player.getInventory();

			// get chest object
			final Chest chest = (Chest) block.getState();

			// get Collection of ItemStack for chest inventory
			final Collection<ItemStack> chestInventory = new LinkedList<>(Arrays.asList(chest.getInventory().getContents()));

			// iterate through all inventory slots in chest inventory
			for (ItemStack itemStack : chestInventory) {

				// if inventory slot item is not null...
				if (itemStack != null) {

					// remove item from chest inventory
					chest.getInventory().removeItem(itemStack);

					// add item to player inventory
					remainingItems.addAll(playerInventory.addItem(itemStack).values());

					// play inventory add sound
					plugin.soundConfig.playSound(player, SoundId.INVENTORY_ADD_ITEM);
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
	void setMetadata(final DeathChest deathChest) {

		// check for null object
		if (deathChest == null || deathChest.getChestUid() == null) {
			return;
		}

		// if DeathBlock location is null, do nothing and return
		if (this.getLocation() == null) {
			return;
		}

		// get in game block at chest block location
		Block block = this.getLocation().getBlock();

		// if block is not death chest material, do nothing and return
		if (!ChestManager.deathChestMaterials.contains(block.getType())) {
			return;
		}

		// set chest uuid metadata
		block.setMetadata("deathchest-uuid", new FixedMetadataValue(plugin, deathChest.getChestUid()));

		// set owner uuid metadata
		if (deathChest.hasValidOwnerUid()) {
			block.setMetadata("deathchest-owner", new FixedMetadataValue(plugin, deathChest.getOwnerUid()));
		}

		// set killer uuid metadata
		if (deathChest.hasValidKillerUid()) {
			block.setMetadata("deathchest-killer", new FixedMetadataValue(plugin, deathChest.getKillerUid()));
		}
	}


	/**
	 * Remove metadata from this chest block
	 */
	private void removeMetadata() {

		// if ChestBlock location is null, do nothing and return
		if (this.getLocation() == null) {
			return;
		}

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
	void destroy() {

		// if ChestBlock location is null, do nothing and return
		if (this.getLocation() == null) {
			return;
		}

		// get in game block at this chestBlock location
		Block block = this.getLocation().getBlock();

		// load chunk if necessary
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load();
		}

		// remove metadata from block
		this.removeMetadata();

		// remove ChestBlock record from datastore
		plugin.chestManager.deleteBlockRecord(this);

		// remove ChestBlock from block map
		plugin.chestManager.removeBlock(this);

		// set block material to air; this will drop chest contents, but not the block itself
		// this must be performed last, because above methods do checks for valid in-game chest material block
		block.setType(Material.AIR);
	}

}
