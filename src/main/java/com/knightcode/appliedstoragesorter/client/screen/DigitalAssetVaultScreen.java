package com.knightcode.appliedstoragesorter.client.screen;

import com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DigitalAssetVaultScreen extends AbstractContainerScreen<DigitalAssetVaultMenu> {
    private static final ResourceLocation CONTAINER_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/gui/container/generic_54.png");
    private static final int CONTAINER_ROWS = 2;

    public DigitalAssetVaultScreen(DigitalAssetVaultMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 114 + CONTAINER_ROWS * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        int topSectionHeight = 17 + CONTAINER_ROWS * 18;

        guiGraphics.blit(CONTAINER_TEXTURE, left, top, 0, 0, this.imageWidth, topSectionHeight);
        guiGraphics.blit(CONTAINER_TEXTURE, left, top + topSectionHeight, 0, 126, this.imageWidth, 96);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
