package com.winterhaven_mc.deathchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

import org.bukkit.configuration.Configuration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PluginConfigTests {

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


    @Test
    @DisplayName("server is not null.")
    void ServerNotNull() {
        Assertions.assertNotNull(server, "server is null.");
    }

    @Test
    @DisplayName("plugin is not null.")
    void PluginNotNull() {
        Assertions.assertNotNull(plugin, "plugin is null.");
    }

    @Nested
    @DisplayName("Test plugin config.")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Config {

        Configuration config = plugin.getConfig();
        Set<String> enumConfigKeyStrings = new HashSet<>();

        public Config() {
            for (ConfigSetting configSetting : ConfigSetting.values()) {
                this.enumConfigKeyStrings.add(configSetting.getKey());
            }
        }

        @Test
        @DisplayName("config not null.")
        void ConfigNotNull() {
            Assertions.assertNotNull(config);
        }

        @Test
        @DisplayName("test configured language.")
        void GetLanguage() {
            Assertions.assertEquals("en-US", config.getString("language"));
        }

        @Test
        @DisplayName("test enum string set not null")
        void EnumStringsNotNull() {
            Assertions.assertNotNull(enumConfigKeyStrings);
        }

        @Test
        @DisplayName("test enum string set not null")
        void EnumStringsNotEmpty() {
            Assertions.assertFalse(enumConfigKeyStrings.isEmpty());
        }

        @Test
        @DisplayName("test enum string set not null")
        void EnumStringsGreaterThanZero() {
            Assertions.assertTrue(enumConfigKeyStrings.size() > 0);
        }

        @SuppressWarnings("unused")
        Set<String> ConfigFileKeys() {
            return config.getKeys(false);
        }

//        @ParameterizedTest
//        @DisplayName("file config key is contained in enum.")
//        @MethodSource("ConfigFileKeys")
//        void ConfigFileKeyNotNull(String key) {
//            Assertions.assertNotNull(key);
//            Assertions.assertTrue(enumConfigKeyStrings.contains(key));
//            System.out.println("key '" + key + "' is contained in enum.");
//        }

        @ParameterizedTest
        @EnumSource(ConfigSetting.class)
        @DisplayName("ConfigSetting enum matches config file key/value pairs.")
        void ConfigFileKeysContainsEnumKey(ConfigSetting configSetting) {
            Assertions.assertEquals(configSetting.getValue(), config.getString(configSetting.getKey()));
        }
    }
}

