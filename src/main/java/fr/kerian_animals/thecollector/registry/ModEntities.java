package fr.kerian_animals.thecollector.registry;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.entity.CollectorEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TheCollectorMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<CollectorEntity>> COLLECTOR = ENTITY_TYPES.register(
            "collector",
            () -> EntityType.Builder.of(CollectorEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.9F)
                    .build("collector")
    );

    private ModEntities() {
    }

    @EventBusSubscriber(modid = TheCollectorMod.MOD_ID)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void onEntityAttributes(EntityAttributeCreationEvent event) {
            event.put(COLLECTOR.get(), CollectorEntity.createAttributes().build());
        }
    }
}

