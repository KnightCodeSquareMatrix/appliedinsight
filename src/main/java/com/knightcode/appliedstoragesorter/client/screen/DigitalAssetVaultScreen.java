package com.knightcode.appliedstoragesorter.client.screen;

import appeng.client.gui.style.ScreenStyle;
import com.knightcode.appliedstoragesorter.client.format.ByteUnitFormatter;
import com.knightcode.appliedstoragesorter.client.gui.SorterBaseScreen;
import com.knightcode.appliedstoragesorter.client.gui.theme.GuiThemeProvider;
import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;
import com.knightcode.appliedstoragesorter.client.gui.widget.ExpansionCellPickerWidget;
import com.knightcode.appliedstoragesorter.client.gui.widget.NewDavToggleButton;
import com.knightcode.appliedstoragesorter.client.gui.widget.ThemedAE2Button;
import com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu;
import com.knightcode.appliedstoragesorter.network.NewDavExpandOncePayload;
import com.knightcode.appliedstoragesorter.network.NewDavMigrateToSqlPayload;
import com.knightcode.appliedstoragesorter.network.NewDavTogglePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DigitalAssetVaultScreen extends SorterBaseScreen<DigitalAssetVaultMenu> {
    private static final int CONTROL_TOP_Y = 26;
    private static final int TOGGLE_ROW_SPACING = 24;
    private static final int TOGGLE_ROW_1_Y = CONTROL_TOP_Y;
    private static final int TOGGLE_ROW_2_Y = CONTROL_TOP_Y + TOGGLE_ROW_SPACING;
    private static final int TOGGLE_ROW_3_Y = CONTROL_TOP_Y + TOGGLE_ROW_SPACING * 2;

    private NewDavToggleButton migrateToggle;
    private NewDavToggleButton autoAcceptToggle;
    private NewDavToggleButton autoExpandToggle;
    private ThemedAE2Button expandOnceButton;
    private ThemedAE2Button migrateToSqlButton;
    private ExpansionCellPickerWidget expansionCellPicker;

    public DigitalAssetVaultScreen(DigitalAssetVaultMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.inventoryLabelY = 10000;
    }

    public ExpansionCellPickerWidget getExpansionCellPicker() {
        return expansionCellPicker;
    }

    @Override
    protected void init() {
        super.init();

        int toggleX = leftPos + DigitalAssetVaultMenu.LEFT_COL_X;

        migrateToggle = addToggle(toggleX, TOGGLE_ROW_1_Y,
                "screen.appliedinsight.digital_asset_vault.toggle.migrate",
                "screen.appliedinsight.digital_asset_vault.tooltip.migrate",
                menu.isMigrateExistingItems(),
                NewDavTogglePayload.SETTING_MIGRATE_EXISTING);

        autoAcceptToggle = addToggle(toggleX, TOGGLE_ROW_2_Y,
                "screen.appliedinsight.digital_asset_vault.toggle.auto_accept",
                "screen.appliedinsight.digital_asset_vault.tooltip.auto_accept",
                menu.isAutoAcceptIncoming(),
                NewDavTogglePayload.SETTING_AUTO_ACCEPT);

        autoExpandToggle = addToggle(toggleX, TOGGLE_ROW_3_Y,
                "screen.appliedinsight.digital_asset_vault.toggle.auto_expand",
                "screen.appliedinsight.digital_asset_vault.tooltip.auto_expand",
                menu.isAutoExpandEnabled(),
                NewDavTogglePayload.SETTING_AUTO_EXPAND);

        expandOnceButton = new ThemedAE2Button(
                leftPos + DigitalAssetVaultMenu.EXPAND_ONCE_BUTTON_X,
                topPos + DigitalAssetVaultMenu.EXPAND_ONCE_BUTTON_Y,
                DigitalAssetVaultMenu.EXPAND_ONCE_BUTTON_WIDTH,
                DigitalAssetVaultMenu.EXPAND_ONCE_BUTTON_HEIGHT,
                Component.translatable("screen.appliedinsight.digital_asset_vault.button.expand_once"),
                btn -> sendExpandOnce());
        expandOnceButton.setTooltip(Tooltip.create(
                Component.translatable("screen.appliedinsight.digital_asset_vault.tooltip.expand_once")));
        addRenderableWidget(expandOnceButton);

        migrateToSqlButton = new ThemedAE2Button(
                leftPos + DigitalAssetVaultMenu.MIGRATE_TO_SQL_BUTTON_X,
                topPos + DigitalAssetVaultMenu.MIGRATE_TO_SQL_BUTTON_Y,
                DigitalAssetVaultMenu.MIGRATE_TO_SQL_BUTTON_WIDTH,
                DigitalAssetVaultMenu.MIGRATE_TO_SQL_BUTTON_HEIGHT,
                Component.translatable("screen.appliedinsight.digital_asset_vault.button.migrate_to_sql"),
                btn -> onMigrateToSql());
        migrateToSqlButton.setTooltip(Tooltip.create(
                Component.translatable("screen.appliedinsight.digital_asset_vault.tooltip.migrate_to_sql")));
        addRenderableWidget(migrateToSqlButton);

        expansionCellPicker = new ExpansionCellPickerWidget(
                leftPos + DigitalAssetVaultMenu.EXPANSION_SLOT_X,
                topPos + DigitalAssetVaultMenu.EXPANSION_SLOT_Y,
                menu);
        expansionCellPicker.setTooltip(Tooltip.create(
                Component.translatable("screen.appliedinsight.digital_asset_vault.tooltip.expansion_cell")));
        addRenderableWidget(expansionCellPicker);
    }

    private NewDavToggleButton addToggle(int x, int localY, String labelKey, String tooltipKey,
            boolean initialState, int setting) {
        NewDavToggleButton toggle = new NewDavToggleButton(
                x, topPos + localY, DigitalAssetVaultMenu.LEFT_COL_WIDTH, DigitalAssetVaultMenu.TOGGLE_HEIGHT, labelKey, initialState,
                newState -> sendToggle(setting));
        toggle.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
        addRenderableWidget(toggle);
        return toggle;
    }

    private void sendToggle(int setting) {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new NewDavTogglePayload(menu.getBlockEntity().getBlockPos(), setting));
    }

    private void sendExpandOnce() {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new NewDavExpandOncePayload(menu.getBlockEntity().getBlockPos()));
    }

    private void onMigrateToSql() {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new NewDavMigrateToSqlPayload(menu.getBlockEntity().getBlockPos()));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        syncToggle(migrateToggle, menu.isMigrateExistingItems());
        syncToggle(autoAcceptToggle, menu.isAutoAcceptIncoming());
        syncToggle(autoExpandToggle, menu.isAutoExpandEnabled());
        if (expandOnceButton != null) {
            var status = menu.getStatus();
            boolean busy = status == DigitalAssetVaultBlockEntity.Status.EXPAND_ONCE_CRAFTING
                    || status == DigitalAssetVaultBlockEntity.Status.AUTO_EXPAND_CRAFTING;
            expandOnceButton.active = !menu.getExpansionCellId().isEmpty() && !busy;
        }
    }

    private void syncToggle(NewDavToggleButton toggle, boolean serverState) {
        if (toggle != null && toggle.isToggled() != serverState) {
            toggle.setState(serverState);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderCustomLabels(guiGraphics);
    }

    private void renderCustomLabels(GuiGraphics guiGraphics) {
        int rightX = DigitalAssetVaultMenu.ABSORPTION_TEXT_X;
        int textColor = themedTextColor();
        int mutedColor = themedMutedTextColor();
        var theme = GuiThemeProvider.resolve();

        guiGraphics.drawString(font,
                Component.translatable("screen.appliedinsight.digital_asset_vault.section.absorption"),
                leftPos + DigitalAssetVaultMenu.SLOT_LABEL_X,
                topPos + DigitalAssetVaultMenu.SLOT_LABEL_Y, mutedColor, false);

        guiGraphics.drawString(font,
                Component.translatable("screen.appliedinsight.digital_asset_vault.absorbed_cells",
                        menu.getAbsorbedCellCount()),
                leftPos + rightX, topPos + DigitalAssetVaultMenu.ABSORPTION_STAT_1_Y, textColor, false);

        String bytesLine = Component.translatable("screen.appliedinsight.digital_asset_vault.bytes_line",
                ByteUnitFormatter.formatPair(menu.getUsedBytes(), menu.getAbsorbedBytes())).getString();
        guiGraphics.drawString(font,
                Component.literal(trimToWidth(bytesLine, DigitalAssetVaultMenu.STAT_TEXT_MAX_WIDTH)),
                leftPos + rightX, topPos + DigitalAssetVaultMenu.ABSORPTION_BYTES_VALUE_Y, textColor, false);

        guiGraphics.drawString(font,
                Component.translatable("screen.appliedinsight.digital_asset_vault.types_total",
                        menu.getAbsorbedTypeCapacity()),
                leftPos + rightX, topPos + DigitalAssetVaultMenu.ABSORPTION_TYPES_TOTAL_Y, textColor, false);

        int typePercent = menu.getAbsorbedTypeCapacity() == 0 ? 0
                : (int) Math.round(100.0 * menu.getUsedTypeCapacity() / menu.getAbsorbedTypeCapacity());
        guiGraphics.drawString(font,
                Component.translatable("screen.appliedinsight.digital_asset_vault.types_used_percent",
                        menu.getUsedTypeCapacity(), typePercent),
                leftPos + rightX, topPos + DigitalAssetVaultMenu.ABSORPTION_TYPES_USED_Y, mutedColor, false);

        guiGraphics.drawString(font,
                Component.translatable("screen.appliedinsight.digital_asset_vault.expansion_cell_label"),
                leftPos + rightX, topPos + DigitalAssetVaultMenu.EXPANSION_LABEL_Y, mutedColor, false);

        renderCellIndicator(guiGraphics, theme, rightX);
        renderStatusLine(guiGraphics, textColor);
    }

    private void renderStatusLine(GuiGraphics guiGraphics, int textColor) {
        DigitalAssetVaultBlockEntity.Status status = menu.getStatus();
        if (status == DigitalAssetVaultBlockEntity.Status.IDLE) {
            return;
        }

        int color = resolveStatusColor(status, textColor);
        Component statusMessage = resolveStatusMessage(status);
        Component statusLine = Component.translatable("screen.appliedinsight.digital_asset_vault.status",
                statusMessage);
        drawWrappedText(guiGraphics, statusLine,
                leftPos + DigitalAssetVaultMenu.STATUS_X,
                topPos + DigitalAssetVaultMenu.STATUS_Y,
                DigitalAssetVaultMenu.STATUS_WIDTH,
                DigitalAssetVaultMenu.STATUS_MAX_LINES,
                color);
    }

    private Component resolveStatusMessage(DigitalAssetVaultBlockEntity.Status status) {
        if (status == DigitalAssetVaultBlockEntity.Status.EXPAND_ONCE_MISSING_INGREDIENTS
                && !menu.getExpandOnceDetail().isEmpty()) {
            return Component.translatable(
                    "screen.appliedinsight.digital_asset_vault.status.expand_once_missing_ingredients.detail",
                    menu.getExpandOnceDetail());
        }
        return Component.translatable(status.translationKey());
    }

    private int resolveStatusColor(DigitalAssetVaultBlockEntity.Status status, int defaultColor) {
        if (status == DigitalAssetVaultBlockEntity.Status.EXPAND_ONCE_COMPLETED) {
            return 0xFF55FF55;
        }
        if (status.isExpandOnceFeedback()
                && status != DigitalAssetVaultBlockEntity.Status.EXPAND_ONCE_CRAFTING) {
            return 0xFFFFAA44;
        }
        if (status == DigitalAssetVaultBlockEntity.Status.MIGRATE_TO_SQL_TBD) {
            return 0xFFFFAA44;
        }
        return defaultColor;
    }

    private void drawWrappedText(GuiGraphics guiGraphics, Component text, int x, int y, int maxWidth,
            int maxLines, int color) {
        int line = 0;
        for (var part : font.split(text, maxWidth)) {
            if (line >= maxLines) {
                break;
            }
            guiGraphics.drawString(font, part, x, y, color, false);
            y += DigitalAssetVaultMenu.STATUS_LINE_HEIGHT;
            line++;
        }
    }

    private String trimToWidth(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        int ellipsisWidth = font.width("...");
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - ellipsisWidth)) + "...";
    }

    private void renderCellIndicator(GuiGraphics guiGraphics,
            com.knightcode.appliedstoragesorter.client.gui.theme.GuiTheme theme, int rightX) {
        if (menu.getExpansionCellId().isEmpty()) {
            return;
        }
        if (!menu.isExpansionCellValid()) {
            guiGraphics.drawString(font,
                    Component.translatable("screen.appliedinsight.digital_asset_vault.indicator.invalid_cell"),
                    leftPos + rightX, topPos + DigitalAssetVaultMenu.INDICATOR_Y, theme.statusOfflineColor(), false);
            return;
        }
        if (!menu.isExpansionCellCraftable()) {
            guiGraphics.drawString(font,
                    Component.translatable("screen.appliedinsight.digital_asset_vault.indicator.no_pattern"),
                    leftPos + rightX, topPos + DigitalAssetVaultMenu.INDICATOR_Y, theme.statusWarningColor(), false);
            return;
        }
        guiGraphics.drawString(font,
                Component.translatable("screen.appliedinsight.digital_asset_vault.indicator.pattern_found"),
                leftPos + rightX, topPos + DigitalAssetVaultMenu.INDICATOR_Y, theme.statusOnlineColor(), false);
    }
}
