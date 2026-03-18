package fr.kerian_animals.thecollector;

import com.mojang.logging.LogUtils;
import fr.kerian_animals.thecollector.advancement.CollectorAdvancementManager;
import fr.kerian_animals.thecollector.config.TheCollectorConfig;
import fr.kerian_animals.thecollector.item.CollectorTraceInteractionHandler;
import fr.kerian_animals.thecollector.registry.ModBlockEntities;
import fr.kerian_animals.thecollector.registry.ModCreativeTabs;
import fr.kerian_animals.thecollector.registry.ModBlocks;
import fr.kerian_animals.thecollector.registry.ModEntities;
import fr.kerian_animals.thecollector.registry.ModItems;
import fr.kerian_animals.thecollector.registry.ModMenus;
import fr.kerian_animals.thecollector.spawn.CollectorSpawnHandler;
import fr.kerian_animals.thecollector.world.CollectorMiniCacheManager;
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
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, TheCollectorConfig.SPEC);
        NeoForge.EVENT_BUS.register(new CollectorAdvancementManager());
        NeoForge.EVENT_BUS.register(new CollectorSpawnHandler());
        NeoForge.EVENT_BUS.register(new CollectorMiniCacheManager());
        NeoForge.EVENT_BUS.register(new CollectorTraceInteractionHandler());
    }
}

