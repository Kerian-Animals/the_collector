package fr.kerian_animals.thecollector.world.dimension;

import fr.kerian_animals.thecollector.TheCollectorMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public final class ModDimensions {
    public static final ResourceKey<Level> COLLECTOR_REALM = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(TheCollectorMod.MOD_ID, "collector_realm")
    );

    public static final ResourceKey<DimensionType> COLLECTOR_REALM_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(TheCollectorMod.MOD_ID, "collector_realm_type")
    );

    private ModDimensions() {
    }
}
