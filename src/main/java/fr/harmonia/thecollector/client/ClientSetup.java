package fr.harmonia.thecollector.client;

import fr.harmonia.thecollector.TheCollectorMod;
import fr.harmonia.thecollector.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = TheCollectorMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    private ClientSetup() {
    }

    public static void registerEntityRenderers() {
        net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntities.COLLECTOR.get(), CollectorRenderer::new);
    }
}
