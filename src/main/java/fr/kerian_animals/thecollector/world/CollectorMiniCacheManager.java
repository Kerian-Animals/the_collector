package fr.kerian_animals.thecollector.world;

import fr.kerian_animals.thecollector.advancement.CollectorAdvancementHelper;
import fr.kerian_animals.thecollector.config.TheCollectorConfig;
import fr.kerian_animals.thecollector.lore.CollectorLoreBookFactory;
import fr.kerian_animals.thecollector.registry.ModItems;
import fr.kerian_animals.thecollector.stash.CollectorMiniCache;
import fr.kerian_animals.thecollector.stash.CollectorSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CollectorMiniCacheManager {
    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!TheCollectorConfig.MOD_ENABLED.get() || !TheCollectorConfig.MINI_CACHE_ENABLED.get()) {
            return;
        }
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (level.getGameTime() % TheCollectorConfig.MINI_CACHE_CHECK_INTERVAL_TICKS.get() != 0L) {
            return;
        }

        List<ServerPlayer> candidates = level.players().stream()
                .filter(player -> !player.isSpectator())
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        ServerPlayer anchor = candidates.get(level.random.nextInt(candidates.size()));
        CollectorSavedData data = CollectorSavedData.get(level);
        long nearbyCount = data.getAllMiniCaches().stream()
                .filter(cache -> cache.pos().distSqr(anchor.blockPosition()) <= square(TheCollectorConfig.MINI_CACHE_PLAYER_RADIUS.get()))
                .count();
        if (nearbyCount >= TheCollectorConfig.MINI_CACHE_MAX_PER_AREA.get()) {
            return;
        }

        BlockPos cachePos = findCachePos(level, anchor.blockPosition(), data);
        if (cachePos == null) {
            return;
        }

        buildMiniCache(level, cachePos);
        fillCacheChest(level, cachePos);
        data.addMiniCache(new CollectorMiniCache(UUID.randomUUID(), cachePos, level.getGameTime()));
        CollectorAdvancementHelper.award(anchor, "a_hidden_stash");
    }

    private static BlockPos findCachePos(ServerLevel level, BlockPos around, CollectorSavedData data) {
        int minDistance = TheCollectorConfig.MINI_CACHE_MIN_DISTANCE.get();
        int maxDistance = Math.max(minDistance, TheCollectorConfig.MINI_CACHE_MAX_DISTANCE.get());
        int minSpacing = TheCollectorConfig.MINI_CACHE_MIN_SPACING.get();

        for (int i = 0; i < 24; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            int distance = minDistance + level.random.nextInt(maxDistance - minDistance + 1);
            int x = around.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = around.getZ() + (int) Math.round(Math.sin(angle) * distance);
            BlockPos pos = findOpenCachePosInColumn(level, x, z);

            if (pos == null) {
                continue;
            }
            boolean tooClose = data.getAllMiniCaches().stream()
                    .anyMatch(cache -> cache.pos().distSqr(pos) < square(minSpacing));
            if (!tooClose) {
                return pos;
            }
        }
        return null;
    }

    private static BlockPos findOpenCachePosInColumn(ServerLevel level, int x, int z) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int minY = Math.max(level.getMinBuildHeight() + 1, topY - 48);
        for (int y = Math.max(level.getMinBuildHeight() + 1, topY); y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isValidCachePos(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isValidCachePos(ServerLevel level, BlockPos center) {
        if (!level.getWorldBorder().isWithinBounds(center)) {
            return false;
        }
        if (!level.getFluidState(center).isEmpty()) {
            return false;
        }
        if (!level.getBlockState(center.below()).isSolidRender(level, center.below())) {
            return false;
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos floor = center.offset(dx, -1, dz);
                BlockPos body = center.offset(dx, 0, dz);
                BlockPos above = center.offset(dx, 1, dz);
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

    private static void buildMiniCache(ServerLevel level, BlockPos center) {
        clearVolume(level, center, 2, 1);
        Block[] floorPalette = new Block[]{
                Blocks.BLACKSTONE,
                Blocks.GILDED_BLACKSTONE,
                Blocks.COBBLED_DEEPSLATE,
                Blocks.POLISHED_BLACKSTONE_BRICKS
        };

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos floorPos = center.offset(dx, -1, dz);
                BlockPos topPos = center.offset(dx, 0, dz);
                placeBlock(level, floorPos, floorPalette[level.random.nextInt(floorPalette.length)]);

                if ((Math.abs(dx) == 2 || Math.abs(dz) == 2) && level.random.nextFloat() < 0.4F) {
                    placeBlock(level, topPos, level.random.nextBoolean() ? Blocks.BLACKSTONE : Blocks.COAL_BLOCK);
                } else if (level.random.nextFloat() < 0.15F && !topPos.equals(center)) {
                    placeBlock(level, topPos, Blocks.COARSE_DIRT);
                }
            }
        }

        placeBlock(level, center.offset(0, 0, -1), Blocks.BLACKSTONE);
        placeBlock(level, center.offset(0, 0, 1), Blocks.BLACKSTONE);
        placeBlock(level, center.offset(0, 0, -2), Blocks.COBBLED_DEEPSLATE_WALL);
        placeBlock(level, center.offset(0, 0, 2), Blocks.COBBLED_DEEPSLATE_WALL);
        placeBlock(level, center.offset(0, 1, -2), Blocks.SOUL_LANTERN);
        placeBlock(level, center.offset(0, 1, 2), Blocks.SOUL_LANTERN);

        if (level.random.nextBoolean()) {
            placeBlock(level, center.offset(-1, 0, 0), Blocks.BLACKSTONE);
        }
        if (level.random.nextBoolean()) {
            placeBlock(level, center.offset(1, 0, 0), Blocks.COBBLED_DEEPSLATE);
        }
    }

    private static void fillCacheChest(ServerLevel level, BlockPos chestPos) {
        Container container = placeContainer(level, chestPos);
        if (container == null) {
            return;
        }

        List<ItemStack> loot = rollLoot(level);
        for (int i = 0; i < Math.min(container.getContainerSize(), loot.size()); i++) {
            container.setItem(i, loot.get(i));
        }
        if (container instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
        }
    }

    private static List<ItemStack> rollLoot(ServerLevel level) {
        List<ItemStack> loot = new ArrayList<>();
        double roll = level.random.nextDouble();

        if (roll < 0.20D) {
            loot.add(randomFragment(level));
        } else if (roll < 0.60D) {
            loot.add(randomStolenObject(level));
        } else if (roll < 0.80D) {
            loot.add(CollectorLoreBookFactory.createRandomFragment(level.random));
        }

        if (!loot.isEmpty() && level.random.nextFloat() < 0.35F) {
            loot.add(new ItemStack(Items.BONE, 1 + level.random.nextInt(2)));
        }
        return loot;
    }

    private static ItemStack randomFragment(ServerLevel level) {
        return switch (level.random.nextInt(3)) {
            case 0 -> new ItemStack(ModItems.NETHER_RELIC_FRAGMENT.get());
            case 1 -> new ItemStack(ModItems.CAVERN_RELIC_FRAGMENT.get());
            default -> new ItemStack(ModItems.ECHO_RELIC_FRAGMENT.get());
        };
    }

    private static ItemStack randomStolenObject(ServerLevel level) {
        return switch (level.random.nextInt(8)) {
            case 0 -> new ItemStack(Items.EMERALD, 1 + level.random.nextInt(3));
            case 1 -> new ItemStack(Items.GOLD_INGOT, 2 + level.random.nextInt(3));
            case 2 -> new ItemStack(Items.ENDER_PEARL);
            case 3 -> new ItemStack(Items.NAME_TAG);
            case 4 -> new ItemStack(Items.CLOCK);
            case 5 -> new ItemStack(Items.COMPASS);
            case 6 -> new ItemStack(Items.AMETHYST_SHARD, 2 + level.random.nextInt(3));
            default -> new ItemStack(Items.IRON_INGOT, 2 + level.random.nextInt(4));
        };
    }

    private static Container placeContainer(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            return chest;
        }
        return null;
    }

    private static void placeBlock(ServerLevel level, BlockPos pos, Block block) {
        if (canReplace(level, pos)) {
            level.setBlock(pos, block.defaultBlockState(), 3);
        }
    }

    private static boolean canReplace(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).canBeReplaced() || level.getBlockState(pos).isAir();
    }

    private static double square(int value) {
        return (double) value * value;
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
