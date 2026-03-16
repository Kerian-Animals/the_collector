package fr.kerian_animals.thecollector;

import com.mojang.logging.LogUtils;
import fr.kerian_animals.thecollector.config.TheCollectorConfig;
import fr.kerian_animals.thecollector.registry.ModCreativeTabs;
import fr.kerian_animals.thecollector.registry.ModEntities;
import fr.kerian_animals.thecollector.registry.ModItems;
import fr.kerian_animals.thecollector.spawn.CollectorSpawnHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(TheCollectorMod.MOD_ID)
public final class TheCollectorMod {
    public static final String MOD_ID = "the_collector";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheCollectorMod(IEventBus modEventBus, ModContainer modContainer) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, TheCollectorConfig.SPEC);
        NeoForge.EVENT_BUS.register(new CollectorSpawnHandler());
    }
}

