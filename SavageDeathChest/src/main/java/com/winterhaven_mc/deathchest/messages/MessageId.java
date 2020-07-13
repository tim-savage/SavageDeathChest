package com.winterhaven_mc.deathchest.messages;


/**
 * An enum whose values represent the text messages displayed to users, corresponding to user
 * configured text in the language yaml files.
 */
public enum MessageId {

	CHEST_SUCCESS,
	DOUBLECHEST_PARTIAL_SUCCESS,
	CHEST_DENIED_PLUGIN,
	CHEST_DENIED_BLOCK,
	CHEST_DENIED_PERMISSION,
	CHEST_DENIED_ADJACENT,
	CHEST_DENIED_SPAWN_RADIUS,
	CHEST_DENIED_WORLD_DISABLED,
	CHEST_DENIED_VOID,
	INVENTORY_EMPTY,
	INVENTORY_FULL,
	NO_CHEST_IN_INVENTORY,
	NOT_OWNER,
	CHEST_EXPIRED,
	CREATIVE_MODE,
	NO_CREATIVE_ACCESS,
	CHEST_CURRENTLY_OPEN,

	COMMAND_FAIL_INVALID_COMMAND,
	COMMAND_FAIL_ARGS_COUNT_OVER,
	COMMAND_FAIL_HELP_PERMISSION,
	COMMAND_FAIL_LIST_PERMISSION,
	COMMAND_FAIL_LIST_OTHER_PERMISSION,
	COMMAND_FAIL_RELOAD_PERMISSION,
	COMMAND_FAIL_STATUS_PERMISSION,
	COMMAND_SUCCESS_RELOAD,

	COMMAND_HELP_INVALID,
	COMMAND_HELP_HELP,
	COMMAND_HELP_LIST,
	COMMAND_HELP_RELOAD,
	COMMAND_HELP_STATUS,

	LIST_HEADER,
	LIST_FOOTER,
	LIST_EMPTY,
	LIST_ITEM,
	LIST_ITEM_ALL,
	LIST_PLAYER_NOT_FOUND,

}
