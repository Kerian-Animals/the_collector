package fr.kerian_animals.thecollector.advancement;

import fr.kerian_animals.thecollector.TheCollectorMod;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class CollectorAdvancementHelper {
    private static final String TAG_ADVANCEMENTS = "the_collector_advancements";

    private CollectorAdvancementHelper() {
    }

    public static boolean isAwarded(ServerPlayer player, String path) {
        CompoundTag cache = player.getPersistentData().getCompound(TAG_ADVANCEMENTS);
        if (cache.contains(path)) {
            return cache.getBoolean(path);
        }

        AdvancementHolder advancement = player.server.getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(TheCollectorMod.MOD_ID, path));
        boolean awarded = advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
        cache.putBoolean(path, awarded);
        player.getPersistentData().put(TAG_ADVANCEMENTS, cache);
        return awarded;
    }

    public static void award(ServerPlayer player, String path) {
        if (isAwarded(player, path)) {
            return;
        }

        AdvancementHolder advancement = player.server.getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(TheCollectorMod.MOD_ID, path));
        if (advancement == null) {
            return;
        }
        if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            player.getAdvancements().award(advancement, "trigger");
        }
        setCached(player, path, true);
    }

    public static void awardNearby(ServerLevel level, net.minecraft.core.BlockPos center, double radius, String path) {
        double radiusSq = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D) <= radiusSq) {
                award(player, path);
            }
        }
    }

    private static void setCached(ServerPlayer player, String path, boolean value) {
        CompoundTag cache = player.getPersistentData().getCompound(TAG_ADVANCEMENTS);
        cache.putBoolean(path, value);
        player.getPersistentData().put(TAG_ADVANCEMENTS, cache);
    }
}
