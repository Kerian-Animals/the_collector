package fr.harmonia.thecollector.loot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ItemValueHelper {
    private ItemValueHelper() {
    }

    public static int score(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        int base = switch (stack.getItem()) {
            case net.minecraft.world.item.Item item when item == Items.NETHERITE_INGOT -> 100;
            case net.minecraft.world.item.Item item when item == Items.DIAMOND -> 80;
            case net.minecraft.world.item.Item item when item == Items.EMERALD -> 70;
            case net.minecraft.world.item.Item item when item == Items.GOLD_INGOT -> 50;
            case net.minecraft.world.item.Item item when item == Items.IRON_INGOT -> 30;
            default -> 0;
        };

        if (base == 0) {
            if (stack.is(ItemTags.COALS)) {
                base = 10;
            } else if (stack.is(ItemTags.PLANKS) || stack.is(ItemTags.LOGS)) {
                base = 2;
            } else if (stack.is(ItemTags.COPPER_ORES)) {
                base = 20;
            }
        }

        if (stack.has(DataComponents.ENCHANTMENTS) || stack.has(DataComponents.STORED_ENCHANTMENTS)) {
            base += 40;
        }
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            base += 20;
        }
        if (stack.is(Items.NETHERITE_SWORD) || stack.is(Items.NETHERITE_PICKAXE) || stack.is(Items.NETHERITE_CHESTPLATE)) {
            base = Math.max(base, 120);
        }

        return Math.max(base, 0);
    }

    public static boolean isInteresting(ItemStack stack) {
        return score(stack) >= 15;
    }
}
