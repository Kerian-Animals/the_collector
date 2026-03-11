package fr.kerian_animals.thecollector.world.vault;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
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

        for (BlockPos pos : chestSlots()) {
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
            }
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

        BlockPos lastUsed = chestSlots().getFirst();
        for (BlockPos chestPos : chestSlots()) {
            if (!(level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest)) {
                continue;
            }
            lastUsed = chestPos;
            for (int i = 0; i < chest.getContainerSize() && !remaining.isEmpty(); i++) {
                if (!chest.getItem(i).isEmpty()) {
                    continue;
                }
                ItemStack stack = remaining.removeFirst();
                chest.setItem(i, stack);
            }
            chest.setChanged();
            if (remaining.isEmpty()) {
                return chestPos;
            }
        }

        // Overflow safety: if every chest is full, drop leftovers near the first chest.
        BlockPos overflowPos = chestSlots().getFirst().above();
        for (ItemStack left : remaining) {
            net.minecraft.world.entity.item.ItemEntity dropped = new net.minecraft.world.entity.item.ItemEntity(
                    level, overflowPos.getX() + 0.5D, overflowPos.getY() + 0.5D, overflowPos.getZ() + 0.5D, left
            );
            level.addFreshEntity(dropped);
        }
        return lastUsed;
    }

    private static List<BlockPos> chestSlots() {
        BlockPos c = VAULT_CENTER;
        return List.of(
                c.offset(-2, 0, 2),
                c.offset(-1, 0, 2),
                c.offset(0, 0, 2),
                c.offset(1, 0, 2),
                c.offset(2, 0, 2),
                c.offset(-2, 0, 1),
                c.offset(2, 0, 1),
                c.offset(-2, 0, 0),
                c.offset(2, 0, 0)
        );
    }
}
