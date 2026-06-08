package com.knightcode.appliedstoragesorter.ae2.analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver;
import com.knightcode.appliedstoragesorter.ae2.CellCapacityInspector;
import com.knightcode.appliedstoragesorter.ae2.CellCapacityInspector.CellCapacity;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor;
import com.knightcode.appliedstoragesorter.ae2.zone.RuntimeTopology;
import com.knightcode.appliedstoragesorter.ae2.scan.CellInfo;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

/**
 * Runtime storage analysis engine.
 *
 * <p>Consumes a pre-built {@link RuntimeTopology} (one scan, shared with
 * the Plan path) and produces a single {@link StorageDiagnosis} that all
 * downstream consumers - GUI, chat, file logs, dumps - read from.</p>
 *
 * <p>As of ADR-012 this is the sole online analysis entry point.
 * The old {@code analyze(IGrid)} is kept for Stage 1C migration.</p>
 */
public final class Ae2StorageAnalyzer {

    private static final int TOP_KEY_COUNT_PER_STORAGE = 5;
    private static final int TOP_HIGHLIGHT_COUNT = 20;
    private static final double EXTERNAL_AMOUNT_DOMINANT_THRESHOLD = 0.80D;
    private static final double FRAGMENTATION_SCORE_HIGH_THRESHOLD = 2.5D;
    private static final double FRAGMENTATION_SCORE_MEDIUM_THRESHOLD = 1.25D;
    private static final long HUGE_STORAGE_AMOUNT_THRESHOLD = 1_000_000_000L;
    private static final double HUGE_STORAGE_SHARE_THRESHOLD = 0.50D;
    private static final Set<String> INFINITE_STORAGE_KEYWORDS = Set.of(
            "creative", "unlimited", "infinite");

    private Ae2StorageAnalyzer() {}

    // ------------------------------------------------------------------
    // Legacy entry point - deprecated in ADR-012, removed in Stage 1C
    // ------------------------------------------------------------------

