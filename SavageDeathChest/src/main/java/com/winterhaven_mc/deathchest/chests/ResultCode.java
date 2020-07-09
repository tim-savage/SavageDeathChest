package com.winterhaven_mc.deathchest.chests;


/**
 * An enum whose values represent the possible results of a death chest location search
 */
enum ResultCode {

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
