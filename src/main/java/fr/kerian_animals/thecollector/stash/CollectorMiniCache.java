package fr.kerian_animals.thecollector.stash;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record CollectorMiniCache(UUID id, BlockPos pos, long createdTick) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putLong("Pos", pos.asLong());
        tag.putLong("CreatedTick", createdTick);
        return tag;
    }

    public static CollectorMiniCache load(CompoundTag tag) {
        return new CollectorMiniCache(
                tag.getUUID("Id"),
                BlockPos.of(tag.getLong("Pos")),
                tag.getLong("CreatedTick")
        );
    }
}