    /** @deprecated Use {@link #analyze(RuntimeTopology, String)} instead. */
    @Deprecated
    public static StorageAnalyzerReport analyze(IGrid grid) {
        List<DriveMachineAccessor.DriveMachine> storageLocations = DriveMachineAccessor.findSupportedDrives(grid);
        List<StorageAnalyzerReport.StorageLocationSummary> locationSummaries = new ArrayList<>();
        Map<AEKey, ItemAccumulator> itemAccumulators = new LinkedHashMap<>();

        int internalStorageLocationCount = 0;
        int externalStorageLocationCount = 0;
        int nonEmptyStorageLocationCount = 0;
        long internalTotalAmount = 0L;
        long externalTotalAmount = 0L;
        long totalAmount = 0L;
        int totalOccurrenceCount = 0;

        for (var location : storageLocations) {
            for (int slot = 0; slot < location.cellCount(); slot++) {
                var storage = location.getCellInventory(slot);
                if (storage == null) continue;

                if (location.isExternalStorageBus()) {
                    externalStorageLocationCount++;
                } else {
                    internalStorageLocationCount++;
                }

                CellCapacityInspector.CellCapacity capacity = null;
                String cellKind = null;
                if (!location.isExternalStorageBus()) {
                    var originalCell = location.getOriginalCellInventory(slot);
                    if (originalCell != null) {
                        capacity = CellCapacityInspector.inspect(originalCell);
                    }
                    cellKind = CellCapacityInspector.resolveCellKind(
                            capacity, location.getCellItemId(slot).orElse(null));
                }
                String zoneId = location.getDeclaredZoneId().orElse(null);

                List<StorageAnalyzerReport.KeyAmountSummary> topKeys = new ArrayList<>();
                int distinctKeyCount = 0;
                long storageTotalAmount = 0L;
                String hostPos = Ae2ControllerTargetResolver.formatBlockPos(location.blockPos());
                String attachedStoragePos = location.attachedStoragePos()
                        .map(Ae2ControllerTargetResolver::formatBlockPos).orElse(null);
                String attachedStorageBlockId = location.attachedStorageBlockId().orElse(null);
                String locationId = buildLocationId(location, slot);

                for (var entry : storage.getAvailableStacks()) {
                    if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEKey key)) continue;
                    long amount = entry.getLongValue();
                    distinctKeyCount++;
                    storageTotalAmount += amount;
                    totalAmount += amount;
                    totalOccurrenceCount++;

                    if (location.isExternalStorageBus()) {
                        externalTotalAmount += amount;
                    } else {
                        internalTotalAmount += amount;
                    }

                    topKeys.add(new StorageAnalyzerReport.KeyAmountSummary(
                            key.getType().getId().toString(), key.getId().toString(),
                            key.getDisplayName().getString(), amount, resolveMaxStackSize(key)));

                    itemAccumulators.computeIfAbsent(key,
                            ignored -> new ItemAccumulator(key.getType().getId().toString(),
                                    key.getId().toString(), key.getDisplayName().getString(),
                                    resolveMaxStackSize(key)))
                            .addOccurrence(amount, location.isExternalStorageBus(), locationId);
                }

                if (storageTotalAmount > 0) nonEmptyStorageLocationCount++;

                topKeys.sort(Comparator.comparingLong(StorageAnalyzerReport.KeyAmountSummary::amount).reversed()
                        .thenComparing(StorageAnalyzerReport.KeyAmountSummary::keyId));
                if (topKeys.size() > TOP_KEY_COUNT_PER_STORAGE) {
                    topKeys = new ArrayList<>(topKeys.subList(0, TOP_KEY_COUNT_PER_STORAGE));
                }

                locationSummaries.add(new StorageAnalyzerReport.StorageLocationSummary(
                        locationId, location.blockId(), attachedStorageBlockId,
                        classifyStorageKind(location), hostPos, attachedStoragePos,
                        location.isExternalStorageBus(), slot, distinctKeyCount, storageTotalAmount, 0.0D,
                        List.copyOf(topKeys),
                        capacity != null ? capacity.totalBytes() : null,
                        capacity != null ? capacity.usedBytes() : null,
                        capacity != null ? capacity.totalItemTypes() : null,
                        capacity != null ? capacity.remainingItemTypes() : null,
                        cellKind, zoneId));
            }
        }

        long finalTotalAmount = totalAmount;
        List<StorageAnalyzerReport.StorageLocationSummary> locationSummariesWithShare = locationSummaries.stream()
                .map(loc -> new StorageAnalyzerReport.StorageLocationSummary(
                        loc.locationId(), loc.sourceBlockId(), loc.attachedStorageBlockId(),
                        loc.storageKind(), loc.hostPos(), loc.attachedStoragePos(),
                        loc.externalStorageBus(), loc.slot(), loc.distinctKeyCount(),
                        loc.totalAmount(), calculateAmountShare(loc.totalAmount(), finalTotalAmount),
                        loc.topKeys(), loc.totalBytes(), loc.usedBytes(),
                        loc.totalItemTypes(), loc.remainingItemTypes(), loc.cellKind(), loc.zoneId()))
                .toList();

        List<StorageAnalyzerReport.ItemDistributionSummary> itemDistributions = itemAccumulators.values().stream()
                .map(ItemAccumulator::toSummary)
                .sorted(Comparator.comparingLong(StorageAnalyzerReport.ItemDistributionSummary::totalAmount).reversed()
                        .thenComparing(StorageAnalyzerReport.ItemDistributionSummary::keyId))
                .toList();

        int duplicatedKeyCount = 0, internalOnlyKeyCount = 0, externalOnlyKeyCount = 0, mixedLocationKeyCount = 0;
        long locationCountSum = 0L;
        List<String> healthFlags = new ArrayList<>();

        for (var item : itemDistributions) {
            locationCountSum += item.locationCount();
            if (item.locationCount() > 1) duplicatedKeyCount++;
            if (item.internalLocationCount() > 0 && item.externalLocationCount() == 0) internalOnlyKeyCount++;
            else if (item.internalLocationCount() == 0 && item.externalLocationCount() > 0) externalOnlyKeyCount++;
            else if (item.internalLocationCount() > 0 && item.externalLocationCount() > 0) mixedLocationKeyCount++;
        }

        double fragmentationScore = itemDistributions.isEmpty()
                ? 0.0D : (double) locationCountSum / (double) itemDistributions.size();
        String fragmentationLevel = classifyFragmentationLevel(fragmentationScore);

        if (duplicatedKeyCount > Math.max(10, itemDistributions.size() / 4))
            healthFlags.add("high_fragmentation");
        if (externalStorageLocationCount > 0 && mixedLocationKeyCount > 0)
            healthFlags.add("mixed_internal_external_distribution");
        if (fragmentationScore >= FRAGMENTATION_SCORE_HIGH_THRESHOLD)
            healthFlags.add("fragmentation_score_high");
        if (totalAmount > 0 && ((double) externalTotalAmount / (double) totalAmount) >= EXTERNAL_AMOUNT_DOMINANT_THRESHOLD)
            healthFlags.add("external_amount_dominant");
        if (internalStorageLocationCount == 0 && externalStorageLocationCount > 0)
            healthFlags.add("external_only_storage_network");
        if (duplicatedKeyCount == 0)
            healthFlags.add("low_fragmentation");
        if (nonEmptyStorageLocationCount < locationSummariesWithShare.size())
            healthFlags.add("has_empty_storage_locations");

        List<StorageAnalyzerReport.StorageLocationSummary> sortedLocations = locationSummariesWithShare.stream()
                .sorted(Comparator.comparingLong(StorageAnalyzerReport.StorageLocationSummary::totalAmount).reversed()
                        .thenComparing(StorageAnalyzerReport.StorageLocationSummary::hostPos)
                        .thenComparingInt(StorageAnalyzerReport.StorageLocationSummary::slot))
                .toList();

        List<StorageAnalyzerReport.StorageSemanticCandidate> suspectedSemanticCandidates = sortedLocations.stream()
                .map(Ae2StorageAnalyzer::toSuspectedInfiniteCandidate)
                .filter(c -> c != null).limit(TOP_HIGHLIGHT_COUNT).toList();
        if (!suspectedSemanticCandidates.isEmpty())
            healthFlags.add("suspected_infinite_storage_present");

        List<StorageAnalyzerReport.ItemDistributionSummary> mostFragmentedItems = itemDistributions.stream()
                .filter(item -> item.locationCount() > 1)
                .sorted(Comparator.comparingInt(StorageAnalyzerReport.ItemDistributionSummary::locationCount).reversed()
                        .thenComparing(Comparator.comparingLong(
                                StorageAnalyzerReport.ItemDistributionSummary::totalAmount).reversed())
                        .thenComparing(StorageAnalyzerReport.ItemDistributionSummary::keyId))
                .limit(TOP_HIGHLIGHT_COUNT).toList();

        List<StorageAnalyzerReport.StorageLocationSummary> largestStorages = sortedLocations.stream()
                .filter(loc -> loc.totalAmount() > 0).limit(TOP_HIGHLIGHT_COUNT).toList();

        List<StorageAnalyzerReport.ItemDistributionSummary> mixedItems = itemDistributions.stream()
                .filter(item -> item.internalLocationCount() > 0 && item.externalLocationCount() > 0)
                .sorted(Comparator.comparingInt(StorageAnalyzerReport.ItemDistributionSummary::locationCount).reversed()
                        .thenComparing(Comparator.comparingLong(
                                StorageAnalyzerReport.ItemDistributionSummary::totalAmount).reversed())
                        .thenComparing(StorageAnalyzerReport.ItemDistributionSummary::keyId))
                .limit(TOP_HIGHLIGHT_COUNT).toList();

        return new StorageAnalyzerReport(
                new StorageAnalyzerReport.NetworkSummary(grid.size(), sortedLocations.size(),
                        nonEmptyStorageLocationCount, internalStorageLocationCount,
                        externalStorageLocationCount, internalTotalAmount, externalTotalAmount,
                        itemDistributions.size(), duplicatedKeyCount, totalAmount,
                        totalOccurrenceCount, internalOnlyKeyCount, externalOnlyKeyCount,
                        mixedLocationKeyCount, fragmentationScore, fragmentationLevel),
                List.copyOf(sortedLocations), itemDistributions, List.copyOf(healthFlags),
                mostFragmentedItems, largestStorages, mixedItems, suspectedSemanticCandidates);
    }

    // ------------------------------------------------------------------
    // ADR-012 entry point - consumes RuntimeTopology, produces StorageDiagnosis
    // ------------------------------------------------------------------

    /**
     * Analyze a pre-built topology and produce a shared diagnosis.
     *
     * @param topology    the runtime topology (one scan, shared with Plan path)
     * @param dimensionId the dimension identifier for building location IDs
     * @return a {@link StorageDiagnosis} ready for all consumers
     */
    public static StorageDiagnosis analyze(RuntimeTopology topology, String dimensionId) {
        List<CellInfo> allCells = topology.allCells();
        if (allCells.isEmpty()) return emptyDiagnosis(dimensionId);

        List<StorageDiagnosis.StorageLocation> locationSummaries = new ArrayList<>();
        Map<AEKey, ItemAccumulator> itemAccumulators = new LinkedHashMap<>();

        int internalCount = 0, externalCount = 0, nonEmptyCount = 0;
        long internalAmount = 0L, externalAmount = 0L, totalAmount = 0L;
        int totalOccurrenceCount = 0;

        for (CellInfo cell : allCells) {
            var storage = cell.storage();
            if (storage == null) continue;

            boolean isExternal = isExternalStorage(cell);
            if (isExternal) { externalCount++; } else { internalCount++; }

            CellCapacity capacity = cell.capacity();
            String cellKind = resolveCellKind(capacity, cell.cellItemId());
            String hostPos = Ae2ControllerTargetResolver.formatBlockPos(cell.reference().drivePos());
            String attachedPos = cell.reference().attachedStoragePos() != null
                    ? Ae2ControllerTargetResolver.formatBlockPos(cell.reference().attachedStoragePos()) : null;
            String locationId = buildLocationId(dimensionId, cell);

            List<StorageDiagnosis.KeyAmount> topKeys = new ArrayList<>();
            int distinctKeyCount = 0;
            long storageTotalAmount = 0L;

            for (var entry : storage.getAvailableStacks()) {
                if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEKey key)) continue;
                long amount = entry.getLongValue();
                distinctKeyCount++;
                storageTotalAmount += amount;
                totalAmount += amount;
                totalOccurrenceCount++;

                if (isExternal) { externalAmount += amount; } else { internalAmount += amount; }

                topKeys.add(new StorageDiagnosis.KeyAmount(key.getType().getId().toString(),
                        key.getId().toString(), key.getDisplayName().getString(), amount,
                        resolveMaxStackSize(key)));

                itemAccumulators.computeIfAbsent(key,
                        ignored -> new ItemAccumulator(key.getType().getId().toString(),
                                key.getId().toString(), key.getDisplayName().getString(),
                                resolveMaxStackSize(key)))
                        .addOccurrence(amount, isExternal, locationId);
            }

            if (storageTotalAmount > 0) nonEmptyCount++;

            topKeys.sort(Comparator.comparingLong(StorageDiagnosis.KeyAmount::amount).reversed()
                    .thenComparing(StorageDiagnosis.KeyAmount::keyId));
            if (topKeys.size() > TOP_KEY_COUNT_PER_STORAGE)
                topKeys = new ArrayList<>(topKeys.subList(0, TOP_KEY_COUNT_PER_STORAGE));

            locationSummaries.add(new StorageDiagnosis.StorageLocation(
                    locationId, cell.sourceBlockId(), null,
                    classifyStorageKind(cell), hostPos, attachedPos, isExternal,
                    cell.reference().slot(), distinctKeyCount, storageTotalAmount, 0.0D,
                    List.copyOf(topKeys),
                    capacity != null ? capacity.totalBytes() : null,
                    capacity != null ? capacity.usedBytes() : null,
                    capacity != null ? capacity.totalItemTypes() : null,
                    capacity != null ? capacity.remainingItemTypes() : null,
                    cellKind, cell.zoneId()));
        }

        long finalTotal = totalAmount;
        List<StorageDiagnosis.StorageLocation> withShare = locationSummaries.stream()
                .map(loc -> new StorageDiagnosis.StorageLocation(
                        loc.locationId(), loc.sourceBlockId(), loc.attachedStorageBlockId(),
                        loc.storageKind(), loc.hostPos(), loc.attachedStoragePos(),
                        loc.externalStorageBus(), loc.slot(), loc.distinctKeyCount(),
                        loc.totalAmount(), calculateAmountShare(loc.totalAmount(), finalTotal),
                        loc.topKeys(), loc.totalBytes(), loc.usedBytes(),
                        loc.totalItemTypes(), loc.remainingItemTypes(), loc.cellKind(), loc.zoneId()))
                .toList();

        List<StorageDiagnosis.ItemDistribution> itemDists = itemAccumulators.values().stream()
                .map(ItemAccumulator::toDistribution)
                .sorted(Comparator.comparingLong(StorageDiagnosis.ItemDistribution::totalAmount).reversed()
                        .thenComparing(StorageDiagnosis.ItemDistribution::keyId))
                .toList();

        int duplicatedKeys = 0, internalOnlyKeys = 0, externalOnlyKeys = 0, mixedKeysCount = 0;
        long locationCountSum = 0L;
        for (var item : itemDists) {
            locationCountSum += item.locationCount();
            if (item.locationCount() > 1) duplicatedKeys++;
            if (item.internalLocationCount() > 0 && item.externalLocationCount() == 0) internalOnlyKeys++;
            else if (item.internalLocationCount() == 0 && item.externalLocationCount() > 0) externalOnlyKeys++;
            else if (item.internalLocationCount() > 0 && item.externalLocationCount() > 0) mixedKeysCount++;
        }

        double fragmentationScore = itemDists.isEmpty()
                ? 0.0D : (double) locationCountSum / (double) itemDists.size();
        String fragmentationLevel = classifyFragmentationLevel(fragmentationScore);

        List<StorageDiagnosis.HealthSignal> healthSignals = buildHealthSignals(
                duplicatedKeys, itemDists.size(), fragmentationScore,
                externalCount, internalCount, mixedKeysCount,
                totalAmount, externalAmount, nonEmptyCount, withShare.size());

        List<StorageDiagnosis.CellSignal> cellSignals = buildCellSignals(withShare);
        List<StorageDiagnosis.CellSignal> suspected = cellSignals.stream()
                .filter(cs -> cs.kind() == StorageDiagnosis.CellKind.INFINITE_SUSPECTED)
                .limit(TOP_HIGHLIGHT_COUNT).toList();
        if (!suspected.isEmpty()) {
            healthSignals.add(StorageDiagnosis.HealthSignal.warning(
                    "suspected_infinite_storage_present",
                    "Suspected infinite storage detected",
                    suspected.size() + " storage location(s) may have unlimited capacity.",
                    "These cells may skew fragmentation analysis. Review manually."));
        }

        List<StorageDiagnosis.StorageLocation> sorted = withShare.stream()
                .sorted(Comparator.comparingLong(StorageDiagnosis.StorageLocation::totalAmount).reversed()
                        .thenComparing(StorageDiagnosis.StorageLocation::hostPos)
                        .thenComparingInt(StorageDiagnosis.StorageLocation::slot))
                .toList();

        List<StorageDiagnosis.ItemDistribution> mostFragmented = itemDists.stream()
                .filter(item -> item.locationCount() > 1)
                .sorted(Comparator.comparingInt(StorageDiagnosis.ItemDistribution::locationCount).reversed()
                        .thenComparing(Comparator.comparingLong(
                                StorageDiagnosis.ItemDistribution::totalAmount).reversed())
                        .thenComparing(StorageDiagnosis.ItemDistribution::keyId))
                .limit(TOP_HIGHLIGHT_COUNT).toList();

        List<StorageDiagnosis.StorageLocation> largest = sorted.stream()
                .filter(loc -> loc.totalAmount() > 0).limit(TOP_HIGHLIGHT_COUNT).toList();

        List<StorageDiagnosis.ItemDistribution> mixedItems = itemDists.stream()
                .filter(item -> item.internalLocationCount() > 0 && item.externalLocationCount() > 0)
                .sorted(Comparator.comparingInt(StorageDiagnosis.ItemDistribution::locationCount).reversed()
                        .thenComparing(Comparator.comparingLong(
                                StorageDiagnosis.ItemDistribution::totalAmount).reversed())
                        .thenComparing(StorageDiagnosis.ItemDistribution::keyId))
                .limit(TOP_HIGHLIGHT_COUNT).toList();

        return new StorageDiagnosis(
                new StorageDiagnosis.NetworkSummary(topology.allCells().size(), sorted.size(),
                        nonEmptyCount, internalCount, externalCount,
                        internalAmount, externalAmount, itemDists.size(), duplicatedKeys,
                        totalAmount, totalOccurrenceCount,
                        internalOnlyKeys, externalOnlyKeys, mixedKeysCount,
                        fragmentationScore, fragmentationLevel),
                List.copyOf(sorted), itemDists, healthSignals,
                mostFragmented, largest, mixedItems, cellSignals);
    }

    // ------------------------------------------------------------------
    // ADR-012 helper methods (consume CellInfo, produce StorageDiagnosis types)
    // ------------------------------------------------------------------

    private static String buildLocationId(String dimensionId, CellInfo cell) {
        DriveCellReference ref = cell.reference();
        String attached = ref.attachedStoragePos() != null
                ? Ae2ControllerTargetResolver.formatBlockPos(ref.attachedStoragePos()) : "<none>";
        return dimensionId + "|" + cell.sourceBlockId() + "|"
                + Ae2ControllerTargetResolver.formatBlockPos(ref.drivePos()) + "|"
                + attached + "|slot=" + ref.slot();
    }

    private static boolean isExternalStorage(CellInfo cell) {
        return cell.sourceBlockId() != null && cell.sourceBlockId().contains("storage_bus");
    }

    private static String resolveCellKind(CellCapacity capacity, String cellItemId) {
        if (capacity != null && capacity.totalBytes() > 0)
            return CellCapacityInspector.describeCellKind(capacity.totalBytes());
        if (cellItemId != null && CellCapacityInspector.isLikelyInfiniteCell(null, cellItemId))
            return "infinite:" + cellItemId;
        return null;
    }

    private static String classifyStorageKind(CellInfo cell) {
        if (isExternalStorage(cell)) return "external_storage_bus";
        String blockId = cell.sourceBlockId();
        if ("appliedinsight:digital_asset_vault".equals(blockId))
            return "internal_dav_cell";
        return "internal_drive_cell";
    }

    private static StorageDiagnosis emptyDiagnosis(String dimensionId) {
        return new StorageDiagnosis(
                new StorageDiagnosis.NetworkSummary(0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0.0D, "low"),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static List<StorageDiagnosis.HealthSignal> buildHealthSignals(
            int duplicatedKeys, int totalKeys, double fragScore,
            int externalCount, int internalCount, int mixedKeys,
            long totalAmount, long externalAmount, int nonEmpty, int totalCells) {

        List<StorageDiagnosis.HealthSignal> signals = new ArrayList<>();

        if (duplicatedKeys > Math.max(10, totalKeys / 4)) {
            signals.add(StorageDiagnosis.HealthSignal.warning("high_fragmentation",
                    "High item fragmentation",
                    duplicatedKeys + " item type(s) stored across 2+ locations",
                    "Consider running /sorter merge to consolidate duplicate items."));
        }
        if (externalCount > 0 && mixedKeys > 0) {
            signals.add(StorageDiagnosis.HealthSignal.info("mixed_internal_external_distribution",
                    "Mixed internal/external storage",
                    "Items are split between drive cells and external storage buses.",
                    "This may be intentional but complicates sorting and analysis."));
        }
        if (fragScore >= FRAGMENTATION_SCORE_HIGH_THRESHOLD) {
            signals.add(StorageDiagnosis.HealthSignal.warning("fragmentation_score_high",
                    "Fragmentation score is high",
                    String.format(Locale.ROOT, "Score: %.2f (threshold: %.2f)",
                            fragScore, FRAGMENTATION_SCORE_HIGH_THRESHOLD),
                    "The average item type appears in many different cells."));
        }
        if (totalAmount > 0 && ((double) externalAmount / (double) totalAmount) >= EXTERNAL_AMOUNT_DOMINANT_THRESHOLD) {
            signals.add(StorageDiagnosis.HealthSignal.info("external_amount_dominant",
                    "External storage dominant",
                    String.format(Locale.ROOT, "%.0f%% of total items are in external storage.",
                            100.0D * externalAmount / totalAmount),
                    "If this is unexpected, review your storage bus configuration."));
        }
        if (internalCount == 0 && externalCount > 0) {
            signals.add(StorageDiagnosis.HealthSignal.info("external_only_storage_network",
                    "External-only storage network",
                    "No internal drive cells detected.",
                    "Internal cells are required for the sorter to function."));
        }
        if (duplicatedKeys == 0) {
            signals.add(StorageDiagnosis.HealthSignal.info("low_fragmentation",
                    "Storage is well-organized",
                    "Every item type appears in exactly one location.", ""));
        }
        if (nonEmpty < totalCells) {
            signals.add(StorageDiagnosis.HealthSignal.info("has_empty_storage_locations",
                    "Empty storage locations exist",
                    (totalCells - nonEmpty) + " cell slot(s) are empty.",
                    "Unused capacity is available for future storage expansion."));
        }
        return signals;
    }

    private static List<StorageDiagnosis.CellSignal> buildCellSignals(
            List<StorageDiagnosis.StorageLocation> locations) {
        List<StorageDiagnosis.CellSignal> signals = new ArrayList<>();
        for (var loc : locations) signals.add(classifyCell(loc));
        return List.copyOf(signals);
    }

    private static StorageDiagnosis.CellSignal classifyCell(StorageDiagnosis.StorageLocation loc) {
        if (loc.cellKind() != null && loc.cellKind().startsWith("infinite:"))
            return new StorageDiagnosis.CellSignal(loc.locationId(),
                    StorageDiagnosis.CellKind.INFINITE_KNOWN,
                    loc.sourceBlockId(), loc.totalBytes(), loc.usedBytes());
        if (isSuspectedInfinite(loc))
            return new StorageDiagnosis.CellSignal(loc.locationId(),
                    StorageDiagnosis.CellKind.INFINITE_SUSPECTED,
                    loc.sourceBlockId(), loc.totalBytes(), loc.usedBytes());
        if (loc.externalStorageBus())
            return new StorageDiagnosis.CellSignal(loc.locationId(),
                    StorageDiagnosis.CellKind.EXTERNAL,
                    loc.sourceBlockId(), loc.totalBytes(), loc.usedBytes());
        if (loc.totalBytes() != null && loc.totalBytes() >= HUGE_STORAGE_AMOUNT_THRESHOLD)
            return new StorageDiagnosis.CellSignal(loc.locationId(),
                    StorageDiagnosis.CellKind.HUGE,
                    loc.sourceBlockId(), loc.totalBytes(), loc.usedBytes());
        if (loc.totalBytes() != null && loc.totalBytes() > 0)
            return new StorageDiagnosis.CellSignal(loc.locationId(),
                    StorageDiagnosis.CellKind.STANDARD,
                    loc.sourceBlockId(), loc.totalBytes(), loc.usedBytes());
        return new StorageDiagnosis.CellSignal(loc.locationId(),
                StorageDiagnosis.CellKind.UNKNOWN,
                loc.sourceBlockId(), loc.totalBytes(), loc.usedBytes());
    }

    private static boolean isSuspectedInfinite(StorageDiagnosis.StorageLocation loc) {
        boolean kw = containsKeyword(loc.sourceBlockId()) || containsKeyword(loc.storageKind());
        boolean huge = loc.totalAmount() >= HUGE_STORAGE_AMOUNT_THRESHOLD
                || loc.networkAmountShare() >= HUGE_STORAGE_SHARE_THRESHOLD;
        return kw && huge;
    }

    private static boolean containsKeyword(String value) {
        if (value == null || value.isBlank()) return false;
        String n = value.toLowerCase(Locale.ROOT);
        return INFINITE_STORAGE_KEYWORDS.stream().anyMatch(n::contains);
    }

    // ------------------------------------------------------------------
    // Legacy helpers (shared)
    // ------------------------------------------------------------------

    @Deprecated
    private static StorageAnalyzerReport.StorageSemanticCandidate toSuspectedInfiniteCandidate(
            StorageAnalyzerReport.StorageLocationSummary location) {
        List<String> matchedHeuristics = new ArrayList<>();
        if (containsKeyword(location.sourceBlockId()))
            matchedHeuristics.add("source_block_id_contains_infinite_keyword");
        if (containsKeyword(location.attachedStorageBlockId()))
            matchedHeuristics.add("attached_storage_block_id_contains_infinite_keyword");
        if (location.totalAmount() >= HUGE_STORAGE_AMOUNT_THRESHOLD)
            matchedHeuristics.add("total_amount_exceeds_huge_threshold");
        if (location.networkAmountShare() >= HUGE_STORAGE_SHARE_THRESHOLD)
            matchedHeuristics.add("network_amount_share_exceeds_huge_threshold");

        boolean kw = matchedHeuristics.stream().anyMatch(v -> v.contains("keyword"));
        boolean huge = matchedHeuristics.stream().anyMatch(v -> v.contains("huge_threshold"));
        if (!kw || !huge) return null;

        return new StorageAnalyzerReport.StorageSemanticCandidate(
                "suspected_infinite_storage", "suspected_infinite_storage",
                location.locationId(), location.sourceBlockId(),
                location.attachedStorageBlockId(), location.storageKind(),
                location.hostPos(), location.attachedStoragePos(),
                location.totalAmount(), location.networkAmountShare(),
                calculateSuspicionScore(matchedHeuristics), List.copyOf(matchedHeuristics));
    }

    private static double calculateSuspicionScore(List<String> matchedHeuristics) {
        double score = 0.50D;
        if (matchedHeuristics.stream().anyMatch(v -> v.contains("block_id_contains"))) score += 0.20D;
        if (matchedHeuristics.stream().anyMatch(v -> v.contains("attached_storage"))) score += 0.20D;
        if (matchedHeuristics.stream().anyMatch(v -> v.contains("share_exceeds"))) score += 0.10D;
        return Math.min(score, 0.99D);
    }

    @Deprecated
    private static String buildLocationId(DriveMachineAccessor.DriveMachine location, int slot) {
        String dim = location.blockEntity().getLevel() != null
                ? location.blockEntity().getLevel().dimension().location().toString()
                : "unknown:unknown";
        String attached = location.attachedStoragePos()
                .map(Ae2ControllerTargetResolver::formatBlockPos).orElse("<none>");
        return dim + "|" + location.blockId() + "|"
                + Ae2ControllerTargetResolver.formatBlockPos(location.blockPos()) + "|"
                + attached + "|slot=" + slot;
    }

    private static double calculateAmountShare(long storageAmount, long totalAmount) {
        if (storageAmount <= 0 || totalAmount <= 0) return 0.0D;
        return (double) storageAmount / (double) totalAmount;
    }

    private static int resolveMaxStackSize(AEKey key) {
        if (key instanceof AEItemKey itemKey) return itemKey.getReadOnlyStack().getMaxStackSize();
        return 1;
    }

    private static String classifyFragmentationLevel(double fragmentationScore) {
        if (fragmentationScore >= FRAGMENTATION_SCORE_HIGH_THRESHOLD) return "high";
        if (fragmentationScore >= FRAGMENTATION_SCORE_MEDIUM_THRESHOLD) return "medium";
        return "low";
    }

    @Deprecated
    private static String classifyStorageKind(DriveMachineAccessor.DriveMachine location) {
        if (location.isExternalStorageBus()) return "external_storage_bus";
        String blockId = location.blockId();
        if ("appliedinsight:digital_asset_vault".equals(blockId))
            return "internal_dav_cell";
        return "internal_drive_cell";
    }

    // ------------------------------------------------------------------
    // Item accumulator (shared by both paths)
    // ------------------------------------------------------------------

    private static final class ItemAccumulator {
        private final String keyType, keyId, displayName;
        private final int maxStackSize;
        private long totalAmount, maxSingleLocationAmount;
        private int internalLocationCount, externalLocationCount;
        private final Map<String, Boolean> countedLocations = new LinkedHashMap<>();

        ItemAccumulator(String keyType, String keyId, String displayName, int maxStackSize) {
            this.keyType = keyType;
            this.keyId = keyId;
            this.displayName = displayName;
            this.maxStackSize = maxStackSize;
        }

        void addOccurrence(long amount, boolean external, String locationId) {
            totalAmount += amount;
            maxSingleLocationAmount = Math.max(maxSingleLocationAmount, amount);
            if (countedLocations.putIfAbsent(locationId, external) == null) {
                if (external) externalLocationCount++;
                else internalLocationCount++;
            }
        }

        StorageAnalyzerReport.ItemDistributionSummary toSummary() {
            return new StorageAnalyzerReport.ItemDistributionSummary(
                    keyType, keyId, displayName, totalAmount,
                    internalLocationCount + externalLocationCount,
                    internalLocationCount, externalLocationCount,
                    maxSingleLocationAmount, maxStackSize);
        }

        StorageDiagnosis.ItemDistribution toDistribution() {
            return new StorageDiagnosis.ItemDistribution(
                    keyType, keyId, displayName, totalAmount,
                    internalLocationCount + externalLocationCount,
                    internalLocationCount, externalLocationCount,
                    maxSingleLocationAmount, maxStackSize);
        }
    }
}
