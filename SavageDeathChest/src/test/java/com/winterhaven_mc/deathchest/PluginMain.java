package com.winterhaven_mc.deathchest;

import com.winterhaven_mc.deathchest.chests.ChestManager;
import com.winterhaven_mc.deathchest.commands.CommandManager;
import com.winterhaven_mc.deathchest.listeners.BlockEventListener;
import com.winterhaven_mc.deathchest.listeners.InventoryEventListener;
import com.winterhaven_mc.deathchest.listeners.PlayerEventListener;
import com.winterhaven_mc.util.LanguageHandler;
import com.winterhaven_mc.util.SoundConfiguration;
import com.winterhaven_mc.util.WorldManager;
import com.winterhaven_mc.util.YamlSoundConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

/**
 * The main class for SavageDeathChest plugin
 */
@SuppressWarnings("unused")
public final class PluginMain extends JavaPlugin {

    public boolean debug = getConfig().getBoolean("debug");

    public LanguageHandler languageHandler;
    public WorldManager worldManager;
    public SoundConfiguration soundConfig;
    public ChestManager chestManager;
    public CommandManager commandManager;
    public PlayerEventListener playerEventListener;
    public BlockEventListener blockEventListener;
    public InventoryEventListener inventoryEventListener;


    public PluginMain() {
        super();
    }


    protected PluginMain(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
        super(loader, descriptionFile, dataFolder, file);
    }


    @Override
    public void onEnable() {

        // copy default config from jar if it doesn't exist
        saveDefaultConfig();

        // initialize language manager
        languageHandler = new LanguageHandler(this);

        // instantiate world manager
        worldManager = new WorldManager(this);

        // instantiate sound configuration
        soundConfig = new YamlSoundConfiguration(this);

        // instantiate chest manager
//        chestManager = new ChestManager(this);

        // instantiate command manager
        commandManager = new CommandManager(this);

        // initialize event listeners
        playerEventListener = new PlayerEventListener(this);
        blockEventListener = new BlockEventListener(this);
        inventoryEventListener = new InventoryEventListener(this);

    }

}