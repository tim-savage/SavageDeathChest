package com.winterhaven_mc.deathchest.chests.search;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.chests.ChestSize;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class QuadrantSearch extends AbstractSearch implements Search {


	private enum Quadrant {
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


	public QuadrantSearch(final PluginMain plugin,
						  final Player player,
						  final ChestSize chestSize) {

		// call superclass constructor
		super(plugin, player, chestSize);
	}


	/**
	 * Execute search algorithm
	 */
	@Override
	public void execute() {

		// get player death location
		Location origin = player.getLocation();

		// if place-above-void configured true and player died in the void, start search at y=1
		if (origin.getY() < 1) {
			if (placeAboveVoid) {
				origin.setY(1);
			}
			else {
				result.setResultCode(ResultCode.VOID);
				result.setLocation(origin);
				return;
			}
		}

		// if player died above world max build height, start search 1 block below max build height
		origin.setY(Math.min(origin.getY(), player.getWorld().getMaxHeight() - 1));

		// set test location to copy of origin
		Location testLocation = origin.clone();

		// search all locations in vertical axis upward, then downward
		for (VerticalAxis verticalAxis : VerticalAxis.values()) {
			for (int y = 0; y < searchDistance; y++) {

				// if world max height reached, break loop
				if (y * verticalAxis.yFactor + testLocation.getY() >= player.getWorld().getMaxHeight()) {
					break;
				}

				// if world min height reached, break loop
				if (y * verticalAxis.yFactor + testLocation.getY() <= 0) {
					break;
				}

				// only test y == 0 in upper vertical axis
				if (!verticalAxis.equals(VerticalAxis.Upper) && y == 0) {
					continue;
				}

				// iterate over all locations within search distance until a valid location is found
				for (int x = 0; x < searchDistance; x++) {
					for (int z = 0; z < searchDistance; z++) {

						// search x,z coordinates in each quadrant
						for (Quadrant quadrant : Quadrant.values()) {

							// only test x == 0 or z == 0 in first quadrant
							if (!quadrant.equals(Quadrant.I) && (x == 0 || z == 0)) {
								continue;
							}

							// set new test location
							testLocation.add(x * quadrant.xFactor,
									y * verticalAxis.yFactor,
									z * quadrant.zFactor);

							// get result for test location
							result = validateChestLocation(player, testLocation, chestSize);

							// if test location is valid, return search result object
							if (result.getResultCode().equals(ResultCode.SUCCESS)) {
								return;
							}

							// rotate test location 90 degrees
							testLocation.setYaw(testLocation.getYaw() - 90);

							// get result for test location
							result = validateChestLocation(player, testLocation, chestSize);

							// if test location is valid, return search result object
							if (result.getResultCode().equals(ResultCode.SUCCESS)) {
								return;
							}

							// reset test location
							testLocation = origin.clone();
						}
					}
				}
			}
		}
	}

}
