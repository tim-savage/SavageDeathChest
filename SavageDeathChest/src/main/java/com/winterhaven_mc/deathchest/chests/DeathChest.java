package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.messages.Macro;
import com.winterhaven_mc.deathchest.messages.Message;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import com.winterhaven_mc.deathchest.tasks.ExpireChestTask;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.winterhaven_mc.deathchest.messages.MessageId.CHEST_EXPIRED;
import static com.winterhaven_mc.deathchest.messages.MessageId.INVENTORY_FULL;


/**
 * A class that represents a death chest, which is comprised of a collection of chest blocks
 */
@Immutable
public final class DeathChest {

	// reference to main class
	private final PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	// the UUID of this death chest
	private final UUID chestUId;

	// the UUID of the owner of this death chest
	private final UUID ownerUid;

	// the UUID of the player who killed the death chest owner, if any; otherwise null
	private final UUID killerUid;

	// item count; for future use
	private final int itemCount;

	// placementTime time of this death chest, in milliseconds since epoch
	private final long placementTime;

	// the expirationTime time of this death chest, in milliseconds since epoch
	private final long expirationTime;

	// the protectionExpirationTime time of this death chest, in milliseconds since epoch
	private final long protectionExpirationTime;

	// task id of expire task for this death chest block
	private final int expireTaskId;

	/**
	 * Class constructor
	 * This constructor is used to create a DeathChest object from an existing record read from the datastore.
	 *
	 * @param chestUId      the chest UUID
	 * @param ownerUid      the chest owner UUID
	 * @param killerUid     the chest killer UUID
	 * @param itemCount      the chest item count
	 * @param placementTime  the chest placement time
	 * @param protectionExpirationTime the chest protection expiration time
	 * @param expirationTime the chest expiration time
	 */
	public DeathChest(final UUID chestUId,
					  final UUID ownerUid,
					  final UUID killerUid,
					  final int itemCount,
					  final long placementTime,
					  final long expirationTime,
	                  final long protectionExpirationTime) {

		this.chestUId = chestUId;
		this.ownerUid = ownerUid;
		this.killerUid = killerUid;
		this.itemCount = itemCount;
		this.placementTime = placementTime;
		this.expirationTime = expirationTime;
		this.protectionExpirationTime = protectionExpirationTime;
		this.expireTaskId = createExpireTask();
	}


	/**
	 * Class constructor
	 * This constructor is used to create a new DeathChest object on player death.
	 *
	 * @param player the death chest owner
	 */
	public DeathChest(final Player player) {

		// create random chestUUID
		this.chestUId = UUID.randomUUID();

		// set playerUUID
		if (player != null) {
			this.ownerUid = player.getUniqueId();
		}
		else {
			this.ownerUid = null;
		}

		// set killerUUID
		if (player != null && player.getKiller() != null) {
			this.killerUid = player.getKiller().getUniqueId();
		}
		else {
			this.killerUid = new UUID(0,0);
		}

		// set item count
		this.itemCount = 0;

		// set placementTime timestamp
		this.placementTime = System.currentTimeMillis();

		// set expirationTime timestamp
		// if configured expiration is zero (or negative), set expiration to zero to signify no expiration
		if (plugin.getConfig().getLong("expire-time") <= 0) {
			this.expirationTime = 0;
		}
		else {
			// set expiration field based on config setting (converting from minutes to milliseconds)
			this.expirationTime = System.currentTimeMillis()
					+ TimeUnit.MINUTES.toMillis(plugin.getConfig().getLong("expire-time"));
		}

		// set expireTaskId from new expire task
		this.expireTaskId = createExpireTask();

		// set protectionExpirationTime timestamp
		// if configured protection expiration is zero (or negative), set protection expiration to zero to signify no expiration
		if (plugin.getConfig().getLong("chest-protection-time") <= 0) {
			this.protectionExpirationTime = 0;
		}
		else {
			// set protection expiration field based on config setting (converting from minutes to milliseconds)
			this.protectionExpirationTime = System.currentTimeMillis()
					+ TimeUnit.MINUTES.toMillis(plugin.getConfig().getLong("chest-protection-time"));
		}
	}


	/**
	 * Getter method for DeathChest chestUUID
	 *
	 * @return UUID
	 */
	public final UUID getChestUid() {
		return chestUId;
	}


	/**
	 * Getter method for DeathChest ownerUUID
	 *
	 * @return UUID
	 */
	public final UUID getOwnerUid() {
		return ownerUid;
	}


	/**
	 * Getter method for DeathChest killerUUID
	 *
	 * @return UUID
	 */
	public final UUID getKillerUid() {
		return killerUid;
	}


	/**
	 * Getter method for DeathChest itemCount
	 *
	 * @return integer - itemCount
	 */
	@SuppressWarnings("unused")
	public final int getItemCount() {
		return itemCount;
	}


