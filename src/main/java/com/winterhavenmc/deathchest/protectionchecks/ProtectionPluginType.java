package com.winterhavenmc.deathchest.protectionchecks;

import com.winterhavenmc.deathchest.protectionchecks.plugins.*;
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
