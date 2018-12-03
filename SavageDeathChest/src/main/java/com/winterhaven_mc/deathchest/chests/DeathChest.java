package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.messages.MessageId;
import com.winterhaven_mc.deathchest.sounds.SoundId;
import com.winterhaven_mc.deathchest.tasks.ExpireChestTask;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * A class that represents a death chest, which is comprised of a collection of chest blocks
 */
@Immutable
public final class DeathChest {

	// reference to main class
	private final PluginMain plugin = PluginMain.instance;

	// the UUID of this death chest
	private final UUID chestUUID;

	// the UUID of the owner of this death chest
	private final UUID ownerUUID;

	// the UUID of the player who killed the death chest owner, if any; otherwise null
	private final UUID killerUUID;

	// item count; for future use
	private final int itemCount;

	// placementTime time of this death chest, in milliseconds since epoch
	private final long placementTime;

	// the expirationTime time of this death chest, in milliseconds since epoch
	private final long expirationTime;

	// task id of expire task for this death chest block
	private final int expireTaskId;


	/**
	 * Class constructor
	 * @param chestUUID the chest UUID
	 * @param ownerUUID the chest owner UUID
	 * @param killerUUID the chest killer UUID
	 * @param itemCount the chest item count
	 * @param placementTime the chest placement time
	 * @param expirationTime the chest expiration time
	 */
	public DeathChest(final UUID chestUUID,
					  final UUID ownerUUID,
					  final UUID killerUUID,
					  final int itemCount,
					  final long placementTime,
					  final long expirationTime) {

		this.chestUUID = chestUUID;
		this.ownerUUID = ownerUUID;
		this.killerUUID = killerUUID;
		this.itemCount = itemCount;
		this.placementTime = placementTime;
		this.expirationTime = expirationTime;
		this.expireTaskId = createExpireTask();
	}


	/**
	 * Class constructor
	 * @param player the death chest owner
	 */
	public DeathChest(final Player player) {

		// create random chestUUID
		this.chestUUID = UUID.randomUUID();

		// set playerUUID
		this.ownerUUID = player.getUniqueId();

		// set killerUUID
		if (player.getKiller() != null) {
			killerUUID = player.getKiller().getUniqueId();
		}
		else {
			killerUUID = null;
		}

		// set item count
		this.itemCount = 0;

		// set placementTime timestamp
		this.placementTime = System.currentTimeMillis();

		// set expirationTime timestamp
		// if configured expiration is zero (or negative), set expiration to zero to signify no expiration
		if (plugin.getConfig().getLong("expire-time") <= 0) {
			this.expirationTime = 0;
		} else {
			// set expiration field based on config setting (converting from minutes to milliseconds)
			this.expirationTime = System.currentTimeMillis()
					+ TimeUnit.MINUTES.toMillis(plugin.getConfig().getLong("expire-time"));
		}

		// set expireTaskId from new expire task
		this.expireTaskId = createExpireTask();
	}


	/**
	 * Getter method for DeathChest chestUUID
	 * @return UUID
	 */
	public final UUID getChestUUID() {
		return chestUUID;
	}


	/**
	 * Getter method for DeathChest ownerUUID
	 * @return UUID
	 */
	public final UUID getOwnerUUID() {
		return ownerUUID;
	}


	/**
	 * Getter method for DeathChest killerUUID
	 * @return UUID
	 */
	public final UUID getKillerUUID() {
		return killerUUID;
	}


	/**
	 * Getter method for DeathChest itemCount
	 * @return integer - itemCount
	 */
	@SuppressWarnings("unused")
	public int getItemCount() {
		return itemCount;
	}


	/**
	 * Getter method for DeathChest placementTime timestamp
	 * @return long placementTime timestamp
	 */
	public final long getPlacementTime() {
		return this.placementTime;
	}


