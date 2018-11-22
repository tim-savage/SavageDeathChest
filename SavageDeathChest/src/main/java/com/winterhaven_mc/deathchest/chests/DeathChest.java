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

import java.util.Collection;
import java.util.EnumMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * A class that represents a death chest, which is comprised of a collection of chest blocks
 */
public final class DeathChest {

	// reference to main class
	private final PluginMain plugin = PluginMain.instance;

	// the UUID of this death chest
	private UUID chestUUID;

	// the UUID of the owner of this death chest
	private UUID ownerUUID;

	// the UUID of the player who killed the death chest owner, if any; otherwise null
	private UUID killerUUID;

	// item count; for future use
	private int itemCount;

	// placementTime time of this death chest, in milliseconds since epoch
	private long placementTime;

	// the expirationTime time of this death chest, in milliseconds since epoch
	private long expirationTime;

	// task id of expire task for this death chest block
	private int expireTaskId;

	// set of chest blocks that make up this death chest
	private final EnumMap<ChestBlockType, ChestBlock> chestBlocks = new EnumMap<>(ChestBlockType.class);

	/**
	 * Class constructor
	 */
	public DeathChest() { }


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
		killerUUID = null;

		if (player.getKiller() != null) {
			killerUUID = player.getKiller().getUniqueId();
		}

		// set item count
		this.itemCount = 0;

		// set placementTime timestamp
		this.placementTime = System.currentTimeMillis();

