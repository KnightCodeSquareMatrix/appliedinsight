package com.knightcode.appliedstoragesorter.ae2.analysis;

import java.util.List;

/**
 * Analyzer output consumed by GUI, chat, logs, and dumps.
 *
 * <p>Replaces {@link StorageAnalyzerReport} as the single shared analysis
 * result (ADR-012). Differs from the old report in two ways:</p>
 * <ul>
 *   <li>Health flags carry {@link Severity} — consumers no longer guess
 *       whether a flag is critical, a warning, or informational.</li>
 *   <li>Cell classifications are explicit records, not ad-hoc strings.</li>
 * </ul>
 */
public record StorageDiagnosis(
        NetworkSummary summary,
        List<StorageLocation> storageLocations,
        List<ItemDistribution> itemDistributions,
        List<HealthSignal> healthSignals,
        List<ItemDistribution> mostFragmentedItems,
        List<StorageLocation> largestStorages,
        List<ItemDistribution> mixedInternalExternalItems,
        List<CellSignal> cellSignals) {

    // ---------------------------------------------------------------
    // Network-level summary
    // ---------------------------------------------------------------

    public record NetworkSummary(
            int networkNodeCount,
            int storageLocationCount,
            int nonEmptyStorageLocationCount,
            int internalStorageLocationCount,
            int externalStorageLocationCount,
            long internalTotalAmount,
            long externalTotalAmount,
            int uniqueKeyCount,
            int duplicatedKeyCount,
            long totalAmount,
            int totalOccurrenceCount,
            int internalOnlyKeyCount,
            int externalOnlyKeyCount,
            int mixedLocationKeyCount,
            double fragmentationScore,
            String fragmentationLevel) {
    }

    // ---------------------------------------------------------------
    // Per-storage-location snapshot
    // ---------------------------------------------------------------

    public record StorageLocation(
            String locationId,
            String sourceBlockId,
            String attachedStorageBlockId,
            String storageKind,
            String hostPos,
            String attachedStoragePos,
            boolean externalStorageBus,
            int slot,
            int distinctKeyCount,
            long totalAmount,
            double networkAmountShare,
            List<KeyAmount> topKeys,
            Long totalBytes,
            Long usedBytes,
            Integer totalItemTypes,
            Integer remainingItemTypes,
            String cellKind,
            String zoneId) {
    }

    // ---------------------------------------------------------------
    // Per-item distribution
    // ---------------------------------------------------------------

    public record ItemDistribution(
            String keyType,
            String keyId,
            String displayName,
            long totalAmount,
            int locationCount,
            int internalLocationCount,
            int externalLocationCount,
            long maxSingleLocationAmount,
            int maxStackSize) {
    }

    // ---------------------------------------------------------------
    // Key + amount pair
    // ---------------------------------------------------------------

    public record KeyAmount(
            String keyType,
            String keyId,
            String displayName,
            long amount,
            int maxStackSize) {
    }

    // ---------------------------------------------------------------
    // Health signal with severity (replaces raw String flags)
    // ---------------------------------------------------------------

    public enum Severity { CRITICAL, WARNING, INFO }

    /**
     * One health signal the analyzer emits. Consumers render these directly
     * instead of interpreting raw flag strings.
     */
    public record HealthSignal(
            String flag,
            Severity severity,
            String headline,
            String detail,
            String tooltip) {

        public static HealthSignal critical(String flag, String headline, String detail, String tooltip) {
            return new HealthSignal(flag, Severity.CRITICAL, headline, detail, tooltip);
        }

        public static HealthSignal warning(String flag, String headline, String detail, String tooltip) {
            return new HealthSignal(flag, Severity.WARNING, headline, detail, tooltip);
        }

        public static HealthSignal info(String flag, String headline, String detail, String tooltip) {
            return new HealthSignal(flag, Severity.INFO, headline, detail, tooltip);
        }
    }

    // ---------------------------------------------------------------
    // Cell classification signal (replaces scattered cellKind checks)
    // ---------------------------------------------------------------

    /**
     * Classification of one storage cell — unlimited, bulk, component-safe,
     * etc. This is the shared source of truth. No consumer should call
     * {@code cellKind.startsWith("infinite:")} themselves.
     */
    public enum CellKind {
        /** Known infinite/creative cell (cellKind starts with "infinite:"). */
        INFINITE_KNOWN,
        /** Suspected infinite cell (heuristic match, not confirmed). */
        INFINITE_SUSPECTED,
        /** Very large cell (>= 1G total bytes). */
        HUGE,
        /** Normal cell with known capacity. */
        STANDARD,
        /** External storage bus — capacity unknown by definition. */
        EXTERNAL,
        /** Capacity could not be determined. */
        UNKNOWN
    }

    public record CellSignal(
            String locationId,
            CellKind kind,
            String cellItemId,
            Long totalBytes,
            Long usedBytes) {

        public boolean isInfinite() {
            return kind == CellKind.INFINITE_KNOWN || kind == CellKind.INFINITE_SUSPECTED;
        }
    }
}
