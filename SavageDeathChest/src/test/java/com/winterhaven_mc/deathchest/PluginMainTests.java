package com.winterhaven_mc.deathchest;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.junit.jupiter.api.*;


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

    @Nested
    class PluginMainObjects {

        @Test
        @DisplayName("plugin config is not null.")
        void ConfigNotNull() {
            Assertions.assertNotNull(plugin.getConfig(), "plugin config is null.");
        }

        @Test
        @DisplayName("language handler is not null.")
        void LanguageHandlerNotNull() {
            Assertions.assertNotNull(plugin.languageHandler, "language handler is null.");
        }

        @Test
        @DisplayName("world manager is not null.")
        void WorldManagerNotNull() {
            Assertions.assertNotNull(plugin.worldManager, "world manager is null.");
        }

        @Test
        @DisplayName("sound config is not null.")
        void SoundConfigNotNull() {
            Assertions.assertNotNull(plugin.soundConfig, "sound config is null.");
        }

        @Test
        @DisplayName("command manager is not null.")
        void CommandManagerNotNul() {
            Assertions.assertNotNull(plugin.commandManager, "command manager is null.");
        }

        @Test
        @DisplayName("player event listener is not null.")
        void PlayerEventListenerNotNull() {
            Assertions.assertNotNull(plugin.playerEventListener,"player event listener is null.");
        }

        @Test
        @DisplayName("block event listener is not null.")
        void BlockEventListenerNotNull() {
            Assertions.assertNotNull(plugin.blockEventListener,"block event listener is null.");
        }

        @Test
        @DisplayName("inventory event listener is not null.")
        void InventoryEventListenerNotNull() {
            Assertions.assertNotNull(plugin.inventoryEventListener,"inventory event listener is null.");
        }
    }


    @Test
    @DisplayName("data folder is not null.")
    void MockPluginDataFolderNotNull() {
        Assertions.assertNotNull(plugin.getDataFolder());
    }
}
