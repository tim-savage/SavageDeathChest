package com.winterhaven_mc.deathchest.messages;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.winterhaven_mc.deathchest.PluginMain;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LanguageHandlerTests {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
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
    @DisplayName("test language handler")
    class LanguageHandler {

        @Test
        @DisplayName("language handler is not null")
        void LanguageHandlerNotNull() {
            Assertions.assertNotNull(plugin.languageHandler);
        }

        @Test
        @DisplayName("item name is not null")
        void ItemNameNotNull() {
            Assertions.assertNotNull(plugin.languageHandler.getItemName());
        }

        @Test
        @DisplayName("item lore is not null")
        void ItemLoreNotNull() {
            Assertions.assertNotNull(plugin.languageHandler.getItemLore());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Test messages:")
    class Messages {

        // collection of enum sound name strings
        Collection<String> enumMessageNames = new HashSet<>();

        // class constructor
        Messages() {
            // add all MessageId enum values to collection
            for (MessageId MessageId : MessageId.values()) {
                enumMessageNames.add(MessageId.name());
            }
        }

        @ParameterizedTest
        @EnumSource(MessageId.class)
        @DisplayName("enum member MessageId is contained in getConfig() keys.")
        void FileKeysContainsEnumValue(MessageId messageId) {
            Assertions.assertNotNull(messageId, "messageId is null");
            Assertions.assertNotNull(plugin.languageHandler.getMessage(messageId),
                    "language handler returned null message for " + messageId.name());
        }


//        @SuppressWarnings("unused")
//        Set<String> ConfigFileKeys() {
//            return plugin.languageHandler.getMessageKeys();
//        }

//        @ParameterizedTest
//        @DisplayName("config file key has matching key in MessageId enum")
//        @MethodSource("ConfigFileKeys")
//        void EnumContainsAllFileKeys(String key) {
//            Assertions.assertNotNull(key);
//            Assertions.assertTrue(enumMessageNames.contains(key));
//            System.out.println("File key '" + key + "' has matching SoundId enum value");
//        }

    }

}
