package com.knightcode.appliedstoragesorter.client.gui.theme;

import appeng.client.gui.style.Color;
import appeng.client.gui.style.PaletteColor;
import appeng.core.AppEng;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

/** Passthrough theme that keeps AE2's default asset paths (for mixin default behavior). */
public final class Ae2VanillaTheme implements GuiTheme {
    public static final Ae2VanillaTheme INSTANCE = new Ae2VanillaTheme();

    private static final WidgetSprites SPRITES = new WidgetSprites(
            AppEng.makeId("button"),
            AppEng.makeId("button_disabled"),
            AppEng.makeId("button_highlighted"));

    private static final Map<PaletteColor, Color> PALETTE = Map.of(
            PaletteColor.DEFAULT_TEXT_COLOR, Color.parse("#413f54"),
            PaletteColor.MUTED_TEXT_COLOR, Color.parse("#878fa5"),
            PaletteColor.SELECTION_COLOR, Color.parse("#ace9ff"),
            PaletteColor.TEXTFIELD_PLACEHOLDER, Color.parse("#dedfe3"),
            PaletteColor.TEXTFIELD_SELECTION, Color.parse("#FF0000FF"),
            PaletteColor.TEXTFIELD_ERROR, Color.parse("#FF1900"),
            PaletteColor.TEXTFIELD_TEXT, Color.parse("#f2f2f2"),
            PaletteColor.ERROR, Color.parse("#CE2401"));

    private Ae2VanillaTheme() {
    }

    @Override
    public String id() {
        return "ae2_vanilla";
    }

    @Override
    public WidgetSprites actionButtonSprites() {
        return SPRITES;
    }

    @Override
    public ResourceLocation verticalToolbarBackground() {
        return AppEng.makeId("vertical_buttons_bg");
    }

    @Override
    public ResourceLocation statesAtlas() {
        return AppEng.makeId("textures/guis/states.png");
    }

    @Override
    public ResourceLocation tiledBackground() {
        return AppEng.makeId("textures/guis/background.png");
    }

    @Override
    public Map<PaletteColor, Color> palette() {
        return PALETTE;
    }

    @Override
    public int actionButtonTextColor(boolean active, boolean hovered) {
        if (!active) {
            return 0x413f54;
        }
        return hovered ? 0x517497 : 0xf2f2f2;
    }

    @Override
    public int statusOnlineColor() {
        return 0x73C99B;
    }

    @Override
    public int statusWarningColor() {
        return 0xD6BB63;
    }

    @Override
    public int statusOfflineColor() {
        return 0xC87474;
    }
}
