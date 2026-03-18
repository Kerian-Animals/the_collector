package fr.kerian_animals.thecollector.advancement;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.registry.ModItems;
import fr.kerian_animals.thecollector.registry.ModBlocks;
import fr.kerian_animals.thecollector.world.dimension.ModDimensions;
import fr.kerian_animals.thecollector.world.vault.CollectorVaultManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class CollectorAdvancementManager {
    public CollectorAdvancementManager() {
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.serverLevel().isClientSide || player.tickCount % 20 != 0) {
            return;
        }

        CollectorAdvancementHelper.award(player, "root");

        if (hasItem(player, ModItems.NETHER_RELIC_FRAGMENT.get()) || hasItem(player, ModItems.CAVERN_RELIC_FRAGMENT.get())) {
            CollectorAdvancementHelper.award(player, "relics_of_the_hunt");
        }
        if (hasItem(player, ModItems.ECHO_RELIC_FRAGMENT.get())) {
            CollectorAdvancementHelper.award(player, "echo_in_the_dark");
        }
        if (hasItem(player, ModItems.COLLECTOR_CATALYST.get())) {
            CollectorAdvancementHelper.award(player, "forging_the_catalyst");
        }
        if (isNearTrace(player.blockPosition(), player)) {
            CollectorAdvancementHelper.award(player, "a_strange_trace");
        }
        if (hasItem(player, ModItems.UNSTABLE_RESONANCE.get())) {
            CollectorAdvancementHelper.award(player, "bottled_disturbance");
        }
        if (hasItem(player, ModItems.ALEMBIC.get()) || hasItem(player, ModItems.DISTILLED_RESONANCE.get())) {
            CollectorAdvancementHelper.award(player, "delicate_distillation");
        }
        if (hasItem(player, ModItems.DISTILLED_RESONANCE.get())) {
            CollectorAdvancementHelper.award(player, "refined_vibrations");
        }
        if (hasItem(player, ModItems.RESONANT_RESIDUE.get())) {
            CollectorAdvancementHelper.award(player, "what_remains");
        }
        if (hasItem(player, ModItems.ATTUNED_RESONANCE.get())) {
            CollectorAdvancementHelper.award(player, "dimensional_attunement");
        }
        if (hasItem(player, ModItems.RESONANCE_CRYSTAL.get())) {
            CollectorAdvancementHelper.award(player, "focus_the_signal");
        }
        if (hasItem(player, ModItems.COLLECTOR_COMPASS.get())) {
            CollectorAdvancementHelper.award(player, "collector_compass");
        }
        if (player.serverLevel().dimension() == ModDimensions.COLLECTOR_REALM
                && player.blockPosition().distManhattan(CollectorVaultManager.VAULT_CENTER) <= 4) {
            CollectorAdvancementHelper.award(player, "the_collectors_domain");
        }
    }

    private static boolean hasItem(ServerPlayer player, Item item) {
        return player.getInventory().contains(stack -> stack.is(item));
    }

    private static boolean isNearTrace(BlockPos center, ServerPlayer player) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
            if (player.serverLevel().getBlockState(pos).is(ModBlocks.COLLECTOR_TRACE.get())) {
                return true;
            }
        }
        return false;
    }
}
