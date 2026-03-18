package fr.kerian_animals.thecollector.client;

import fr.kerian_animals.thecollector.TheCollectorMod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import fr.kerian_animals.thecollector.registry.ModMenus;
import fr.kerian_animals.thecollector.registry.ModEntities;
import fr.kerian_animals.thecollector.registry.ModBlocks;
import fr.kerian_animals.thecollector.registry.ModItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.LodestoneTracker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TheCollectorMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.COLLECTOR.get(), CollectorRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ALEMBIC.get(), AlembicScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ALEMBIC.get(), RenderType.cutout());

            CompassItemPropertyFunction vanillaFunction = new CompassItemPropertyFunction((level, stack, entity) -> {
                LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
                return tracker != null ? tracker.target().orElse(null) : null;
            });

            ItemProperties.register(
                    ModItems.COLLECTOR_COMPASS.get(),
                    ResourceLocation.withDefaultNamespace("angle"),
                    (stack, level, entity, seed) -> {
                        if (level != null && entity != null && isCloseToTarget(level, stack, entity, 10.0D)) {
                            long time = level.getGameTime();
                            float spin = (float) Mth.positiveModulo((time * 0.35D) + ((seed * 0.1327D) % 1.0D), 1.0D);
                            return spin;
                        }
                        return vanillaFunction.unclampedCall(stack, level, entity, seed);
                    }
            );
        });
    }

    private static boolean isCloseToTarget(ClientLevel level, net.minecraft.world.item.ItemStack stack, Entity entity, double radius) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null) {
            return false;
        }
        GlobalPos target = tracker.target().orElse(null);
        if (target == null || target.dimension() != level.dimension()) {
            return false;
        }
        double dx = target.pos().getX() + 0.5D - entity.getX();
        double dz = target.pos().getZ() + 0.5D - entity.getZ();
        return dx * dx + dz * dz <= radius * radius;
    }
}

