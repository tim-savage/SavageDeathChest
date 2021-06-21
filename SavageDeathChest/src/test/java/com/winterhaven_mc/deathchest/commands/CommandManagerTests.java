package com.winterhaven_mc.deathchest.commands;

import com.winterhaven_mc.deathchest.PluginMain;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import org.junit.jupiter.api.*;


@SuppressWarnings({"FieldCanBeLocal", "unused"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommandManagerTests {
    private ServerMock server;
    private PlayerMock player;
    private PluginMain plugin;

    @BeforeAll
    public void setUp() {
        // Start the mock server
        server = MockBukkit.mock();

        player = server.addPlayer("testy");

        // start the mock plugin
        plugin = MockBukkit.load(PluginMain.class);

    }

    @AfterAll
    public void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Test Help Command.")
    void HelpCommand() {
        server.dispatchCommand(server.addPlayer(), "/deathchest help");
    }

    @Test
    @DisplayName("Test List Command.")
    void ListCommand() {
        server.dispatchCommand(server.addPlayer(), "/deathchest list");
    }

    @Test
    @DisplayName("Test Reload Command.")
    void ReloadCommand() {
        server.dispatchCommand(server.addPlayer(), "/deathchest reload");
    }

    @Test
    @DisplayName("Test Status Command.")
    void StatusCommand() {
        server.dispatchCommand(server.addPlayer(), "/deathchest status");
    }
}
