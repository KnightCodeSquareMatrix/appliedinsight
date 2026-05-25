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
import com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

public final class Ae2StorageAnalyzer {
    private static final int TOP_KEY_COUNT_PER_STORAGE = 5;
    private static final int TOP_HIGHLIGHT_COUNT = 20;
    private static final double EXTERNAL_AMOUNT_DOMINANT_THRESHOLD = 0.80D;
    private static final double FRAGMENTATION_SCORE_HIGH_THRESHOLD = 2.5D;
    private static final double FRAGMENTATION_SCORE_MEDIUM_THRESHOLD = 1.25D;
    private static final long HUGE_STORAGE_AMOUNT_THRESHOLD = 1_000_000_000L;
    private static final double HUGE_STORAGE_SHARE_THRESHOLD = 0.50D;
    private static final Set<String> INFINITE_STORAGE_KEYWORDS = Set.of(
            "creative",
            "unlimited",
            "infinite",
            "无限",
            "创造");

    private Ae2StorageAnalyzer() {
    }

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
                if (storage == null) {
                    continue;
                }

                if (location.isExternalStorageBus()) {
                    externalStorageLocationCount++;
                } else {
                    internalStorageLocationCount++;
                }

                // Read cell capacity from original StorageCell
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
                        .map(Ae2ControllerTargetResolver::formatBlockPos)
                        .orElse(null);
                String attachedStorageBlockId = location.attachedStorageBlockId().orElse(null);
                String locationId = buildLocationId(location, slot);