	/**
	 * Getter method for DeathChest placementTime timestamp
	 *
	 * @return long placementTime timestamp
	 */
	public final long getPlacementTime() {
		return this.placementTime;
	}


	/**
	 * Getter method for DeathChest expirationTime timestamp
	 *
	 * @return long expirationTime timestamp
	 */
	public final long getExpirationTime() {
		return this.expirationTime;
	}


	/**
	 * Getter method for DeathChest protectionExpirationTime timestamp
	 *
	 * @return long expirationTime timestamp
	 */
	public final long getProtectionExpirationTime() {
		return this.protectionExpirationTime;
	}


	/**
	 * Getter method for DeathChest expireTaskId
	 *
	 * @return the value of the expireTaskId field in the DeathChest object
	 */
	private int getExpireTaskId() {
		return this.expireTaskId;
	}


	/**
	 * Get chest location. Attempt to get chest location from right chest, left chest or sign in that order.
	 * Returns null if location could not be derived from chest blocks.
	 *
	 * @return Location - the chest location or null if no location found
	 */
	public final Location getLocation() {

		Map<ChestBlockType, ChestBlock> chestBlockMap = plugin.chestManager.getChestBlockMap(this.chestUId);

		if (chestBlockMap.containsKey(ChestBlockType.RIGHT_CHEST)) {
			return chestBlockMap.get(ChestBlockType.RIGHT_CHEST).getLocation();
		}
		else if (chestBlockMap.containsKey(ChestBlockType.LEFT_CHEST)) {
			return chestBlockMap.get(ChestBlockType.LEFT_CHEST).getLocation();
		}
		else if (chestBlockMap.containsKey(ChestBlockType.SIGN)) {
			return chestBlockMap.get(ChestBlockType.SIGN).getLocation();
		}

		return null;
	}


	/**
	 * Set chest metadata on all component blocks
	 */
	final void setMetadata() {

		// set metadata on blocks in set
		for (ChestBlock chestBlock : plugin.chestManager.getBlockSet(this.chestUId)) {
			chestBlock.setMetadata(this);
			if (plugin.debug) {
				plugin.getLogger().info("Metadata set on chest block " + this.chestUId);
			}
		}
	}


	/**
	 * Test if a player is the owner of this DeathChest
	 *
	 * @param player The player to test for DeathChest ownership
	 * @return {@code true} if the player is the DeathChest owner, false if not
	 */
	public final boolean isOwner(final Player player) {

		// if ownerUUID is null, return false
		if (this.getOwnerUid() == null) {
			return false;
		}
		return this.getOwnerUid().equals(player.getUniqueId());
	}


	/**
	 * Test if a player is the killer of this DeathChest owner
	 *
	 * @param player The player to test for DeathChest killer
	 * @return {@code true} if the player is the killer of the DeathChest owner, false if not
	 */
	public final boolean isKiller(final Player player) {
		return this.hasValidKillerUid() && this.getKillerUid().equals(player.getUniqueId());
	}


	/**
	 * Transfer all chest contents to player inventory and remove in-game chest if empty.
	 * Items that do not fit in player inventory will retained in chest.
	 *
	 * @param player the player whose inventory the chest contents will be transferred
	 */
	public final void autoLoot(final Player player) {

		// if passed player is null, do nothing and return
		if (player == null) {
			return;
		}

		// create ArrayList to hold items that did not fit in player inventory
		Collection<ItemStack> remainingItems = new ArrayList<>();

		// transfer contents of any chest blocks to player, putting any items that did not fit in remainingItems
		for (ChestBlock chestBlock : plugin.chestManager.getBlockSet(this.chestUId)) {
			remainingItems.addAll(chestBlock.transferContents(player));
		}

		// if remainingItems is empty, all chest items fit in player inventory so destroy chest and return
		if (remainingItems.isEmpty()) {
			this.destroy();
			return;
		}

		// send player message
		Message.create(player, INVENTORY_FULL)
				.setMacro(Macro.LOCATION, player.getLocation())
				.send();

		// try to put remaining items back in chest
		remainingItems = this.fill(remainingItems);

		// if remainingItems is still not empty, items could not be placed back in chest, so drop items at player location
		// this should never actually occur, but let's play it safe just in case
		if (!remainingItems.isEmpty()) {
			for (ItemStack itemStack : remainingItems) {
				player.getWorld().dropItem(player.getLocation(), itemStack);
			}
		}
	}


	/**
	 * Expire this death chest
	 */
	public final void expire() {

		// get player from ownerUUID
		final Player player = plugin.getServer().getPlayer(this.ownerUid);

		// destroy DeathChest
		this.destroy();

		// if player is not null, send player message
		if (player != null) {
			Message.create(player, CHEST_EXPIRED)
					.setMacro(Macro.LOCATION, this.getLocation())
					.send();
		}
	}


