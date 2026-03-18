package fr.kerian_animals.thecollector.client;

import fr.kerian_animals.thecollector.menu.AlembicMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class AlembicScreen extends AbstractContainerScreen<AlembicMenu> {
    private static final ResourceLocation FUEL_LENGTH_SPRITE =
            ResourceLocation.withDefaultNamespace("container/brewing_stand/fuel_length");
    private static final ResourceLocation BREW_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("container/brewing_stand/brew_progress");
    private static final ResourceLocation BUBBLES_SPRITE =
            ResourceLocation.withDefaultNamespace("container/brewing_stand/bubbles");
    private static final ResourceLocation BREWING_STAND_LOCATION =
            ResourceLocation.withDefaultNamespace("textures/gui/container/brewing_stand.png");
    private static final int[] BUBBLE_LENGTHS = new int[] {29, 24, 20, 16, 11, 6, 0};

    public AlembicScreen(AlembicMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BREWING_STAND_LOCATION, left, top, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int fuel = Mth.clamp((18 * this.menu.getFuel() + 20 - 1) / 20, 0, 18);
        if (fuel > 0) {
            guiGraphics.blitSprite(FUEL_LENGTH_SPRITE, 18, 4, 0, 0, left + 60, top + 44, fuel, 4);
        }

        int brewingTicks = this.menu.getBrewingTicks();
        if (brewingTicks > 0) {
            int progress = (int) (28.0F * (1.0F - brewingTicks / 400.0F));
            if (progress > 0) {
                guiGraphics.blitSprite(BREW_PROGRESS_SPRITE, 9, 28, 0, 0, left + 97, top + 16, 9, progress);
            }

            int bubbleLength = BUBBLE_LENGTHS[(brewingTicks / 2) % 7];
            if (bubbleLength > 0) {
                guiGraphics.blitSprite(BUBBLES_SPRITE, 12, 29, 0, 29 - bubbleLength, left + 63, top + 14 + 29 - bubbleLength, 12, bubbleLength);
            }
        }
    }
}
