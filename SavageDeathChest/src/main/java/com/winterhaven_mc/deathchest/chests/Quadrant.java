package com.winterhaven_mc.deathchest.chests;

public enum Quadrant {

	I(1,1),
	II(-1,1),
	III(-1,-1),
	IV(1,-1);

	int xFactor;
	int zFactor;


	Quadrant(final int xFactor, final int zFactor) {

		this.xFactor = xFactor;
		this.zFactor = zFactor;
	}

}
