package fr.kerian_animals.thecollector.stash;

import fr.kerian_animals.thecollector.config.TheCollectorConfig;
import fr.kerian_animals.thecollector.entity.CollectorEntity;
import fr.kerian_animals.thecollector.lore.CollectorLoreBookFactory;
import fr.kerian_animals.thecollector.world.dimension.CollectorEntryManager;
import fr.kerian_animals.thecollector.world.dimension.ModDimensions;
import fr.kerian_animals.thecollector.world.vault.CollectorVaultManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CollectorStashManager {
    private CollectorStashManager() {
    }

    public static void createStashFromCollector(ServerLevel level, CollectorEntity collector) {
        if (!TheCollectorConfig.STASH_ENABLED.get()) {
            return;
        }

        List<ItemStack> stolenItems = new ArrayList<>(collector.getStolenItems());
        if (stolenItems.isEmpty()) {
            return;
        }
        stolenItems.add(CollectorLoreBookFactory.createRandomFragment(level.random));
        if (TheCollectorConfig.BONUS_LOOT_ENABLED.get() && level.random.nextDouble() < 0.35D) {
            stolenItems.add(level.random.nextBoolean() ? new ItemStack(Items.EMERALD, 2) : new ItemStack(Items.GOLD_INGOT, 3));
        }

        ServerLevel realm = level.getServer().getLevel(ModDimensions.COLLECTOR_REALM);
        BlockPos stashPos;
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> stashDimension;

        if (realm != null) {
            CollectorVaultManager.ensureVaultBuilt(realm);
            stashPos = CollectorVaultManager.depositLoot(realm, stolenItems);
            stashDimension = ModDimensions.COLLECTOR_REALM;

            // Ensure at least one random discoverable entry exists in the Overworld.
            CollectorEntryManager.ensureEntryExists(level.getServer().overworld(), collector.blockPosition());
        } else {
            BlockPos fallback = findStashPos(level, collector.blockPosition());
            if (fallback == null) {
                fallback = forceFallbackStashPos(level, collector.blockPosition());
            }
            buildStashStructure(level, fallback);
            Container container = placeContainerWithFallback(level, fallback);
            if (container != null) {
                for (int i = 0; i < Math.min(container.getContainerSize(), stolenItems.size()); i++) {
                    container.setItem(i, stolenItems.get(i));
                }
                if (container instanceof BlockEntity blockEntity) {
                    blockEntity.setChanged();
                }
            }
            stashPos = fallback;
            stashDimension = level.dimension();
        }

        UUID stashId = UUID.randomUUID();
        CollectorStash stash = new CollectorStash(stashId, stashDimension, stashPos, stolenItems, level.getGameTime());
        CollectorSavedData data = CollectorSavedData.get(level);
        data.addStash(stash);

        List<ServerPlayer> nearbyPlayers = level.players().stream()
                .filter(player -> player.distanceToSqr(collector) <= 128 * 128)
                .toList();
        for (ServerPlayer player : nearbyPlayers) {
            data.setLastStashForPlayer(player.getUUID(), stashId);
        }
    }

    private static BlockPos findStashPos(ServerLevel level, BlockPos around) {
        for (int i = 0; i < 32; i++) {
            int x = around.getX() + level.random.nextInt(64) - 32;
            int z = around.getZ() + level.random.nextInt(64) - 32;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos candidate = new BlockPos(x, Math.max(level.getMinBuildHeight() + 1, y - 1), z);

            if (isValidStashPos(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isValidStashPos(ServerLevel level, BlockPos pos) {
        if (!level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        if (!level.getBlockState(pos).canBeReplaced()) {
            return false;
        }
        if (!level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
            return false;
        }
        return level.getFluidState(pos).isEmpty();
    }

    private static BlockPos forceFallbackStashPos(ServerLevel level, BlockPos around) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, around.getX(), around.getZ());
        BlockPos pos = new BlockPos(around.getX(), Math.max(level.getMinBuildHeight() + 1, y - 1), around.getZ());

        if (!level.getWorldBorder().isWithinBounds(pos)) {
            BlockPos center = BlockPos.containing(level.getWorldBorder().getCenterX(), pos.getY(), level.getWorldBorder().getCenterZ());
            pos = center;
        }

        // Force a valid surface and empty placement space so stash creation never silently fails.
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        if (!level.getFluidState(pos).isEmpty()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        return pos;
    }

    private static Container placeContainerWithFallback(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            return chest;
        }

        level.setBlock(pos, Blocks.BARREL.defaultBlockState(), 3);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container container) {
            return container;
        }

        // Last-resort safety: clear once and retry chest.
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof Container container ? container : null;
    }

    private static void buildStashStructure(ServerLevel level, BlockPos center) {
        Block[] floorPalette = new Block[]{
                Blocks.COBBLED_DEEPSLATE, Blocks.MOSSY_COBBLESTONE, Blocks.TUFF_BRICKS
        };

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int manhattan = Math.abs(dx) + Math.abs(dz);
                if (manhattan > 4 && level.random.nextFloat() > 0.25F) {
                    continue;
                }

                BlockPos floorPos = center.offset(dx, -1, dz);
                BlockPos topPos = center.offset(dx, 0, dz);
                Block floorBlock = floorPalette[level.random.nextInt(floorPalette.length)];
                placeBlockIfReplaceable(level, floorPos, floorBlock);

                if (manhattan >= 3 && level.random.nextFloat() < 0.65F) {
                    placeBlockIfReplaceable(level, topPos, Blocks.COARSE_DIRT);
                }
            }
        }

        placeBlockIfReplaceable(level, center.offset(2, 0, 0), Blocks.DEEPSLATE_TILE_WALL);
        placeBlockIfReplaceable(level, center.offset(-2, 0, 0), Blocks.DEEPSLATE_TILE_WALL);
        placeBlockIfReplaceable(level, center.offset(0, 0, 2), Blocks.DEEPSLATE_TILE_WALL);
        placeBlockIfReplaceable(level, center.offset(0, 0, -2), Blocks.DEEPSLATE_TILE_WALL);

        placeBlockIfReplaceable(level, center.offset(2, 1, 0), Blocks.SOUL_LANTERN);
        placeBlockIfReplaceable(level, center.offset(-2, 1, 0), Blocks.SOUL_LANTERN);

        if (level.random.nextBoolean()) {
            placeBlockIfReplaceable(level, center.offset(1, 0, 2), Blocks.SKELETON_SKULL);
        }
        if (level.random.nextBoolean()) {
            placeBlockIfReplaceable(level, center.offset(-1, 0, -2), Blocks.CANDLE);
        }
    }

    private static void placeBlockIfReplaceable(ServerLevel level, BlockPos pos, Block block) {
        if (level.getBlockState(pos).canBeReplaced() || level.getBlockState(pos).isAir()) {
            level.setBlock(pos, block.defaultBlockState(), 3);
        }
    }
}

