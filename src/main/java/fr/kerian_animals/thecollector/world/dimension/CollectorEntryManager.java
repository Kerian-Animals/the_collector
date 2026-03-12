package fr.kerian_animals.thecollector.world.dimension;

import fr.kerian_animals.thecollector.stash.CollectorEntry;
import fr.kerian_animals.thecollector.stash.CollectorSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Optional;
import java.util.UUID;

public final class CollectorEntryManager {
    private CollectorEntryManager() {
    }

    public static CollectorEntry ensureEntryExists(ServerLevel overworld, BlockPos around) {
        CollectorSavedData data = CollectorSavedData.get(overworld);
        Optional<CollectorEntry> existing = data.getLatestEntry();
        if (existing.isPresent()) {
            return existing.get();
        }

        BlockPos entryPos = findEntryPos(overworld, around);
        buildEntryStructure(overworld, entryPos);

        CollectorEntry entry = new CollectorEntry(UUID.randomUUID(), entryPos, overworld.getGameTime(), false);
        data.addEntry(entry);
        return entry;
    }

    public static Optional<CollectorEntry> getNearestEntryFor(ServerLevel level, BlockPos from) {
        return CollectorSavedData.get(level).getNearestEntry(from);
    }

    private static BlockPos findEntryPos(ServerLevel level, BlockPos around) {
        for (int i = 0; i < 36; i++) {
            int x = around.getX() + level.random.nextInt(800) - 400;
            int z = around.getZ() + level.random.nextInt(800) - 400;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, Math.max(level.getMinBuildHeight() + 1, y), z);
            if (isGoodEntryPos(level, pos)) {
                return pos;
            }
        }

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, around.getX(), around.getZ());
        return new BlockPos(around.getX(), Math.max(level.getMinBuildHeight() + 1, y), around.getZ());
    }

    private static boolean isGoodEntryPos(ServerLevel level, BlockPos pos) {
        if (!level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        if (!level.getFluidState(pos).isEmpty()) {
            return false;
        }
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below());
    }

    public static void buildEntryStructure(ServerLevel level, BlockPos center) {
        Block[] palette = new Block[]{Blocks.COBBLED_DEEPSLATE, Blocks.DEEPSLATE_BRICKS, Blocks.TUFF_BRICKS};
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos floor = center.offset(dx, -1, dz);
                level.setBlock(floor, palette[level.random.nextInt(palette.length)].defaultBlockState(), 3);
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    BlockPos rim = center.offset(dx, 0, dz);
                    if (level.getBlockState(rim).canBeReplaced() || level.getBlockState(rim).isAir()) {
                        level.setBlock(rim, Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState(), 3);
                    }
                }
            }
        }

        level.setBlock(center, Blocks.LODESTONE.defaultBlockState(), 3);
        level.setBlock(center.above(), Blocks.SOUL_FIRE.defaultBlockState(), 3);

        // Unstable ruins: player must complete the ritual structure to activate the gate.
        level.setBlock(center.offset(2, 0, 0), Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(), 3);
        level.setBlock(center.offset(-2, 0, 0), Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 0, 2), Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(), 3);
        level.setBlock(center.offset(0, 0, -2), Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState(), 3);
    }
}
