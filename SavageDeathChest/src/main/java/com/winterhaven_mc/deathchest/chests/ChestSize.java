package com.winterhaven_mc.deathchest.chests;

enum ChestSize {

	SINGLE(27),
	DOUBLE(54);

	private final int size;

	ChestSize(int size) {
		this.size = size;
	}

	public static ChestSize selectFor(int itemCount) {

		if (itemCount > SINGLE.size) {
			return DOUBLE;
		}
		return SINGLE;
	}

}
