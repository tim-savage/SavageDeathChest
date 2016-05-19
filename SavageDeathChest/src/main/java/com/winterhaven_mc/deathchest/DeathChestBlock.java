package com.winterhaven_mc.deathchest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Sign;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.winterhaven_mc.deathchest.util.LocationUtilities;

public final class DeathChestBlock {

	// reference to main class
	private static final PluginMain plugin = PluginMain.instance;

	// the location for this deathchest item
	private Location location;

	// the UUID of the owner of this deathchest item
	private UUID ownerUUID;

	// the UUID of the player who killed the deathchest owner
	private UUID killerUUID;

	// the expiration time of this deathchest item, in milliseconds since epoch 
	private long expiration;

	// task id of expire task for this death chest block
	private int expireTaskId;
	
	// number of items removed from this deathchest by killer
	private int itemsRemoved;

	/**
	 * Empty DeathChestBlock constructor
	 */
	public DeathChestBlock() {}


	/**
	 * DeathChestBlock constructor with parameters
	 * @param player
	 * @param block
	 */
	public DeathChestBlock(final Player player, final Block block) {

		// set location field
		this.setLocation(block.getLocation());

		// set owner uuid field
		this.setOwnerUUID(player.getUniqueId());
		
		// set items removed to zero
		this.itemsRemoved = 0;

		// if player's killer was another player, set killer uuid field
		if (player.getKiller() instanceof Player) {
			this.setKillerUUID(player.getKiller().getUniqueId());
		}
		
		// if configured expiration is zero (or less), set expiration to zero to signify never expire
		if (plugin.getConfig().getLong("expire-time") < 1) {
			this.setExpiration(0);
		}
		else {
			// set expiration field based on config setting (config setting is in minutes, so multiply by 60000)
			this.setExpiration(System.currentTimeMillis() + plugin.getConfig().getLong("expire-time") * 60000);
		}

		// set deathChestBlock metadata on in game block
		this.setBlockMetadata();
	}


	/**
	 * Class constructor<br>
	 * Create DeathChestBlock object from existing in game DeathChest block
	 * @param block
	 */
	private DeathChestBlock(final Block block) {
		
		// check that block is death chest block
		if (DeathChestBlock.isDeathChestBlock(block)) {

			// set location field
			this.setLocation(block.getLocation());

			// set owner uuid
			this.setOwnerUUID(getBlockMetadataUUID(block,"deathchest-owner"));

			// set killer uuid
			this.setKillerUUID(getBlockMetadataUUID(block,"deathchest-killer")); 

			// get items removed from block metadata
			if (block.hasMetadata("deathchest-items-removed")) {
				this.itemsRemoved = getBlockMetadataInteger(block,"deathchest-items-removed");
			}
			else {
				this.itemsRemoved = 0;
			}
			
			// get expire taskId from block metadata
			if (block.hasMetadata("deathchest-expire-task")) {
				this.expireTaskId = getBlockMetadataInteger(block,"deathchest-expire-task");
			}
		}
	}


