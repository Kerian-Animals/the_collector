package fr.kerian_animals.thecollector.client.jei;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.client.AlembicScreen;
import fr.kerian_animals.thecollector.item.AlembicRecipes;
import fr.kerian_animals.thecollector.registry.ModBlocks;
import fr.kerian_animals.thecollector.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@JeiPlugin
public final class JeiPluginImpl implements IModPlugin {
    public static final RecipeType<AlembicRecipes.AlembicRecipe> ALEMBIC_RECIPE_TYPE =
            RecipeType.create(TheCollectorMod.MOD_ID, "alembic", AlembicRecipes.AlembicRecipe.class);
    public static final RecipeType<JeiProcessRecipes.CauldronCrystallizationRecipe> CAULDRON_RECIPE_TYPE =
            RecipeType.create(TheCollectorMod.MOD_ID, "cauldron_crystallization", JeiProcessRecipes.CauldronCrystallizationRecipe.class);
    public static final RecipeType<JeiProcessRecipes.CollectorRitualRecipe> RITUAL_RECIPE_TYPE =
            RecipeType.create(TheCollectorMod.MOD_ID, "collector_ritual", JeiProcessRecipes.CollectorRitualRecipe.class);
    public static final RecipeType<JeiProcessRecipes.TraceCaptureRecipe> TRACE_CAPTURE_RECIPE_TYPE =
            RecipeType.create(TheCollectorMod.MOD_ID, "trace_capture", JeiProcessRecipes.TraceCaptureRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(TheCollectorMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new AlembicJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new CauldronCrystallizationJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new CollectorRitualJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new TraceCaptureJeiCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(ALEMBIC_RECIPE_TYPE, AlembicRecipes.all());
        registration.addRecipes(CAULDRON_RECIPE_TYPE, JeiProcessRecipes.cauldronRecipes());
        registration.addRecipes(RITUAL_RECIPE_TYPE, JeiProcessRecipes.ritualRecipes());
        registration.addRecipes(TRACE_CAPTURE_RECIPE_TYPE, JeiProcessRecipes.traceCaptureRecipes());

        registration.addIngredientInfo(
                new ItemStack(ModItems.DISTILLED_RESONANCE.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.the_collector.info.distilled_resonance.1"),
                Component.translatable("jei.the_collector.info.distilled_resonance.2")
        );
        registration.addIngredientInfo(
                new ItemStack(ModItems.RESONANCE_LADLE.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.the_collector.info.resonance_ladle.1"),
                Component.translatable("jei.the_collector.info.resonance_ladle.2")
        );
        registration.addIngredientInfo(
                new ItemStack(ModItems.COLLECTOR_CATALYST.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.the_collector.info.collector_catalyst.1"),
                Component.translatable("jei.the_collector.info.collector_catalyst.2"),
                Component.translatable("jei.the_collector.info.collector_catalyst.3")
        );
        registration.addIngredientInfo(
                new ItemStack(Items.LODESTONE),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.the_collector.info.lodestone.1"),
                Component.translatable("jei.the_collector.info.lodestone.2")
        );
        registration.addIngredientInfo(
                new ItemStack(ModItems.UNSTABLE_RESONANCE.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.the_collector.info.unstable_resonance.1"),
                Component.translatable("jei.the_collector.info.unstable_resonance.2")
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.ALEMBIC.get(), ALEMBIC_RECIPE_TYPE);
        registration.addRecipeCatalyst(Items.CAULDRON, CAULDRON_RECIPE_TYPE);
        registration.addRecipeCatalyst(ModItems.RESONANCE_LADLE.get(), CAULDRON_RECIPE_TYPE);
        registration.addRecipeCatalyst(ModItems.COLLECTOR_CATALYST.get(), RITUAL_RECIPE_TYPE);
        registration.addRecipeCatalyst(Items.LODESTONE, RITUAL_RECIPE_TYPE);
        registration.addRecipeCatalyst(ModItems.COLLECTOR_TRACE.get(), TRACE_CAPTURE_RECIPE_TYPE);
        registration.addRecipeCatalyst(Items.GLASS_BOTTLE, TRACE_CAPTURE_RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AlembicScreen.class, 97, 16, 9, 28, ALEMBIC_RECIPE_TYPE);
    }
}
