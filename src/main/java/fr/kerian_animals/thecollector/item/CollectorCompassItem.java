package fr.kerian_animals.thecollector.item;

import fr.kerian_animals.thecollector.stash.CollectorSavedData;
import fr.kerian_animals.thecollector.stash.CollectorStash;
import fr.kerian_animals.thecollector.world.dimension.CollectorEntryManager;
import fr.kerian_animals.thecollector.world.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class CollectorCompassItem extends CompassItem {
    public CollectorCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }

        CollectorSavedData data = CollectorSavedData.get(serverPlayer.serverLevel());
        Optional<CollectorStash> stashOptional = data.getLastStashForPlayer(serverPlayer.getUUID())
                .or(data::getLatestStash);

        if (stashOptional.isEmpty()) {
            Optional<fr.kerian_animals.thecollector.stash.CollectorEntry> nearestEntry =
                    CollectorEntryManager.getNearestEntryFor(serverPlayer.serverLevel(), serverPlayer.blockPosition());
            if (nearestEntry.isPresent()) {
                serverPlayer.sendSystemMessage(Component.translatable(
                                "item.the_collector.collector_compass.entry_hint",
                                nearestEntry.get().pos().getX(),
                                nearestEntry.get().pos().getY(),
                                nearestEntry.get().pos().getZ())
                        .withStyle(ChatFormatting.DARK_AQUA));
                spawnBreadcrumbTrail(serverPlayer, nearestEntry.get().pos());
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("item.the_collector.collector_compass.no_stash")
                        .withStyle(ChatFormatting.GRAY));
            }
            return InteractionResultHolder.consume(stack);
        }

        CollectorStash stash = stashOptional.get();
        data.setLastStashForPlayer(serverPlayer.getUUID(), stash.id());

        boolean sameDimension = serverPlayer.serverLevel().dimension() == stash.dimension();
        String directionText = sameDimension
                ? getCardinalDirection(serverPlayer.blockPosition(), stash.pos())
                : "???";
        int horizontalDistance = sameDimension
                ? horizontalDistance(serverPlayer.blockPosition(), stash.pos())
                : -1;

        serverPlayer.sendSystemMessage(Component.translatable(
                        "item.the_collector.collector_compass.found",
                        stash.pos().getX(),
                        stash.pos().getY(),
                        stash.pos().getZ(),
                        stash.dimension().location().toString())
                .withStyle(ChatFormatting.GOLD));

        if (sameDimension) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "item.the_collector.collector_compass.hint",
                    directionText,
                    horizontalDistance
            ).withStyle(ChatFormatting.AQUA));
            spawnBreadcrumbTrail(serverPlayer, stash.pos());
        } else {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "item.the_collector.collector_compass.cross_dimension"
            ).withStyle(ChatFormatting.DARK_AQUA));
            if (stash.dimension() == ModDimensions.COLLECTOR_REALM) {
                CollectorEntryManager.getNearestEntryFor(serverPlayer.serverLevel(), serverPlayer.blockPosition())
                        .ifPresent(entry -> serverPlayer.sendSystemMessage(Component.translatable(
                                "item.the_collector.collector_compass.entry_hint",
                                entry.pos().getX(), entry.pos().getY(), entry.pos().getZ()
                        ).withStyle(ChatFormatting.AQUA)));
            }
        }

        serverPlayer.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.consume(stack);
    }

    private static String getCardinalDirection(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        float angle = (float) Math.toDegrees(Math.atan2(dz, dx));
        float normalized = Mth.positiveModulo(angle, 360.0F);

        if (normalized < 22.5F || normalized >= 337.5F) {
            return "E";
        }
        if (normalized < 67.5F) {
            return "SE";
        }
        if (normalized < 112.5F) {
            return "S";
        }
        if (normalized < 157.5F) {
            return "SW";
        }
        if (normalized < 202.5F) {
            return "W";
        }
        if (normalized < 247.5F) {
            return "NW";
        }
        if (normalized < 292.5F) {
            return "N";
        }
        return "NE";
    }

    private static void spawnBreadcrumbTrail(ServerPlayer player, BlockPos stashPos) {
        Vec3 from = player.position().add(0.0D, 1.2D, 0.0D);
        Vec3 to = Vec3.atCenterOf(stashPos).add(0.0D, 0.6D, 0.0D);
        Vec3 direction = to.subtract(from);
        double length = direction.length();
        if (length <= 1.0D) {
            return;
        }

        Vec3 unit = direction.scale(1.0D / length);
        double maxTrail = Math.min(length, 48.0D);
        for (double d = 2.0D; d <= maxTrail; d += 2.0D) {
            Vec3 p = from.add(unit.scale(d));
            player.serverLevel().sendParticles(
                    ParticleTypes.WITCH,
                    p.x, p.y, p.z,
                    2,
                    0.15D, 0.15D, 0.15D,
                    0.0D
            );
        }
    }

    private static int horizontalDistance(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        return (int) Math.sqrt((double) dx * dx + (double) dz * dz);
    }
}