	/**
	 * Returns an instance of a DeathChestBlock representing the passed block 
	 * @param block
	 * @return An instance of DeathChestBlock. If the passed block is a DeathSign,
	 * the returned DeathChestBlock object will reference the attached DeathChest.
	 * Returns null if the block is not a DeathChestBlock or in case of a DeathSign, the attached block
	 * is not a DeathChest.
	 */
	public final static DeathChestBlock getChestInstance(final Block block) {

		// if passed block is null, return null
		if (block == null) {
			return null;
		}

		// if passed block is not a death chest block, return null
		if (!isDeathChestBlock(block)) {
			return null;
		}

		// if passed block is a death sign, try to get attached chest
		if (isDeathSign(block)) {

			// get attached block
			final Block attachedBlock = getAttachedBlock(block);

			// if attached block is null or not a death chest, return null
			if (attachedBlock == null || !isDeathChest(attachedBlock)) {
				return null;
			}
			// otherwise, return new DeathChestBlock object representing attachedBlock (chest)
			else {
				return new DeathChestBlock(attachedBlock);
			}
		}

		// return DeathChestBlock (chest) with attributes set from block
		return new DeathChestBlock(block);
	}
	
	
	/**
	 * Returns an instance of a DeathChestBlock (sign) representing the passed block.
	 * @param block
	 * @return An instance of DeathChestBlock of block type sign.
	 * Returns null if the block is not a DeathSign.
	 */
	public final static DeathChestBlock getSignInstance(final Block block) {
		
		// if passed block is null, return null
		if (block == null) {
			return null;
		}
		
		// if passed block is not a death sign block, return null
		if (!isDeathSign(block)) {
			return null;
		}

		// return DeathChestBlock (sign) with attributes set from block
		return new DeathChestBlock(block);
	}
	
	
	public final static DeathChestBlock getChestInstance(final Inventory inventory) {

		// if inventory is null, return null
		if (inventory == null) {
			return null;
		}

		// if inventory holder is null, return null
		if (inventory.getHolder() == null) {
			return null;
		}

		// if inventory type is not a chest inventory, return null
		if (!inventory.getType().equals(InventoryType.CHEST)) {
			return null;
		}

		// if inventory holder is not a block, return null
		if (!(inventory.getHolder() instanceof Block)) {
			return null;
		}

		// get inventory holder block
		final Block block = (Block)inventory.getHolder();

		// if inventory holder block is not a DeathChest, return null
		if (!DeathChestBlock.isDeathChest(block)) {
			return null;
		}

		return getChestInstance(block);
	}
	

	/**
	 * Getter method for DeathChestBlock location
	 * @return location
	 */
	public final Location getLocation() {
		return location;
	}


	/**
	 * Setter method for DeathChestBlock location
	 * @param location
	 */
	public final void setLocation(final Location location) {
		this.location = location;
	}


	/**
	 * Getter method for DeathChestBlock ownerUUID
	 * @return UUID
	 */
	public final UUID getOwnerUUID() {
		return ownerUUID;
	}


	/**
	 * Setter method for DeathChestBlock ownerUUID
	 * @param ownerUUID
	 */
	public final void setOwnerUUID(final UUID ownerUUID) {
		this.ownerUUID = ownerUUID;
	}


	/**
	 * Getter method for DeathChestBlock killerUUID
	 * @return UUID
	 */
	public final UUID getKillerUUID() {
		return killerUUID;
	}


	/**
	 * Setter method for DeathChestBlock killerUUID
	 * @param killerUUID
	 */
	public final void setKillerUUID(final UUID killerUUID) {
		this.killerUUID = killerUUID;
	}


	/**
	 * Getter method for DeathChestBlock expiration
	 * @return long
	 */
	public final long getExpiration() {
		return expiration;
	}


	/**
	 * Setter method for DeathChestBlock expiration
	 * @param expiration
	 */
	public final void setExpiration(final long expiration) {
		this.expiration = expiration;
	}

	
	/**
	 * Getter method for DeathChestBlock expireTaskId
	 * @return
	 */
	public final int getExpireTaskId() {
		return this.expireTaskId;
	}
	

	public final void setExpireTaskId(final int expireTaskId) {

		// set expire task id in this DeathChestBlock object
		this.expireTaskId = expireTaskId;
		
		// get block represented by this DeathChestBlock object
		final Block block = this.location.getBlock();

		// set expire task id metadata in block metadata
		block.setMetadata("deathchest-expire-task", new FixedMetadataValue(plugin, this.expireTaskId));
	}

	/**
	 * Set block metadata
	 */
	public final void setBlockMetadata() {

		final Block block = this.location.getBlock();

		// set owner uuid metadata
		if (this.ownerUUID != null) {
			block.setMetadata("deathchest-owner", new FixedMetadataValue(plugin, this.ownerUUID));
		}

		// set killer uuid metadata
		if (this.killerUUID != null) {
			block.setMetadata("deathchest-killer", new FixedMetadataValue(plugin, this.killerUUID));
		}
		
		// set items taken metadata
		block.setMetadata("deathchest-items-removed", new FixedMetadataValue(plugin, this.itemsRemoved));
	}


	private final UUID getBlockMetadataUUID(final Block block, final String string) {
		
		if (block == null) {
			return null;
		}
		
		if (!block.hasMetadata(string)) {
			return null;
		}
		
		UUID result = null;
		
		for (MetadataValue mdv : block.getMetadata(string)) {
			if (mdv.getOwningPlugin().equals(plugin)) {
				try {
					result = (UUID)mdv.value();
				} catch (Exception e) {
					plugin.getLogger().warning("An error occured while trying to "
							+ "fetch UUID metadata from a block.");
				}
			}
		}
		return result;
	}


