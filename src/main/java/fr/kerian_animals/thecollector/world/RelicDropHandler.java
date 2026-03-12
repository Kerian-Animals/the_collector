package fr.kerian_animals.thecollector.world;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.registry.ModItems;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TheCollectorMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RelicDropHandler {
    private RelicDropHandler() {
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return;
        }

        if (event.getEntity().getType() == EntityType.BLAZE && level.dimension() == Level.NETHER) {
            tryDrop(event, new ItemStack(ModItems.NETHER_RELIC_FRAGMENT.get()), 0.07D);
        }

        if (event.getEntity() instanceof CaveSpider && event.getEntity().getY() < 50) {
            tryDrop(event, new ItemStack(ModItems.CAVERN_RELIC_FRAGMENT.get()), 0.06D);
        } else if (event.getEntity() instanceof Spider && event.getEntity().getY() < 30) {
            tryDrop(event, new ItemStack(ModItems.CAVERN_RELIC_FRAGMENT.get()), 0.025D);
        }

        if (event.getEntity().getType() == EntityType.ENDERMAN && level.dimension() == Level.OVERWORLD && event.getEntity().getY() < 20) {
            tryDrop(event, new ItemStack(ModItems.ECHO_RELIC_FRAGMENT.get()), 0.05D);
        }
    }

    private static void tryDrop(LivingDropsEvent event, ItemStack stack, double chance) {
        if (event.getEntity().getRandom().nextDouble() > chance) {
            return;
        }
        ItemEntity itemEntity = new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY() + 0.2D,
                event.getEntity().getZ(),
                stack
        );
        event.getDrops().add(itemEntity);
    }
}
