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

public final class TraceCaptureJeiCategory implements IRecipeCategory<JeiProcessRecipes.TraceCaptureRecipe> {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 56;

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public TraceCaptureJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(JeiProcessRecipes.TRACE_CAPTURE.icon());
        this.arrow = guiHelper.createAnimatedRecipeArrow(80);
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<JeiProcessRecipes.TraceCaptureRecipe> getRecipeType() {
        return JeiPluginImpl.TRACE_CAPTURE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.the_collector.category.trace_capture");
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
    public void setRecipe(IRecipeLayoutBuilder builder, JeiProcessRecipes.TraceCaptureRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(0, 19)
                .setStandardSlotBackground()
                .addItemStack(recipe.trace());
        builder.addInputSlot(26, 19)
                .setStandardSlotBackground()
                .addItemStack(recipe.bottle());
        builder.addOutputSlot(82, 19)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(JeiProcessRecipes.TraceCaptureRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 53, 19);
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("jei.the_collector.category.trace_capture.step"),
                0,
                0,
                0x404040,
                false
        );
    }
}
