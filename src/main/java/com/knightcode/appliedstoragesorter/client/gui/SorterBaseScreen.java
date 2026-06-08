package com.knightcode.appliedstoragesorter.client.gui;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.AEBaseMenu;
import com.knightcode.appliedstoragesorter.client.gui.theme.GuiThemeProvider;
import com.knightcode.appliedstoragesorter.client.gui.widget.ThemedOpenGuideButton;
import com.knightcode.appliedstoragesorter.client.gui.widget.ThemedVerticalButtonBar;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class SorterBaseScreen<T extends AEBaseMenu> extends AEBaseScreen<T> {
    private final ThemedVerticalButtonBar themedToolbar = new ThemedVerticalButtonBar();
    protected final ThemedOpenGuideButton themedHelpButton;

    protected SorterBaseScreen(T menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        themedHelpButton = new ThemedOpenGuideButton(btn -> openHelp());
        themedToolbar.add(themedHelpButton);
        widgets.add("verticalToolbar", themedToolbar);
    }

    @Override
    protected boolean shouldAddToolbar() {
        return false;
    }

    @Override
    protected void updateBeforeRender() {
        themedHelpButton.setVisibility(getHelpTopic() != null);
    }

    protected int themedTextColor() {
        return GuiThemeProvider.resolve().palette()
                .get(appeng.client.gui.style.PaletteColor.DEFAULT_TEXT_COLOR)
                .toARGB();
    }

    protected int themedMutedTextColor() {
        return GuiThemeProvider.resolve().palette()
                .get(appeng.client.gui.style.PaletteColor.MUTED_TEXT_COLOR)
                .toARGB();
    }
}