	private final Integer getBlockMetadataInteger(final Block block, final String string) {
		
		if (block == null) {
			return null;
		}
		
		if (!block.hasMetadata(string)) {
			return null;
		}
		
		Integer result = null;
		
		for (MetadataValue mdv : block.getMetadata(string)) {
			if (mdv.getOwningPlugin().equals(plugin)) {
				try {
					result = mdv.asInt();
				} catch (Exception e) {
					plugin.getLogger().warning("An error occured while trying to "
							+ "fetch Integer metadata from a block.");
				}
			}
		}
		return result;
	}


	/**
	 * Remove DeathChestBlock metadata from a block
	 * @param block
	 */
	public final void removeBlockMetadata() {
	
		final Block block = this.getLocation().getBlock();
		
		block.removeMetadata("deathchest-owner", plugin);
		block.removeMetadata("deathchest-killer", plugin);
		block.removeMetadata("deathchest-items-removed", plugin);
	}


	/**
	 * Remove DeathChestBlock metadata from a block
	 * @param block
	 */
	static final void removeBlockMetadata(final Block block) {
	
		block.removeMetadata("deathchest-owner", plugin);
		block.removeMetadata("deathchest-killer", plugin);
		block.removeMetadata("deathchest-items-removed", plugin);
	}


	/**
	 * Combine ItemStacks of same material up to max stack size
	 * @param itemStacks	Collection of ItemStacks to combine
	 * @return List of ItemStack with same materials combined
	 */
	public final static List<ItemStack> consolidateItems(final Collection<ItemStack> itemStacks) {

		final List<ItemStack> returnList = new ArrayList<ItemStack>();

		for (ItemStack itemStack : itemStacks) {
			if (itemStack == null) {
				continue;
			}

			for (ItemStack checkStack : returnList) {
				if (checkStack == null) {
					continue;
				}
				if (checkStack.isSimilar(itemStack)) {
					int transferAmount = 
							Math.min(itemStack.getAmount(),checkStack.getMaxStackSize() - checkStack.getAmount());
					itemStack.setAmount(itemStack.getAmount() - transferAmount);
					checkStack.setAmount(checkStack.getAmount()	+ transferAmount);
				}
			}
			if (itemStack.getAmount() > 0) {
				returnList.add(itemStack);
			}
		}
		return returnList;
	}


	/**
	 * Test if a block is a DeathChestBlock; either signs or chests with deathchest metadata
	 * @param block
	 * @return boolean True if block has deathchest-owner metadata, false if it does not
	 */
	public final static boolean isDeathChestBlock(final Block block) {

		// if passed block is null return false 
		if (block == null) {
			return false;
		}

		// return true if block is a death chest or death sign
		return (isDeathChest(block) || isDeathSign(block));
	}


	/**
	 * Test if a block is a deathchest sign; wall sign or sign post with deathchest metadata
	 * @param block
	 * @return true if block is a deathchest sign, false if not
	 */
	public final static boolean isDeathSign(final Block block) {

		// if passed block is null return false
		if (block == null) {
			return false;
		}

		// if block is wall sign or sign post and has death chest metadata, return true
		if ((block.getType().equals(Material.WALL_SIGN) 
				|| block.getType().equals(Material.SIGN_POST))
				&& block.hasMetadata("deathchest-owner")) {
			return true;
		}
		return false;
	}


