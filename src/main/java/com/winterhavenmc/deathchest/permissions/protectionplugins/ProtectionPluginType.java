/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathchest.permissions.protectionplugins;

import com.winterhavenmc.deathchest.permissions.protectionplugins.plugins.*;
import org.bukkit.plugin.java.JavaPlugin;

public enum ProtectionPluginType {

	GRIEF_PREVENTION("GriefPrevention") {
		@Override
		ProtectionPlugin create(final JavaPlugin plugin, final String version) {
			return new GriefPrevention(plugin, getName(), version);
		}
	},

	LANDS("Lands") {
		@Override
		ProtectionPlugin create(final JavaPlugin plugin, final String version) {
			return new Lands(plugin, getName(), version);
		}
	},

	PRECIOUS_STONES("PreciousStones") {
		@Override
		ProtectionPlugin create(final JavaPlugin plugin, final String version) {
			return new PreciousStones(plugin, getName(), version);
		}
	},

	ROAD_BLOCK("RoadBlock") {
		@Override
		ProtectionPlugin create(final JavaPlugin plugin, final String version) {
			return new RoadBlock(plugin, getName(), version);
		}
	},

	TOWNY("Towny") {
		@Override
		ProtectionPlugin create(final JavaPlugin plugin, final String version) {
			return new Towny(plugin, getName(), version);
		}
	},

	WORLDGUARD("WorldGuard") {
		@Override
		ProtectionPlugin create(final JavaPlugin plugin, final String version) {
			return new WorldGuard(plugin, getName(), version);
		}
	};

	private final String name;


	ProtectionPluginType(final String name) {
		this.name = name;
	}

	abstract ProtectionPlugin create(final JavaPlugin plugin, final String version);

	String getName() {
		return this.name;
	}

}
