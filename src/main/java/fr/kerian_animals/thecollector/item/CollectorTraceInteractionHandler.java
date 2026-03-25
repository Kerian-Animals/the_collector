package fr.kerian_animals.thecollector.item;

import fr.kerian_animals.thecollector.advancement.CollectorAdvancementHelper;
import fr.kerian_animals.thecollector.registry.ModBlocks;
import fr.kerian_animals.thecollector.registry.ModItems;
import fr.kerian_animals.thecollector.world.ResonanceCauldronSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

/**
 * Centralizes the custom item-on-block interactions used by the resonance progression.
 *
 * <p>This handler covers three steps: collecting a trace with a glass bottle, pouring distilled
 * resonance into a cauldron, and harvesting residue with the ladle once the resting timer is
 * complete.</p>
 */
public final class CollectorTraceInteractionHandler {
    @SubscribeEvent
    public void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.BLOCK) {
            return;
        }

        ItemStack heldStack = event.getItemStack();
        if (event.getLevel().getBlockState(event.getPos()).is(ModBlocks.COLLECTOR_TRACE.get()) && heldStack.is(Items.GLASS_BOTTLE)) {
            collectTrace(event, heldStack);
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (event.getLevel().getBlockState(event.getPos()).is(Blocks.CAULDRON) && heldStack.is(ModItems.DISTILLED_RESONANCE.get())) {
            pourIntoCauldron(event, level, heldStack);
            return;
        }

        if (event.getLevel().getBlockState(event.getPos()).is(Blocks.WATER_CAULDRON) && heldStack.is(ModItems.RESONANCE_LADLE.get())) {
            harvestResidue(event, level, heldStack);
        }
    }

    /**
     * Converts a Collector trace block into unstable resonance and grants the corresponding hint.
     */
    private static void collectTrace(UseItemOnBlockEvent event, ItemStack heldStack) {
        event.cancelWithResult(ItemInteractionResult.SUCCESS);

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Player player = event.getPlayer();
        BlockPos pos = event.getPos();
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        if (player != null && !player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        ItemStack unstableResonance = new ItemStack(ModItems.UNSTABLE_RESONANCE.get());
        if (player == null || !player.getInventory().add(unstableResonance)) {
            BlockPos dropPos = pos.above();
            net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    dropPos.getX() + 0.5D,
                    dropPos.getY() + 0.2D,
                    dropPos.getZ() + 0.5D,
                    unstableResonance
            );
            level.addFreshEntity(entity);
        }

        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D, 5, 0.15D, 0.04D, 0.15D, 0.003D);
        level.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5D, pos.getY() + 0.12D, pos.getZ() + 0.5D, 8, 0.2D, 0.05D, 0.2D, 0.0D);
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.BLOCKS, 0.8F, 1.15F);
        if (player instanceof ServerPlayer serverPlayer) {
            CollectorAdvancementHelper.award(serverPlayer, "bottled_disturbance");
            serverPlayer.sendSystemMessage(Component.translatable("hint.the_collector.bottled_disturbance")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    /**
     * Starts the cauldron resting phase used before residue can be harvested.
     */
    private static void pourIntoCauldron(UseItemOnBlockEvent event, ServerLevel level, ItemStack heldStack) {
        ResonanceCauldronSavedData data = ResonanceCauldronSavedData.get(level);
        BlockPos pos = event.getPos();
        if (!data.startResting(level, pos)) {
            return;
        }

        event.cancelWithResult(ItemInteractionResult.SUCCESS);
        level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState(), 3);

        Player player = event.getPlayer();
        if (player != null && !player.getAbilities().instabuild) {
            heldStack.shrink(1);
            player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
        }

        level.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D, 12, 0.2D, 0.12D, 0.2D, 0.0D);
        level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.8F, 0.9F);
        if (player instanceof ServerPlayer serverPlayer) {
            CollectorAdvancementHelper.award(serverPlayer, "patient_crystallization");
            serverPlayer.sendSystemMessage(Component.translatable("hint.the_collector.patient_crystallization")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    /**
     * Finishes the cauldron loop by consuming the resting state and producing resonant residue.
     */
    private static void harvestResidue(UseItemOnBlockEvent event, ServerLevel level, ItemStack heldStack) {
        ResonanceCauldronSavedData data = ResonanceCauldronSavedData.get(level);
        BlockPos pos = event.getPos();
        if (!data.isResting(level, pos)) {
            return;
        }

        event.cancelWithResult(ItemInteractionResult.SUCCESS);
        Player player = event.getPlayer();

        if (!data.isReady(level, pos)) {
            if (player != null) {
                int remainingSeconds = Math.max(1, data.remainingTicks(level, pos) / 20);
                player.sendSystemMessage(Component.translatable(
                        "item.the_collector.resonance_ladle.not_ready",
                        remainingSeconds
                ).withStyle(ChatFormatting.GRAY));
            }
            return;
        }

        data.clear(level, pos);
        level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);

        ItemStack residue = new ItemStack(ModItems.RESONANT_RESIDUE.get(), 3);
        if (player == null || !player.getInventory().add(residue)) {
            net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    pos.getX() + 0.5D,
                    pos.getY() + 1.0D,
                    pos.getZ() + 0.5D,
                    residue
            );
            level.addFreshEntity(entity);
        }

        if (player != null) {
            heldStack.hurtAndBreak(1, player, Player.getSlotForHand(event.getHand()));
        }

        level.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 0.95D, pos.getZ() + 0.5D, 10, 0.18D, 0.1D, 0.18D, 0.0D);
        level.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5D, pos.getY() + 0.95D, pos.getZ() + 0.5D, 16, 0.18D, 0.1D, 0.18D, 0.0D);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.7F, 1.35F);
        if (player instanceof ServerPlayer serverPlayer) {
            CollectorAdvancementHelper.award(serverPlayer, "what_remains");
            serverPlayer.sendSystemMessage(Component.translatable("hint.the_collector.what_remains")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}
