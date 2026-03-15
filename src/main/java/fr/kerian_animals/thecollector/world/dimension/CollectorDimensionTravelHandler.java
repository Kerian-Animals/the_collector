package fr.kerian_animals.thecollector.world.dimension;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.stash.CollectorEntry;
import fr.kerian_animals.thecollector.stash.CollectorSavedData;
import fr.kerian_animals.thecollector.world.vault.CollectorVaultManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;

@EventBusSubscriber(modid = TheCollectorMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class CollectorDimensionTravelHandler {
    private static final String TAG_TRAVEL_COOLDOWN = "the_collector_travel_cooldown";
    private static final String TAG_LAST_ENTRY_POS = "the_collector_last_entry_pos";

    private CollectorDimensionTravelHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.serverLevel().isClientSide || !player.isCrouching()) {
            return;
        }

        int cooldown = player.getPersistentData().getInt(TAG_TRAVEL_COOLDOWN);
        if (cooldown > 0) {
            player.getPersistentData().putInt(TAG_TRAVEL_COOLDOWN, cooldown - 1);
            return;
        }

        if (player.serverLevel().dimension() == Level.OVERWORLD) {
            tryEnterCollectorRealm(player);
        } else if (player.serverLevel().dimension() == ModDimensions.COLLECTOR_REALM) {
            tryReturnToOverworld(player);
        }
    }

    private static void tryEnterCollectorRealm(ServerPlayer player) {
        ServerLevel overworld = player.serverLevel();
        Optional<CollectorEntry> nearest = CollectorSavedData.get(overworld).getNearestEntry(player.blockPosition());
        if (nearest.isEmpty() || nearest.get().pos().distManhattan(player.blockPosition()) > 1) {
            return;
        }
        if (!player.serverLevel().getBlockState(nearest.get().pos()).is(Blocks.LODESTONE)) {
            return;
        }
        if (!nearest.get().activated()) {
            player.sendSystemMessage(Component.translatable("dimension.the_collector.entry_inactive"));
            return;
        }

        ServerLevel realm = player.server.getLevel(ModDimensions.COLLECTOR_REALM);
        if (realm == null) {
            player.sendSystemMessage(Component.translatable("dimension.the_collector.not_ready"));
            return;
        }

        CollectorVaultManager.ensureVaultBuilt(realm);
        prepareSafeStand(realm, CollectorVaultManager.VAULT_ENTRY_PAD);
        player.getPersistentData().putLong(TAG_LAST_ENTRY_POS, nearest.get().pos().asLong());
        player.getPersistentData().putInt(TAG_TRAVEL_COOLDOWN, 20);
        player.changeDimension(new DimensionTransition(
                realm,
                new Vec3(
                        CollectorVaultManager.VAULT_ENTRY_PAD.getX() + 0.5D,
                        CollectorVaultManager.VAULT_ENTRY_PAD.getY() + 1.0D,
                        CollectorVaultManager.VAULT_ENTRY_PAD.getZ() + 0.5D
                ),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                entity -> {
                }
        ));
        player.sendSystemMessage(Component.translatable("dimension.the_collector.entered"));
    }

    private static void tryReturnToOverworld(ServerPlayer player) {
        if (player.blockPosition().distManhattan(CollectorVaultManager.VAULT_ENTRY_PAD) > 1) {
            return;
        }
        if (!player.serverLevel().getBlockState(CollectorVaultManager.VAULT_ENTRY_PAD).is(Blocks.LODESTONE)) {
            return;
        }

        ServerLevel overworld = player.server.overworld();
        CollectorSavedData data = CollectorSavedData.get(overworld);
        long rawPos = player.getPersistentData().getLong(TAG_LAST_ENTRY_POS);
        Optional<CollectorEntry> target = rawPos != 0
                ? data.getAllEntries().stream().filter(entry -> entry.pos().asLong() == rawPos).findFirst()
                : data.getLatestEntry();
        if (target.isEmpty()) {
            target = data.getLatestEntry();
        }

        if (target.isEmpty()) {
            player.sendSystemMessage(Component.translatable("dimension.the_collector.no_entry"));
            return;
        }

        prepareSafeStand(overworld, target.get().pos());
        player.getPersistentData().putInt(TAG_TRAVEL_COOLDOWN, 20);
        player.changeDimension(new DimensionTransition(
                overworld,
                new Vec3(target.get().pos().getX() + 0.5D, target.get().pos().getY() + 1.0D, target.get().pos().getZ() + 0.5D),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                entity -> {
                }
        ));
        player.sendSystemMessage(Component.translatable("dimension.the_collector.left"));
    }

    private static void prepareSafeStand(ServerLevel level, net.minecraft.core.BlockPos pos) {
        if (level.getBlockState(pos).canBeReplaced()) {
            level.setBlock(pos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
        }
        if (!level.getBlockState(pos.above()).canBeReplaced()) {
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
        }
        if (!level.getBlockState(pos.above(2)).canBeReplaced()) {
            level.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
