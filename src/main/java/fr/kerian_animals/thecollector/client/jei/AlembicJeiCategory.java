package fr.kerian_animals.thecollector.client.jei;

import fr.kerian_animals.thecollector.item.AlembicRecipes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class AlembicJeiCategory implements IRecipeCategory<AlembicRecipes.AlembicRecipe> {
    private static final int WIDTH = 116;
    private static final int HEIGHT = 56;

    private final IDrawable icon;
    private final IDrawableAnimated arrow;
    private final IDrawableAnimated flame;

    public AlembicJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(fr.kerian_animals.thecollector.registry.ModBlocks.ALEMBIC.get());
        this.arrow = guiHelper.createAnimatedRecipeArrow(200);
        this.flame = guiHelper.createAnimatedRecipeFlame(200);
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<AlembicRecipes.AlembicRecipe> getRecipeType() {
        return JeiPluginImpl.ALEMBIC_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.the_collector.category.alembic");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AlembicRecipes.AlembicRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(0, 19)
                .setStandardSlotBackground()
                .addItemStack(recipe.input().getDefaultInstance());
        builder.addInputSlot(26, 1)
                .setStandardSlotBackground()
                .addItemStack(recipe.ingredient().getDefaultInstance());
        builder.addInputSlot(26, 37)
                .setStandardSlotBackground()
                .addItemStack(recipe.fuel().getDefaultInstance());
        builder.addOutputSlot(94, 19)
                .setOutputSlotBackground()
                .addItemStack(recipe.output().getDefaultInstance());
    }

    @Override
    public void draw(AlembicRecipes.AlembicRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        flame.draw(guiGraphics, 51, 37);
        arrow.draw(guiGraphics, 51, 19);
        guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                Component.translatable("jei.the_collector.category.alembic.time", recipe.processTicks() / 20),
                0,
                0,
                0x404040,
                false
        );
    }
}
