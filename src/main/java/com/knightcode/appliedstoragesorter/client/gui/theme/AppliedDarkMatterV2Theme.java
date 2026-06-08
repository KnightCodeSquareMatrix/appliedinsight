package com.knightcode.appliedstoragesorter.client.gui.theme;

import appeng.client.gui.style.Color;
import appeng.client.gui.style.PaletteColor;
import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

public final class AppliedDarkMatterV2Theme implements GuiTheme {
    public static final AppliedDarkMatterV2Theme INSTANCE = new AppliedDarkMatterV2Theme();

    private static final ResourceLocation NS = ResourceLocation.fromNamespaceAndPath(
            AppliedStorageSorter.MODID, "button");

    private static final WidgetSprites SPRITES = new WidgetSprites(
            NS,
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "button_disabled"),
            ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "button_highlighted"));

    private static final Map<PaletteColor, Color> PALETTE = new EnumMap<>(PaletteColor.class);

    static {
        PALETTE.put(PaletteColor.DEFAULT_TEXT_COLOR, Color.parse("#EAFBFF"));
        PALETTE.put(PaletteColor.MUTED_TEXT_COLOR, Color.parse("#87AFC0"));
        PALETTE.put(PaletteColor.SELECTION_COLOR, Color.parse("#2FA7C0"));
        PALETTE.put(PaletteColor.TEXTFIELD_PLACEHOLDER, Color.parse("#87AFC0"));
        PALETTE.put(PaletteColor.TEXTFIELD_SELECTION, Color.parse("#802FA7C0"));
        PALETTE.put(PaletteColor.TEXTFIELD_ERROR, Color.parse("#E55C5C"));
        PALETTE.put(PaletteColor.TEXTFIELD_TEXT, Color.parse("#EAFBFF"));
        PALETTE.put(PaletteColor.ERROR, Color.parse("#E55C5C"));
    }

    private AppliedDarkMatterV2Theme() {
    }

    @Override
    public String id() {
        return "applied_dark_matter_v2";
    }

    @Override
    public WidgetSprites actionButtonSprites() {
        return SPRITES;
    }

    @Override
    public ResourceLocation verticalToolbarBackground() {
        return ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "vertical_buttons_bg");
    }

    @Override
    public ResourceLocation statesAtlas() {
        return ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "textures/gui/states.png");
    }

    @Override
    public ResourceLocation tiledBackground() {
        return ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "textures/gui/background.png");
    }

    @Override
    public Map<PaletteColor, Color> palette() {
        return PALETTE;
    }

    @Override
    public int actionButtonTextColor(boolean active, boolean hovered) {
        if (!active) {
            return 0x4F7188;
        }
        return hovered ? 0x8FF1FF : 0xEAFBFF;
    }

    @Override
    public int statusOnlineColor() {
        return 0x36BCD5;
    }

    @Override
    public int statusWarningColor() {
        return 0xD6A343;
    }

    @Override
    public int statusOfflineColor() {
        return 0xE55C5C;
    }
}
