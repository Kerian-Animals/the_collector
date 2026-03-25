package fr.kerian_animals.thecollector.stash;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistent world data for Collector-related progression structures.
 *
 * <p>The mod stores this data in the Overworld data storage even when the referenced content lives
 * in another dimension. That keeps stashes, entries, mini-caches, and per-player stash pointers
 * under a single source of truth.</p>
 */
public class CollectorSavedData extends SavedData {
    public static final String DATA_NAME = "the_collector_stashes";

    private final Map<UUID, CollectorStash> stashes = new HashMap<>();
    private final Map<UUID, UUID> playerLastStash = new HashMap<>();
    private final Map<UUID, CollectorEntry> entries = new HashMap<>();
    private final Map<UUID, CollectorMiniCache> miniCaches = new HashMap<>();

    /**
     * Returns the shared Collector saved data instance anchored in the Overworld.
     */
    public static CollectorSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(CollectorSavedData::new, CollectorSavedData::load, null),
                DATA_NAME
        );
    }

    /**
     * Deserializes all Collector-related persistent state from disk.
     */
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

        ListTag entryList = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < entryList.size(); i++) {
            CollectorEntry entry = CollectorEntry.load(entryList.getCompound(i));
            data.entries.put(entry.id(), entry);
        }

        ListTag miniCacheList = tag.getList("MiniCaches", Tag.TAG_COMPOUND);
        for (int i = 0; i < miniCacheList.size(); i++) {
            CollectorMiniCache miniCache = CollectorMiniCache.load(miniCacheList.getCompound(i));
            data.miniCaches.put(miniCache.id(), miniCache);
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

        ListTag entriesTag = new ListTag();
        for (CollectorEntry entry : this.entries.values()) {
            entriesTag.add(entry.save());
        }
        tag.put("Entries", entriesTag);

        ListTag miniCachesTag = new ListTag();
        for (CollectorMiniCache miniCache : this.miniCaches.values()) {
            miniCachesTag.add(miniCache.save());
        }
        tag.put("MiniCaches", miniCachesTag);
        return tag;
    }

    /**
     * Records a newly created stash and marks the saved data dirty.
     */
    public void addStash(CollectorStash stash) {
        this.stashes.put(stash.id(), stash);
        setDirty();
    }

    /**
     * Updates the last stash pointer associated with a player.
     */
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

    public Optional<CollectorStash> getLatestStash() {
        return this.stashes.values().stream()
                .max(Comparator.comparingLong(CollectorStash::createdTick));
    }

    public void addEntry(CollectorEntry entry) {
        this.entries.put(entry.id(), entry);
        setDirty();
    }

    public Collection<CollectorEntry> getAllEntries() {
        return this.entries.values();
    }

    public Optional<CollectorEntry> getLatestEntry() {
        return this.entries.values().stream().max(Comparator.comparingLong(CollectorEntry::createdTick));
    }

    public Optional<CollectorEntry> getNearestEntry(BlockPos from) {
        return this.entries.values().stream()
                .min(Comparator.comparingDouble(entry -> entry.pos().distSqr(from)));
    }

    public Optional<CollectorEntry> getEntryAt(BlockPos pos, int maxDistance) {
        int max = Math.max(0, maxDistance);
        return this.entries.values().stream()
                .filter(entry -> entry.pos().distManhattan(pos) <= max)
                .min(Comparator.comparingInt(entry -> entry.pos().distManhattan(pos)));
    }

    public boolean setEntryActivated(UUID entryId, boolean activated) {
        CollectorEntry current = this.entries.get(entryId);
        if (current == null || current.activated() == activated) {
            return false;
        }
        this.entries.put(entryId, current.withActivated(activated));
        setDirty();
        return true;
    }

    public void addMiniCache(CollectorMiniCache miniCache) {
        this.miniCaches.put(miniCache.id(), miniCache);
        setDirty();
    }

    public Collection<CollectorMiniCache> getAllMiniCaches() {
        return this.miniCaches.values();
    }
}

