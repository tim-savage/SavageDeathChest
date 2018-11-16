package com.winterhaven_mc.deathchest.chests;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.messages.MessageId;
import com.winterhaven_mc.deathchest.tasks.ExpireChestTask;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.EnumMap;
import java.util.UUID;


public final class DeathChest {

	// reference to main class
	private final PluginMain plugin = PluginMain.instance;

	// the UUID of this death chest
	private UUID chestUUID;

	// the UUID of the owner of this death chest
	private UUID ownerUUID;

	// the UUID of the player who killed the death chest owner, if any; otherwise null
	private UUID killerUUID;

	// the expiration time of this death chest item, in milliseconds since epoch
	private long expiration;

	// task id of expire task for this death chest block
	private int expireTaskId;

	// set of chest blocks that make up this death chest
	private EnumMap<ChestBlockType, ChestBlock> chestBlocks = new EnumMap<>(ChestBlockType.class);

	/**
	 * Class constructor
	 */
	public DeathChest() { }


	/**
	 * Class constructor
	 * @param player the death chest owner
	 */
	public DeathChest(Player player) {

		// create random chestUUID
		this.chestUUID = UUID.randomUUID();

		// set playerUUID
		this.ownerUUID = player.getUniqueId();

		// set killerUUID
		killerUUID = null;

		if (player.getKiller() != null) {
			killerUUID = player.getKiller().getUniqueId();
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
	 * Getter method for DeathChest expiration
	 *
	 * @return long expiration timestamp
	 */
	public final long getExpiration() {
		return this.expiration;
	}


	/**
	 * Setter method for DeathChest expiration
	 *
	 * @param expiration the expiration time in milliseconds since epoch to set in the
	 *                   expiration field of the DeathChest object
	 */
	public final void setExpiration(final long expiration) {
		this.expiration = expiration;
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

			if (plugin.debug) {
				plugin.getLogger().info("Setting metadata on chest block at " + chestBlock.getLocation().toString());
			}

			setMetadata(chestBlock);
		}
	}


	/**
	 * Set block metadata
	 */
	private void setMetadata(final ChestBlock chestBlock) {

		// get in game block at chest block location
		Block block = chestBlock.getLocation().getBlock();

		if (plugin.debug) {
			plugin.getLogger().info("Setting metadata on block type " + block.getType().toString());
		}

		for (Material material : ChestManager.deathChestMaterials) {
			plugin.getLogger().info(material.toString());
		}

		// if block is not death chest material, do nothing and return
		if (!ChestManager.deathChestMaterials.contains(block.getType())) {
			return;
		}

		// set chest uuid metadata
		if (this.chestUUID != null) {
			block.setMetadata("deathchest-uuid", new FixedMetadataValue(plugin, this.chestUUID));
		}

		// set owner uuid metadata
		if (this.ownerUUID != null) {
			block.setMetadata("deathchest-owner", new FixedMetadataValue(plugin, this.ownerUUID));
		}

		// set killer uuid metadata
		if (this.killerUUID != null) {
			block.setMetadata("deathchest-killer", new FixedMetadataValue(plugin, this.killerUUID));
		}

		if (plugin.debug) {
			plugin.getLogger().info("Metadata set on DeathChest block" + block.toString());
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
	public final void autoLoot(Player player) {

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
	 * Destroy a death chest, dropping chest contents
	 */
	public final void destroy() {

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

		if (plugin.debug) {
			plugin.getLogger().info("Expire chest task #" + this.getExpireTaskId() + " cancelled.");
			plugin.getLogger().info("Removing chest UUID: " + this.getChestUUID());
		}

		// remove DeathChest from ChestManager DeathChest map
		plugin.chestManager.removeDeathChest(this);
	}


	public final void expire() {

		// get player from ownerUUID
		final Player player = plugin.getServer().getPlayer(this.ownerUUID);

		// destroy DeathChest
		this.destroy();

		// if player is not null, send player message
		if (player != null) {
			plugin.messageManager.sendMessage(player, MessageId.CHEST_EXPIRED);
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

		// if DeathChestBlock expiration is zero or less, it is set to never expire
		if (this.getExpiration() < 1) {
			return;
		}

		// get current time
		Long currentTime = System.currentTimeMillis();

		// get death chest block expire time
		Long expireTime = this.getExpiration();

		// compute ticks remaining until expire time
		long ticksRemaining = (expireTime - currentTime) / 50;
		if (ticksRemaining < 1) {
			ticksRemaining = (long) 1;
		}

		// create task to expire death chest after ticksRemaining
		BukkitTask chestExpireTask = new ExpireChestTask(this).runTaskLater(plugin, ticksRemaining);

		// set taskId in deathChest object
		this.setExpireTaskId(chestExpireTask.getTaskId());

		if (plugin.debug) {
			plugin.getLogger().info("Created chest expire task id:" + chestExpireTask.getTaskId());
		}
	}

}
