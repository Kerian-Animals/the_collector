package fr.kerian_animals.thecollector.registry;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.item.CollectorCatalystItem;
import fr.kerian_animals.thecollector.item.CollectorCompassItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TheCollectorMod.MOD_ID);

    public static final RegistryObject<Item> COLLECTOR_COMPASS = ITEMS.register(
            "collector_compass",
            () -> new CollectorCompassItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> NETHER_RELIC_FRAGMENT = ITEMS.register(
            "nether_relic_fragment",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> CAVERN_RELIC_FRAGMENT = ITEMS.register(
            "cavern_relic_fragment",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> ECHO_RELIC_FRAGMENT = ITEMS.register(
            "echo_relic_fragment",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> COLLECTOR_CATALYST = ITEMS.register(
            "collector_catalyst",
            () -> new CollectorCatalystItem(new Item.Properties().stacksTo(16))
    );

    private ModItems() {
    }
}

