package com.winterhaven_mc.deathchest;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class DeathChest {

    // reference to main class
    private static final PluginMain plugin = PluginMain.instance;

    // the left chest location for this deathchest
    private Location leftChestLocation;

    // the right chest location for this deathchest
    private Location rightChestLocation;

    // the UUID of the owner of this deathchest
    private UUID ownerUUID;

    // the UUID of the player who killed the deathchest owner, if any; otherwise null
    private UUID killerUUID;

    // the expiration time of this deathchest, in milliseconds since epoch
    private long expiration;

    // task id of expire task for this death chest
    private int expireTaskId;

    // list of ItemStack containing items that did not fit in deployed chest
    private List<ItemStack> itemsDropped;

    // number of items removed from this deathchest by killer
    private int itemsRemoved;


    /**
     * Empty DeathChest constructor
     */
    public DeathChest() {}


    /**
     * DeathChest constructor
     */
    public DeathChest(Player player, List<ItemStack> droppedItems) { }

    public Location getLeftChestLocation() {
        return leftChestLocation;
    }

    public void setLeftChestLocation(Location leftChestLocation) {
        this.leftChestLocation = leftChestLocation;
    }

    public Location getRightChestLocation() {
        return rightChestLocation;
    }

    public void setRightChestLocation(Location rightChestLocation) {
        this.rightChestLocation = rightChestLocation;
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

    public int getExpireTaskId() {
        return expireTaskId;
    }

    public void setExpireTaskId(int expireTaskId) {
        this.expireTaskId = expireTaskId;
    }

    public List<ItemStack> getItemsDropped() {
        return itemsDropped;
    }

    public void setItemsDropped(List<ItemStack> itemsDropped) {
        this.itemsDropped = itemsDropped;
    }

    public int getItemsRemoved() {
        return itemsRemoved;
    }

    public void setItemsRemoved(int itemsRemoved) {
        this.itemsRemoved = itemsRemoved;
    }


}
