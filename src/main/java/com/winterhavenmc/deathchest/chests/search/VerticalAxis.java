package com.winterhavenmc.deathchest.chests.search;

enum VerticalAxis {

	UPPER(1),
	LOWER(-1);

	final int yFactor;

	VerticalAxis(final int yFactor) {
		this.yFactor = yFactor;
	}

}
