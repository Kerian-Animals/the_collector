package fr.kerian_animals.thecollector.item;

import fr.kerian_animals.thecollector.advancement.CollectorAdvancementHelper;
import fr.kerian_animals.thecollector.stash.CollectorEntry;
import fr.kerian_animals.thecollector.stash.CollectorSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

/**
 * Activates a discovered Collector entry when the player completes the required ritual structure.
 */
public class CollectorCatalystItem extends Item {
    public CollectorCatalystItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clicked = context.getClickedPos();
        if (!level.getBlockState(clicked).is(Blocks.LODESTONE)) {
            return InteractionResult.PASS;
        }

        CollectorSavedData data = CollectorSavedData.get(level);
        Optional<CollectorEntry> entryOptional = data.getEntryAt(clicked, 1);
        if (entryOptional.isEmpty()) {
            player.sendSystemMessage(Component.translatable("item.the_collector.collector_catalyst.not_entry")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }

        CollectorEntry entry = entryOptional.get();
        if (entry.activated()) {
            player.sendSystemMessage(Component.translatable("item.the_collector.collector_catalyst.already_active")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }
        if (!isRitualStructureValid(level, clicked)) {
            player.sendSystemMessage(Component.translatable("item.the_collector.collector_catalyst.invalid_structure")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        data.setEntryActivated(entry.id(), true);
        context.getItemInHand().shrink(1);
        triggerActivationDanger(level, clicked, player);
        CollectorAdvancementHelper.award(player, "the_stone_listens");

        player.sendSystemMessage(Component.translatable("item.the_collector.collector_catalyst.activated")
                .withStyle(ChatFormatting.GOLD));
        return InteractionResult.SUCCESS;
    }

    private static boolean isRitualStructureValid(ServerLevel level, BlockPos center) {
        if (!level.getBlockState(center.offset(1, 0, 0)).is(Blocks.CRYING_OBSIDIAN)) return false;
        if (!level.getBlockState(center.offset(-1, 0, 0)).is(Blocks.CRYING_OBSIDIAN)) return false;
        if (!level.getBlockState(center.offset(0, 0, 1)).is(Blocks.CRYING_OBSIDIAN)) return false;
        if (!level.getBlockState(center.offset(0, 0, -1)).is(Blocks.CRYING_OBSIDIAN)) return false;

        if (!level.getBlockState(center.offset(2, 1, 0)).is(Blocks.SOUL_LANTERN)) return false;
        if (!level.getBlockState(center.offset(-2, 1, 0)).is(Blocks.SOUL_LANTERN)) return false;
        if (!level.getBlockState(center.offset(0, 1, 2)).is(Blocks.SOUL_LANTERN)) return false;
        if (!level.getBlockState(center.offset(0, 1, -2)).is(Blocks.SOUL_LANTERN)) return false;

        return true;
    }

    private static void triggerActivationDanger(ServerLevel level, BlockPos center, ServerPlayer activator) {
        level.playSound(null, center, SoundEvents.WARDEN_NEARBY_CLOSE, SoundSource.HOSTILE, 1.0F, 0.75F);
        level.playSound(null, center, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.0F, 0.65F);

        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI * 2.0D / 4.0D) * i;
            BlockPos spawnPos = center.offset((int) Math.round(Math.cos(angle) * 3.0D), 0, (int) Math.round(Math.sin(angle) * 3.0D));
            net.minecraft.world.entity.monster.WitherSkeleton skeleton =
                    net.minecraft.world.entity.EntityType.WITHER_SKELETON.create(level);
            if (skeleton != null) {
                skeleton.moveTo(spawnPos, level.random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(skeleton);
            }
        }

        net.minecraft.world.entity.monster.EnderMan enderman =
                net.minecraft.world.entity.EntityType.ENDERMAN.create(level);
        if (enderman != null) {
            enderman.moveTo(center.offset(0, 0, 4), level.random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(enderman);
        }

        activator.hurt(activator.damageSources().magic(), 4.0F);
    }
}
