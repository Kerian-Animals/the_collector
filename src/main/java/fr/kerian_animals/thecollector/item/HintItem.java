package fr.kerian_animals.thecollector.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class HintItem extends Item {
    private final String loreKey;
    private final String hintKey;

    public HintItem(Properties properties, String loreKey, String hintKey) {
        super(properties);
        this.loreKey = loreKey;
        this.hintKey = hintKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(loreKey).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltipComponents.add(Component.translatable(hintKey).withStyle(ChatFormatting.DARK_AQUA));
    }
}
