package com.knightcode.appliedstoragesorter.client.gui.theme;

/**
 * Central access point for GUI theming.
 * <p>
 * Phase 1: mod screens read {@link GuiThemes#active()} directly.
 * Phase 2: AE2 mixins will call {@link #resolve(GuiTheme)} to optionally override AE2 widget assets
 * without replacing files under {@code assets/ae2/}.
 */
public final class GuiThemeProvider {
    private GuiThemeProvider() {
    }

    public static GuiTheme resolve() {
        return GuiThemes.active();
    }

    /**
     * When false, mixin targets should passthrough to AE2 vanilla assets.
     * Reserved for a future global "theme AE2 native GUIs" toggle.
     */
    public static boolean shouldThemeAe2NativeScreens() {
        return false;
    }
}
