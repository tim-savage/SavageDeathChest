package com.winterhaven_mc.deathchest.storage;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;


public class DeathRecord {

    // the chest location for this death record
    private Location location;

    // the UUID of the owner of this death record
    private UUID ownerUUID;

    // the UUID of the player who killed the death record owner, if any; otherwise null
    private UUID killerUUID;

    // the expiration time of this death record, in milliseconds since epoch
    private long expiration;

    // number of items removed from this death record by killer
    private int itemsRemoved;


    /**
     * class constructor
     * @param player chest owner
     */
    public DeathRecord(Player player) {

        this.ownerUUID = player.getUniqueId();
    }


    /**
     * class constructor
      * @param player chest owner
     * @param location chest location
     */
    public DeathRecord(Player player, Location location) {

        this.ownerUUID = player.getUniqueId();
        this.location = location;
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

    public int getItemsRemoved() {
        return itemsRemoved;
    }

    public void setItemsRemoved(int itemsRemoved) {
        this.itemsRemoved = itemsRemoved;
    }

}
