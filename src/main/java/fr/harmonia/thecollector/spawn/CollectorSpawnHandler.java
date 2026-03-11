package fr.harmonia.thecollector.spawn;

import fr.harmonia.thecollector.config.TheCollectorConfig;
import fr.harmonia.thecollector.entity.CollectorEntity;
import fr.harmonia.thecollector.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectorSpawnHandler {
    private final Map<ServerLevel, Long> lastSpawnByLevel = new HashMap<>();

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        if (!TheCollectorConfig.MOD_ENABLED.get()) {
            return;
        }
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (TheCollectorConfig.NIGHT_ONLY_SPAWN.get() && !level.isNight()) {
            return;
        }
        if (level.getGameTime() % 200 != 0) {
            return;
        }

        long lastSpawn = this.lastSpawnByLevel.getOrDefault(level, Long.MIN_VALUE);
        if (level.getGameTime() - lastSpawn < TheCollectorConfig.SPAWN_COOLDOWN_TICKS.get()) {
            return;
        }
        if (level.random.nextDouble() > TheCollectorConfig.SPAWN_CHANCE_PER_CHECK.get()) {
            return;
        }

        List<ServerPlayer> candidates = level.players().stream()
                .filter(player -> !player.isSpectator())
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        ServerPlayer anchor = candidates.get(level.random.nextInt(candidates.size()));
        BlockPos spawnPos = findSpawnPos(level, anchor.blockPosition());
        if (spawnPos == null) {
            return;
        }
        if (!level.getEntitiesOfClass(CollectorEntity.class,
                new net.minecraft.world.phys.AABB(spawnPos).inflate(96)).isEmpty()) {
            return;
        }

        CollectorEntity collector = ModEntities.COLLECTOR.get().create(level);
        if (collector == null) {
            return;
        }

        collector.moveTo(spawnPos, level.random.nextFloat() * 360.0F, 0.0F);
        if (collector.checkSpawnRules(level, MobSpawnType.EVENT) && collector.checkSpawnObstruction(level)) {
            level.addFreshEntity(collector);
            this.lastSpawnByLevel.put(level, level.getGameTime());
        }
    }

    private BlockPos findSpawnPos(ServerLevel level, BlockPos playerPos) {
        int minDistance = TheCollectorConfig.SPAWN_MIN_DISTANCE.get();
        int maxDistance = Math.max(minDistance, TheCollectorConfig.SPAWN_MAX_DISTANCE.get());
        for (int i = 0; i < 12; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            int distance = minDistance + level.random.nextInt(maxDistance - minDistance + 1);
            int x = playerPos.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = playerPos.getZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos.below()).isSolidRender(level, pos.below()) && level.getFluidState(pos).isEmpty()) {
                return pos;
            }
        }
        return null;
    }
}
