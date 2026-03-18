package fr.kerian_animals.thecollector.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ResonanceItem extends Item {
    private final String loreKey;
    private final String hintKey;
    private final boolean emitsParticles;

    public ResonanceItem(Properties properties, String loreKey, boolean emitsParticles) {
        this(properties, loreKey, null, emitsParticles);
    }

    public ResonanceItem(Properties properties, String loreKey, String hintKey, boolean emitsParticles) {
        super(properties);
        this.loreKey = loreKey;
        this.hintKey = hintKey;
        this.emitsParticles = emitsParticles;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(loreKey).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        if (hintKey != null) {
            tooltipComponents.add(Component.translatable(hintKey).withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!emitsParticles || level.isClientSide || !isSelected || entity.tickCount % 12 != 0) {
            return;
        }

        level.addParticle(
                ParticleTypes.SOUL,
                entity.getX(),
                entity.getY() + 1.0D,
                entity.getZ(),
                0.0D,
                0.02D,
                0.0D
        );
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!emitsParticles || entity.level().isClientSide || entity.tickCount % 14 != 0) {
            return false;
        }

        entity.level().addParticle(
                ParticleTypes.SOUL,
                entity.getX(),
                entity.getY() + 0.15D,
                entity.getZ(),
                0.0D,
                0.01D,
                0.0D
        );
        return false;
    }
}
