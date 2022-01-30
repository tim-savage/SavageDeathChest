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

package com.winterhavenmc.deathchest.chests.search;


/**
 * An enum that defines upper and lower region and provides a multiplier to achieve
 * the positive or negative sign of each member
 */
enum VerticalAxis {

	UPPER(1),
	LOWER(-1);

	final int yFactor;

	VerticalAxis(final int yFactor) {
		this.yFactor = yFactor;
	}

}