                for (var entry : storage.getAvailableStacks()) {
                    if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEKey key)) {
                        continue;
                    }

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
                            key.getType().getId().toString(),
                            key.getId().toString(),
                            key.getDisplayName().getString(),
                            amount,
                            resolveMaxStackSize(key)));

                    itemAccumulators.computeIfAbsent(key,
                            ignored -> new ItemAccumulator(
                                    key.getType().getId().toString(),
                                    key.getId().toString(),
                                    key.getDisplayName().getString(),
                                    resolveMaxStackSize(key)))
                            .addOccurrence(amount, location.isExternalStorageBus(), locationId);
                }

                if (storageTotalAmount > 0) {
                    nonEmptyStorageLocationCount++;
                }

                topKeys.sort(Comparator.comparingLong(StorageAnalyzerReport.KeyAmountSummary::amount).reversed()
                        .thenComparing(StorageAnalyzerReport.KeyAmountSummary::keyId));
                if (topKeys.size() > TOP_KEY_COUNT_PER_STORAGE) {
                    topKeys = new ArrayList<>(topKeys.subList(0, TOP_KEY_COUNT_PER_STORAGE));
                }

                locationSummaries.add(new StorageAnalyzerReport.StorageLocationSummary(
                        locationId,
                        location.blockId(),
                        attachedStorageBlockId,
                        classifyStorageKind(location),
                        hostPos,
                        attachedStoragePos,
                        location.isExternalStorageBus(),
                        slot,
                        distinctKeyCount,
                        storageTotalAmount,
                        0.0D,
                        List.copyOf(topKeys),
                        capacity != null ? capacity.totalBytes() : null,
                        capacity != null ? capacity.usedBytes() : null,
                        capacity != null ? capacity.totalItemTypes() : null,
                        capacity != null ? capacity.remainingItemTypes() : null,
                        cellKind,
                        zoneId));
            }
        }

        long finalTotalAmount = totalAmount;
        List<StorageAnalyzerReport.StorageLocationSummary> locationSummariesWithShare = locationSummaries.stream()
                .map(location -> new StorageAnalyzerReport.StorageLocationSummary(
                        location.locationId(),
                        location.sourceBlockId(),
                        location.attachedStorageBlockId(),
                        location.storageKind(),
                        location.hostPos(),
                        location.attachedStoragePos(),
                        location.externalStorageBus(),
                        location.slot(),
                        location.distinctKeyCount(),
                        location.totalAmount(),
                        calculateAmountShare(location.totalAmount(), finalTotalAmount),
                        location.topKeys(),
                        location.totalBytes(),
                        location.usedBytes(),
                        location.totalItemTypes(),
                        location.remainingItemTypes(),
                        location.cellKind(),
                        location.zoneId()))
                .toList();

        List<StorageAnalyzerReport.ItemDistributionSummary> itemDistributions = itemAccumulators.values().stream()
                .map(ItemAccumulator::toSummary)
                .sorted(Comparator.comparingLong(StorageAnalyzerReport.ItemDistributionSummary::totalAmount).reversed()
                        .thenComparing(StorageAnalyzerReport.ItemDistributionSummary::keyId))
                .toList();

        int duplicatedKeyCount = 0;
        int internalOnlyKeyCount = 0;
        int externalOnlyKeyCount = 0;
        int mixedLocationKeyCount = 0;
        long locationCountSum = 0L;
        List<String> healthFlags = new ArrayList<>();

        for (var item : itemDistributions) {
            locationCountSum += item.locationCount();
            if (item.locationCount() > 1) {
                duplicatedKeyCount++;
            }
            if (item.internalLocationCount() > 0 && item.externalLocationCount() == 0) {
                internalOnlyKeyCount++;
            } else if (item.internalLocationCount() == 0 && item.externalLocationCount() > 0) {
                externalOnlyKeyCount++;
            } else if (item.internalLocationCount() > 0 && item.externalLocationCount() > 0) {
                mixedLocationKeyCount++;
            }
        }

        double fragmentationScore = itemDistributions.isEmpty()
                ? 0.0D
                : (double) locationCountSum / (double) itemDistributions.size();
        String fragmentationLevel = classifyFragmentationLevel(fragmentationScore);

        if (duplicatedKeyCount > Math.max(10, itemDistributions.size() / 4)) {
            healthFlags.add("high_fragmentation");
        }
        if (externalStorageLocationCount > 0 && mixedLocationKeyCount > 0) {
            healthFlags.add("mixed_internal_external_distribution");
        }
        if (fragmentationScore >= FRAGMENTATION_SCORE_HIGH_THRESHOLD) {
            healthFlags.add("fragmentation_score_high");
        }
        if (totalAmount > 0 && ((double) externalTotalAmount / (double) totalAmount) >= EXTERNAL_AMOUNT_DOMINANT_THRESHOLD) {
            healthFlags.add("external_amount_dominant");
        }
        if (internalStorageLocationCount == 0 && externalStorageLocationCount > 0) {
            healthFlags.add("external_only_storage_network");
        }
        if (duplicatedKeyCount == 0) {
            healthFlags.add("low_fragmentation");
        }
        if (nonEmptyStorageLocationCount < locationSummariesWithShare.size()) {
            healthFlags.add("has_empty_storage_locations");
        }

        List<StorageAnalyzerReport.StorageLocationSummary> sortedLocations = locationSummariesWithShare.stream()
                .sorted(Comparator
                        .comparingLong(StorageAnalyzerReport.StorageLocationSummary::totalAmount).reversed()
                        .thenComparing(StorageAnalyzerReport.StorageLocationSummary::hostPos)
                        .thenComparingInt(StorageAnalyzerReport.StorageLocationSummary::slot))
                .toList();

        List<StorageAnalyzerReport.StorageSemanticCandidate> suspectedSemanticCandidates = sortedLocations.stream()
                .map(Ae2StorageAnalyzer::toSuspectedInfiniteCandidate)
                .filter(candidate -> candidate != null)
                .limit(TOP_HIGHLIGHT_COUNT)
                .toList();
        if (!suspectedSemanticCandidates.isEmpty()) {
            healthFlags.add("suspected_infinite_storage_present");
        }

        List<StorageAnalyzerReport.ItemDistributionSummary> mostFragmentedItems = itemDistributions.stream()
                .filter(item -> item.locationCount() > 1)
                .sorted(Comparator.comparingInt(StorageAnalyzerReport.ItemDistributionSummary::locationCount).reversed()
                        .thenComparing(Comparator
                                .comparingLong(StorageAnalyzerReport.ItemDistributionSummary::totalAmount)
                                .reversed())
                        .thenComparing(StorageAnalyzerReport.ItemDistributionSummary::keyId))
                .limit(TOP_HIGHLIGHT_COUNT)
                .toList();

        List<StorageAnalyzerReport.StorageLocationSummary> largestStorages = sortedLocations.stream()
                .filter(location -> location.totalAmount() > 0)
                .limit(TOP_HIGHLIGHT_COUNT)
                .toList();

        List<StorageAnalyzerReport.ItemDistributionSummary> mixedItems = itemDistributions.stream()
                .filter(item -> item.internalLocationCount() > 0 && item.externalLocationCount() > 0)
                .sorted(Comparator.comparingInt(StorageAnalyzerReport.ItemDistributionSummary::locationCount).reversed()
                        .thenComparing(Comparator
                                .comparingLong(StorageAnalyzerReport.ItemDistributionSummary::totalAmount)
                                .reversed())
                        .thenComparing(StorageAnalyzerReport.ItemDistributionSummary::keyId))
                .limit(TOP_HIGHLIGHT_COUNT)
                .toList();

        return new StorageAnalyzerReport(
                new StorageAnalyzerReport.NetworkSummary(
                        grid.size(),
                        sortedLocations.size(),
                        nonEmptyStorageLocationCount,
                        internalStorageLocationCount,
                        externalStorageLocationCount,
                        internalTotalAmount,
                        externalTotalAmount,
                        itemDistributions.size(),
                        duplicatedKeyCount,
                        totalAmount,
                        totalOccurrenceCount,
                        internalOnlyKeyCount,
                        externalOnlyKeyCount,
                        mixedLocationKeyCount,
                        fragmentationScore,
                        fragmentationLevel),
                List.copyOf(sortedLocations),
                itemDistributions,
                List.copyOf(healthFlags),
                mostFragmentedItems,
                largestStorages,
                mixedItems,
                suspectedSemanticCandidates);
    }

    private static StorageAnalyzerReport.StorageSemanticCandidate toSuspectedInfiniteCandidate(
            StorageAnalyzerReport.StorageLocationSummary location) {
        List<String> matchedHeuristics = new ArrayList<>();
        if (containsInfiniteKeyword(location.sourceBlockId())) {
            matchedHeuristics.add("source_block_id_contains_infinite_keyword");
        }
        if (containsInfiniteKeyword(location.attachedStorageBlockId())) {
            matchedHeuristics.add("attached_storage_block_id_contains_infinite_keyword");
        }
        if (location.totalAmount() >= HUGE_STORAGE_AMOUNT_THRESHOLD) {
            matchedHeuristics.add("total_amount_exceeds_huge_threshold");
        }
        if (location.networkAmountShare() >= HUGE_STORAGE_SHARE_THRESHOLD) {
            matchedHeuristics.add("network_amount_share_exceeds_huge_threshold");
        }

        boolean keywordMatched = matchedHeuristics.stream().anyMatch(value -> value.contains("keyword"));
        boolean hugeAmountMatched = matchedHeuristics.stream().anyMatch(value -> value.contains("huge_threshold"));
        if (!keywordMatched || !hugeAmountMatched) {
            return null;
        }

        return new StorageAnalyzerReport.StorageSemanticCandidate(
                "suspected_infinite_storage",
                "suspected_infinite_storage",
                location.locationId(),
                location.sourceBlockId(),
                location.attachedStorageBlockId(),
                location.storageKind(),
                location.hostPos(),
                location.attachedStoragePos(),
                location.totalAmount(),
                location.networkAmountShare(),
                calculateSuspicionScore(matchedHeuristics),
                List.copyOf(matchedHeuristics));
    }

    private static double calculateSuspicionScore(List<String> matchedHeuristics) {
        double score = 0.50D;
        if (matchedHeuristics.stream().anyMatch(value -> value.contains("source_block_id_contains_infinite_keyword"))) {
            score += 0.20D;
        }
        if (matchedHeuristics.stream().anyMatch(value -> value.contains("attached_storage_block_id_contains_infinite_keyword"))) {
            score += 0.20D;
        }
        if (matchedHeuristics.stream().anyMatch(value -> value.contains("network_amount_share_exceeds_huge_threshold"))) {
            score += 0.10D;
        }
        return Math.min(score, 0.99D);
    }

    private static boolean containsInfiniteKeyword(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return INFINITE_STORAGE_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private static String buildLocationId(DriveMachineAccessor.DriveMachine location, int slot) {
        String dimensionId = location.blockEntity().getLevel() != null
                ? location.blockEntity().getLevel().dimension().location().toString()
                : "unknown:unknown";
        String attachedStoragePos = location.attachedStoragePos()
                .map(Ae2ControllerTargetResolver::formatBlockPos)
                .orElse("<none>");
        return dimensionId
                + "|" + location.blockId()
                + "|" + Ae2ControllerTargetResolver.formatBlockPos(location.blockPos())
                + "|" + attachedStoragePos
                + "|slot=" + slot;
    }

    private static double calculateAmountShare(long storageAmount, long totalAmount) {
        if (storageAmount <= 0 || totalAmount <= 0) {
            return 0.0D;
        }
        return (double) storageAmount / (double) totalAmount;
    }

    private static int resolveMaxStackSize(AEKey key) {
        if (key instanceof AEItemKey itemKey) {
            return itemKey.getReadOnlyStack().getMaxStackSize();
        }
        return 1;
    }

    private static String classifyFragmentationLevel(double fragmentationScore) {
        if (fragmentationScore >= FRAGMENTATION_SCORE_HIGH_THRESHOLD) {
            return "high";
        }
        if (fragmentationScore >= FRAGMENTATION_SCORE_MEDIUM_THRESHOLD) {
            return "medium";
        }
        return "low";
    }

    private static String classifyStorageKind(DriveMachineAccessor.DriveMachine location) {
        if (location.isExternalStorageBus()) {
            return "external_storage_bus";
        }
        if ("appliedstoragesorter:digital_asset_vault".equals(location.blockId())) {
            return "internal_dav_cell";
        }
        return "internal_drive_cell";
    }

    private static final class ItemAccumulator {
        private final String keyType;
        private final String keyId;
        private final String displayName;
        private final int maxStackSize;
        private long totalAmount;
        private int internalLocationCount;
        private int externalLocationCount;
        private long maxSingleLocationAmount;
        private final Map<String, Boolean> countedLocations = new LinkedHashMap<>();

        private ItemAccumulator(String keyType, String keyId, String displayName, int maxStackSize) {
            this.keyType = keyType;
            this.keyId = keyId;
            this.displayName = displayName;
            this.maxStackSize = maxStackSize;
        }

        private void addOccurrence(long amount, boolean externalStorageBus, String locationId) {
            totalAmount += amount;
            maxSingleLocationAmount = Math.max(maxSingleLocationAmount, amount);
            if (countedLocations.putIfAbsent(locationId, externalStorageBus) == null) {
                if (externalStorageBus) {
                    externalLocationCount++;
                } else {
                    internalLocationCount++;
                }
            }
        }

        private StorageAnalyzerReport.ItemDistributionSummary toSummary() {
            return new StorageAnalyzerReport.ItemDistributionSummary(
                    keyType,
                    keyId,
                    displayName,
                    totalAmount,
                    internalLocationCount + externalLocationCount,
                    internalLocationCount,
                    externalLocationCount,
                    maxSingleLocationAmount,
                    maxStackSize);
        }
    }
}
