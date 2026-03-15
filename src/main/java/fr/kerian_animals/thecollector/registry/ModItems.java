package fr.kerian_animals.thecollector.registry;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.item.CollectorCatalystItem;
import fr.kerian_animals.thecollector.item.CollectorCompassItem;
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

    private ModItems() {
    }
}

