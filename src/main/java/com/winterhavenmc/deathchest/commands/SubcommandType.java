package com.winterhavenmc.deathchest.commands;

import com.winterhavenmc.deathchest.PluginMain;


public enum SubcommandType {

	HELP() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new HelpCommand(plugin, subcommandRegistry));
		}
	},

	LIST() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new ListCommand(plugin));
		}
	},

	RELOAD() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new ReloadCommand(plugin));
		}
	},

	STATUS() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new StatusCommand(plugin));
		}
	};

	abstract void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry);

}
