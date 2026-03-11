package fr.harmonia.thecollector.item;

import fr.harmonia.thecollector.stash.CollectorSavedData;
import fr.harmonia.thecollector.stash.CollectorStash;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class CollectorCompassItem extends Item {
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
        Optional<CollectorStash> stashOptional = data.getLastStashForPlayer(serverPlayer.getUUID());

        if (stashOptional.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.translatable("item.the_collector.collector_compass.no_stash")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.consume(stack);
        }

        CollectorStash stash = stashOptional.get();
        serverPlayer.sendSystemMessage(Component.translatable(
                        "item.the_collector.collector_compass.found",
                        stash.pos().getX(),
                        stash.pos().getY(),
                        stash.pos().getZ(),
                        stash.dimension().location().toString())
                .withStyle(ChatFormatting.GOLD));
        serverPlayer.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.consume(stack);
    }
}
