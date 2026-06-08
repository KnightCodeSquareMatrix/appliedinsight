package com.knightcode.appliedstoragesorter.client.gui.widget;

import appeng.client.gui.widgets.AE2Button;
import com.knightcode.appliedstoragesorter.client.gui.theme.GuiThemeProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ThemedAE2Button extends AE2Button {
    public ThemedAE2Button(int x, int y, int width, int height, Component component, OnPress onPress) {
        super(x, y, width, height, component, onPress);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var theme = GuiThemeProvider.resolve();
        var sprites = theme.actionButtonSprites();
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blitSprite(sprites.get(this.active, this.isHovered()), this.getX(), this.getY(), this.getWidth(),
                this.getHeight());
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        int color = theme.actionButtonTextColor(this.active, this.isHovered());
        if (!this.active) {
            renderThemedButtonText(guiGraphics, minecraft.font, 2,
                    color | Mth.ceil(this.alpha * 255.0F) << 24, -1);
        } else if (this.isHovered()) {
            renderThemedButtonText(guiGraphics, minecraft.font, 2,
                    color | Mth.ceil(this.alpha * 255.0F) << 24, 0);
        } else {
            renderThemedButtonText(guiGraphics, minecraft.font, 2,
                    color | Mth.ceil(this.alpha * 255.0F) << 24, 1);
        }
    }

    private void renderThemedButtonText(GuiGraphics guiGraphics, Font font, int width, int color, int yOffset) {
        int minX = this.getX() + width;
        int maxX = this.getX() + this.getWidth() - width;
        AE2Button.renderButtonText(guiGraphics, font, this.getMessage(), minX, this.getY(), maxX,
                this.getY() + this.getHeight(), yOffset, color);
    }
}
