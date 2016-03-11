package com.winterhaven_mc.deathchest;

/*
 * Copyright (C) 2012
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal 
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 * SOFTWARE.
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigAccessor {

	private final String fileName;
	private final JavaPlugin plugin;

	private File configFile;
	private FileConfiguration fileConfiguration;

	/**
	 * Class constructor
	 * @param plugin
	 * @param fileName (will always contain system file separator character if there are subdirectories) 
	 * @throws IOException
	 */
	public ConfigAccessor(JavaPlugin plugin, String fileName) throws IOException {

		// check if passed reference to plugin is null
		if (plugin == null) {
			throw new IllegalArgumentException("plugin cannot be null.");
		}

		this.plugin = plugin;
		this.fileName = fileName;

		// get reference to plugin data folder
		File dataFolder = plugin.getDataFolder();
		
		if (dataFolder == null) {
			throw new IOException("The plugin data folder does not exist or cannot be accessed.");
		}

		// create new file object
		this.configFile = new File(plugin.getDataFolder(), fileName);
	}

	/**
	 * Reload the config file
	 */
	public void reloadConfig() {

		fileConfiguration = YamlConfiguration.loadConfiguration(configFile);

		// Look for defaults in the jar
		Reader defaultConfigReader = null;
		
		try {
			defaultConfigReader = new InputStreamReader(plugin.getResource(fileName.replace(File.separatorChar, '/')),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			plugin.getLogger().info("The embedded resource contained in " 
					+ "the plugin jar file has an unsupported encoding."
					+ "It should be encoded with UTF-8.");
		}
		
		if (defaultConfigReader != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigReader);
			fileConfiguration.setDefaults(defaultConfig);
		}
		else {
			plugin.getLogger().warning("The default resource in the plugin jar could not be read.");
		}
		
		// try to close the reader
		try {
			defaultConfigReader.close();
		} catch (IOException e) {
			plugin.getLogger().warning("An error occured while trying to close the resource file.");
		}
	}

	
	public FileConfiguration getConfig() {
		if (fileConfiguration == null) {
			this.reloadConfig();
		}
		return fileConfiguration;
	}

	public void saveConfig() {
		if (fileConfiguration == null || configFile == null) {
			return;
		} else {
			try {
				getConfig().save(configFile);
			} catch (IOException ex) {
				plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
			}
		}
	}

	public void saveDefaultConfig() {
		if (!configFile.exists()) {            
			this.plugin.saveResource(fileName, false);
		}
	}

}