		// set expirationTime timestamp
		// if configured expiration is zero (or negative), set expiration to zero to signify no expiration
		if (plugin.getConfig().getLong("expire-time") <= 0) {
			this.setExpirationTime(0);
		} else {
			// set expiration field based on config setting (in minutes)
			this.setExpirationTime(System.currentTimeMillis()
					+ TimeUnit.MINUTES.toMillis(plugin.getConfig().getLong("expire-time")));
		}
	}


	/**
	 * Getter method for DeathChest chestUUID
	 *
	 * @return UUID
	 */
	public final UUID getChestUUID() {
		return chestUUID;
	}


	/**
	 * Setter method for DeathChest chestUUID
	 *
	 * @param chestUUID the chest UUID to set in chestUUID field of the DeathChest object
	 */
	public final void setChestUUID(final UUID chestUUID) {
		this.chestUUID = chestUUID;
	}


	/**
	 * Getter method for DeathChest ownerUUID
	 *
	 * @return UUID
	 */
	public final UUID getOwnerUUID() {
		return ownerUUID;
	}


	/**
	 * Setter method for DeathChest ownerUUID
	 *
	 * @param ownerUUID the player UUID to set in ownerUUID field of the DeathChest object
	 */
	public final void setOwnerUUID(final UUID ownerUUID) {
		this.ownerUUID = ownerUUID;
	}


	/**
	 * Getter method for DeathChest killerUUID
	 *
	 * @return UUID
	 */
	public final UUID getKillerUUID() {
		return killerUUID;
	}


	/**
	 * Setter method for DeathChest killerUUID
	 *
	 * @param killerUUID the player UUID to set in the killerUUID field of the DeathChest object
	 */
	public final void setKillerUUID(final UUID killerUUID) {
		this.killerUUID = killerUUID;
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
	 * Setter method for DeathChest itemCount
	 * @param itemCount the itemCount to set
	 */
	@SuppressWarnings("unused")
	public void setItemCount(final int itemCount) {
		this.itemCount = itemCount;
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
	 * Setter method for DeathChest placementTime timestamp
	 *
	 * @param placementTime the placementTime time in milliseconds since epoch to set in the
	 *                  placementTime field of the DeathChest object
	 */
	public final void setPlacementTime(final long placementTime) {
		this.placementTime = placementTime;
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
	 * Setter method for DeathChest expirationTime timestamp
	 *
	 * @param expirationTime the expirationTime time in milliseconds since epoch to set in the
	 *                   expirationTime field of the DeathChest object
	 */
	public final void setExpirationTime(final long expirationTime) {
		this.expirationTime = expirationTime;
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
	 * Setter method for DeathChest expireTaskId
	 *
	 * @param expireTaskId the bukkit task id of the expire task associated with this DeathChest object
	 */
	private void setExpireTaskId(final int expireTaskId) {

		// set expire task id in this DeathChest object
		this.expireTaskId = expireTaskId;
	}


	/**
	 * Getter method for DeathChest chestBlocks
	 * @return List of Blocks in chestBlocks
	 */
	public Collection<ChestBlock> getChestBlocks() {
		return this.chestBlocks.values();
	}


	/**
	 * Add a chest block to this DeathChest
	 * @param chestBlockType the type of chest block to add to this DeathChest
	 * @param chestBlock the chest block to add to this DeathChest
	 */
	void addChestBlock(final ChestBlockType chestBlockType, final ChestBlock chestBlock) {
		if (chestBlock != null) {
			this.chestBlocks.put(chestBlockType, chestBlock);
		}
	}


	/**
	 * Set chest metadata
	 */
	final void setMetadata() {

		// set metadata on blocks in set
		for (ChestBlock chestBlock : this.getChestBlocks()) {
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
	@SuppressWarnings("SimplifiableIfStatement")
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
		for (ChestBlock chestBlock : chestBlocks.values()) {
			chestBlock.transferContents(player);
		}

		// destroy death chest
		this.destroy();
	}


	/**
	 * Destroy this death chest, dropping chest contents
	 */
	public final void destroy() {

		// play chest break sound at chest location
		plugin.soundConfig.playSound(this.getLocation(), SoundId.CHEST_BREAK);

		// destroy sign blocks first, to prevent detached sign drop
		if (chestBlocks.containsKey(ChestBlockType.SIGN)) {
			chestBlocks.get(ChestBlockType.SIGN).destroy();
			this.chestBlocks.remove(ChestBlockType.SIGN);
		}

		// destroy remaining DeathChest blocks
		for (ChestBlock chestBlock : chestBlocks.values()) {
			chestBlock.destroy();
		}

		// remove ChestBlocks from set
		this.chestBlocks.remove(ChestBlockType.LEFT_CHEST);
		this.chestBlocks.remove(ChestBlockType.RIGHT_CHEST);

		// delete DeathChest record from datastore
		plugin.dataStore.deleteChestRecord(this);

		// cancel expire block task
		plugin.getServer().getScheduler().cancelTask(this.getExpireTaskId());

		// remove DeathChest from ChestManager DeathChest map
		plugin.chestManager.removeDeathChest(this);
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
	 * Get the number of players currently viewing a DeathChest inventory
	 * @return The number of inventory viewers
     */
	public final int getViewerCount() {

		Block block = null;

		// get chestBlock
		for (ChestBlock chestBlock : this.chestBlocks.values()) {
			if (chestBlock.getLocation().getBlock().getType().equals(Material.CHEST)) {
				block = chestBlock.getLocation().getBlock();
				break;
			}
		}

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
	final void createExpireTask() {

		// if DeathChestBlock expirationTime is zero or less, it is set to never expire
		if (this.getExpirationTime() < 1) {
			return;
		}

		// get current time
		Long currentTime = System.currentTimeMillis();

		// get death chest block expire time
		Long expireTime = this.getExpirationTime();

		// compute ticks remaining until expire time
		long ticksRemaining = (expireTime - currentTime) / 50;
		if (ticksRemaining < 1) {
			ticksRemaining = (long) 1;
		}

		// create task to expire death chest after ticksRemaining
		BukkitTask chestExpireTask = new ExpireChestTask(this).runTaskLater(plugin, ticksRemaining);

		// set taskId in deathChest object
		this.setExpireTaskId(chestExpireTask.getTaskId());
	}


	/**
	 * Get chest location. Attempt to get chest location from right chest, left chest or sign in that order.
	 * Returns null if location could not be derived from chest blocks.
	 * @return Location - the chest location or null if no location found
	 */
	public Location getLocation() {

		if (chestBlocks.containsKey(ChestBlockType.RIGHT_CHEST)) {
			return this.chestBlocks.get(ChestBlockType.RIGHT_CHEST).getLocation();
		}
		else if (chestBlocks.containsKey(ChestBlockType.LEFT_CHEST)) {
			return this.chestBlocks.get(ChestBlockType.LEFT_CHEST).getLocation();
		}
		else if (chestBlocks.containsKey(ChestBlockType.SIGN)) {
			return this.chestBlocks.get(ChestBlockType.SIGN).getLocation();
		}
		else {
			return null;
		}
	}

}
