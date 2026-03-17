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
            ensureEntryStructure(overworld, existing.get());
            return existing.get();
        }

        BlockPos entryPos = findEntryPos(overworld, around);
        buildEntryStructure(overworld, entryPos);

        CollectorEntry entry = new CollectorEntry(UUID.randomUUID(), entryPos, overworld.getGameTime(), false);
        data.addEntry(entry);
        return entry;
    }

    public static void ensureEntryStructure(ServerLevel level, CollectorEntry entry) {
        if (isEntryStructurePresent(level, entry.pos())) {
            return;
        }
        buildEntryStructure(level, entry.pos());
    }

    public static Optional<CollectorEntry> getNearestEntryFor(ServerLevel level, BlockPos from) {
        return CollectorSavedData.get(level).getNearestEntry(from);
    }

    private static BlockPos findEntryPos(ServerLevel level, BlockPos around) {
        for (int i = 0; i < 36; i++) {
            int x = around.getX() + level.random.nextInt(800) - 400;
            int z = around.getZ() + level.random.nextInt(800) - 400;
            BlockPos pos = findOpenEntryPosInColumn(level, x, z);
            if (pos != null) {
                return pos;
            }
        }

        BlockPos fallback = findOpenEntryPosInColumn(level, around.getX(), around.getZ());
        return fallback != null
                ? fallback
                : new BlockPos(around.getX(), Math.max(level.getMinBuildHeight() + 1, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, around.getX(), around.getZ())), around.getZ());
    }

    private static BlockPos findOpenEntryPosInColumn(ServerLevel level, int x, int z) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int minY = Math.max(level.getMinBuildHeight() + 1, topY - 48);
        for (int y = Math.max(level.getMinBuildHeight() + 1, topY); y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isGoodEntryPos(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isGoodEntryPos(ServerLevel level, BlockPos pos) {
        if (!level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        if (!level.getFluidState(pos).isEmpty()) {
            return false;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos floor = pos.offset(dx, -1, dz);
                BlockPos body = pos.offset(dx, 0, dz);
                BlockPos above = pos.offset(dx, 1, dz);
                if (!level.getBlockState(floor).isSolidRender(level, floor)) {
                    return false;
                }
                if (!canReplace(level, body) || !canReplace(level, above)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isEntryStructurePresent(ServerLevel level, BlockPos center) {
        if (!level.getBlockState(center).is(Blocks.LODESTONE)) {
            return false;
        }
        return level.getBlockState(center.offset(2, 0, 0)).is(Blocks.COBBLED_DEEPSLATE_WALL)
                && level.getBlockState(center.offset(-2, 0, 0)).is(Blocks.COBBLED_DEEPSLATE_WALL)
                && level.getBlockState(center.offset(0, 0, 2)).is(Blocks.COBBLED_DEEPSLATE_WALL)
                && level.getBlockState(center.offset(0, 0, -2)).is(Blocks.COBBLED_DEEPSLATE_WALL);
    }

    public static void buildEntryStructure(ServerLevel level, BlockPos center) {
        clearVolume(level, center, 2, 1);
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

    private static boolean canReplace(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).canBeReplaced() || level.getBlockState(pos).isAir();
    }

    private static void clearVolume(ServerLevel level, BlockPos center, int radius, int height) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= height; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
