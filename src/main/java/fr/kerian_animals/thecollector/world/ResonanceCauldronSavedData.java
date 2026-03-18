package fr.kerian_animals.thecollector.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public final class ResonanceCauldronSavedData extends SavedData {
    private static final String DATA_NAME = "the_collector_resonance_cauldron";
    private static final int CRYSTALLIZATION_TICKS = 20 * 60 * 3;

    private final Map<String, Long> restingCauldrons = new HashMap<>();

    public static ResonanceCauldronSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ResonanceCauldronSavedData::new, ResonanceCauldronSavedData::load, null),
                DATA_NAME
        );
    }

    public static ResonanceCauldronSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ResonanceCauldronSavedData data = new ResonanceCauldronSavedData();
        ListTag cauldrons = tag.getList("RestingCauldrons", Tag.TAG_COMPOUND);
        for (int i = 0; i < cauldrons.size(); i++) {
            CompoundTag entry = cauldrons.getCompound(i);
            data.restingCauldrons.put(entry.getString("Key"), entry.getLong("StartedAt"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag cauldrons = new ListTag();
        for (Map.Entry<String, Long> entry : restingCauldrons.entrySet()) {
            CompoundTag cauldronTag = new CompoundTag();
            cauldronTag.putString("Key", entry.getKey());
            cauldronTag.putLong("StartedAt", entry.getValue());
            cauldrons.add(cauldronTag);
        }
        tag.put("RestingCauldrons", cauldrons);
        return tag;
    }

    public boolean startResting(ServerLevel level, BlockPos pos) {
        String key = key(level, pos);
        if (restingCauldrons.containsKey(key)) {
            return false;
        }
        restingCauldrons.put(key, serverTime(level));
        setDirty();
        return true;
    }

    public boolean isResting(ServerLevel level, BlockPos pos) {
        return restingCauldrons.containsKey(key(level, pos));
    }

    public boolean isReady(ServerLevel level, BlockPos pos) {
        Long startedAt = restingCauldrons.get(key(level, pos));
        return startedAt != null && serverTime(level) - startedAt >= CRYSTALLIZATION_TICKS;
    }

    public int remainingTicks(ServerLevel level, BlockPos pos) {
        Long startedAt = restingCauldrons.get(key(level, pos));
        if (startedAt == null) {
            return 0;
        }
        long elapsed = serverTime(level) - startedAt;
        return (int) Math.max(0L, CRYSTALLIZATION_TICKS - elapsed);
    }

    public void clear(ServerLevel level, BlockPos pos) {
        if (restingCauldrons.remove(key(level, pos)) != null) {
            setDirty();
        }
    }

    private static long serverTime(ServerLevel level) {
        return level.getServer().overworld().getGameTime();
    }

    private static String key(ServerLevel level, BlockPos pos) {
        return level.dimension().location() + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
    }
}
