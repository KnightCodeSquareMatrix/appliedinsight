package com.knightcode.appliedstoragesorter.client.screen;

import java.util.ArrayList;
import java.util.List;

import appeng.client.gui.style.ScreenStyle;
import com.knightcode.appliedstoragesorter.client.analysis.AnalysisPresenter;
import com.knightcode.appliedstoragesorter.client.analysis.PlayerFacingAnalysis;
import com.knightcode.appliedstoragesorter.client.format.CompactNumberFormatter;
import com.knightcode.appliedstoragesorter.client.gui.SorterBaseScreen;
import com.knightcode.appliedstoragesorter.client.gui.theme.GuiThemeProvider;
import com.knightcode.appliedstoragesorter.client.gui.widget.ThemedAE2Button;
import com.knightcode.appliedstoragesorter.menu.SorterCommandBlockMenu;
import com.knightcode.appliedstoragesorter.network.SorterAnalysisPayloadHandler;
import com.knightcode.appliedstoragesorter.network.SorterCommandPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class SorterCommandBlockScreen extends SorterBaseScreen<SorterCommandBlockMenu> {
    private static final String I18N_PREFIX = "screen.appliedinsight.sorter_command_block.";
    private static final String PRESENTER_I18N = "analysis.appliedinsight.presenter.";
    private static final int STATUS_LAMP_SIZE = 7;
    private static final int STATUS_LAMP_GAP = 5;
    private static final int INFO_COLOR = 0x7FB7FF;

    private SorterTerminalLayout layout;
    private Component feedbackMessage = t("feedback.idle");
    private FeedbackTone feedbackTone = FeedbackTone.INFO;
    private String lastAnalysisJson;

    private PlayerFacingAnalysis currentAnalysis;
    private final List<AnalysisLineHitbox> analysisLineHitboxes = new ArrayList<>();

    public SorterCommandBlockScreen(SorterCommandBlockMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.inventoryLabelY = 10000;
        this.lastAnalysisJson = SorterAnalysisPayloadHandler.getRawJson();
    }

    @Override
    protected void init() {
        super.init();
        layout = SorterTerminalLayout.of(leftPos, topPos, themedHelpButton.getWidth(), themedHelpButton.getHeight());

        addCommandButton(layout.analysisButton(), t("button.analyze"),
                t("tooltip.analyze"), SorterCommandPayload.CMD_ME_STORAGE_DUMP, t("feedback.sent_analyze"));
        addCommandButton(layout.autoSortButton(), t("button.auto_sort"),
                t("tooltip.auto_sort"), SorterCommandPayload.CMD_ME_PLAN_AND_MOVE, t("feedback.sent_auto_sort"));
        addCommandButton(layout.mergeButton(), t("button.merge_items"),
                t("tooltip.merge_items"), SorterCommandPayload.CMD_MERGE, t("feedback.sent_merge_items"));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        String latestJson = SorterAnalysisPayloadHandler.getRawJson();
        if (latestJson != null && !latestJson.equals(lastAnalysisJson)) {
            lastAnalysisJson = latestJson;
            setFeedback(t("feedback.analysis_updated"), FeedbackTone.SUCCESS);
        }
    }

    private void sendCommand(int commandId, Component feedback) {
        if (commandId == SorterCommandPayload.CMD_ME_STORAGE_DUMP) {
            lastAnalysisJson = null;
        }
        setFeedback(feedback, FeedbackTone.INFO);
        PacketDistributor.sendToServer(new SorterCommandPayload(commandId));
    }

    private void addCommandButton(GuiRect rect, Component label, Component tooltip, int commandId, Component feedback) {
        ThemedAE2Button button = new ThemedAE2Button(
                rect.x(), rect.y(), rect.width(), rect.height(),
                label,
                btn -> sendCommand(commandId, feedback));
        button.setTooltip(Tooltip.create(tooltip));
        addRenderableWidget(button);
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTick) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTick);
        SorterTerminalLayout currentLayout = requireLayout();
        renderSectionTitles(guiGraphics, currentLayout);
        renderAnalysisPanels(guiGraphics, currentLayout);
        renderFeedbackBar(guiGraphics, currentLayout);
    }

    private void renderSectionTitles(GuiGraphics guiGraphics, SorterTerminalLayout currentLayout) {
        int textColor = themedTextColor();
        guiGraphics.drawString(this.font, t("section.actions"),
                currentLayout.leftColumn().x() + 6, currentLayout.leftColumn().y() + 6,
                textColor, false);
        guiGraphics.drawString(this.font, t("section.analysis"),
                currentLayout.middleColumn().x() + 6, currentLayout.middleColumn().y() + 6,
                textColor, false);
        guiGraphics.drawString(this.font, t("section.diagnosis"),
                currentLayout.rightColumn().x() + 6, currentLayout.rightColumn().y() + 6,
                textColor, false);
    }

    private void renderAnalysisPanels(GuiGraphics guiGraphics, SorterTerminalLayout currentLayout) {
        var report = SorterAnalysisPayloadHandler.getReport();
        currentAnalysis = AnalysisPresenter.present(report);
        analysisLineHitboxes.clear();

        var theme = GuiThemeProvider.resolve();
        if (report == null || report.summary() == null) {
            drawWrappedText(guiGraphics, t("empty.no_analysis_data"),
                    currentLayout.middleColumn().x() + 8, currentLayout.middleColumn().y() + 24,
                    currentLayout.middleColumn().width() - 14, theme.statusWarningColor());
            drawWrappedText(guiGraphics, t("hint.run_storage_analysis"),
                    currentLayout.rightColumn().x() + 8, currentLayout.rightColumn().y() + 24,
                    currentLayout.rightColumn().width() - 14, themedMutedTextColor());
            return;
        }

        renderMiddleColumn(guiGraphics, currentLayout, theme);
        renderRightColumn(guiGraphics, currentLayout, theme);
    }

    private void renderMiddleColumn(GuiGraphics guiGraphics, SorterTerminalLayout currentLayout,
            com.knightcode.appliedstoragesorter.client.gui.theme.GuiTheme theme) {
        int x = currentLayout.middleColumn().x() + 8;
        int y = currentLayout.middleColumn().y() + 24;
        int maxWidth = currentLayout.middleColumn().width() - 14;
        var storage = currentAnalysis.storage();
        var health = currentAnalysis.health();

        int healthColor = switch (health) {
            case GOOD -> theme.statusOnlineColor();
            case FAIR -> theme.statusWarningColor();
            case POOR -> theme.statusOfflineColor();
            case UNKNOWN -> themedMutedTextColor();
        };
        Component healthText = t("health.label").copy()
                .append(" ")
                .append(presenterT("health." + health.name().toLowerCase(java.util.Locale.ROOT)));
        y = drawWrappedText(guiGraphics, healthText, x, y, maxWidth, healthColor) + 4;

        y = drawWrappedText(guiGraphics,
                presenterT("storage_count.internal", String.valueOf(storage.internalStorageCount())),
                x, y, maxWidth, themedTextColor()) + 1;
        y = drawWrappedText(guiGraphics,
                presenterT("storage_count.external", String.valueOf(storage.externalStorageCount())),
                x, y, maxWidth, themedTextColor()) + 6;

        String capacityLine;
        if (storage.capacityPercent() >= 0) {
            capacityLine = presenterT("capacity.used_pct",
                    CompactNumberFormatter.format(storage.usedBytes()),
                    CompactNumberFormatter.format(storage.totalBytes()),
                    String.valueOf(storage.capacityPercent())).getString();
            if (storage.hasInfiniteCapacity()) {
                capacityLine += "  +∞";
            }
        } else if (storage.hasInfiniteCapacity()) {
            capacityLine = presenterT("capacity.unlimited").getString();
        } else {
            capacityLine = presenterT("capacity.unknown").getString();
        }
        y = drawKeyValueLine(guiGraphics, x, y, maxWidth,
                t("capacity.label"), Component.literal(capacityLine)) + 4;
        y = drawKeyValueLine(guiGraphics, x, y, maxWidth,
                t("fragmentation.label"),
                storage.fragmentationLabel()) + 4;

        if (!storage.topFragmentedItems().isEmpty()) {
            guiGraphics.drawString(this.font,
                    trimToWidth("— " + storage.topFragmentedItems().get(0).getString(), maxWidth),
                    x, y, themedMutedTextColor(), false);
        }
    }

    private void renderRightColumn(GuiGraphics guiGraphics, SorterTerminalLayout currentLayout,
            com.knightcode.appliedstoragesorter.client.gui.theme.GuiTheme theme) {
        int x = currentLayout.rightColumn().x() + 8;
        int y = currentLayout.rightColumn().y() + 24;
        int maxWidth = currentLayout.rightColumn().width() - 14;
        int maxHeight = currentLayout.rightColumn().bottom() - y;

        List<PlayerFacingAnalysis.AnalysisLine> lines = currentAnalysis.lines();
        List<PlayerFacingAnalysis.GoodNews> goodNews = currentAnalysis.goodNews();

        if (lines.isEmpty() && goodNews.isEmpty()) {
            drawWrappedText(guiGraphics, Component.literal("—"), x, y, maxWidth, themedMutedTextColor());
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            int lineStartY = y;

            String prefix = switch (line.severity()) {
                case CRITICAL -> "!! ";
                case WARNING -> "!  ";
                case INFO -> "i  ";
            };
            int severityColor = switch (line.severity()) {
                case CRITICAL -> theme.statusOfflineColor();
                case WARNING -> theme.statusWarningColor();
                case INFO -> INFO_COLOR;
            };

            Component headline = Component.literal(prefix).append(line.headline());
            for (var part : this.font.split(headline, maxWidth)) {
                if (y - currentLayout.rightColumn().y() > maxHeight) {
                    break;
                }
                guiGraphics.drawString(this.font, part, x, y, severityColor, false);
                y += 9;
            }

            if (line.detail() != null && !line.detail().getString().isEmpty()) {
                if (y - currentLayout.rightColumn().y() + 9 <= maxHeight) {
                    Component detail = Component.literal("   ").append(line.detail());
                    for (var part : this.font.split(detail, maxWidth)) {
                        if (y - currentLayout.rightColumn().y() > maxHeight) {
                            break;
                        }
                        guiGraphics.drawString(this.font, part, x, y, themedMutedTextColor(), false);
                        y += 9;
                    }
                }
            }

            int lineHeight = y - lineStartY;
            if (lineHeight > 0) {
                analysisLineHitboxes.add(new AnalysisLineHitbox(
                        new GuiRect(x, lineStartY, maxWidth, lineHeight),
                        i));
            }

            y += 3;
            if (y - currentLayout.rightColumn().y() > maxHeight) {
                break;
            }
        }

        for (var gn : goodNews) {
            if (y - currentLayout.rightColumn().y() + 9 > maxHeight) {
                break;
            }
            Component gnHeadline = Component.literal("+ ").append(gn.headline());
            y = drawWrappedText(guiGraphics, gnHeadline, x, y, maxWidth, theme.statusOnlineColor()) + 1;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderAnalysisTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderAnalysisTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (currentAnalysis == null || analysisLineHitboxes.isEmpty()) {
            return;
        }

        for (var hitbox : analysisLineHitboxes) {
            if (hitbox.rect().contains(mouseX, mouseY)) {
                var lines = currentAnalysis.lines();
                if (hitbox.lineIndex() >= 0 && hitbox.lineIndex() < lines.size()) {
                    var line = lines.get(hitbox.lineIndex());
                    if (line.tooltipBody() != null && !line.tooltipBody().getString().isEmpty()) {
                        guiGraphics.renderTooltip(
                                this.font,
                                this.font.split(line.tooltipBody(), 200),
                                mouseX, mouseY);
                    }
                }
                return;
            }
        }
    }

    private void renderFeedbackBar(GuiGraphics guiGraphics, SorterTerminalLayout currentLayout) {
        var theme = GuiThemeProvider.resolve();
        int color = switch (feedbackTone) {
            case SUCCESS -> theme.statusOnlineColor();
            case ERROR -> theme.statusOfflineColor();
            case INFO -> INFO_COLOR;
        };
        guiGraphics.drawString(this.font, t("feedback.label"), currentLayout.panel().x() + 8,
                currentLayout.footer().y() + 4, themedMutedTextColor(), false);
        guiGraphics.drawString(this.font,
                Component.literal(trimToWidth(feedbackMessage.getString(), currentLayout.panel().width() - 84)),
                currentLayout.panel().x() + 52, currentLayout.footer().y() + 4, color, false);
    }

    private int drawKeyValueLine(GuiGraphics guiGraphics, int x, int y, int maxWidth,
            Component key, Component value) {
        int keyWidth = this.font.width(key);
        int valueMaxWidth = Math.max(1, maxWidth - keyWidth - 4);
        String valueStr = trimToWidth(value.getString(), valueMaxWidth);
        guiGraphics.drawString(this.font, key, x, y, themedMutedTextColor(), false);
        guiGraphics.drawString(this.font, Component.literal(valueStr), x + keyWidth + 4, y, themedTextColor(), false);
        return y + 11;
    }

    private int drawWrappedText(GuiGraphics guiGraphics, Component text, int x, int y, int width, int color) {
        int currentY = y;
        for (var line : this.font.split(text, width)) {
            guiGraphics.drawString(this.font, line, x, currentY, color, false);
            currentY += 9;
        }
        return currentY;
    }

    private String trimToWidth(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        }
        int ellipsisWidth = this.font.width("...");
        return this.font.plainSubstrByWidth(text, Math.max(0, maxWidth - ellipsisWidth)) + "...";
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        SorterTerminalLayout currentLayout = requireLayout();
        StatusPresentation statusPresentation = getStatusPresentation(menu);
        int statusX = offsetX + currentLayout.statusRightX()
                - this.font.width(statusPresentation.text().getString());
        int lampX = statusX - STATUS_LAMP_GAP - STATUS_LAMP_SIZE;
        int titleX = offsetX + currentLayout.titleLabelX();
        int maxTitleWidth = Math.max(0, lampX - titleX - 8);

        drawStatusLamp(guiGraphics, lampX, offsetY + this.titleLabelY + 1, statusPresentation.color());
        guiGraphics.drawString(this.font, statusPresentation.text(), statusX, offsetY + this.titleLabelY,
                statusPresentation.color(), false);
        if (maxTitleWidth < this.font.width(this.title.getString())) {
            guiGraphics.drawString(this.font,
                    Component.literal(trimToWidth(this.title.getString(), maxTitleWidth)),
                    titleX, offsetY + this.titleLabelY, themedTextColor(), false);
        }
    }

    private void drawStatusLamp(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x, y, x + STATUS_LAMP_SIZE, y + STATUS_LAMP_SIZE, 0xFF08090B);
        guiGraphics.fill(x + 1, y + 1, x + STATUS_LAMP_SIZE - 1, y + STATUS_LAMP_SIZE - 1, color);
        guiGraphics.fill(x + 2, y + 2, x + STATUS_LAMP_SIZE - 3, y + 3, 0x90FFFFFF);
    }

    private StatusPresentation getStatusPresentation(SorterCommandBlockMenu menu) {
        var theme = GuiThemeProvider.resolve();
        var status = menu.getNetworkStatusSnapshot();
        return switch (status) {
            case ONLINE -> new StatusPresentation(t("status.online"), theme.statusOnlineColor());
            case CHANNEL_LIMITED -> new StatusPresentation(t("status.channel_limited"), theme.statusWarningColor());
            case OFFLINE -> new StatusPresentation(t("status.offline"), theme.statusOfflineColor());
        };
    }

    private SorterTerminalLayout requireLayout() {
        if (layout == null) {
            layout = SorterTerminalLayout.of(leftPos, topPos, themedHelpButton.getWidth(), themedHelpButton.getHeight());
        }
        return layout;
    }

    private void setFeedback(Component feedback, FeedbackTone tone) {
        this.feedbackMessage = feedback;
        this.feedbackTone = tone;
    }

    private static Component t(String key, Object... args) {
        return Component.translatable(I18N_PREFIX + key, args);
    }

    private static Component presenterT(String key, Object... args) {
        return Component.translatable(PRESENTER_I18N + key, args);
    }

    private record StatusPresentation(Component text, int color) {
    }

    private record AnalysisLineHitbox(GuiRect rect, int lineIndex) {
    }

    private enum FeedbackTone {
        SUCCESS,
        ERROR,
        INFO
    }
}
