package fr.kerian_animals.thecollector.advancement;

import fr.kerian_animals.thecollector.TheCollectorMod;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class CollectorAdvancementHelper {
    private CollectorAdvancementHelper() {
    }

    public static void award(ServerPlayer player, String path) {
        AdvancementHolder advancement = player.server.getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(TheCollectorMod.MOD_ID, path));
        if (advancement == null) {
            return;
        }
        if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            player.getAdvancements().award(advancement, "trigger");
        }
    }

    public static void awardNearby(ServerLevel level, net.minecraft.core.BlockPos center, double radius, String path) {
        double radiusSq = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D) <= radiusSq) {
                award(player, path);
            }
        }
    }
}
