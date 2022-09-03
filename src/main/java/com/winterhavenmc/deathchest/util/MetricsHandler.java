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

package com.winterhavenmc.deathchest.util;

import com.winterhavenmc.deathchest.PluginMain;
import com.winterhavenmc.deathchest.permissions.protectionplugins.ProtectionPlugin;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

import java.util.HashMap;
import java.util.Map;


public class MetricsHandler {

	public MetricsHandler(PluginMain plugin) {

		Metrics metrics = new Metrics(plugin, 13916);

		// pie chart of configured language
		metrics.addCustomChart(new SimplePie("language", () -> plugin.getConfig().getString("language")));

		// pie chart for chest protection enabled
		metrics.addCustomChart(new SimplePie("chest_protection_enabled", () -> plugin.getConfig().getString("chest-protection")));

//		// pie chart for holograms enabled
//		metrics.addCustomChart(new SimplePie("holograms_enabled", () -> plugin.getConfig().getString("holograms-enabled")));

		// pie chart for killer looting enabled
		metrics.addCustomChart(new SimplePie("killer_looting_enabled", () -> plugin.getConfig().getString("killer-looting")));

		// pie chart for require chest enabled
		metrics.addCustomChart(new SimplePie("require_chest_enabled", () -> plugin.getConfig().getString("require-chest")));

		// pie chart for search distance
		metrics.addCustomChart(new SimplePie("search_distance", () -> plugin.getConfig().getString("search-distance")));

		// pie chart for creative deploy allowed
		metrics.addCustomChart(new SimplePie("creative_deploy_allowed", () -> plugin.getConfig().getString("creative-deploy")));

		// pie chart for creative access allowed
		metrics.addCustomChart(new SimplePie("creative_access_allowed", () -> plugin.getConfig().getString("creative-access")));

		// get number of currently deployed chests
		metrics.addCustomChart(new SingleLineChart("deployed_chest_count", () -> plugin.chestManager.getChestCount()));

		// pie chart of detected protection plugins
		metrics.addCustomChart(new AdvancedPie("protection_plugins", () -> {
			Map<String, Integer> valueMap = new HashMap<>();
			for (ProtectionPlugin protectionPlugin : plugin.protectionPluginRegistry.getAll()) {
				valueMap.put(protectionPlugin.getPluginName(), 1);
			}
			return valueMap;
		}));


//		// multi-line chart of detected protection plugins
//		metrics.addCustomChart(new MultiLineChart("protection_plugins", () -> {
//			Map<String, Integer> valueMap = new HashMap<>();
//			for (ProtectionPlugin protectionPlugin : plugin.protectionPluginRegistry.getAll()) {
//				valueMap.put(protectionPlugin.getPluginName(), 1);
//			}
//			return valueMap;
//		}));


//		// bar chart of detected protection plugins
//		metrics.addCustomChart(new SimpleBarChart("protection_plugins", () -> {
//			Map<String, Integer> map = new HashMap<>();
//			for (ProtectionPlugin protectionPlugin : plugin.protectionPluginRegistry.getAll()) {
//				map.put(protectionPlugin.getPluginName(), 1);
//			}
//			return map;
//		}));


	}

}
