/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathchest.chests;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.chests.search.SearchResult;
import com.winterhavenmc.deathchest.chests.search.SearchResultCode;
import com.winterhavenmc.deathchest.messages.Macro;
import com.winterhavenmc.deathchest.messages.MessageId;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.winterhavenmc.util.TimeUnit.MINUTES;


/**
 * Abstract class with common methods required for the deployment of a death chest
 */
public abstract class AbstractDeployment implements Deployment {

	protected final PluginMain plugin;
	protected final Player player;
	protected final Collection<ItemStack> droppedItems;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 * @param player the player for whom chest is being deployed
	 * @param droppedItems the player's death drops
	 */
	public AbstractDeployment(final PluginMain plugin, final Player player, final Collection<ItemStack> droppedItems) {
		this.plugin = plugin;
		this.player = player;
		this.droppedItems = droppedItems;
	}


	/**
	 * Execute deployment of death chest
	 *
	 * @return the result of the attempted death chest deployment
	 */
	public abstract SearchResult deploy();


	/**
	 * Check if Collection of ItemStack contains at least one chest
	 *
	 * @param itemStacks Collection of ItemStack to check for chest
	 * @return boolean - {@code true} if collection contains at least one chest, {@code false} if not
	 */
	boolean containsChest(final Collection<ItemStack> itemStacks) {

		// check for null parameter
		if (itemStacks == null) {
			return false;
		}

		boolean result = false;
		for (ItemStack itemStack : itemStacks) {
			if (itemStack.getType().equals(Material.CHEST)) {
				result = true;
				break;
			}
		}
		return result;
	}


	/**
	 * Remove one chest from list of item stacks. If a stack contains only one chest, remove the stack from
	 * the list and return. If a stack contains more than one chest, decrease the stack amount by one and return.
	 *
	 * @param itemStacks List of ItemStack to remove chest
	 * @return Collection of ItemStacks with one chest item removed. If passed collection contained no chest items,
	 * the returned collection will be a copy of the passed collection.
	 */
	Collection<ItemStack> removeOneChest(final Collection<ItemStack> itemStacks) {

		Collection<ItemStack> remainingItems = new LinkedList<>(itemStacks);

		Iterator<ItemStack> iterator = remainingItems.iterator();

		while (iterator.hasNext()) {

			ItemStack itemStack = iterator.next();

			if (itemStack.getType().equals(Material.CHEST) && !itemStack.hasItemMeta()) {
				if (itemStack.getAmount() == 1) {
					iterator.remove();
				}
				else {
					itemStack.setAmount(itemStack.getAmount() - 1);
				}
				break;
			}
		}
		return remainingItems;
	}


	/**
	 * Place a chest block and fill with items
	 *
	 * @param location       the location to place the chest block
	 * @param chestBlockType the type of chest block (left or right)
	 */
	void placeChest(final Player player, final DeathChest deathChest, final Location location, final ChestBlockType chestBlockType) {

		// get current block at location
		Block block = location.getBlock();

		// set block material to chest
		block.setType(Material.CHEST);

		// set custom inventory name
		setCustomInventoryName(player, block);

		// set chest direction
		setChestDirection(block, location);

		// create new ChestBlock object
		ChestBlock chestBlock = new ChestBlock(deathChest.getChestUid(), block.getLocation());

		// add this ChestBlock to block map
		plugin.chestManager.putBlock(chestBlockType, chestBlock);

		// set block metadata
		chestBlock.setMetadata(deathChest);
	}


	/**
	 * Conditional check for death chest required config and permission
	 *
	 * @return boolean - true if chest is required, false if not
	 */
	boolean chestRequired() {
		return plugin.getConfig().getBoolean("require-chest")
				&& !player.hasPermission("deathchest.freechest");
	}


