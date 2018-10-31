package com.winterhaven_mc.deathchest.util;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Iterator;


public final class ChestUtilities {

	/**
	 * Private constructor to prevent instantiation this class
	 */
	private ChestUtilities() {
		throw new AssertionError();
	}


	/**
	 * Check if block is above a grass path block
	 * @param block the block to check underneath
	 * @return true if passed block is above a grass path block, false if not
	 */
	public static boolean isAboveGrassPath(final Block block) {
		return block.getRelative(0, -1, 0).getType().equals(Material.GRASS_PATH);
	}


	/**
	 * Check if Collection of ItemStack contains at least one chest
	 * @param itemStacks Collection of ItemStack to check for chest
	 * @return boolean
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean hasChest(final Collection<ItemStack> itemStacks) {
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
	 * @param itemStacks List of ItemStack to remove chest
	 */
	public static void removeOneChest(final Collection<ItemStack> itemStacks) {

		Iterator<ItemStack> iterator = itemStacks.iterator();

		while (iterator.hasNext()) {

			ItemStack itemStack = iterator.next();

			if (itemStack.getType().equals(Material.CHEST)) {
				if (itemStack.getAmount() == 1) {
					iterator.remove();
				} else {
					itemStack.setAmount(itemStack.getAmount() - 1);
				}
				break;
			}
		}
	}

}
