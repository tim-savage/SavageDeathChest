package com.winterhavenmc.deathchest;

public enum ConfigSetting {

    LANGUAGE("en-US"),
    ENABLED_WORLDS("[]"),
    DISABLED_WORLDS("[disabled_world1, disabled_world2]"),
    CHEST_DEPLOYMENT_DELAY("2"),
    LOG_INVENTORY_ON_DEATH("false"),
    SOUND_EFFECTS("true"),
    EXPIRE_TIME("60"),
    CHEST_PROTECTION("true"),
    CHEST_PROTECTION_TIME("-1"),
    KILLER_LOOTING("false"),
    REQUIRE_CHEST("false"),
    PREVENT_ITEM_PLACEMENT("true"),
    CREATIVE_DEPLOY("false"),
    CREATIVE_ACCESS("false"),
    SEARCH_DISTANCE("10"),
    PLACE_ABOVE_VOID("true"),
    QUICK_LOOT("true"),
    LIST_PAGE_SIZE_PLAYER("5"),
    LIST_PAGE_SIZE_CONSOLE("10"),
    CHEST_SIGNS("true"),
    REPLACEABLE_BLOCKS("[AIR, CAVE_AIR, VOID_AIR, WATER, GRASS, TALL_GRASS, " +
            "SNOW, VINE, LILY_PAD, DEAD_BUSH, ALLIUM, AZURE_BLUET, BLUE_ORCHID, CORN_FLOWER, DANDELION, " +
            "LILAC, LILY_OF_THE_VALLEY, OXEYE_DAISY, PEONY, POPPY, ROSE_BUSH, SUNFLOWER, ORANGE_TULIP, " +
            "PINK_TULIP, RED_TULIP, WHITE_TULIP, BROWN_MUSHROOM, RED_MUSHROOM]");


    private final String value;

    ConfigSetting(String value) {
        this.value = value;
    }

    public String getKey() {
        return this.name().toLowerCase().replace('_', '-');
    }

    public String getValue() {
        return this.value;
    }

}