	/**
	 * Set custom inventory name for chest
	 *
	 * @param player the owner of the chest
	 * @param block the chest block
	 */
	private void setCustomInventoryName(final Player player, final Block block) {

		// get custom inventory name from language file
		Optional<String> optionalInventoryName = plugin.messageBuilder.getString("CHEST_INFO.INVENTORY_NAME");

		if (optionalInventoryName.isEmpty()) {
			return;
		}

		String customInventoryName = optionalInventoryName.get();

		// if custom inventory name is not blank, do substitutions for player name
		if (!customInventoryName.isEmpty()) {
			customInventoryName = customInventoryName.replace("%PLAYER%", player.getDisplayName());
			customInventoryName = customInventoryName.replace("%OWNER%", player.getDisplayName());
		}
		else {
			// set default custom inventory name
			customInventoryName = "Death Chest";
		}

		// set custom inventory name in chest metadata
		org.bukkit.block.Chest chestState = (org.bukkit.block.Chest) block.getState();
		chestState.setCustomName(customInventoryName);
		chestState.update();
	}


	/**
	 * Set the block state for a death chest block
	 *
	 * @param block the chest block
	 * @param chestType the chest block type ( SINGLE, LEFT, RIGHT )
	 */
	void setChestBlockState(Block block, Chest.Type chestType) {

		// get chest block state
		BlockState chestBlockState = block.getState();

		// get chest block data
		Chest chestBlockData = (Chest) chestBlockState.getBlockData();

		// set chest block data type to single chest
		chestBlockData.setType(chestType);

		// set chest block data
		chestBlockState.setBlockData(chestBlockData);

		// update chest block state
		chestBlockState.update();
	}


	/**
	 * Set chest facing direction from player death location
	 *
	 * @param block the chest block
	 * @param location the player death location
	 */
	private void setChestDirection(final Block block, final Location location) {
		// get block direction data
		Directional blockData = (Directional) block.getBlockData();

		// set new direction from player death location
		blockData.setFacing(LocationUtilities.getCardinalBlockFace(location));

		// set block data
		block.setBlockData(blockData);
	}


	/**
	 * Finish a death chest deployment by sending messages and storing death chest in datastore
	 *
	 * @param searchResult the search result of the deployment
	 * @param deathChest the death chest object
	 */
	void finish(final SearchResult searchResult, final DeathChest deathChest) {

		// if debugging, log result
		if (plugin.getConfig().getBoolean("debug")) {
			logResult(searchResult);
		}

		// send message based on result
		sendResultMessage(player, deathChest, searchResult);

		// drop any remaining items that were not placed in a chest
		for (ItemStack item : searchResult.getRemainingItems()) {
			player.getWorld().dropItemNaturally(player.getLocation(), item);
		}

		// if result is negative cancel expire task and return
		if (!searchResult.getResultCode().equals(SearchResultCode.SUCCESS)
				&& !searchResult.getResultCode().equals(SearchResultCode.PARTIAL_SUCCESS)) {

			// cancel DeathChest expire task
			deathChest.cancelExpireTask();
			return;
		}

		// get configured chest protection time
		long chestProtectionTime = plugin.getConfig().getLong("chest-protection-time");

		// protection time is zero, set to negative to display infinite time in message
		if (chestProtectionTime == 0) {
			chestProtectionTime = -1;
		}

		// if chest protection is enabled and chest-protection-time is set (non-zero), send message
		if (plugin.getConfig().getBoolean("chest-protection") && chestProtectionTime > 0) {
			plugin.messageBuilder.compose(player, MessageId.CHEST_DEPLOYED_PROTECTION_TIME)
					.setMacro(Macro.OWNER, player.getName())
					.setMacro(Macro.LOCATION, deathChest.getLocation())
					.setMacro(Macro.PROTECTION_DURATION, MINUTES.toMillis(chestProtectionTime))
					.setMacro(Macro.PROTECTION_DURATION_MINUTES, MINUTES.toMillis(chestProtectionTime))
					.send();
		}

		// create new deathchest object with hologramUid set
//		DeathChest finalDeathChest = new DeathChest(deathChest, searchResult.getHologramUid());

		// put DeathChest in DeathChest map
//		plugin.chestManager.putChest(finalDeathChest);
		plugin.chestManager.putChest(deathChest);

		// put DeathChest in datastore
//		Set<DeathChest> deathChests = Collections.singleton(finalDeathChest);
		Set<DeathChest> deathChests = Collections.singleton(deathChest);
		plugin.chestManager.insertChestRecords(deathChests);
	}


