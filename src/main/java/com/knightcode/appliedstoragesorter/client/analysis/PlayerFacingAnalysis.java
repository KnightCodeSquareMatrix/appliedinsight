package com.knightcode.appliedstoragesorter.client.analysis;

import java.util.List;

import net.minecraft.network.chat.Component;

/**
 * Player-facing view model produced by {@link AnalysisPresenter}.
 *
 * <p>Transforms the raw {@code StorageAnalyzerReport} (technical JSON) into
 * categories the game GUI can render directly: health assessment, capacity
 * summary, analysis lines with inline text + hover tooltips, and good news.
 *
 * <p>All text fields are localized {@link Component} instances so the GUI
 * does not need to perform its own translations.
 */
public record PlayerFacingAnalysis(
        NetworkHealthAssessment health,
        StorageSummary storage,
        List<AnalysisLine> lines,
        List<GoodNews> goodNews) {

    // -----------------------------------------------------------------------
    // Nested value types
    // -----------------------------------------------------------------------

    public enum Severity {
        /** Neutral observation — blue/gray. */
        INFO,
        /** Something worth attention — yellow/amber. */
        WARNING,
        /** Critical problem requiring action — red. */
        CRITICAL
    }

    /**
     * Overall network health assessment, derived from health flag severity mix.
     */
    public enum NetworkHealthAssessment {
        GOOD,
        FAIR,
        POOR,
        /** No analysis data available yet. */
        UNKNOWN
    }

    /**
     * Simplified capacity snapshot for the middle-column dashboard.
     * All numeric fields use {@code -1} to mean "unknown / not applicable".
     */
    public record StorageSummary(
            int internalStorageCount,
            int externalStorageCount,
            long usedBytes,
            long totalBytes,
            /** 0–100 percentage, or -1 when unknown. */
            int capacityPercent,
            /** true when at least one storage reports infinite capacity. */
            boolean hasInfiniteCapacity,
            Component fragmentationLabel,
            /** 1-3 items visible at a glance. */
            List<Component> topFragmentedItems) {
        public static final long UNKNOWN = -1L;
        public static final int UNKNOWN_PCT = -1;
    }

    /**
     * One diagnostic line rendered directly in the right column.
     *
     * <p><strong>Rendering contract:</strong>
     * <ul>
     *   <li>{@link #headline} — always shown (1 line, colour-coded by
     *       {@link #severity})</li>
     *   <li>{@link #detail} — shown on a second line when there is space
     *       (may be {@code null} or empty)</li>
     *   <li>{@link #tooltipBody} — shown only when the player hovers over
     *       the headline + detail region, containing the detailed
     *       explanation and suggested action</li>
     * </ul>
     */
    public record AnalysisLine(
            Severity severity,
            Component headline,
            Component detail,
            Component tooltipBody) {
    }

    /**
     * A positive observation — something that is working well.
     * Rendered with a green checkmark and no tooltip.
     */
    public record GoodNews(
            Component headline,
            Component detail) {
    }
}
