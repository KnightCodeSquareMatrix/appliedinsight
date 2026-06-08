package com.knightcode.appliedstoragesorter.client.screen;

record SorterTerminalLayout(
        GuiRect panel,
        GuiRect header,
        GuiRect footer,
        GuiRect guideToolbarBackground,
        GuiRect leftColumn,
        GuiRect middleColumn,
        GuiRect rightColumn,
        GuiRect analysisButton,
        GuiRect autoSortButton,
        GuiRect mergeButton,
        int guideButtonX,
        int guideButtonY,
        int titleLabelX,
        int statusRightX) {
    static final int PANEL_WIDTH = 440;
    static final int PANEL_HEIGHT = 224;

    private static final int HEADER_HEIGHT = 20;
    private static final int FOOTER_HEIGHT = 16;
    private static final int HORIZONTAL_GAP = 8;
    private static final int VERTICAL_GAP = 6;
    private static final int LEFT_COL_WIDTH = 120;
    private static final int MID_COL_WIDTH = 140;
    private static final int RIGHT_COL_WIDTH = 148;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_GAP = 12;
    private static final int BUTTONS_TOP_PADDING = 30;
    private static final int BUTTONS_BOTTOM_PADDING = 18;
    private static final int AE2_TOOLBAR_LEFT = 3;
    private static final int AE2_TOOLBAR_TOP = 1;
    private static final int AE2_TOOLBAR_MARGIN = 2;
    private static final int AE2_TOOLBAR_VERTICAL_SPACING = 6;
    private static final int TITLE_LABEL_X = 8;

    static SorterTerminalLayout of(int leftPos, int topPos, int guideButtonWidth, int guideButtonHeight) {
        GuiRect panel = new GuiRect(leftPos, topPos, PANEL_WIDTH, PANEL_HEIGHT);
        GuiRect header = panel.topBand(HEADER_HEIGHT).inset(1, 1);
        GuiRect footer = panel.bottomBand(FOOTER_HEIGHT).inset(1, 1);

        int contentTop = topPos + HEADER_HEIGHT + VERTICAL_GAP;
        int contentBottom = topPos + PANEL_HEIGHT - FOOTER_HEIGHT - VERTICAL_GAP;
        int contentHeight = contentBottom - contentTop;

        GuiRect leftColumn = new GuiRect(leftPos + HORIZONTAL_GAP, contentTop, LEFT_COL_WIDTH, contentHeight);
        GuiRect middleColumn = new GuiRect(leftColumn.right() + HORIZONTAL_GAP, contentTop, MID_COL_WIDTH, contentHeight);
        GuiRect rightColumn = new GuiRect(middleColumn.right() + HORIZONTAL_GAP, contentTop, RIGHT_COL_WIDTH, contentHeight);

        int buttonX = leftColumn.x() + Math.max(0, (leftColumn.width() - BUTTON_WIDTH) / 2);
        int buttonBlockHeight = BUTTON_HEIGHT * 3 + BUTTON_GAP * 2;
        int availableButtonSpace = Math.max(0, leftColumn.height() - BUTTONS_TOP_PADDING - BUTTONS_BOTTOM_PADDING);
        int buttonStartY = leftColumn.y() + BUTTONS_TOP_PADDING + Math.max(0, (availableButtonSpace - buttonBlockHeight) / 2);

        GuiRect analysisButton = new GuiRect(buttonX, buttonStartY, BUTTON_WIDTH, BUTTON_HEIGHT);
        GuiRect autoSortButton = analysisButton.move(0, BUTTON_HEIGHT + BUTTON_GAP);
        GuiRect mergeButton = autoSortButton.move(0, BUTTON_HEIGHT + BUTTON_GAP);

        int guideButtonX = leftPos + AE2_TOOLBAR_LEFT - AE2_TOOLBAR_MARGIN - guideButtonWidth;
        int guideButtonY = topPos + AE2_TOOLBAR_TOP + AE2_TOOLBAR_MARGIN;
        int toolbarBoundsX = AE2_TOOLBAR_LEFT - guideButtonWidth - AE2_TOOLBAR_MARGIN * 2;
        int toolbarBoundsY = AE2_TOOLBAR_TOP;
        int toolbarBoundsWidth = guideButtonWidth + AE2_TOOLBAR_MARGIN * 2;
        int toolbarBoundsHeight = guideButtonHeight + AE2_TOOLBAR_MARGIN + AE2_TOOLBAR_VERTICAL_SPACING;
        // Mirror AE2 VerticalButtonBar exactly so the guide button shell stitches to the
        // main panel the same way as native AE2 screens:
        // drawX = screenLeft + boundX - 2
        // drawY = screenTop + boundY - 1
        // drawW = boundWidth + 1
        // drawH = boundHeight + 4
        GuiRect guideToolbarBackground = new GuiRect(
                leftPos + toolbarBoundsX - 2,
                topPos + toolbarBoundsY - 1,
                toolbarBoundsWidth + 1,
                toolbarBoundsHeight + 4);
        int titleLabelX = TITLE_LABEL_X;
        int statusRightX = PANEL_WIDTH - HORIZONTAL_GAP;

        return new SorterTerminalLayout(
                panel,
                header,
                footer,
                guideToolbarBackground,
                leftColumn,
                middleColumn,
                rightColumn,
                analysisButton,
                autoSortButton,
                mergeButton,
                guideButtonX,
                guideButtonY,
                titleLabelX,
                statusRightX);
    }
}
