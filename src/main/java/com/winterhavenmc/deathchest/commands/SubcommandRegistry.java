package com.winterhavenmc.deathchest.commands;

import java.util.*;


final class SubcommandRegistry {

	Map<String, Subcommand> subcommandMap = new LinkedHashMap<>();
	Map<String, String> aliasMap = new HashMap<>();


	/**
	 * Register a subcommand in the map by name and register aliases (if any) in the alias map.
	 * @param subcommand an instance of the command
	 */
	void register(final Subcommand subcommand) {

		String name = subcommand.getName().toLowerCase();

		subcommandMap.put(name, subcommand);

		for (String alias : subcommand.getAliases()) {
			aliasMap.put(alias.toLowerCase(), name);
		}
	}


	/**
	 * Get command instance from map by name
	 * @param name the command to retrieve from the map
	 * @return Subcommand - the subcommand instance, or null if no matching name
	 */
	Subcommand getCommand(final String name) {

		String key = name;

		if (aliasMap.containsKey(key)) {
			key = aliasMap.get(key);
		}

		return (subcommandMap.get(key));
	}


	/**
	 * Get list of keys (subcommand names) from the subcommand map
	 * @return List of String - keys of the subcommand map
	 */
	Collection<String> getNames() {
		return new LinkedHashSet<>(subcommandMap.keySet());
	}
}
