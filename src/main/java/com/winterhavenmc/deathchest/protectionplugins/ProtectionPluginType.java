package com.winterhavenmc.deathchest.protectionplugins;

import org.bukkit.plugin.java.JavaPlugin;

public enum ProtectionPluginType {

	GRIEF_PREVENTION("GriefPrevention") {
		@Override
		ProtectionPlugin enable(final JavaPlugin plugin) {
			return new GriefPrevention(plugin, getName());
		}
	},

	LANDS("Lands") {
		@Override
		ProtectionPlugin enable(final JavaPlugin plugin) {
			return new Lands(plugin, getName());
		}
	},

	ROAD_BLOCK("RoadBlock") {
		@Override
		ProtectionPlugin enable(final JavaPlugin plugin) {
			return new RoadBlock(plugin, getName());
		}
	},

	TOWNY("Towny") {
		@Override
		ProtectionPlugin enable(final JavaPlugin plugin) {
			return new Towny(plugin, getName());
		}
	},

	WORLDGUARD("WorldGuard") {
		@Override
		ProtectionPlugin enable(final JavaPlugin plugin) {
			return new WorldGuard(plugin, getName());
		}
	};

	private final String name;


	ProtectionPluginType(final String name) {
		this.name = name;
	}

	abstract ProtectionPlugin enable(final JavaPlugin plugin);

	String getName() {
		return this.name;
	}

}
