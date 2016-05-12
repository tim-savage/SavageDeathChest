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
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;
import org.bukkit.metadata.FixedMetadataValue;

public class DeathChestBlock {
	
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
		
		// set deathChestBlock metadata
		this.setMetadata();
	}
	
	/**
	 * Get death chest block object from existing death chest block
	 * @param block
	 */
	public DeathChestBlock(final Block block) {
		
		// test if block is death chest block
		if (DeathChestBlock.isDeathChestBlock(block)) {
		
			// set location field
			this.setLocation(block.getLocation());

			// try to set owner uuid
			if (block.hasMetadata("deathchest-owner")) {
				try {
					this.setOwnerUUID(UUID.fromString(block.getMetadata("deathchest-owner").get(0).asString()));
				}
				catch (Exception e) {
					this.setOwnerUUID(null);
				}
			}
			else {
				this.setOwnerUUID(null);
			}

			// try to set killer uuid
			if (block.hasMetadata("deathchest-killer")) {
				try {
					this.setKillerUUID(UUID.fromString(block.getMetadata("deathchest-killer").get(0).asString()));
				}
				catch (Exception e) {
					this.setKillerUUID(null);
				}
			}
			else {
				this.setKillerUUID(null);
			}
		}
	}

	
	/**
	 * Getter method for DeathChestBlock location
	 * @return location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Setter method for DeathChestBlock location
	 * @param location
	 */
	public void setLocation(final Location location) {
		this.location = location;
	}

	/**
	 * Getter method for DeathChestBlock ownerUUID
	 * @return UUID
	 */
	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	/**
	 * Setter method for DeathChestBlock ownerUUID
	 * @param ownerUUID
	 */
	public void setOwnerUUID(UUID ownerUUID) {
		this.ownerUUID = ownerUUID;
	}

	/**
	 * Getter method for DeathChestBlock killerUUID
	 * @return UUID
	 */
	public UUID getKillerUUID() {
		return killerUUID;
	}

	/**
	 * Setter method for DeathChestBlock killerUUID
	 * @param killerUUID
	 */
	public void setKillerUUID(UUID killerUUID) {
		this.killerUUID = killerUUID;
	}

	/**
	 * Getter method for DeathChestBlock expiration
	 * @return long
	 */
	public long getExpiration() {
		return expiration;
	}

	/**
	 * Setter method for DeathChestBlock expiration
	 * @param expiration
	 */
	public void setExpiration(final long expiration) {
		this.expiration = expiration;
	}

	
	/**
	 * Set block metadata
	 */
	public void setMetadata() {
		
		Block block = this.location.getBlock();

		// set owner uuid metadata
		if (this.ownerUUID != null) {
			block.setMetadata("deathchest-owner", new FixedMetadataValue(plugin, this.ownerUUID));
		}
		
		// set killer uuid metadata
		if (this.killerUUID != null) {
			block.setMetadata("deathchest-killer", new FixedMetadataValue(plugin, this.killerUUID));
		}
	}

	
	/** Combine item stacks of same material up to max stack size
	 * 
	 * @param itemlist	Collection of ItemStack to combine
	 * @return List of ItemStack with same materials combined
	 */
	public static List<ItemStack> consolidateItems(final Collection<ItemStack> itemlist) {

		List<ItemStack> returnlist = new ArrayList<ItemStack>();
		
		for (ItemStack itemstack : itemlist) {
			if (itemstack == null) {
				continue;
			}
			
			for (ItemStack checkstack : returnlist) {
				if (checkstack == null) {
					continue;
				}
				if (checkstack.isSimilar(itemstack)) {
					int transfer = Math.min(itemstack.getAmount(),checkstack.getMaxStackSize() - checkstack.getAmount());
					itemstack.setAmount(itemstack.getAmount() - transfer);
					checkstack.setAmount(checkstack.getAmount()	+ transfer);
				}
			}
			if (itemstack.getAmount() > 0) {
				returnlist.add(itemstack);
			}
		}
		return returnlist;
	}
	
	
	/**
	 * Test if a block is a DeathChestBlock; either signs or chests with death chest metadata
	 * @param block
	 * @return boolean True if block has deathchest-owner metadata, false if it does not
	 */
	public static boolean isDeathChestBlock(final Block block) {
		
		// if passed block is null return false 
		if (block == null) {
			return false;
		}
		
		// return true if block is a death chest or death sign
		return (isDeathChest(block) || isDeathSign(block));
	}
	
	
	public static boolean isDeathSign(final Block block) {
		
		// if passed block is null return false
		if (block == null) {
			return false;
		}
		
		// if passed block is not a wall sign and is not a sign post, return false
		if (!block.getType().equals(Material.WALL_SIGN) && !block.getType().equals(Material.SIGN_POST)) {
			return false;
		}

		// if passed block does not have death chest metadata, return false
		if (!block.hasMetadata("deathchest-owner")) {
			return false;
		}		
		return true;
	}

	
	public static boolean isDeathChest(final Block block) {
		
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

	public static Block getSignAttachedBlock(final Block passedBlock) {
		
		// if passed block is null return null
		if (passedBlock == null) {
			return null;
		}
		
		// if passed block is not a DeathSign, return null
		if (!isDeathSign(passedBlock)) {
			return null;
		}
//		// if passed block is not a wall sign and is not a sign post, return null
//		if (!passedBlock.getType().equals(Material.WALL_SIGN) && !passedBlock.getType().equals(Material.SIGN_POST)) {
//			return null;
//		}

		// initialize return block
		Block returnBlock = null;
		
		// if block is wall sign, set block to attached block
		if (passedBlock.getType().equals(Material.WALL_SIGN)) {
		    Sign sign = (Sign)passedBlock.getState().getData();
		    returnBlock = passedBlock.getRelative(sign.getAttachedFace());
		}
		// else if block is sign post, set block to one block below
		else if (passedBlock.getType().equals(Material.SIGN_POST)) {
			returnBlock = passedBlock.getRelative(0, -1, 0);
		}
		
		return returnBlock;
	}
	
	
	/**
	 * Test if a player is a DeathChestBlockOwner
	 * @param player
	 * @param block
	 * @return
	 */
	public static boolean isDeathChestOwner(final Player player, final Block block) {
		
		if (block == null) {
			return false;
		}
		
		if (block.hasMetadata("deathchest-owner")
				&& block.getMetadata("deathchest-owner")
				.get(0).equals(player.getUniqueId())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Test if a player is a DeathChestBlockKiller
	 * @param player
	 * @param block
	 * @return
	 */
	public static boolean isDeathChestKiller(final Player player, final Block block) {
		
		if (block == null) {
			return false;
		}
		
		if (block.hasMetadata("deathchest-killer")
				&& block.getMetadata("deathchest-killer")
				.get(0).equals(player.getUniqueId())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Remove DeathChestBlock metadata from a block
	 * @param block
	 */
	public static void removeMetadata(final Block block) {
		
		block.removeMetadata("deathchest-owner", PluginMain.instance);
		block.removeMetadata("deathchest-killer", PluginMain.instance);
	}
	
	
	/**
	 * Open inventory associated with a death chest for player
	 * @param player
	 * @param passedBlock
	 */
	public static void openInventory(final Player player, final Block passedBlock) {
		
		// if block is not a death chest block, do nothing and return
		if (!isDeathChestBlock(passedBlock)) {
			return;
		}
		
		Block block = passedBlock;
		
		// if block is wall sign, set block to attached block
		if (block.getType().equals(Material.WALL_SIGN)) {
		    Sign sign = (Sign)block.getState().getData();
		    block = block.getRelative(sign.getAttachedFace());
		}
		// if block is sign post, set block to one block below
		else if (block.getType().equals(Material.SIGN_POST)) {
			block = block.getRelative(0, -1, 0);
		}
		
		// confirm block is a death chest
		if (! block.getType().equals(Material.CHEST) || ! DeathChestBlock.isDeathChestBlock(block)) {
			return;
		}
		
		// open chest inventory
		BlockState state = block.getState();
		Chest chest = (Chest)state;
		Inventory inventory = chest.getInventory();
		player.openInventory(inventory);
	}
	
}
