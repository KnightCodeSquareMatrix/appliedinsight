package com.knightcode.appliedstoragesorter.client.gui.widget;

import appeng.client.gui.widgets.IconButton;
import appeng.core.localization.ButtonToolTips;
import com.knightcode.appliedstoragesorter.client.gui.theme.GuiThemeProvider;
import com.knightcode.appliedstoragesorter.client.gui.theme.SorterIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ThemedOpenGuideButton extends IconButton {
    public ThemedOpenGuideButton(OnPress onPress) {
        super(onPress);
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(
                ButtonToolTips.OpenGuide.text(),
                ButtonToolTips.OpenGuideDetail.text());
    }

    @Override
    protected appeng.client.gui.Icon getIcon() {
        return null;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        if (!this.visible) {
            return;
        }

        var theme = GuiThemeProvider.resolve();
        int yOffset = isHovered() ? 1 : 0;
        SorterIcon bgIcon = isHovered()
                ? SorterIcon.TOOLBAR_BUTTON_BACKGROUND_HOVER
                : isFocused()
                        ? SorterIcon.TOOLBAR_BUTTON_BACKGROUND_FOCUS
                        : SorterIcon.TOOLBAR_BUTTON_BACKGROUND;

        bgIcon.getBlitter(theme)
                .dest(getX() - 1, getY() + yOffset, 18, 20)
                .zOffset(2)
                .blit(guiGraphics);
        SorterIcon.HELP.getBlitter(theme)
                .dest(getX(), getY() + 1 + yOffset)
                .zOffset(3)
                .blit(guiGraphics);
    }
}
