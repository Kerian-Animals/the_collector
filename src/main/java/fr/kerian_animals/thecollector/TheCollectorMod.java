package fr.kerian_animals.thecollector;

import com.mojang.logging.LogUtils;
import fr.kerian_animals.thecollector.client.ClientSetup;
import fr.kerian_animals.thecollector.config.TheCollectorConfig;
import fr.kerian_animals.thecollector.registry.ModEntities;
import fr.kerian_animals.thecollector.registry.ModItems;
import fr.kerian_animals.thecollector.spawn.CollectorSpawnHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TheCollectorMod.MOD_ID)
public final class TheCollectorMod {
    public static final String MOD_ID = "the_collector";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheCollectorMod(FMLJavaModLoadingContext context) {
        ModEntities.ENTITY_TYPES.register(context.getModEventBus());
        ModItems.ITEMS.register(context.getModEventBus());

        context.getModEventBus().addListener(this::onClientSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TheCollectorConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(new CollectorSpawnHandler());
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        ClientSetup.registerEntityRenderers();
    }
}
