package fr.kerian_animals.thecollector.world.vault;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.ArrayList;
import java.util.List;

public final class CollectorVaultManager {
    public static final BlockPos VAULT_CENTER = new BlockPos(0, 64, 0);
    public static final BlockPos VAULT_ENTRY_PAD = VAULT_CENTER.offset(0, 0, -2);

    private CollectorVaultManager() {
    }

    public static void ensureVaultBuilt(ServerLevel level) {
        BlockPos c = VAULT_CENTER;

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -1; dy <= 4; dy++) {
                    BlockPos p = c.offset(dx, dy, dz);
                    if (dy == -1) {
                        level.setBlock(p, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
                    } else if (dy == 4 || Math.abs(dx) == 3 || Math.abs(dz) == 3) {
                        level.setBlock(p, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
                    } else {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        level.setBlock(c.offset(0, 0, -2), Blocks.LODESTONE.defaultBlockState(), 3);
        level.setBlock(c.offset(0, 1, -2), Blocks.SOUL_TORCH.defaultBlockState(), 3);
        level.setBlock(c.offset(-2, 1, -2), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
        level.setBlock(c.offset(2, 1, -2), Blocks.SOUL_LANTERN.defaultBlockState(), 3);

        for (BlockPos pos : storageSlots()) {
            ensureStorageContainer(level, pos);
        }
    }

    public static BlockPos depositLoot(ServerLevel level, List<ItemStack> incomingLoot) {
        ensureVaultBuilt(level);
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack s : incomingLoot) {
            if (!s.isEmpty()) {
                remaining.add(s.copy());
            }
        }

        BlockPos lastUsed = storageSlots().getFirst();
        for (BlockPos storagePos : storageSlots()) {
            Container container = ensureStorageContainer(level, storagePos);
            if (container == null) {
                continue;
            }
            lastUsed = storagePos;
            for (int i = 0; i < container.getContainerSize() && !remaining.isEmpty(); i++) {
                if (!container.getItem(i).isEmpty()) {
                    continue;
                }
                ItemStack stack = remaining.removeFirst();
                container.setItem(i, stack);
            }
            if (container instanceof BlockEntity blockEntity) {
                blockEntity.setChanged();
            }
            if (remaining.isEmpty()) {
                return storagePos;
            }
        }

        // Overflow safety: if every container is full, drop leftovers near the first storage slot.
        BlockPos overflowPos = storageSlots().getFirst().above();
        for (ItemStack left : remaining) {
            net.minecraft.world.entity.item.ItemEntity dropped = new net.minecraft.world.entity.item.ItemEntity(
                    level, overflowPos.getX() + 0.5D, overflowPos.getY() + 0.5D, overflowPos.getZ() + 0.5D, left
            );
            level.addFreshEntity(dropped);
        }
        return lastUsed;
    }

    private static Container ensureStorageContainer(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container container) {
            return container;
        }

        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            return chest;
        }

        level.setBlock(pos, Blocks.BARREL.defaultBlockState(), 3);
        blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof Container container ? container : null;
    }

    private static List<BlockPos> storageSlots() {
        BlockPos c = VAULT_CENTER;
        return List.of(
                c.offset(-2, 0, 2),
                c.offset(0, 0, 2),
                c.offset(2, 0, 2),
                c.offset(-1, 0, 1),
                c.offset(1, 0, 1),
                c.offset(-2, 0, 0),
                c.offset(0, 0, 0),
                c.offset(2, 0, 0)
        );
    }
}
