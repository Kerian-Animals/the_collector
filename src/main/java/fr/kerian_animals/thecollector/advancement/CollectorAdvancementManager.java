package fr.kerian_animals.thecollector.advancement;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.registry.ModItems;
import fr.kerian_animals.thecollector.registry.ModBlocks;
import fr.kerian_animals.thecollector.world.dimension.ModDimensions;
import fr.kerian_animals.thecollector.world.vault.CollectorVaultManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Evaluates gameplay-driven advancements that are easier to infer in code than through pure JSON
 * triggers.
 *
 * <p>The manager runs on a coarse tick cadence and avoids repeated expensive checks by caching
 * already-awarded advancements and scanning the inventory only once when several item-based
 * conditions are still pending.</p>
 */
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

        if (!CollectorAdvancementHelper.isAwarded(player, "root")) {
            CollectorAdvancementHelper.award(player, "root");
        }

        boolean needsRelics = !CollectorAdvancementHelper.isAwarded(player, "relics_of_the_hunt");
        boolean needsEcho = !CollectorAdvancementHelper.isAwarded(player, "echo_in_the_dark");
        boolean needsCatalyst = !CollectorAdvancementHelper.isAwarded(player, "forging_the_catalyst");
        boolean needsResonance = !CollectorAdvancementHelper.isAwarded(player, "bottled_disturbance");
        boolean needsDistillation = !CollectorAdvancementHelper.isAwarded(player, "delicate_distillation");
        boolean needsRefined = !CollectorAdvancementHelper.isAwarded(player, "refined_vibrations");
        boolean needsResidue = !CollectorAdvancementHelper.isAwarded(player, "what_remains");
        boolean needsAttuned = !CollectorAdvancementHelper.isAwarded(player, "dimensional_attunement");
        boolean needsCrystal = !CollectorAdvancementHelper.isAwarded(player, "focus_the_signal");
        boolean needsCompass = !CollectorAdvancementHelper.isAwarded(player, "collector_compass");

        InventorySignals signals = (needsRelics || needsEcho || needsCatalyst || needsResonance
                || needsDistillation || needsRefined || needsResidue || needsAttuned || needsCrystal || needsCompass)
                ? scanInventory(player)
                : InventorySignals.EMPTY;

        if (needsRelics && (signals.hasNetherRelic || signals.hasCavernRelic)) {
            CollectorAdvancementHelper.award(player, "relics_of_the_hunt");
        }
        if (needsEcho && signals.hasEchoRelic) {
            CollectorAdvancementHelper.award(player, "echo_in_the_dark");
        }
        if (needsCatalyst && signals.hasCatalyst) {
            CollectorAdvancementHelper.award(player, "forging_the_catalyst");
        }
        if (!CollectorAdvancementHelper.isAwarded(player, "a_strange_trace") && isNearTrace(player.blockPosition(), player)) {
            CollectorAdvancementHelper.award(player, "a_strange_trace");
        }
        if (needsResonance && signals.hasUnstableResonance) {
            CollectorAdvancementHelper.award(player, "bottled_disturbance");
        }
        if (needsDistillation && (signals.hasAlembic || signals.hasDistilledResonance)) {
            CollectorAdvancementHelper.award(player, "delicate_distillation");
        }
        if (needsRefined && signals.hasDistilledResonance) {
            CollectorAdvancementHelper.award(player, "refined_vibrations");
        }
        if (needsResidue && signals.hasResidue) {
            CollectorAdvancementHelper.award(player, "what_remains");
        }
        if (needsAttuned && signals.hasAttunedResonance) {
            CollectorAdvancementHelper.award(player, "dimensional_attunement");
        }
        if (needsCrystal && signals.hasResonanceCrystal) {
            CollectorAdvancementHelper.award(player, "focus_the_signal");
        }
        if (needsCompass && signals.hasCompass) {
            CollectorAdvancementHelper.award(player, "collector_compass");
        }
        if (!CollectorAdvancementHelper.isAwarded(player, "the_collectors_domain")
                && player.serverLevel().dimension() == ModDimensions.COLLECTOR_REALM
                && player.blockPosition().distManhattan(CollectorVaultManager.VAULT_CENTER) <= 4) {
            CollectorAdvancementHelper.award(player, "the_collectors_domain");
        }
    }

    /**
     * Performs a single inventory pass and records which progression items are currently present.
     */
    private static InventorySignals scanInventory(ServerPlayer player) {
        InventorySignals signals = new InventorySignals();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.NETHER_RELIC_FRAGMENT.get())) {
                signals.hasNetherRelic = true;
            } else if (stack.is(ModItems.CAVERN_RELIC_FRAGMENT.get())) {
                signals.hasCavernRelic = true;
            } else if (stack.is(ModItems.ECHO_RELIC_FRAGMENT.get())) {
                signals.hasEchoRelic = true;
            } else if (stack.is(ModItems.COLLECTOR_CATALYST.get())) {
                signals.hasCatalyst = true;
            } else if (stack.is(ModItems.UNSTABLE_RESONANCE.get())) {
                signals.hasUnstableResonance = true;
            } else if (stack.is(ModItems.ALEMBIC.get())) {
                signals.hasAlembic = true;
            } else if (stack.is(ModItems.DISTILLED_RESONANCE.get())) {
                signals.hasDistilledResonance = true;
            } else if (stack.is(ModItems.RESONANT_RESIDUE.get())) {
                signals.hasResidue = true;
            } else if (stack.is(ModItems.ATTUNED_RESONANCE.get())) {
                signals.hasAttunedResonance = true;
            } else if (stack.is(ModItems.RESONANCE_CRYSTAL.get())) {
                signals.hasResonanceCrystal = true;
            } else if (stack.is(ModItems.COLLECTOR_COMPASS.get())) {
                signals.hasCompass = true;
            }

            if (signals.isComplete()) {
                break;
            }
        }
        return signals;
    }

    /**
     * Checks a compact area around the player for nearby Collector traces.
     */
    private static boolean isNearTrace(BlockPos center, ServerPlayer player) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
            if (player.serverLevel().getBlockState(pos).is(ModBlocks.COLLECTOR_TRACE.get())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Aggregated inventory flags used to avoid repeated {@code Inventory.contains(...)} scans.
     */
    private static final class InventorySignals {
        private static final InventorySignals EMPTY = new InventorySignals();

        private boolean hasNetherRelic;
        private boolean hasCavernRelic;
        private boolean hasEchoRelic;
        private boolean hasCatalyst;
        private boolean hasUnstableResonance;
        private boolean hasAlembic;
        private boolean hasDistilledResonance;
        private boolean hasResidue;
        private boolean hasAttunedResonance;
        private boolean hasResonanceCrystal;
        private boolean hasCompass;

        private boolean isComplete() {
            return (hasNetherRelic || hasCavernRelic)
                    && hasEchoRelic
                    && hasCatalyst
                    && hasUnstableResonance
                    && hasAlembic
                    && hasDistilledResonance
                    && hasResidue
                    && hasAttunedResonance
                    && hasResonanceCrystal
                    && hasCompass;
        }
    }
}
