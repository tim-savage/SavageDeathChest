package com.winterhavenmc.deathchest.chests.search;

public enum VerticalAxis {

	UPPER(1),
	LOWER(-1);

	final int yFactor;

	VerticalAxis(int yFactor) {
		this.yFactor = yFactor;
	}

}
