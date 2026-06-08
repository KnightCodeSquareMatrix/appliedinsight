package com.knightcode.appliedstoragesorter.client.gui.widget;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import com.knightcode.appliedstoragesorter.client.gui.theme.GuiThemeProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Themed clone of AE2 {@link appeng.client.gui.widgets.VerticalButtonBar}.
 */
public class ThemedVerticalButtonBar implements ICompositeWidget {
    private static final int VERTICAL_SPACING = 6;
    private static final int MARGIN = 2;

    private final List<Button> buttons = new ArrayList<>();
    private Point screenOrigin = Point.ZERO;
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);
    private Point position;

    public void add(Button button) {
        buttons.add(button);
    }

    @Override
    public void setPosition(Point position) {
        this.position = position;
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public Rect2i getBounds() {
        return bounds;
    }

    @Override
    public void updateBeforeRender() {
        int currentY = position.getY() + MARGIN;
        int maxWidth = 0;

        for (Button button : buttons) {
            if (!button.visible) {
                continue;
            }
            button.setX(screenOrigin.getX() + position.getX() - MARGIN - button.getWidth());
            button.setY(screenOrigin.getY() + currentY);
            currentY += button.getHeight() + VERTICAL_SPACING;
            maxWidth = Math.max(button.getWidth(), maxWidth);
        }

        if (maxWidth == 0) {
            bounds = new Rect2i(0, 0, 0, 0);
        } else {
            int boundX = position.getX() - maxWidth - 2 * MARGIN;
            int boundY = position.getY();
            bounds = new Rect2i(boundX, boundY, maxWidth + 2 * MARGIN, currentY - boundY);
        }
    }

    @Override
    public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i screenBounds, AEBaseScreen<?> screen) {
        this.screenOrigin = Point.fromTopLeft(screenBounds);
        for (var button : buttons) {
            if (button.isFocused()) {
                button.setFocused(false);
            }
            addWidget.accept(button);
        }
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i screenBounds, Point mouse) {
        var sprite = GuiThemeProvider.resolve().verticalToolbarBackground();
        guiGraphics.blitSprite(
                sprite,
                screenBounds.getX() + bounds.getX() - 2,
                screenBounds.getY() + bounds.getY() - 1,
                1,
                bounds.getWidth() + 1,
                bounds.getHeight() + 4);
    }
}
