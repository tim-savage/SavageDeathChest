package com.winterhavenmc.deathchest.commands;

import com.winterhavenmc.deathchest.PluginMain;


enum SubcommandType {

	LIST() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new ListCommand(plugin);
		}
	},

	RELOAD() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new ReloadCommand(plugin);
		}
	},

	STATUS() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new StatusCommand(plugin);
		}
	};

	abstract Subcommand create(final PluginMain plugin);

}
