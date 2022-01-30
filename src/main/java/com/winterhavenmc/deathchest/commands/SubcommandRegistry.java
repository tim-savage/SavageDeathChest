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

package com.winterhavenmc.deathchest.commands;

import java.util.*;


final class SubcommandRegistry {

	final Map<String, Subcommand> subcommandMap = new LinkedHashMap<>();


	/**
	 * Register a subcommand in the map by name and register aliases (if any) in the alias map.
	 * @param subcommand an instance of the command
	 */
	void register(final Subcommand subcommand) {
		subcommandMap.put(subcommand.getName().toLowerCase(), subcommand);
	}


	/**
	 * Get command instance from map by name
	 * @param name the command to retrieve from the map
	 * @return Subcommand - the subcommand instance, or null if no matching name
	 */
	Subcommand getCommand(final String name) {
		return subcommandMap.get(name.toLowerCase());
	}


	/**
	 * Get list of keys (subcommand names) from the subcommand map
	 * @return List of String - keys of the subcommand map
	 */
	Collection<String> getNames() {
		return new LinkedHashSet<>(subcommandMap.keySet());
	}
}
