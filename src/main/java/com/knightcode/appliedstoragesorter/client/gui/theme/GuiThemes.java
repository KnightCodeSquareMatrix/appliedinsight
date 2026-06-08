package com.knightcode.appliedstoragesorter.client.gui.theme;

public final class GuiThemes {
    private static GuiTheme active = AppliedDarkMatterV2Theme.INSTANCE;

    private GuiThemes() {
    }

    public static GuiTheme active() {
        return active;
    }

    public static void setActive(GuiTheme theme) {
        active = theme;
    }
}
