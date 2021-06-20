package com.winterhaven_mc.deathchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.configuration.Configuration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PluginMainTests {

    private ServerMock server;
    private WorldMock worldMock;
    private PluginMain plugin;

    @BeforeAll
    public void setUp() {
        // Start the mock server
        server = MockBukkit.mock();

        // create mock world
        worldMock = server.addSimpleWorld("world");

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
        @DisplayName("Test mock server is not null.")
        void MockServerNotNull() {
            Assertions.assertNotNull(server);
        }

        @Test
        @DisplayName("Test mock plugin is not null.")
        void MockPluginNotNull() {
            Assertions.assertNotNull(plugin);
        }
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

        @Test
        void test1() {
            Assertions.assertNotNull(plugin.getConfig().getKeys(false));
            for (String key : plugin.getConfig().getKeys(false)) {
                System.out.println("config key: " + key);
            }
        }

//        @SuppressWarnings("unused")
//        Set<String> ConfigFileKeys() {
//            return plugin.getConfig().getKeys(false);
//        }
//
//        @ParameterizedTest
//        @DisplayName("file config key is contained in enum.")
//        @MethodSource("ConfigFileKeys")
//        void ConfigFileKeyNotNull(String key) {
//            Assertions.assertNotNull(key);
//            System.out.println("config key:" + key + " value: " + config.getString(key));
//        }


        @ParameterizedTest
        @EnumSource(ConfigSetting.class)
        @DisplayName("ConfigSetting enum matches config file key/value pairs.")
        void ConfigFileKeysContainsEnumKey(ConfigSetting configSetting) {
            Assertions.assertEquals(configSetting.getValue(), plugin.getConfig().getString(configSetting.getKey()));
//            System.out.println("Enum name: " + configSetting.name());
        }

    }


    @Nested
    @DisplayName("Test Command Manager")
    class Commands {

        @Test
        @DisplayName("Test Help Command.")
        void HelpCommand() {
            server.dispatchCommand(server.addPlayer(), "/sdc help");
        }

        @Test
        @DisplayName("Test List Command.")
        void ListCommand() {
            server.dispatchCommand(server.addPlayer(), "/sdc list");
        }

        @Test
        @DisplayName("Test Reload Command.")
        void ReloadCommand() {
            server.dispatchCommand(server.addPlayer(), "/sdc reload");
        }

        @Test
        @DisplayName("Test Status Command.")
        void StatusCommand() {
            server.dispatchCommand(server.addPlayer(), "/sdc status");
        }


    }

    @Test
    @DisplayName("Test worldManager is not null.")
    void MockPluginWorldManagerNotNull() {
        Assertions.assertNotNull(plugin.worldManager);
    }

    @Test
    @DisplayName("Test data folder is not null.")
    void MockPluginDataFolderNotNull() {
        Assertions.assertNotNull(plugin.getDataFolder());
    }

    @Test
    @DisplayName("Test soundConfig is not null.")
    void SoundConfigNotNull() {
        Assertions.assertNotNull(plugin.soundConfig);
    }

}
