package fr.kerian_animals.thecollector.stash;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record CollectorEntry(UUID id, BlockPos pos, long createdTick, boolean activated) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putLong("Pos", pos.asLong());
        tag.putLong("CreatedTick", createdTick);
        tag.putBoolean("Activated", activated);
        return tag;
    }

    public static CollectorEntry load(CompoundTag tag) {
        return new CollectorEntry(
                tag.getUUID("Id"),
                BlockPos.of(tag.getLong("Pos")),
                tag.getLong("CreatedTick"),
                tag.getBoolean("Activated")
        );
    }

    public CollectorEntry withActivated(boolean value) {
        return new CollectorEntry(this.id, this.pos, this.createdTick, value);
    }
}