	/**
	 * Test if a block is a deathchest chest; material type of chest with deathchest metadata
	 * @param block
	 * @return true if block is a deathchest chest, false if not
	 */
	public final static boolean isDeathChest(final Block block) {

		// if passed block is null return false
		if (block == null) {
			return false;
		}

		// if passed block is not a chest return false
		if (!block.getType().equals(Material.CHEST)) {
			return false;
		}

		// if passed block has death chest metadata return true, otherwise false
		return block.hasMetadata("deathchest-owner");
	}

	
	/**
	 * Test that inventory is a death chest inventory
	 * @param inventory
	 * @return
	 */
	public final static boolean isDeathChest(final Inventory inventory) {
		
		// if inventory type is not a chest inventory, return false
	    if (!inventory.getType().equals(InventoryType.CHEST)) {
	    	return false;
	    }
	    
	    // if inventory holder is null, return false
	    if (inventory.getHolder() == null) {
	    	return false;
	    }
	    
	    // try to get inventory holder block
    	Block block = null;
    	
    	try {
			if (inventory.getHolder() instanceof DoubleChest) {
				DoubleChest doubleChest;
				doubleChest = (DoubleChest) inventory.getHolder();
				block = doubleChest.getLocation().getBlock();
			}
			else {
				Chest chest;
				chest = (Chest) inventory.getHolder();
				block = chest.getBlock();
			}
		} catch (Exception e) {
			if (plugin.debug) {
				plugin.getLogger().warning("isDeathChest(inventory) threw an exception "
						+ "while trying to get inventory holder block.");
				plugin.getLogger().warning(e.getMessage());
			}
			return false;
		}

		// if inventory holder block is not a DeathChest, return false
		if (!DeathChestBlock.isDeathChest(block)) {
			return false;
		}
		return true;
	}



