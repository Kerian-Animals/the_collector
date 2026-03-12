package fr.kerian_animals.thecollector;

import com.mojang.logging.LogUtils;
import fr.kerian_animals.thecollector.config.TheCollectorConfig;
import fr.kerian_animals.thecollector.registry.ModEntities;
import fr.kerian_animals.thecollector.registry.ModItems;
import fr.kerian_animals.thecollector.spawn.CollectorSpawnHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TheCollectorMod.MOD_ID)
public final class TheCollectorMod {
    public static final String MOD_ID = "the_collector";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheCollectorMod(FMLJavaModLoadingContext context) {
        ModEntities.ENTITY_TYPES.register(context.getModEventBus());
        ModItems.ITEMS.register(context.getModEventBus());
        context.getModEventBus().addListener(this::onBuildTab);

        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, TheCollectorConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(new CollectorSpawnHandler());
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

