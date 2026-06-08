package com.knightcode.appliedstoragesorter.client.screen;

record GuiRect(int x, int y, int width, int height) {
    int right() {
        return x + width;
    }

    int bottom() {
        return y + height;
    }

    GuiRect inset(int value) {
        return inset(value, value);
    }

    GuiRect inset(int horizontal, int vertical) {
        return new GuiRect(
                x + horizontal,
                y + vertical,
                Math.max(0, width - horizontal * 2),
                Math.max(0, height - vertical * 2));
    }

    GuiRect move(int deltaX, int deltaY) {
        return new GuiRect(x + deltaX, y + deltaY, width, height);
    }

    GuiRect topBand(int bandHeight) {
        return new GuiRect(x, y, width, Math.min(height, bandHeight));
    }

    GuiRect bottomBand(int bandHeight) {
        int clampedHeight = Math.min(height, bandHeight);
        return new GuiRect(x, bottom() - clampedHeight, width, clampedHeight);
    }

    boolean contains(int px, int py) {
        return px >= x && px < right() && py >= y && py < bottom();
    }
}
