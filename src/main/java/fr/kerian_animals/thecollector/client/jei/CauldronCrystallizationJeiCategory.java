package fr.kerian_animals.thecollector.client.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class CauldronCrystallizationJeiCategory implements IRecipeCategory<JeiProcessRecipes.CauldronCrystallizationRecipe> {
    private static final int WIDTH = 134;
    private static final int HEIGHT = 60;

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public CauldronCrystallizationJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(JeiProcessRecipes.CAULDRON_CRYSTALLIZATION.icon());
        this.arrow = guiHelper.createAnimatedRecipeArrow(200);
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<JeiProcessRecipes.CauldronCrystallizationRecipe> getRecipeType() {
        return JeiPluginImpl.CAULDRON_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.the_collector.category.cauldron");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiProcessRecipes.CauldronCrystallizationRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(0, 19)
                .setStandardSlotBackground()
                .addItemStack(recipe.input());
        builder.addInputSlot(26, 19)
                .setStandardSlotBackground()
                .addItemStack(recipe.cauldron());
        builder.addInputSlot(52, 19)
                .setStandardSlotBackground()
                .addItemStack(recipe.tool());
        builder.addOutputSlot(108, 19)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(JeiProcessRecipes.CauldronCrystallizationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 79, 19);
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("jei.the_collector.category.cauldron.time", recipe.restingSeconds()),
                0,
                0,
                0x404040,
                false
        );
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("jei.the_collector.category.cauldron.step"),
                0,
                48,
                0x606060,
                false
        );
    }
}
