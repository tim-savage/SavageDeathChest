package com.winterhavenmc.deathchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.winterhavenmc.deathchest.sounds.SoundId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SavageDeathChestPluginTests {

    private ServerMock server;
    private PluginMain plugin;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private PlayerMock player;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private WorldMock world;

    @BeforeAll
    public void setUp() {
        // Start the mock server
        server = MockBukkit.mock();

        // start the mock plugin
        plugin = MockBukkit.load(PluginMain.class);

        // create mock player
        player = server.addPlayer("testy");

        // create mock world
        world = MockBukkit.getMock().addSimpleWorld("world");

    }

    @AfterAll
    public void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }


    @Nested
    @DisplayName("Test mock objects.")
    class MockingSetupTests {

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
    class PluginMainObjectTests {

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
    class ConfigTests {

        Set<String> enumConfigKeyStrings = new HashSet<>();

        /**
         * Constructor for test class
         * populates enumConfigKeyStrings set
         */
        public ConfigTests() {
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



    @Nested
    @DisplayName("Test Sounds config.")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SoundTests {

        // collection of enum sound name strings
        Collection<String> enumSoundNames = new HashSet<>();

        // class constructor
        SoundTests() {
            // add all SoundId enum values to collection
            for (com.winterhavenmc.deathchest.sounds.SoundId SoundId : SoundId.values()) {
                enumSoundNames.add(SoundId.name());
            }
        }

        @SuppressWarnings("unused")
        Collection<String> GetConfigFileKeys() {
            return plugin.soundConfig.getSoundConfigKeys();
        }

        @ParameterizedTest
        @EnumSource(SoundId.class)
        @DisplayName("enum member soundId is contained in getConfig() keys.")
        void FileKeysContainsEnumValue(SoundId soundId) {
            Assertions.assertTrue(plugin.soundConfig.isValidSoundConfigKey(soundId.name()),
                    "Enum key '" + soundId.name() + "' not found in sound config file.");
        }

        @ParameterizedTest
        @MethodSource("GetConfigFileKeys")
        @DisplayName("config file key has matching key in enum sound names.")
        void SoundConfigEnumContainsAllFileSounds(String key) {
            Assertions.assertTrue(enumSoundNames.contains(key),
                    "sound config file key '" + key + "' not found in Enum.");
        }

        @ParameterizedTest
        @MethodSource("GetConfigFileKeys")
        @DisplayName("sound file key has valid bukkit sound name")
        void SoundConfigFileHasValidBukkitSound(String key) {
            String bukkitSoundName = plugin.soundConfig.getBukkitSoundName(key);
            Assertions.assertTrue(plugin.soundConfig.isValidBukkitSoundName(bukkitSoundName),
                    "File key '" + key + "' has invalid bukkit sound name: '" + bukkitSoundName + "'.");
        }

//        @Nested
//        @DisplayName("Play all sounds.")
//        class PlaySoundTests {
//
//            @Nested
//            @DisplayName("Play all sounds in SoundId for player")
//            class PlayerSoundTests {
//
//                private final EnumMap<SoundId, Boolean> soundsPlayed = new EnumMap<>(SoundId.class);
//
//                @ParameterizedTest
//                @EnumSource(SoundId.class)
//                @DisplayName("play sound for player")
//                void SoundConfigPlaySoundForPlayer(SoundId soundId) {
//                    plugin.soundConfig.playSound(player, soundId);
//                    soundsPlayed.put(soundId, true);
//                    Assertions.assertTrue(soundsPlayed.containsKey(soundId),
//                            "Sound '" + soundId.name() + "' did not play for player." );
//                }
//            }
//
//            @Nested
//            @DisplayName("Play all sounds in SoundId at world location")
//            class WorldSoundTests {
//
//                private final EnumMap<SoundId, Boolean> soundsPlayed = new EnumMap<>(SoundId.class);
//
//                @ParameterizedTest
//                @EnumSource(SoundId.class)
//                @DisplayName("play sound for location")
//                void SoundConfigPlaySoundForPlayer(SoundId soundId) {
//                    plugin.soundConfig.playSound(world.getSpawnLocation(), soundId);
//                    soundsPlayed.put(soundId, true);
//                    Assertions.assertTrue(soundsPlayed.containsKey(soundId),
//                            "Sound '" + soundId.name() + "' did not play for location." );
//                }
//            }
//        }

    }



    @Test
    @DisplayName("Test Help Command.")
    void HelpCommand() {
        Assertions.assertFalse(server.dispatchCommand(server.addPlayer(), "/deathchest help"),
                "help command returned true.");
    }

    @Test
    @DisplayName("Test List Command.")
    void ListCommand() {
        Assertions.assertFalse(server.dispatchCommand(server.addPlayer(), "/deathchest list"),
                "list command returned true.");
    }

    @Test
    @DisplayName("Test Reload Command.")
    void ReloadCommand() {
        Assertions.assertFalse(server.dispatchCommand(server.addPlayer(), "/deathchest reload"),
                "reload command returned true.");
    }

    @Test
    @DisplayName("Test Status Command.")
    void StatusCommand() {
        Assertions.assertFalse(server.dispatchCommand(server.addPlayer(), "/deathchest status"),
                "status command returned true.");
    }

}
