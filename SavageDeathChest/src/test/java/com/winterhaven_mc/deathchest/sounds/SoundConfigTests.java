package com.winterhaven_mc.deathchest.sounds;


import com.winterhaven_mc.deathchest.PluginMain;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SoundConfigTests {
    private PluginMain plugin;
    @SuppressWarnings("FieldCanBeLocal")
    private ServerMock server;
    private WorldMock world;
    private PlayerMock player;

    @BeforeAll
    public void setUp() {
        // Start the mock server
        server = MockBukkit.mock();

        // create mock player
        player = server.addPlayer("testy");

        // create mock world
        world = MockBukkit.getMock().addSimpleWorld("world");

        // start the mock plugin
        plugin = MockBukkit.load(PluginMain.class);
    }

    @AfterAll
    public void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }


    @Nested
    @DisplayName("test mocking setup.")
    class MockingSetup {
        @Test
        @DisplayName("server is not null.")
        void ServerNotNull() {
            Assertions.assertNotNull(server, "server is null.");
        }

        @Test
        @DisplayName("world is not null.")
        void WorldNotNull() {
            Assertions.assertNotNull(world, "world is null.");
        }

        @Test
        @DisplayName("player is not null.")
        void PlayerNotNull() {
            Assertions.assertNotNull(player, "player is null.");
        }

        @Test
        @DisplayName("plugin is not null.")
        void PluginNotNull() {
            Assertions.assertNotNull(plugin, "plugin is null.");
        }
    }


    @Nested
    @DisplayName("Test Sounds config.")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Sounds {

        // collection of enum sound name strings
        Collection<String> enumSoundNames = new HashSet<>();

        // class constructor
        Sounds() {
            // add all SoundId enum values to collection
            for (SoundId SoundId : SoundId.values()) {
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

        @Nested
        @DisplayName("Play all sounds.")
        class PlaySounds {

            @Nested
            @DisplayName("Play all sounds in SoundId for player")
            class PlayerSounds {

                private final EnumMap<SoundId, Boolean> soundsPlayed = new EnumMap<>(SoundId.class);

                @ParameterizedTest
                @EnumSource(SoundId.class)
                @DisplayName("play sound for player")
                void SoundConfigPlaySoundForPlayer(SoundId soundId) {
                    plugin.soundConfig.playSound(player, soundId);
                    soundsPlayed.put(soundId, true);
                    Assertions.assertTrue(soundsPlayed.containsKey(soundId),
                            "Sound '" + soundId.name() + "' did not play for player." );
                }
            }

            @Nested
            @DisplayName("Play all sounds in SoundId at world location")
            class WorldSounds {

                private final EnumMap<SoundId, Boolean> soundsPlayed = new EnumMap<>(SoundId.class);

                @ParameterizedTest
                @EnumSource(SoundId.class)
                @DisplayName("play sound for location")
                void SoundConfigPlaySoundForPlayer(SoundId soundId) {
                    plugin.soundConfig.playSound(world.getSpawnLocation(), soundId);
                    soundsPlayed.put(soundId, true);
                    Assertions.assertTrue(soundsPlayed.containsKey(soundId),
                            "Sound '" + soundId.name() + "' did not play for location." );
                }
            }
        }
    }
}
