package com.knightcode.appliedstoragesorter.client.gui.theme;

import appeng.client.gui.style.Blitter;
import net.minecraft.resources.ResourceLocation;

/**
 * Mirror of AE2 {@link appeng.client.gui.Icon} UV coordinates against the themed states atlas.
 */
public enum SorterIcon {
    HELP(176, 0, 16, 16),
    TOOLBAR_BUTTON_BACKGROUND(176, 128, 18, 20),
    TOOLBAR_BUTTON_BACKGROUND_FOCUS(194, 128, 18, 20),
    TOOLBAR_BUTTON_BACKGROUND_HOVER(212, 128, 18, 20);

    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    SorterIcon(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Blitter getBlitter(GuiTheme theme) {
        ResourceLocation texture = theme.statesAtlas();
        return Blitter.texture(texture, TEXTURE_WIDTH, TEXTURE_HEIGHT).src(x, y, width, height);
    }
}
