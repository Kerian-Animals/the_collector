package fr.kerian_animals.thecollector.loot;

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

        int base = 0;
        if (stack.is(Items.NETHERITE_INGOT)) {
            base = 100;
        } else if (stack.is(Items.DIAMOND)) {
            base = 80;
        } else if (stack.is(Items.EMERALD)) {
            base = 70;
        } else if (stack.is(Items.GOLD_INGOT)) {
            base = 50;
        } else if (stack.is(Items.IRON_INGOT)) {
            base = 30;
        }

        if (base == 0) {
            if (stack.is(ItemTags.COALS)) {
                base = 10;
            } else if (stack.is(ItemTags.PLANKS) || stack.is(ItemTags.LOGS)) {
                base = 2;
            } else if (stack.is(Items.RAW_COPPER)) {
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