	/**
	 * Destroy this death chest, dropping chest contents
	 */
	public final void destroy() {

		// play chest break sound at chest location
		plugin.soundConfig.playSound(this.getLocation(), SoundId.CHEST_BREAK);

		// get block map for this chest
		Map<ChestBlockType, ChestBlock> chestBlockMap = plugin.chestManager.getChestBlockMap(this.chestUId);

		// destroy DeathChest blocks (sign gets destroyed first due to enum order)
		for (ChestBlock chestBlock : chestBlockMap.values()) {
			chestBlock.destroy();
		}

		// delete DeathChest record from datastore
		plugin.dataStore.deleteChestRecord(this);

		// cancel expire block task
		if (this.getExpireTaskId() > 0) {
			plugin.getServer().getScheduler().cancelTask(this.getExpireTaskId());
		}

		// remove DeathChest from ChestManager DeathChest map
		plugin.chestManager.removeDeathChest(this);
	}


	/**
	 * Get inventory associated with this death chest
	 *
	 * @return Inventory - the inventory associated with this death chest;
	 * returns null if both right and left chest block inventories are invalid
	 */
	public final Inventory getInventory() {

		// get chest block map
		Map<ChestBlockType, ChestBlock> chestBlocks = plugin.chestManager.getChestBlockMap(this.chestUId);

		// get right chest inventory
		Inventory inventory = chestBlocks.get(ChestBlockType.RIGHT_CHEST).getInventory();

		// if right chest inventory is null, try left chest
		if (inventory == null) {
			inventory = chestBlocks.get(ChestBlockType.LEFT_CHEST).getInventory();
		}

		// return the inventory, or null if right and left chest inventories were both invalid
		return inventory;
	}


	/**
	 * Get the number of players currently viewing a DeathChest inventory
	 *
	 * @return The number of inventory viewers
	 */
	public final int getViewerCount() {

		// get chest inventory
		Inventory inventory = this.getInventory();

		// if inventory is not null, return viewer count
		if (inventory != null) {
			return inventory.getViewers().size();
		}
		else {
			// inventory is null, so return 0 for viewer count
			return 0;
		}
	}


	/**
	 * Create expire chest task
	 */
	private int createExpireTask() {

		// if DeathChestBlock expirationTime is zero or less, it is set to never expire
		if (this.getExpirationTime() < 1) {
			return -1;
		}

		// get current time
		long currentTime = System.currentTimeMillis();

		// compute ticks remaining until expire time (millisecond interval divided by 50 yields ticks)
		long ticksRemaining = (this.expirationTime - currentTime) / 50;
		if (ticksRemaining < 1) {
			ticksRemaining = 1L;
		}

		// create task to expire death chest after ticksRemaining
		BukkitTask chestExpireTask = new ExpireChestTask(this).runTaskLater(plugin, ticksRemaining);

		// return taskId
		return chestExpireTask.getTaskId();
	}


	/**
	 * Cancel expire task for this death chest
	 */
	void cancelExpireTask() {

		// if task id is positive integer, cancel task
		if (this.expireTaskId > 0) {
			plugin.getServer().getScheduler().cancelTask(this.expireTaskId);
		}
	}


	/**
	 * Place collection of ItemStacks in chest, returning collection of ItemStacks that did not fit in chest
	 *
	 * @param itemStacks Collection of ItemStacks to place in chest
	 * @return Collection of ItemStacks that did not fit in chest
	 */
	final Collection<ItemStack> fill(final Collection<ItemStack> itemStacks) {

		// create empty list for return
		Collection<ItemStack> remainingItems = new ArrayList<>();

		// get inventory for this death chest
		Inventory inventory = this.getInventory();

		// if inventory is not null, add itemStacks to inventory and put leftovers in remainingItems
		if (inventory != null) {
			remainingItems = new ArrayList<>(inventory.addItem(itemStacks.toArray(new ItemStack[0])).values());
		}

		// return collection of items that did not fit in inventory
		return remainingItems;
	}


	/**
	 * Check if protection is enabled and ha expired
	 * @return boolean - true if protection has expired, false if not
	 */
	public final boolean protectionExpired() {
		return this.getProtectionExpirationTime() > 0 &&
				this.getProtectionExpirationTime() < System.currentTimeMillis();
	}


	public final boolean hasValidOwnerUid() {
		return this.ownerUid != null &&
				(this.ownerUid.getMostSignificantBits() != 0 && this.ownerUid.getLeastSignificantBits() != 0);
	}


	public final boolean hasValidKillerUid() {
		return this.killerUid != null &&
				(this.killerUid.getMostSignificantBits() != 0 && this.killerUid.getLeastSignificantBits() != 0);
	}


}
