package fr.harmonia.thecollector.stash;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CollectorSavedData extends SavedData {
    public static final String DATA_NAME = "the_collector_stashes";

    private final Map<UUID, CollectorStash> stashes = new HashMap<>();
    private final Map<UUID, UUID> playerLastStash = new HashMap<>();

    public static CollectorSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(CollectorSavedData::new, CollectorSavedData::load),
                DATA_NAME
        );
    }

    public static CollectorSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        CollectorSavedData data = new CollectorSavedData();

        ListTag stashesList = tag.getList("Stashes", Tag.TAG_COMPOUND);
        for (int i = 0; i < stashesList.size(); i++) {
            CollectorStash stash = CollectorStash.load(stashesList.getCompound(i), registries);
            data.stashes.put(stash.id(), stash);
        }

        ListTag playerMap = tag.getList("PlayerMap", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerMap.size(); i++) {
            CompoundTag mapEntry = playerMap.getCompound(i);
            data.playerLastStash.put(mapEntry.getUUID("Player"), mapEntry.getUUID("Stash"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag stashesList = new ListTag();
        for (CollectorStash stash : stashes.values()) {
            stashesList.add(stash.save(registries));
        }
        tag.put("Stashes", stashesList);

        ListTag playerMap = new ListTag();
        for (Map.Entry<UUID, UUID> entry : playerLastStash.entrySet()) {
            CompoundTag mapEntry = new CompoundTag();
            mapEntry.putUUID("Player", entry.getKey());
            mapEntry.putUUID("Stash", entry.getValue());
            playerMap.add(mapEntry);
        }
        tag.put("PlayerMap", playerMap);
        return tag;
    }

    public void addStash(CollectorStash stash) {
        this.stashes.put(stash.id(), stash);
        setDirty();
    }

    public void setLastStashForPlayer(UUID playerId, UUID stashId) {
        this.playerLastStash.put(playerId, stashId);
        setDirty();
    }

    public Optional<CollectorStash> getLastStashForPlayer(UUID playerId) {
        UUID stashId = this.playerLastStash.get(playerId);
        if (stashId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.stashes.get(stashId));
    }

    public Collection<CollectorStash> getAllStashes() {
        return this.stashes.values();
    }
}
