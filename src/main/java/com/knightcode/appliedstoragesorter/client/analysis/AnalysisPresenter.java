package com.knightcode.appliedstoragesorter.client.analysis;

import java.util.ArrayList;
import java.util.List;

import com.knightcode.appliedstoragesorter.ae2.analysis.StorageAnalyzerReport;
import com.knightcode.appliedstoragesorter.client.format.LocationIdFormatter;

import net.minecraft.network.chat.Component;

/**
 * Transforms a raw StorageAnalyzerReport into a PlayerFacingAnalysis for GUI rendering.
 *
 * <p>Architecture boundary: this class lives in client/analysis/ - it only transforms
 * already-received data, never initiates network requests or modifies server state.</p>
 */
public final class AnalysisPresenter {

    private static final String I18N = "analysis.appliedinsight.presenter.";

    private AnalysisPresenter() {}

    public static PlayerFacingAnalysis present(StorageAnalyzerReport report) {
        if (report == null || report.summary() == null) {
            return empty();
        }

        StorageAnalyzerReport.NetworkSummary summary = report.summary();
        List<StorageAnalyzerReport.StorageLocationSummary> locations =
                report.storageLocations() != null ? report.storageLocations() : List.of();
        List<String> healthFlags = report.healthFlags() != null ? report.healthFlags() : List.of();

        PlayerFacingAnalysis.StorageSummary storageSummary = buildStorageSummary(summary, locations);

        List<PlayerFacingAnalysis.AnalysisLine> lines = new ArrayList<>();
        boolean hasCritical = false;
        boolean hasWarning = false;

        for (String flag : healthFlags) {
            if ("suspected_infinite_storage_present".equals(flag)) continue;
            var line = buildLineForHealthFlag(flag, summary);
            if (line != null) {
                lines.add(line);
                if (line.severity() == PlayerFacingAnalysis.Severity.CRITICAL) hasCritical = true;
                else if (line.severity() == PlayerFacingAnalysis.Severity.WARNING) hasWarning = true;
            }
        }

        List<PlayerFacingAnalysis.GoodNews> goodNews = new ArrayList<>();
        if (healthFlags.contains("low_fragmentation")) {
            goodNews.add(new PlayerFacingAnalysis.GoodNews(
                    t("good.low_fragmentation.headline"),
                    t("good.low_fragmentation.detail")));
        }

        PlayerFacingAnalysis.NetworkHealthAssessment health;
        if (hasCritical) health = PlayerFacingAnalysis.NetworkHealthAssessment.POOR;
        else if (hasWarning) health = PlayerFacingAnalysis.NetworkHealthAssessment.FAIR;
        else health = PlayerFacingAnalysis.NetworkHealthAssessment.GOOD;

        return new PlayerFacingAnalysis(health, storageSummary, List.copyOf(lines), List.copyOf(goodNews));
    }

    private static PlayerFacingAnalysis.StorageSummary buildStorageSummary(
            StorageAnalyzerReport.NetworkSummary summary,
            List<StorageAnalyzerReport.StorageLocationSummary> locations) {

        long totalCapacity = locations.stream()
                .filter(loc -> loc.totalBytes() != null && loc.totalBytes() > 0)
                .mapToLong(StorageAnalyzerReport.StorageLocationSummary::totalBytes).sum();
        long usedBytes = locations.stream()
                .filter(loc -> loc.usedBytes() != null)
                .mapToLong(StorageAnalyzerReport.StorageLocationSummary::usedBytes).sum();
        boolean hasInfinite = locations.stream()
                .anyMatch(loc -> loc.cellKind() != null && loc.cellKind().startsWith("infinite:"));

        int capacityPctInt = hasInfinite ? PlayerFacingAnalysis.StorageSummary.UNKNOWN_PCT
                : (totalCapacity > 0 ? (int) ((double) usedBytes / (double) totalCapacity * 100.0D) : 0);
        return new PlayerFacingAnalysis.StorageSummary(
                summary.internalStorageLocationCount(),
                summary.externalStorageLocationCount(),
                usedBytes,
                totalCapacity,
                capacityPctInt,
                hasInfinite,
                Component.literal(summary.fragmentationLevel() != null ? summary.fragmentationLevel() : "low"),
                List.of());
    }

    private static PlayerFacingAnalysis.AnalysisLine buildLineForHealthFlag(
            String flag, StorageAnalyzerReport.NetworkSummary summary) {
        return switch (flag) {
            case "high_fragmentation", "fragmentation_score_high" ->
                new PlayerFacingAnalysis.AnalysisLine(PlayerFacingAnalysis.Severity.WARNING,
                        t("line.high_fragmentation.headline"),
                        t("line.high_fragmentation.detail", String.valueOf(summary.duplicatedKeyCount())),
                        t("line.high_fragmentation.tooltip"));
            case "mixed_internal_external_distribution" ->
                new PlayerFacingAnalysis.AnalysisLine(PlayerFacingAnalysis.Severity.INFO,
                        t("line.mixed.headline"), t("line.mixed.detail"), t("line.mixed.tooltip"));
            case "external_amount_dominant" ->
                new PlayerFacingAnalysis.AnalysisLine(PlayerFacingAnalysis.Severity.INFO,
                        t("line.external_dominant.headline"), t("line.external_dominant.detail"),
                        t("line.external_dominant.tooltip"));
            case "external_only_storage_network" ->
                new PlayerFacingAnalysis.AnalysisLine(PlayerFacingAnalysis.Severity.WARNING,
                        t("line.external_only.headline"), t("line.external_only.detail"),
                        t("line.external_only.tooltip"));
            case "has_empty_storage_locations" ->
                new PlayerFacingAnalysis.AnalysisLine(PlayerFacingAnalysis.Severity.INFO,
                        t("line.empty.headline"), t("line.empty.detail"), t("line.empty.tooltip"));
            default ->
                new PlayerFacingAnalysis.AnalysisLine(PlayerFacingAnalysis.Severity.INFO,
                        Component.literal(flag), Component.literal(""), Component.literal(""));
        };
    }

    private static PlayerFacingAnalysis empty() {
        return new PlayerFacingAnalysis(
                PlayerFacingAnalysis.NetworkHealthAssessment.GOOD,
                new PlayerFacingAnalysis.StorageSummary(0, 0, 0, 0, 0, false,
                        Component.literal("low"), List.of()),
                List.of(), List.of());
    }

    private static Component t(String key, Object... args) {
        return Component.translatable(I18N + key, args);
    }
}
