package fr.kerian_animals.thecollector.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import fr.kerian_animals.thecollector.registry.ModBlocks;

public final class CollectorResidueManager {
    private CollectorResidueManager() {
    }

    public static void spawnResidueTrail(ServerLevel level, BlockPos theftPos, BlockPos ritualPos) {
        int traceCount = 3 + level.random.nextInt(4);
        double dx = ritualPos.getX() - theftPos.getX();
        double dz = ritualPos.getZ() - theftPos.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        double unitX = length > 0.001D ? dx / length : 1.0D;
        double unitZ = length > 0.001D ? dz / length : 0.0D;

        for (int i = 0; i < traceCount; i++) {
            double progress = (i + 1.0D) / (traceCount + 1.0D);
            int stepDistance = 3 + i * 2 + level.random.nextInt(2);
            int x = Mth.floor(theftPos.getX() + unitX * stepDistance + lateralOffset(level, unitZ));
            int z = Mth.floor(theftPos.getZ() + unitZ * stepDistance + lateralOffset(level, -unitX));
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos placePos = new BlockPos(x, y, z);

            if (!canPlaceResidue(level, placePos)) {
                continue;
            }

            level.setBlock(placePos, residueState(), 3);
            level.sendParticles(ParticleTypes.SMOKE, x + 0.5D, y + 0.08D, z + 0.5D, 4, 0.18D, 0.02D, 0.18D, 0.005D);
            level.sendParticles(ParticleTypes.SOUL, x + 0.5D, y + 0.12D, z + 0.5D, 2, 0.12D, 0.03D, 0.12D, 0.0D);

            if (i == 0 || progress > 0.65D) {
                level.playSound(
                        null,
                        placePos,
                        SoundEvents.SOUL_ESCAPE.value(),
                        SoundSource.BLOCKS,
                        0.25F,
                        0.85F + level.random.nextFloat() * 0.25F
                );
            }
        }
    }

    private static BlockState residueState() {
        return ModBlocks.COLLECTOR_TRACE.get().defaultBlockState();
    }

    private static double lateralOffset(ServerLevel level, double axis) {
        return axis * (level.random.nextDouble() * 4.0D - 2.0D);
    }

    private static boolean canPlaceResidue(ServerLevel level, BlockPos pos) {
        if (!level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        if (!level.getFluidState(pos).isEmpty()) {
            return false;
        }
        if (!level.getBlockState(pos).canBeReplaced()) {
            return false;
        }
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below());
    }
}
