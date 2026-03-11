package fr.harmonia.thecollector.stash;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CollectorStash(
        UUID id,
        ResourceKey<Level> dimension,
        BlockPos pos,
        List<ItemStack> contents,
        long createdTick
) {
    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putString("Dimension", dimension.location().toString());
        tag.putLong("Pos", pos.asLong());
        tag.putLong("CreatedTick", createdTick);

        ListTag contentList = new ListTag();
        for (ItemStack stack : contents) {
            contentList.add(stack.save(registries));
        }
        tag.put("Contents", contentList);
        return tag;
    }

    public static CollectorStash load(CompoundTag tag, HolderLookup.Provider registries) {
        UUID id = tag.getUUID("Id");
        ResourceLocation dimensionId = ResourceLocation.parse(tag.getString("Dimension"));
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
        BlockPos pos = BlockPos.of(tag.getLong("Pos"));
        long createdTick = tag.getLong("CreatedTick");

        List<ItemStack> contents = new ArrayList<>();
        ListTag contentList = tag.getList("Contents", Tag.TAG_COMPOUND);
        for (int i = 0; i < contentList.size(); i++) {
            ItemStack.parse(registries, contentList.getCompound(i)).ifPresent(contents::add);
        }
        return new CollectorStash(id, dimension, pos, contents, createdTick);
    }
}
