package com.winterhaven_mc.deathchest.messages;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.winterhaven_mc.deathchest.PluginMain;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Collection;
import java.util.HashSet;


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
    @DisplayName("test message builder")
    class MessageBuilderTests {

        @Test
        @DisplayName("message builder is not null")
        void LanguageHandlerNotNull() {
            Assertions.assertNotNull(plugin.messageBuilder);
        }

        @Test
        @DisplayName("item name is not null")
        void ItemNameNotNull() {
            Assertions.assertNotNull(plugin.messageBuilder.getItemName());
        }

        @Test
        @DisplayName("item lore is not null")
        void ItemLoreNotNull() {
            Assertions.assertNotNull(plugin.messageBuilder.getItemLore());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Test messages:")
    class MessageTests {

        // collection of enum sound name strings
        Collection<String> enumMessageNames = new HashSet<>();

        // class constructor
        MessageTests() {
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
            Assertions.assertNotNull(plugin.messageBuilder.getMessage(messageId),
                    "language handler returned null message for " + messageId.name());
        }


//        @SuppressWarnings("unused")
//        Set<String> ConfigFileKeys() {
//            return plugin.messageBuilder.getMessageKeys();
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
