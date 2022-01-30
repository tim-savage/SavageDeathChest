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


/**
 * An enum whose values represent the different sizes of death chests
 */
public enum ChestSize {

	SINGLE(27),
	DOUBLE(54);

	private final int size;


	/**
	 * Constructor
	 *
	 * @param size the chest inventory size
	 */
	ChestSize(final int size) {
		this.size = size;
	}


	/**
	 * Determine chest size required for a given inventory size
	 *
	 * @param itemCount the number of ItemStacks to be considered for chest size
	 * @return ChestSize enum value, SINGLE or DOUBLE
	 */
	public static ChestSize selectFor(final int itemCount) {

		if (itemCount > SINGLE.size) {
			return DOUBLE;
		}
		return SINGLE;
	}

}
