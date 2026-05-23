package fr.kerian_animals.thecollector.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Defines the shared server-side configuration for spawn rules, theft limits, and world features.
 */
public final class TheCollectorConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue MOD_ENABLED = BUILDER
            .comment("Master switch for the mod.")
            .define("enabled", true);

    public static final ModConfigSpec.DoubleValue SPAWN_CHANCE_PER_CHECK = BUILDER
            .comment("Spawn chance [0..1] on each server spawn check.")
            .defineInRange("spawnChancePerCheck", 0.04D, 0.0D, 1.0D);

    public static final ModConfigSpec.IntValue SPAWN_COOLDOWN_TICKS = BUILDER
            .comment("Minimal cooldown between two collector spawns.")
            .defineInRange("spawnCooldownTicks", 12_000, 200, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue SPAWN_MIN_DISTANCE = BUILDER
            .comment("Minimum spawn distance from selected player.")
            .defineInRange("spawnMinDistance", 24, 8, 256);

    public static final ModConfigSpec.IntValue SPAWN_MAX_DISTANCE = BUILDER
            .comment("Maximum spawn distance from selected player.")
            .defineInRange("spawnMaxDistance", 48, 8, 256);

    public static final ModConfigSpec.BooleanValue NIGHT_ONLY_SPAWN = BUILDER
            .comment("If true, collector can spawn only at night.")
            .define("nightOnlySpawn", true);

    public static final ModConfigSpec.IntValue ITEM_SEARCH_RADIUS = BUILDER
            .comment("Radius used to scan for valuable item entities.")
            .defineInRange("itemSearchRadius", 20, 6, 64);

    public static final ModConfigSpec.BooleanValue CHEST_THEFT_ENABLED = BUILDER
            .comment("If true, collector can steal from nearby chests.")
            .define("chestTheftEnabled", true);

    public static final ModConfigSpec.IntValue CHEST_SEARCH_RADIUS = BUILDER
            .comment("Radius used to scan for chest blocks.")
            .defineInRange("chestSearchRadius", 12, 4, 32);

    public static final ModConfigSpec.IntValue MAX_STEALS_PER_CHEST = BUILDER
            .comment("How many stacks can be stolen from one chest before moving on.")
            .defineInRange("maxStealsPerChest", 2, 1, 9);

    public static final ModConfigSpec.IntValue COLLECTOR_INVENTORY_SLOTS = BUILDER
            .comment("Internal collector inventory size.")
            .defineInRange("collectorInventorySlots", 9, 1, 54);

    public static final ModConfigSpec.IntValue MAX_STOLEN_STACKS = BUILDER
            .comment("Max stacks the collector can steal before fleeing.")
            .defineInRange("maxStolenStacks", 6, 1, 54);

    public static final ModConfigSpec.IntValue ESCAPE_DISTANCE = BUILDER
            .comment("Distance used by escape behavior.")
            .defineInRange("escapeDistance", 36, 12, 128);

    public static final ModConfigSpec.IntValue MAX_PRESENCE_TICKS = BUILDER
            .comment("Maximum lifetime before the collector forces escape.")
            .defineInRange("maxPresenceTicks", 8_000, 200, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue STASH_ENABLED = BUILDER
            .comment("If true, collector creates a stash chest on despawn.")
            .define("stashEnabled", true);

    public static final ModConfigSpec.BooleanValue BONUS_LOOT_ENABLED = BUILDER
            .comment("If true, stash can include a tiny bonus loot.")
            .define("bonusLootEnabled", true);

    public static final ModConfigSpec.BooleanValue MINI_CACHE_ENABLED = BUILDER
            .comment("If true, small Collector mini-caches can generate in the Overworld.")
            .define("miniCacheEnabled", true);

    public static final ModConfigSpec.IntValue MINI_CACHE_CHECK_INTERVAL_TICKS = BUILDER
            .comment("How often mini-cache generation is checked.")
            .defineInRange("miniCacheCheckIntervalTicks", 1_200, 200, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MINI_CACHE_MIN_DISTANCE = BUILDER
            .comment("Minimum distance from a player when placing a mini-cache.")
            .defineInRange("miniCacheMinDistance", 48, 16, 512);

    public static final ModConfigSpec.IntValue MINI_CACHE_MAX_DISTANCE = BUILDER
            .comment("Maximum distance from a player when placing a mini-cache.")
            .defineInRange("miniCacheMaxDistance", 128, 16, 512);

    public static final ModConfigSpec.IntValue MINI_CACHE_MIN_SPACING = BUILDER
            .comment("Minimum spacing between two mini-caches.")
            .defineInRange("miniCacheMinSpacing", 96, 16, 1024);

    public static final ModConfigSpec.IntValue MINI_CACHE_PLAYER_RADIUS = BUILDER
            .comment("Radius around a player used to count nearby mini-caches.")
            .defineInRange("miniCachePlayerRadius", 160, 32, 1024);

    public static final ModConfigSpec.IntValue MINI_CACHE_MAX_PER_AREA = BUILDER
            .comment("Maximum number of mini-caches allowed around a player area.")
            .defineInRange("miniCacheMaxPerArea", 2, 1, 32);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private TheCollectorConfig() {
    }
}

