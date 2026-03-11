package fr.kerian_animals.thecollector.registry;

import fr.kerian_animals.thecollector.TheCollectorMod;
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

    private ModItems() {
    }
}

