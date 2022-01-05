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
	ChestSize(int size) {
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
