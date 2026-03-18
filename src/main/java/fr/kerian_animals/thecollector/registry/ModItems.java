package fr.kerian_animals.thecollector.registry;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.item.CollectorCatalystItem;
import fr.kerian_animals.thecollector.item.CollectorCompassItem;
import fr.kerian_animals.thecollector.item.HintBlockItem;
import fr.kerian_animals.thecollector.item.HintItem;
import fr.kerian_animals.thecollector.item.ResonanceItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.createItems(TheCollectorMod.MOD_ID);

    public static final DeferredHolder<Item, Item> COLLECTOR_COMPASS = ITEMS.register(
            "collector_compass",
            () -> new CollectorCompassItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredHolder<Item, Item> NETHER_RELIC_FRAGMENT = ITEMS.register(
            "nether_relic_fragment",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> CAVERN_RELIC_FRAGMENT = ITEMS.register(
            "cavern_relic_fragment",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> ECHO_RELIC_FRAGMENT = ITEMS.register(
            "echo_relic_fragment",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> COLLECTOR_CATALYST = ITEMS.register(
            "collector_catalyst",
            () -> new CollectorCatalystItem(new Item.Properties().stacksTo(16))
    );

    public static final DeferredHolder<Item, Item> COLLECTOR_TRACE = ITEMS.register(
            "collector_trace",
            () -> new BlockItem(ModBlocks.COLLECTOR_TRACE.get(), new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> ALEMBIC = ITEMS.register(
            "alembic",
            () -> new HintBlockItem(
                    ModBlocks.ALEMBIC.get(),
                    new Item.Properties(),
                    "item.the_collector.alembic.lore",
                    "item.the_collector.alembic.hint"
            )
    );

    public static final DeferredHolder<Item, Item> UNSTABLE_RESONANCE = ITEMS.register(
            "unstable_resonance",
            () -> new ResonanceItem(
                    new Item.Properties().stacksTo(1),
                    "item.the_collector.unstable_resonance.lore",
                    "item.the_collector.unstable_resonance.hint",
                    true
            )
    );

    public static final DeferredHolder<Item, Item> RESONANT_RESIDUE = ITEMS.register(
            "resonant_residue",
            () -> new ResonanceItem(
                    new Item.Properties().stacksTo(16),
                    "item.the_collector.resonant_residue.lore",
                    "item.the_collector.resonant_residue.hint",
                    true
            )
    );

    public static final DeferredHolder<Item, Item> DISTILLED_RESONANCE = ITEMS.register(
            "distilled_resonance",
            () -> new ResonanceItem(
                    new Item.Properties().stacksTo(1),
                    "item.the_collector.distilled_resonance.lore",
                    "item.the_collector.distilled_resonance.hint",
                    true
            )
    );

    public static final DeferredHolder<Item, Item> ATTUNED_RESONANCE = ITEMS.register(
            "attuned_resonance",
            () -> new ResonanceItem(
                    new Item.Properties().stacksTo(16),
                    "item.the_collector.attuned_resonance.lore",
                    "item.the_collector.attuned_resonance.hint",
                    true
            )
    );

    public static final DeferredHolder<Item, Item> RESONANCE_CRYSTAL = ITEMS.register(
            "resonance_crystal",
            () -> new ResonanceItem(
                    new Item.Properties(),
                    "item.the_collector.resonance_crystal.lore",
                    "item.the_collector.resonance_crystal.hint",
                    false
            )
    );

    public static final DeferredHolder<Item, Item> RESONANCE_LADLE = ITEMS.register(
            "resonance_ladle",
            () -> new HintItem(
                    new Item.Properties().durability(64).stacksTo(1),
                    "item.the_collector.resonance_ladle.lore",
                    "item.the_collector.resonance_ladle.hint"
            )
    );

    private ModItems() {
    }
}

