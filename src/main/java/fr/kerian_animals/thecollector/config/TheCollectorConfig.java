package fr.kerian_animals.thecollector.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class TheCollectorConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue MOD_ENABLED = BUILDER
            .comment("Master switch for the mod.")
            .define("enabled", true);

    public static final ForgeConfigSpec.DoubleValue SPAWN_CHANCE_PER_CHECK = BUILDER
            .comment("Spawn chance [0..1] on each server spawn check.")
            .defineInRange("spawnChancePerCheck", 0.04D, 0.0D, 1.0D);

    public static final ForgeConfigSpec.IntValue SPAWN_COOLDOWN_TICKS = BUILDER
            .comment("Minimal cooldown between two collector spawns.")
            .defineInRange("spawnCooldownTicks", 12_000, 200, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue SPAWN_MIN_DISTANCE = BUILDER
            .comment("Minimum spawn distance from selected player.")
            .defineInRange("spawnMinDistance", 24, 8, 256);

    public static final ForgeConfigSpec.IntValue SPAWN_MAX_DISTANCE = BUILDER
            .comment("Maximum spawn distance from selected player.")
            .defineInRange("spawnMaxDistance", 48, 8, 256);

    public static final ForgeConfigSpec.BooleanValue NIGHT_ONLY_SPAWN = BUILDER
            .comment("If true, collector can spawn only at night.")
            .define("nightOnlySpawn", true);

    public static final ForgeConfigSpec.IntValue ITEM_SEARCH_RADIUS = BUILDER
            .comment("Radius used to scan for valuable item entities.")
            .defineInRange("itemSearchRadius", 20, 6, 64);

    public static final ForgeConfigSpec.IntValue COLLECTOR_INVENTORY_SLOTS = BUILDER
            .comment("Internal collector inventory size.")
            .defineInRange("collectorInventorySlots", 9, 1, 54);

    public static final ForgeConfigSpec.IntValue MAX_STOLEN_STACKS = BUILDER
            .comment("Max stacks the collector can steal before fleeing.")
            .defineInRange("maxStolenStacks", 6, 1, 54);

    public static final ForgeConfigSpec.IntValue ESCAPE_DISTANCE = BUILDER
            .comment("Distance used by escape behavior.")
            .defineInRange("escapeDistance", 36, 12, 128);

    public static final ForgeConfigSpec.IntValue MAX_PRESENCE_TICKS = BUILDER
            .comment("Maximum lifetime before the collector forces escape.")
            .defineInRange("maxPresenceTicks", 8_000, 200, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.BooleanValue STASH_ENABLED = BUILDER
            .comment("If true, collector creates a stash chest on despawn.")
            .define("stashEnabled", true);

    public static final ForgeConfigSpec.BooleanValue BONUS_LOOT_ENABLED = BUILDER
            .comment("If true, stash can include a tiny bonus loot.")
            .define("bonusLootEnabled", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private TheCollectorConfig() {
    }
}

