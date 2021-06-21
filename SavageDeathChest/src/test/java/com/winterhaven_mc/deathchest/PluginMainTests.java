package com.winterhaven_mc.deathchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashSet;
import java.util.Set;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PluginMainTests {

    private ServerMock server;
    private PluginMain plugin;

    @BeforeAll
    public void setUp() {
        // Start the mock server
        server = MockBukkit.mock();

        // start the mock plugin
        plugin = MockBukkit.load(PluginMain.class);

    }

    @AfterAll
    public void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }


    @Nested
    @DisplayName("Test mock objects.")
    class Mocking {

        @Test
        @DisplayName("server is not null.")
        void ServerNotNull() {
            Assertions.assertNotNull(server,"server is null.");
        }

        @Test
        @DisplayName("plugin is not null.")
        void PluginNotNull() {
            Assertions.assertNotNull(plugin,"plugin is null.");
        }

        @Test
        @DisplayName("plugin is enabled.")
        void PluginEnabled() {
            Assertions.assertTrue(plugin.isEnabled(),"plugin is not enabled.");
        }

        @Test
        @DisplayName("data folder is not null.")
        void DataFolderNotNull() {
            Assertions.assertNotNull(plugin.getDataFolder(),
                    "data folder is null.");
        }

    }


    @Nested
    @DisplayName("Test plugin main objects.")
    class PluginMainObjects {

        @Test
        @DisplayName("config not null.")
        void ConfigNotNull() {
            Assertions.assertNotNull(plugin.getConfig(),
                    "plugin configuration is null.");
        }

        @Test
        @DisplayName("world manager not null.")
        void WorldManagerNotNull() {
            Assertions.assertNotNull(plugin.worldManager,
                    "world manager is null.");
        }

        @Test
        @DisplayName("sound config not null.")
        void SoundConfigNotNull() {
            Assertions.assertNotNull(plugin.soundConfig,
                    "sound configuration is null.");
        }

        @Test
        @DisplayName("command manager not null.")
        void CommandManagerNotNull() {
            Assertions.assertNotNull(plugin.commandManager,
                    "command manager is null.");
        }

    }


    @Nested
    @DisplayName("Test plugin config.")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Config {

        Set<String> enumConfigKeyStrings = new HashSet<>();

        /**
         * Constructor for test class
         * populates enumConfigKeyStrings set
         */
        public Config() {
            for (ConfigSetting configSetting : ConfigSetting.values()) {
                this.enumConfigKeyStrings.add(configSetting.getKey());
            }
        }

        @Test
        @DisplayName("test enum string set not null")
        void EnumStringsNotNull() {
            Assertions.assertNotNull(enumConfigKeyStrings,
                    "Enum key set is null.");
        }

        @Test
        @DisplayName("test enum string set not empty.")
        void EnumStringsNotEmpty() {
            Assertions.assertFalse(enumConfigKeyStrings.isEmpty(),
                    "Enum key set is empty.");
        }

        @ParameterizedTest
        @EnumSource(ConfigSetting.class)
        @DisplayName("ConfigSetting enum matches config file key/value pairs.")
        void ConfigFileKeysContainsEnumKey(ConfigSetting configSetting) {
            Assertions.assertEquals(configSetting.getValue(), plugin.getConfig().getString(configSetting.getKey()),
                    "Enum key " + configSetting.getKey() + " not found in config file.");
        }

    }

}
