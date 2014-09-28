package com.winterhaven_mc.deathchest;

import java.util.UUID;

import org.bukkit.Location;

public class DeathChestBlock {
	
	// a unique id for this deathchest item
	private int blockId;
	
	// the location for this deathchest item
	private Location location;
	
	// the UUID of the owner of this deathchest item
	private UUID ownerUUID;
	
	// the UUID of the player who killed the deathchest owner
	private UUID killerUUID;
	
	// the expiration time of this deathchest item, in milliseconds since epoch 
	private long expiration;

	public int getBlockId() {
		return blockId;
	}

	public void setBlockId(int blockId) {
		this.blockId = blockId;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	public void setOwnerUUID(UUID ownerUUID) {
		this.ownerUUID = ownerUUID;
	}

	public UUID getKillerUUID() {
		return killerUUID;
	}

	public void setKillerUUID(UUID killerUUID) {
		this.killerUUID = killerUUID;
	}

	public long getExpiration() {
		return expiration;
	}

	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}

}
