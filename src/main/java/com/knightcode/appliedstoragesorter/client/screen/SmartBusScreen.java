package com.knightcode.appliedstoragesorter.client.screen;

import appeng.client.gui.style.ScreenStyle;
import com.knightcode.appliedstoragesorter.Config;
import com.knightcode.appliedstoragesorter.client.filter.FilterEditorSupport;
import com.knightcode.appliedstoragesorter.block.SmartBusMode;
import com.knightcode.appliedstoragesorter.client.gui.SorterBaseScreen;
import com.knightcode.appliedstoragesorter.client.gui.theme.GuiThemeProvider;
import com.knightcode.appliedstoragesorter.client.gui.widget.ThemedAE2Button;
import com.knightcode.appliedstoragesorter.menu.SmartBusMenu;
import com.knightcode.appliedstoragesorter.network.SmartBusFilterPayload;
import com.knightcode.appliedstoragesorter.network.SmartBusModePayload;
import com.knightcode.appliedstoragesorter.rule.filter.FilterCondition;
import com.knightcode.appliedstoragesorter.rule.filter.FilterExpression;
import com.knightcode.appliedstoragesorter.rule.filter.FilterExpressionJsonCodec;
import com.knightcode.appliedstoragesorter.rule.filter.FilterGroup;
import com.knightcode.appliedstoragesorter.rule.filter.SmartBusFilterPresets;
import com.knightcode.appliedstoragesorter.rule.filter.SmartBusFilterPresets.Preset;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class SmartBusScreen extends SorterBaseScreen<SmartBusMenu> {
    private static final int FILTER_MAX_LENGTH = 32767;
    private static final int TEXT_MAX_WIDTH = 360;
    private static final int SUMMARY_MAX_CHARS = 80;

    private static final int CYCLE_X = 228;
    private static final int CYCLE_W = 72;
    private static final int WEB_X = 306;
    private static final int WEB_W = 80;
    private static final int HEADER_TEXT_X = 16;
    private static final int HEADER_TEXT_MAX_WIDTH = CYCLE_X - HEADER_TEXT_X - 8;
    private static final int HEADER_ROW_Y = 22;
    private static final int FILTER_LABEL_Y = 42;
    private static final int PRESETS_Y = 52;
    private static final int PRESET_BTN_W = 80;
    private static final int PRESET_BTN_H = 16;
    private static final int PRESET_GAP = 6;
    private static final int FILTER_BOX_X = 14;
    private static final int FILTER_BOX_Y = 70;
    private static final int FILTER_BOX_W = 360;
    private static final int FILTER_BOX_H = 166;
    private static final int SUMMARY_Y = 242;
    private static final int STATUS_Y = 254;
    private static final int ACTIONS_Y = 262;

    private MultiLineEditBox filterBox;
    private Component statusText = Component.empty();
    private int statusColor;
    private Component previewSummary = Component.empty();
    private int previewSummaryColor;

    public SmartBusScreen(SmartBusMenu menu, Inventory playerInv, Component title, ScreenStyle style) {
        super(menu, playerInv, title, style);
        this.inventoryLabelY = 10000;
        statusColor = themedMutedTextColor();
        previewSummaryColor = themedMutedTextColor();
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(new ThemedAE2Button(
                leftPos + CYCLE_X, topPos + HEADER_ROW_Y - 1, CYCLE_W, 18,
                Component.translatable("screen.appliedinsight.smart_bus.cycle_button"),
                button -> PacketDistributor.sendToServer(
                        new SmartBusModePayload(menu.getBlockPos(), menu.getSide()))));

        addRenderableWidget(new ThemedAE2Button(
                leftPos + WEB_X, topPos + HEADER_ROW_Y - 1, WEB_W, 18,
                Component.translatable("screen.appliedinsight.smart_bus.open_web_editor"),
                button -> openFilterEditor()));

        filterBox = new MultiLineEditBox(
                font,
                leftPos + FILTER_BOX_X,
                topPos + FILTER_BOX_Y,
                FILTER_BOX_W,
                FILTER_BOX_H,
                Component.translatable("screen.appliedinsight.smart_bus.filter_hint"),
                Component.translatable("screen.appliedinsight.smart_bus.filter_label"));
        filterBox.setCharacterLimit(FILTER_MAX_LENGTH);
        String currentFilter = menu.getFilterJson();
        if (currentFilter != null) {
            filterBox.setValue(currentFilter);
        }
        filterBox.setValueListener(this::onFilterTextChanged);
        addRenderableWidget(filterBox);
        refreshPreview(filterBox.getValue());

        addPresetButtons();

        int actionX = 14;
        addRenderableWidget(new ThemedAE2Button(
                leftPos + actionX, topPos + ACTIONS_Y, 52, 16,
                Component.translatable("screen.appliedinsight.smart_bus.paste"),
                button -> onPaste()));
        actionX += 56;
        addRenderableWidget(new ThemedAE2Button(
                leftPos + actionX, topPos + ACTIONS_Y, 52, 16,
                Component.translatable("screen.appliedinsight.smart_bus.copy"),
                button -> onCopy()));
        actionX += 56;
        addRenderableWidget(new ThemedAE2Button(
                leftPos + actionX, topPos + ACTIONS_Y, 60, 16,
                Component.translatable("screen.appliedinsight.smart_bus.save"),
                button -> onSave()));
        actionX += 64;
        addRenderableWidget(new ThemedAE2Button(
                leftPos + actionX, topPos + ACTIONS_Y, 48, 16,
                Component.translatable("screen.appliedinsight.smart_bus.delete"),
                button -> onDelete()));
    }

    private void addPresetButtons() {
        int presetX = 14;
        for (Preset preset : SmartBusFilterPresets.Preset.values()) {
            ThemedAE2Button button = new ThemedAE2Button(
                    leftPos + presetX,
                    topPos + PRESETS_Y,
                    PRESET_BTN_W,
                    PRESET_BTN_H,
                    Component.translatable(preset.translationKey()),
                    btn -> applyPreset(preset));
            button.setTooltip(Tooltip.create(Component.translatable(preset.tooltipKey())));
            addRenderableWidget(button);
            presetX += PRESET_BTN_W + PRESET_GAP;
        }
    }

    private void applyPreset(Preset preset) {
        String json = preset.toFilterJson();
        filterBox.setValue(json);
        refreshPreview(json);
        PacketDistributor.sendToServer(
                new SmartBusFilterPayload(menu.getBlockPos(), menu.getSide(), json.trim()));
        statusText = Component.translatable(
                "screen.appliedinsight.smart_bus.preset_applied",
                Component.translatable(preset.translationKey()));
        statusColor = GuiThemeProvider.resolve().statusOnlineColor();
    }

    private void onFilterTextChanged(String text) {
        statusText = Component.empty();
        refreshPreview(text);
    }

    private void refreshPreview(String text) {
        if (text == null || text.isBlank()) {
            previewSummary = Component.translatable("screen.appliedinsight.smart_bus.filter_state.none");
            previewSummaryColor = themedMutedTextColor();
            return;
        }

        try {
            FilterExpression root = FilterExpressionJsonCodec.parse(text);
            previewSummary = Component.literal(buildCompactSummary(root));
            previewSummaryColor = themedTextColor();
        } catch (RuntimeException ex) {
            previewSummary = Component.translatable(
                    "screen.appliedinsight.smart_bus.filter_invalid", ex.getMessage());
            previewSummaryColor = GuiThemeProvider.resolve().statusWarningColor();
        }
    }

    private void openFilterEditor() {
        try {
            URI uri = FilterEditorSupport.resolveEditorUri(Config.FILTER_EDITOR_URL.get());
            Util.getPlatform().openUri(uri);
        } catch (Exception ex) {
            statusText = Component.translatable(
                    "screen.appliedinsight.smart_bus.web_editor_failed", ex.getMessage());
            statusColor = GuiThemeProvider.resolve().statusWarningColor();
        }
    }

    private void onPaste() {
        String clipboard = minecraft.keyboardHandler.getClipboard();
        if (clipboard == null || clipboard.isBlank()) {
            statusText = Component.translatable("screen.appliedinsight.smart_bus.paste_empty");
            statusColor = GuiThemeProvider.resolve().statusWarningColor();
            return;
        }

        filterBox.setValue(clipboard);
        statusText = Component.translatable("screen.appliedinsight.smart_bus.paste_applied");
        statusColor = themedMutedTextColor();
        refreshPreview(clipboard);
    }

    private void onCopy() {
        String text = filterBox.getValue();
        if (text == null || text.isBlank()) {
            statusText = Component.translatable("screen.appliedinsight.smart_bus.copy_empty");
            statusColor = GuiThemeProvider.resolve().statusWarningColor();
            return;
        }

        minecraft.keyboardHandler.setClipboard(text);
        statusText = Component.translatable("screen.appliedinsight.smart_bus.copy_applied");
        statusColor = GuiThemeProvider.resolve().statusOnlineColor();
    }

    private void onSave() {
        String text = filterBox.getValue().trim();
        if (!text.isEmpty()) {
            try {
                FilterExpressionJsonCodec.parse(text);
            } catch (RuntimeException ex) {
                statusText = Component.translatable(
                        "screen.appliedinsight.smart_bus.filter_invalid", ex.getMessage());
                statusColor = GuiThemeProvider.resolve().statusWarningColor();
                refreshPreview(text);
                return;
            }
        }
        PacketDistributor.sendToServer(
                new SmartBusFilterPayload(menu.getBlockPos(), menu.getSide(), text));
        statusText = Component.translatable("screen.appliedinsight.smart_bus.filter_saved");
        statusColor = GuiThemeProvider.resolve().statusOnlineColor();
        refreshPreview(text);
    }

    private void onDelete() {
        filterBox.setValue("");
        PacketDistributor.sendToServer(
                new SmartBusFilterPayload(menu.getBlockPos(), menu.getSide(), ""));
        statusText = Component.translatable("screen.appliedinsight.smart_bus.filter_deleted");
        statusColor = GuiThemeProvider.resolve().statusWarningColor();
        refreshPreview("");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderCustomLabels(graphics);
    }

    private void renderCustomLabels(GuiGraphics graphics) {
        SmartBusMode mode = menu.getMode();

        int modeColor = switch (mode) {
            case EMPTY -> themedMutedTextColor();
            case IMPORT -> 0xFF6BB6FF;
            case EXPORT -> 0xFFFFB36B;
        };

        renderHeaderRow(graphics, mode, modeColor);
        graphics.drawString(font,
                Component.translatable("screen.appliedinsight.smart_bus.filter_label"),
                leftPos + HEADER_TEXT_X, topPos + FILTER_LABEL_Y, themedMutedTextColor(), false);

        Component summaryLine = previewSummary;
        int summaryColor = previewSummaryColor;
        graphics.drawString(font, summaryLine,
                leftPos + 16, topPos + SUMMARY_Y, summaryColor, false);

        if (!statusText.getString().isEmpty()) {
            graphics.drawString(font, trimToWidth(statusText),
                    leftPos + 16, topPos + STATUS_Y, statusColor, false);
        }
    }

    private void renderHeaderRow(GuiGraphics graphics, SmartBusMode mode, int modeColor) {
        int x = leftPos + HEADER_TEXT_X;
        int y = topPos + HEADER_ROW_Y;

        Component modeLabel = Component.translatable("screen.appliedinsight.smart_bus.mode_label");
        graphics.drawString(font, modeLabel, x, y, themedMutedTextColor(), false);
        x += font.width(modeLabel) + 4;

        Component modeName = mode.getDisplayName();
        graphics.drawString(font, modeName, x, y, modeColor, false);
        x += font.width(modeName) + 8;

        int remainingWidth = leftPos + HEADER_TEXT_X + HEADER_TEXT_MAX_WIDTH - x;
        if (remainingWidth <= 0) {
            return;
        }

        String hintText = Component.translatable("screen.appliedinsight.smart_bus.shift_cycle_hint").getString();
        hintText = font.plainSubstrByWidth(hintText, remainingWidth);
        graphics.drawString(font, hintText, x, y, themedMutedTextColor(), false);
    }

    private Component trimToWidth(Component text) {
        return Component.literal(font.plainSubstrByWidth(text.getString(), TEXT_MAX_WIDTH));
    }

    private static String buildCompactSummary(FilterExpression root) {
        List<String> lines = new ArrayList<>();
        collectSummary(root, lines);
        if (lines.isEmpty()) {
            return "";
        }

        String joined = String.join(" · ", lines);
        if (joined.length() <= SUMMARY_MAX_CHARS) {
            return joined;
        }
        return joined.substring(0, SUMMARY_MAX_CHARS - 3) + "...";
    }

    private static void collectSummary(FilterExpression node, List<String> lines) {
        if (node == null || lines.size() >= 4) {
            return;
        }
        if (node instanceof FilterCondition condition) {
            String value = condition.value() == null ? "" : condition.value();
            lines.add(condition.field().name() + " " + condition.operator().name() + " " + value);
            return;
        }

        FilterGroup group = (FilterGroup) node;
        for (FilterExpression child : group.rules()) {
            collectSummary(child, lines);
            if (lines.size() >= 4) {
                break;
            }
        }
    }
}