	/**
	 * Getter method for DeathChest expirationTime timestamp
	 * @return long expirationTime timestamp
	 */
	public final long getExpirationTime() {
		return this.expirationTime;
	}


	/**
	 * Getter method for DeathChest expireTaskId
	 * @return the value of the expireTaskId field in the DeathChest object
	 */
	private int getExpireTaskId() {
		return this.expireTaskId;
	}


	/**
	 * Get chest location. Attempt to get chest location from right chest, left chest or sign in that order.
	 * Returns null if location could not be derived from chest blocks.
	 * @return Location - the chest location or null if no location found
	 */
	public Location getLocation() {

		Map<ChestBlockType,ChestBlock> chestBlockMap = plugin.chestManager.getChestBlockMap(this.chestUUID);

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
		for (ChestBlock chestBlock :  plugin.chestManager.getBlockSet(this.chestUUID)) {
			chestBlock.setMetadata(this);
		}
	}


	/**
	 * Test if a player is the owner of this DeathChest
	 * @param player The player to test for DeathChestB ownership
	 * @return {@code true} if the player is the DeathChest owner, false if not
     */
	public final boolean isOwner(final Player player) {

		// if ownerUUID is null, return false
		if (this.getOwnerUUID() == null ) {
			return false;
		}
		return this.getOwnerUUID().equals(player.getUniqueId());
	}


	/**
	 * Test if a player is the killer of this DeathChest owner
	 * @param player The player to test for DeathChest killer
	 * @return {@code true} if the player is the killer of the DeathChest owner, false if not
	 */
	public final boolean isKiller(final Player player) {

		// if killer uuid is null, return false
		if (this.getKillerUUID() == null) {
			return false;
		}
		return this.getKillerUUID().equals(player.getUniqueId());
	}


	/**
	 * Transfer all chest contents to player inventory and remove in-game chest.
	 * Items that do not fit in player inventory will be dropped on ground.
	 * @param player the player whose inventory the chest contents will be transferred
	 */
	public final void autoLoot(final Player player) {

		// if passed player is null, do nothing and return
		if (player == null) {
			return;
		}

		// transfer contents of any chest blocks to player
		for (ChestBlock chestBlock : plugin.chestManager.getBlockSet(this.chestUUID)) {
			chestBlock.transferContents(player);
		}

		// destroy death chest
		this.destroy();
	}


	/**
	 * Expire this death chest
	 */
	public final void expire() {

		// get player from ownerUUID
		final Player player = plugin.getServer().getPlayer(this.ownerUUID);

		// destroy DeathChest
		this.destroy();

		// if player is not null, send player message
		if (player != null) {
			plugin.messageManager.sendMessage(player, MessageId.CHEST_EXPIRED, this);
		}
	}


	/**
	 * Destroy this death chest, dropping chest contents
	 */
	public final void destroy() {

		// play chest break sound at chest location
		plugin.soundConfig.playSound(this.getLocation(), SoundId.CHEST_BREAK);

		// get block map for this chest
		Map<ChestBlockType,ChestBlock> chestBlockMap = plugin.chestManager.getChestBlockMap(this.chestUUID);

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
	 * Get the number of players currently viewing a DeathChest inventory
	 * @return The number of inventory viewers
     */
	public final int getViewerCount() {

		// get chest block map
		Map<ChestBlockType,ChestBlock> chestBlocks = plugin.chestManager.getChestBlockMap(this.chestUUID);

		// get chestBlock
		Block block = chestBlocks.get(ChestBlockType.RIGHT_CHEST).getLocation().getBlock();

		int count = 0;
		
		// confirm block is a chest
		if (block != null && block.getType().equals(Material.CHEST)) {
			
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
	 * Create expire chest task
	 */
	private int createExpireTask() {

		// if DeathChestBlock expirationTime is zero or less, it is set to never expire
		if (this.getExpirationTime() < 1) {
			return -1;
		}

		// get current time
		Long currentTime = System.currentTimeMillis();

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

}
