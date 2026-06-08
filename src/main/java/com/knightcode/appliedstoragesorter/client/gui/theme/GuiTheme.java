package com.knightcode.appliedstoragesorter.client.gui.theme;

import appeng.client.gui.style.Color;
import appeng.client.gui.style.PaletteColor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Describes themed GUI assets for Applied Insight screens.
 * Phase 2 mixins into AE2 will resolve the same interface for native AE2 widgets.
 */
public interface GuiTheme {
    String id();

    WidgetSprites actionButtonSprites();

    ResourceLocation verticalToolbarBackground();

    ResourceLocation statesAtlas();

    ResourceLocation tiledBackground();

    Map<PaletteColor, Color> palette();

    int actionButtonTextColor(boolean active, boolean hovered);

    int statusOnlineColor();

    int statusWarningColor();

    int statusOfflineColor();
}
