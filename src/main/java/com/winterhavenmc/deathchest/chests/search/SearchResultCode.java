package com.winterhavenmc.deathchest.chests.search;


/**
 * An enum whose values represent the possible results of a death chest location search
 */
public enum SearchResultCode {

	SUCCESS,
	PARTIAL_SUCCESS,
	PROTECTION_PLUGIN,
	SPAWN_RADIUS,
	NON_REPLACEABLE_BLOCK,
	ABOVE_GRASS_PATH,
	ADJACENT_CHEST,
	NO_CHEST,
	VOID,

}