	/**
	 * Get DeathChest chest block that DeathSign is attached to
	 * @param passedBlock
	 * @return DeathChest chest block; returns null if sign is not a DeathSign or attached block is not a DeathChest
	 */
	public final Block getAttachedBlock() {

		// get block represented by this DeathChestBlock
		final Block block = this.getLocation().getBlock();
		
		// if block is null return null
		if (block == null) {
			return null;
		}

		// if block is not a DeathSign, return null
		if (!isDeathSign(block)) {
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
		else if (block.getType().equals(Material.SIGN_POST)) {
			returnBlock = block.getRelative(0, -1, 0);
		}

		// if attached block is not a DeathChest, return null
		if (!isDeathChest(returnBlock)) {
			return null;
		}
		
		return returnBlock;
	}


	/**
	 * Get DeathChest chest block that DeathSign is attached to
	 * @param block
	 * @return DeathChest chest block; returns null if sign is not a DeathSign or attached block is not a DeathChest
	 */
	static final Block getAttachedBlock(final Block block) {
	
		// if passed block is null return null
		if (block == null) {
			return null;
		}
	
		// if passed block is not a DeathSign, return null
		if (!isDeathSign(block)) {
			return null;
		}
	
		// initialize return block
		Block attachedBlock = null;
	
		// if block is wall sign, set block to attached block
		if (block.getType().equals(Material.WALL_SIGN)) {
			Sign sign = (Sign)block.getState().getData();
			attachedBlock = block.getRelative(sign.getAttachedFace());
		}
		// else if block is sign post, set block to one block below
		else if (block.getType().equals(Material.SIGN_POST)) {
			attachedBlock = block.getRelative(0, -1, 0);
		}
	
		// if attached block is not a death chest, return null
		if (!isDeathChest(attachedBlock)) {
			return null;
		}
		
		// return attached block
		return attachedBlock;
	}


	public final boolean isOwner(final Player player) {
		return this.getOwnerUUID().equals(player.getUniqueId());
	}


	public final boolean isKiller(final Player player) {
		return this.getKillerUUID().equals(player.getUniqueId());
	}

	
	/**
	 * Destroy a death chest block, dropping chest contents
	 */
	public final void destroy() {

		// get block represented by this DeathChestBlock
		final Block block = this.getLocation().getBlock();
		
		// delete record from datastore
		plugin.dataStore.deleteRecord(block.getLocation());
		
		// if block is indeed a DeathChestBlock, break block and drop contents
		if (DeathChestBlock.isDeathChestBlock(block)) {

			// if chunk containing death chest is not loaded, load it so items will drop
			if (!block.getChunk().isLoaded()) {
				block.getChunk().load();
			}
			
			// destroy block by setting to AIR, dropping contents but not block itself
			block.setType(Material.AIR);
		}
		
		// remove block metadata
		this.removeBlockMetadata();
		
		// cancel expire block task
		plugin.getServer().getScheduler().cancelTask(this.getExpireTaskId());
		
		if (plugin.debug) {
			plugin.getLogger().info("Expire chest task #" + this.getExpireTaskId() + " cancelled.");
		}
		
	}


	public final void expire() {
	
		// get player from ownerUUID
		final Player player = plugin.getServer().getPlayer(this.ownerUUID);
		
		// if deathChest no longer exists at location, do nothing and return
		if (!isDeathChest(this.getLocation().getBlock())) {
			return;
		}
		
		// if player is not null, send player message
		if (player != null) {
			plugin.messageManager.sendPlayerMessage(player, "chest-expired");
		}
		
		// destroy DeathChestBlock
		this.destroy();
	}


	public final int getViewerCount() {
		
		final Block block = this.getLocation().getBlock();
		
		int count = 0;
		
		// confirm block is a chest
		if (block.getType().equals(Material.CHEST)) {
			
			// get chest inventory object
			BlockState state = block.getState();
			Chest chest = (Chest)state;
			
			// get count of inventory viewers
			count = chest.getInventory().getViewers().size();
		}
		
		// return number of chest inventory viewers
		return count;
	}


	/**
	 * Open the inventory of this DeathChestBlock for player
	 * @param player
	 */
	public final void openInventory(final Player player) {

		// get the block state of block represented by this DeathChestBlock
		final BlockState blockState = this.getLocation().getBlock().getState();

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
	 * Transfer all chest contents to player inventory and remove in-game chest.
	 * Items that do not fit in player inventory will be dropped on ground.
	 * @param player
	 */
	public final void autoLoot(Player player) {

		// if passed player is null, do nothing and return
		if (player == null) {
			return;
		}

		// get reference to this deathChestBlock
		DeathChestBlock initialDeathChest = this;
		
		// if this deathChestBlock is a DeathSign, get attached chest
		if (isDeathSign(this.getLocation().getBlock())) {
			initialDeathChest = getChestInstance(this.getLocation().getBlock());
		}
		
		// if deathChestBlock is null (sign not attached to deathChest), do nothing and return
		if (initialDeathChest == null) {
			return;
		}

		DeathChestBlock secondDeathChest = null;
		
		// check for adjacent chests
		if (isDeathChest(LocationUtilities.blockToLeft(initialDeathChest.getLocation()))) {
			secondDeathChest = getChestInstance(LocationUtilities.blockToLeft(initialDeathChest.getLocation()));
		}
		else if (isDeathChest(LocationUtilities.blockToRight(initialDeathChest.getLocation()))) {
			secondDeathChest = getChestInstance(LocationUtilities.blockToRight(initialDeathChest.getLocation()));
		}
		else if (isDeathChest(LocationUtilities.blockInFront(initialDeathChest.getLocation()))) {
			secondDeathChest = getChestInstance(LocationUtilities.blockInFront(initialDeathChest.getLocation()));
		}
		else if (isDeathChest(LocationUtilities.blockToRear(initialDeathChest.getLocation()))) {
			secondDeathChest = getChestInstance(LocationUtilities.blockToRear(initialDeathChest.getLocation()));
		}
		
		// transfer contents and destroy chest
		initialDeathChest.transferChestContents(player);
		initialDeathChest.destroy();
		
		// if adjacent chest was found, transfer contents and destroy chest
		if (secondDeathChest != null) {
			secondDeathChest.transferChestContents(player);
			secondDeathChest.destroy();
		}
	}

	
	private final void transferChestContents(final Player player) {
		
		// get in-game block represented by this DeathChestBlock object
		Block block = this.getLocation().getBlock();
		
		if (block != null && isDeathChest(block)) {
			
			// get player inventory object
			final PlayerInventory playerinventory = player.getInventory();
			
			// get chest inventory object
			final Chest chest = (Chest)block.getState();
			
			// get array of ItemStack for chest inventory
			final ItemStack[] chestinventory = chest.getInventory().getContents();
			
			// iterate through all inventory slots in chest inventory
			for (int i = 0; i < chestinventory.length && playerinventory.firstEmpty() != -1; i++) {
				
				// if inventory slot item is not null...
				if (chestinventory[i] != null) {
					
					// add item to player inventory
					playerinventory.addItem(chestinventory[i]);
					
					// remove item from chest inventory
					chest.getInventory().removeItem(chestinventory[i]);
					
					// play inventory add sound
					plugin.messageManager.playerSound(player,"INVENTORY_ADD_ITEM");
				}
			}
		}
	}
}
