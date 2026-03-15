package fr.kerian_animals.thecollector;

import com.mojang.logging.LogUtils;
import fr.kerian_animals.thecollector.config.TheCollectorConfig;
import fr.kerian_animals.thecollector.registry.ModEntities;
import fr.kerian_animals.thecollector.registry.ModItems;
import fr.kerian_animals.thecollector.spawn.CollectorSpawnHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(TheCollectorMod.MOD_ID)
public final class TheCollectorMod {
    public static final String MOD_ID = "the_collector";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheCollectorMod(IEventBus modEventBus, ModContainer modContainer) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(this::onBuildTab);

        modContainer.registerConfig(ModConfig.Type.COMMON, TheCollectorConfig.SPEC);
        NeoForge.EVENT_BUS.register(new CollectorSpawnHandler());
    }

    private void onBuildTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.COLLECTOR_COMPASS.get());
            event.accept(ModItems.COLLECTOR_CATALYST.get());
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.NETHER_RELIC_FRAGMENT.get());
            event.accept(ModItems.CAVERN_RELIC_FRAGMENT.get());
            event.accept(ModItems.ECHO_RELIC_FRAGMENT.get());
        }
    }
}

