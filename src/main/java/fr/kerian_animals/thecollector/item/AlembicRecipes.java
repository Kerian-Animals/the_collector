package fr.kerian_animals.thecollector.item;

import fr.kerian_animals.thecollector.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class AlembicRecipes {
    private static final AlembicRecipe[] RECIPES = new AlembicRecipe[] {
            new AlembicRecipe(ModItems.UNSTABLE_RESONANCE.get(), Items.ECHO_SHARD, Items.BLAZE_POWDER, ModItems.DISTILLED_RESONANCE.get(), 20 * 15),
            new AlembicRecipe(ModItems.RESONANT_RESIDUE.get(), Items.ENDER_PEARL, Items.BLAZE_POWDER, ModItems.ATTUNED_RESONANCE.get(), 20 * 20)
    };

    private AlembicRecipes() {
    }

    public static List<AlembicRecipe> all() {
        return List.of(RECIPES);
    }

    public static AlembicRecipe find(ItemStack input, ItemStack reagent, ItemStack fuel) {
        for (AlembicRecipe recipe : RECIPES) {
            if (input.is(recipe.input()) && reagent.is(recipe.ingredient()) && fuel.is(recipe.fuel())) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean isValidInput(ItemStack stack) {
        for (AlembicRecipe recipe : RECIPES) {
            if (stack.is(recipe.input())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidReagent(ItemStack stack) {
        for (AlembicRecipe recipe : RECIPES) {
            if (stack.is(recipe.ingredient())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidFuel(ItemStack stack) {
        for (AlembicRecipe recipe : RECIPES) {
            if (stack.is(recipe.fuel())) {
                return true;
            }
        }
        return false;
    }

    public record AlembicRecipe(Item input, Item ingredient, Item fuel, Item output, int processTicks) {
    }
}
