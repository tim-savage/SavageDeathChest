package com.winterhaven_mc.deathchest.chests.search;

public enum VerticalAxis {

	Upper(1),
	Lower(-1);

	int yFactor;

	VerticalAxis(int yFactor) {
		this.yFactor = yFactor;
	}

}