	/**
	 * Send player message with result of death chest deployment
	 *
	 * @param player the player to send message
	 * @param deathChest the death chest object for player
	 * @param result the search result of the attempted deployment
	 */
	private void sendResultMessage(final Player player, final DeathChest deathChest, final SearchResult result) {

		// get configured expire-time
		long expireTime = plugin.getConfig().getLong("expire-time");

		// if configured expire-time is zero, set to negative to display infinite time in messages
		if (expireTime == 0) {
			expireTime = -1;
		}

		// send message based on result
		switch (result.getResultCode()) {
			case SUCCESS:
				plugin.messageBuilder.compose(player, MessageId.CHEST_SUCCESS)
						.setMacro(Macro.LOCATION, result.getLocation())
						.setMacro(Macro.EXPIRATION_DURATION, MINUTES.toMillis(expireTime))
						.setMacro(Macro.EXPIRATION_DURATION_MINUTES, MINUTES.toMillis(expireTime))
						.setMacro(Macro.PROTECTION_DURATION, MINUTES.toMillis(deathChest.getProtectionTime()))
						.setMacro(Macro.PROTECTION_DURATION_MINUTES, MINUTES.toMillis(deathChest.getProtectionTime()))
						.send();
				break;

			case PARTIAL_SUCCESS:
				plugin.messageBuilder.compose(player, MessageId.DOUBLECHEST_PARTIAL_SUCCESS)
						.setMacro(Macro.LOCATION, result.getLocation())
						.setMacro(Macro.EXPIRATION_DURATION, MINUTES.toMillis(expireTime))
						.setMacro(Macro.EXPIRATION_DURATION_MINUTES, MINUTES.toMillis(expireTime))
						.send();
				break;

			case PROTECTION_PLUGIN:
				plugin.messageBuilder.compose(player, MessageId.CHEST_DENIED_DEPLOYMENT_BY_PLUGIN)
						.setMacro(Macro.LOCATION, result.getLocation())
						.setMacro(Macro.PLUGIN, result.getProtectionPlugin())
						.send();
				break;

			case ABOVE_GRASS_PATH:
			case NON_REPLACEABLE_BLOCK:
				plugin.messageBuilder.compose(player, MessageId.CHEST_DENIED_BLOCK)
						.setMacro(Macro.LOCATION, result.getLocation())
						.send();
				break;

			case ADJACENT_CHEST:
				plugin.messageBuilder.compose(player, MessageId.CHEST_DENIED_ADJACENT)
						.setMacro(Macro.LOCATION, result.getLocation())
						.send();
				break;

			case NO_CHEST:
				plugin.messageBuilder.compose(player, MessageId.NO_CHEST_IN_INVENTORY)
						.setMacro(Macro.LOCATION, result.getLocation())
						.send();
				break;

			case SPAWN_RADIUS:
				plugin.messageBuilder.compose(player, MessageId.CHEST_DENIED_SPAWN_RADIUS)
						.setMacro(Macro.LOCATION, result.getLocation())
						.send();
				break;

			case VOID:
				plugin.messageBuilder.compose(player, MessageId.CHEST_DENIED_VOID)
						.setMacro(Macro.LOCATION, result.getLocation())
						.send();
				break;
		}
	}


	/**
	 * Log result of a death chest deployment for debugging purposes
	 *
	 * @param result the search result of a death chest deployment
	 */
	private void logResult(final SearchResult result) {

		if (result == null) {
			plugin.getLogger().info("SearchResult is null!");
			return;
		}

		if (result.getResultCode() != null) {
			plugin.getLogger().info("SearchResult Code: " + result.getResultCode());
		}
		if (result.getLocation() != null) {
			plugin.getLogger().info("Location: " + result.getLocation());
		}
		if (result.getProtectionPlugin() != null) {
			plugin.getLogger().info("Protection Plugin: " + result.getProtectionPlugin());
		}
		if (result.getRemainingItems() != null) {
			plugin.getLogger().info("Remaining Items: " + result.getRemainingItems());
		}
	}

}
