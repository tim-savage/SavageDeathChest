package com.winterhaven_mc.deathchest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class DeathChestBlock {
	
	// reference to main class
	private DeathChestMain plugin = DeathChestMain.plugin;
	
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
	public DeathChestBlock(Player player, Block block) {

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
	public void setLocation(Location location) {
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
	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}

	
	/**
	 * Set block metadata
	 */
	public void setMetadata() {
		
		Block block = this.location.getBlock();

		// set owner uuid metadata
		if (this.ownerUUID != null) {
			block.setMetadata("deathchest-owner", new FixedMetadataValue(plugin, this.ownerUUID.toString()));
		}
		
		// set killer uuid metadata
		if (this.killerUUID != null) {
			block.setMetadata("deathchest-killer", new FixedMetadataValue(plugin, this.killerUUID.toString()));			
		}
	}

	
	/** Combine item stacks of same material up to max stack size
	 * 
	 * @param itemlist	List of itemstacks to combine
	 * @return List of ItemStack with same materials combined
	 */
	public static List<ItemStack> consolidateItems(List<ItemStack> itemlist) {

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
	 * Test if a block is a DeathChestBlock
	 * @param block
	 * @return boolean True if block has deathchest-owner metadata, false if it does not
	 */
	public static boolean isDeathChestBlock(Block block) {
		
		if (block == null) {
			return false;
		}
		
		return block.hasMetadata("deathchest-owner");
	}
	
	/**
	 * Remove DeathChestBlock metadata from a block
	 * @param block
	 */
	public static void removeMetadata(Block block) {
		
		block.removeMetadata("deathchest-owner", DeathChestMain.plugin);
		block.removeMetadata("deathchest-killer", DeathChestMain.plugin);
	}
	
}
