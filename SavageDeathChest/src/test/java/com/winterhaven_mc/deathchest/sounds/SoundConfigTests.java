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

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("Test Sounds config.")
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

        @Test
        @DisplayName("Sounds config is not null.")
        void SoundConfigNotNull() {
            Assertions.assertNotNull(plugin.soundConfig);
        }

        @SuppressWarnings("unused")
        Collection<String> GetConfigFileKeys() {
            return plugin.soundConfig.getSoundConfigKeys();
        }

        @ParameterizedTest
        @EnumSource(SoundId.class)
        @DisplayName("enum member soundId is contained in getConfig() keys.")
        void FileKeysContainsEnumValue(SoundId soundId) {
            Assertions.assertTrue(plugin.soundConfig.isValidSoundConfigKey(soundId.name()));
            System.out.println("Enum value '" + soundId.name() + "' has matching config key in sounds.yml");
        }

        @ParameterizedTest
        @MethodSource("GetConfigFileKeys")
        @DisplayName("config file key has matching key in enum sound names")
        void SoundConfigEnumContainsAllFileSounds(String key) {
            Assertions.assertTrue(enumSoundNames.contains(key));
            System.out.println("File key '" + key + "' has matching SoundId enum value");
        }

        @ParameterizedTest
        @MethodSource("GetConfigFileKeys")
        @DisplayName("sound file key has valid bukkit sound name")
        void SoundConfigFileHasValidBukkitSound(String key) {
            String bukkitSoundName = plugin.soundConfig.getBukkitSoundName(key);
            Assertions.assertTrue(plugin.soundConfig.isValidBukkitSoundName(bukkitSoundName));
            System.out.println("File key '" + key + "' has valid bukkit sound name: " + bukkitSoundName);
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
                void SoundConfigPlaySoundForPlayer(SoundId SoundId) {
                    plugin.soundConfig.playSound(player, SoundId);
                    soundsPlayed.put(SoundId, true);
                    Assertions.assertTrue(soundsPlayed.containsKey(SoundId));
                }
            }

            @Nested
            @DisplayName("Play all sounds in SoundId at world location")
            class WorldSounds {

                private final EnumMap<SoundId, Boolean> soundsPlayed = new EnumMap<>(SoundId.class);

                @ParameterizedTest
                @EnumSource(SoundId.class)
                @DisplayName("play sound for location")
                void SoundConfigPlaySoundForPlayer(SoundId SoundId) {
                    plugin.soundConfig.playSound(world.getSpawnLocation(), SoundId);
                    soundsPlayed.put(SoundId, true);
                    Assertions.assertTrue(soundsPlayed.containsKey(SoundId));
                }
            }
        }
    }
}
