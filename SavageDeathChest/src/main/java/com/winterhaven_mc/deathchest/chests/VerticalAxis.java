package com.winterhaven_mc.deathchest.chests;

public enum VerticalAxis {

	Up(1),
	Down(-1);

	int yFactor;

	VerticalAxis(int yFactor) {
		this.yFactor = yFactor;
	}

}
