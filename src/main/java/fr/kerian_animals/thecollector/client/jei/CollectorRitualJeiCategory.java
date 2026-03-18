package fr.kerian_animals.thecollector.client.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class CollectorRitualJeiCategory implements IRecipeCategory<JeiProcessRecipes.CollectorRitualRecipe> {
    private static final int WIDTH = 162;
    private static final int HEIGHT = 74;

    private final IDrawable icon;

    public CollectorRitualJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(JeiProcessRecipes.COLLECTOR_RITUAL.icon());
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<JeiProcessRecipes.CollectorRitualRecipe> getRecipeType() {
        return JeiPluginImpl.RITUAL_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.the_collector.category.ritual");
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
    public void setRecipe(IRecipeLayoutBuilder builder, JeiProcessRecipes.CollectorRitualRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(72, 28)
                .setStandardSlotBackground()
                .addItemStack(recipe.lodestone());
        builder.addInputSlot(18, 28)
                .setStandardSlotBackground()
                .addItemStack(recipe.cryingObsidian());
        builder.addInputSlot(126, 28)
                .setStandardSlotBackground()
                .addItemStack(recipe.soulLantern());
        builder.addInputSlot(72, 2)
                .setStandardSlotBackground()
                .addItemStack(recipe.catalyst());
    }

    @Override
    public void draw(JeiProcessRecipes.CollectorRitualRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("jei.the_collector.category.ritual.step1"),
                0,
                56,
                0x404040,
                false
        );
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("jei.the_collector.category.ritual.step2"),
                0,
                66,
                0x404040,
                false
        );
    }
}
