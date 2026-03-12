package fr.kerian_animals.thecollector.registry;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.entity.CollectorEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TheCollectorMod.MOD_ID);

    public static final RegistryObject<EntityType<CollectorEntity>> COLLECTOR = ENTITY_TYPES.register(
            "collector",
            () -> EntityType.Builder.of(CollectorEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.9F)
                    .build("collector")
    );

    private ModEntities() {
    }

    @Mod.EventBusSubscriber(modid = TheCollectorMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void onEntityAttributes(EntityAttributeCreationEvent event) {
            event.put(COLLECTOR.get(), CollectorEntity.createAttributes().build());
        }
    }
}

