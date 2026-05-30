package com.knightcode.appliedstoragesorter.logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult;
import com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanSummary;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor;
import com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveExecutionResult;
import com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveOperation;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.commands.CommandSourceStack;

public final class SorterMergeReportFileLogger {
    private static final Logger log = LoggerFactory.getLogger(SorterMergeReportFileLogger.class);
    private static final Path LOG_DIR = ReportFileSupport.resolveLogDir("appliedstoragesorter");
    private static final int TOP_BENEFIT_ITEM_LIMIT = 20;

    private SorterMergeReportFileLogger() {
    }

    public static MergeNetworkSnapshot captureSnapshot(IGrid grid) {
        List<DriveMachineAccessor.DriveMachine> drives = DriveMachineAccessor.findSupportedDrives(grid);
        Map<AEItemKey, ItemDistributionAccumulator> itemAccumulators = new LinkedHashMap<>();

        int occupiedInternalCellCount = 0;
        long usedTypeSlotCount = 0L;
        long availableGroupCount = 0L;
        long usedBytes = 0L;
        int nonEmptyStorageLocationCount = 0;
        int internalStorageLocationCount = 0;
        int externalStorageLocationCount = 0;
        int totalDistributionLocationCount = 0;
        long totalStoredAmount = 0L;

        for (var drive : drives) {
            for (int slot = 0; slot < drive.cellCount(); slot++) {
                var storage = drive.getCellInventory(slot);
                if (storage == null) {
                    continue;
                }

                var reference = new DriveCellReference(
                        drive.blockPos().immutable(),
                        slot,
                        drive.attachedStoragePos().map(pos -> pos.immutable()).orElse(null),
                        drive.attachmentSide().map(side -> side.getSerializedName()).orElse(null));
                boolean external = drive.isExternalStorageBus();
                if (external) {
                    externalStorageLocationCount++;
                } else {
                    internalStorageLocationCount++;
                }

                int distinctItemKeyCount = 0;
                long locationTotalAmount = 0L;
                for (var entry : storage.getAvailableStacks()) {
                    if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEItemKey itemKey)) {
                        continue;
                    }

                    long amount = entry.getLongValue();
                    distinctItemKeyCount++;
                    locationTotalAmount += amount;
                    totalStoredAmount += amount;
                    totalDistributionLocationCount++;

                    itemAccumulators.computeIfAbsent(itemKey, ItemDistributionAccumulator::new)
                            .addOccurrence(reference, external, amount);
                }

                if (locationTotalAmount > 0) {
                    nonEmptyStorageLocationCount++;
                }

                if (!external) {
                    usedTypeSlotCount += distinctItemKeyCount;
                    if (distinctItemKeyCount > 0) {
                        occupiedInternalCellCount++;
                    }

                    var cellStats = inspectCellStats(drive.getOriginalCellInventory(slot));
                    if (cellStats != null) {
                        usedBytes += cellStats.usedBytes();
                        availableGroupCount += cellStats.remainingItemTypes();
                    }
                }
            }
        }

        List<ItemDistributionSnapshot> items = itemAccumulators.values().stream()
                .map(ItemDistributionAccumulator::toSnapshot)
                .sorted(Comparator.comparingLong(ItemDistributionSnapshot::totalAmount).reversed()
                        .thenComparing(ItemDistributionSnapshot::itemId))
                .toList();

        return new MergeNetworkSnapshot(
                grid.size(),
                drives.size(),
                internalStorageLocationCount,
                externalStorageLocationCount,
                nonEmptyStorageLocationCount,
                totalDistributionLocationCount,
                occupiedInternalCellCount,
                usedTypeSlotCount,
                usedBytes,
                availableGroupCount,
                totalStoredAmount,
                items);
    }

    public static String logMergeReport(
            CommandSourceStack source,
            Ae2GridTargetResult targetResult,
            Ae2DriveScanSummary beforeScanSummary,
            MergeNetworkSnapshot beforeSnapshot,
            SorterMoveOperation moveOperation,
            SorterMoveExecutionResult executionResult,
            Ae2DriveScanSummary afterScanSummary,
            MergeNetworkSnapshot afterSnapshot) {
        String fileName = ReportFileSupport.timestampedFileName("merge-", ".log");
        return writeLog(fileName, buildContent(
                source,
                targetResult,
                beforeScanSummary,
                beforeSnapshot,
                moveOperation,
                executionResult,
                afterScanSummary,
                afterSnapshot));
    }

    private static String buildContent(
            CommandSourceStack source,
            Ae2GridTargetResult targetResult,
            Ae2DriveScanSummary beforeScanSummary,
            MergeNetworkSnapshot beforeSnapshot,
            SorterMoveOperation moveOperation,
            SorterMoveExecutionResult executionResult,
            Ae2DriveScanSummary afterScanSummary,
            MergeNetworkSnapshot afterSnapshot) {
        StringBuilder content = buildHeader(source, "/sorter merge")
                .append("target_block=")
                .append(nullToPlaceholder(targetResult.targetBlockId()))
                .append('\n')
                .append("target_pos=")
                .append(formatBlockPos(targetResult.targetPos()))
                .append('\n')
                .append("controller_pos=")
                .append(formatBlockPos(targetResult.controllerPos()))
                .append('\n')
                .append("network_dimension=")
                .append(nullToPlaceholder(targetResult.dimensionId()))
                .append('\n')
                .append("grid_resolved=")
                .append(targetResult.gridResolved())
                .append('\n')
                .append("network_node_count=")
                .append(targetResult.grid() != null ? targetResult.grid().size() : 0)
                .append('\n')
                .append("planned_merge_count=")
                .append(moveOperation.plannedMoveCount())
                .append('\n')
                .append("attempted_merge_count=")
                .append(executionResult.attemptedMoveCount())
                .append('\n')
                .append("completed_merge_count=")
                .append(executionResult.completedMoveCount())
                .append('\n')
                .append("failed_merge_count=")
                .append(executionResult.failedMoveCount())
                .append('\n')
                .append("requested_merge_amount=")
                .append(executionResult.requestedAmount())
                .append('\n')
                .append("merged_amount=")
                .append(executionResult.movedAmount())
                .append("\n\n");

        appendScanSummary(content, "before_scan_summary", beforeScanSummary);
        appendScanSummary(content, "after_scan_summary", afterScanSummary);
        appendEffectReviewSection(content, beforeSnapshot, afterSnapshot, executionResult);
        appendTopBenefitSection(content, beforeSnapshot, afterSnapshot, executionResult);
        appendChangedItemSection(content, beforeSnapshot, afterSnapshot, executionResult);
        appendExecutedMoveSection(content, executionResult);
        appendEnergyCostSection(content, executionResult);
        return content.toString();
    }

    private static void appendScanSummary(StringBuilder content, String sectionName, Ae2DriveScanSummary scanSummary) {
        content.append('[')
                .append(sectionName)
                .append("]\n")
                .append("drive_count=")
                .append(scanSummary.driveCount())
                .append('\n')
                .append("scanned_cell_slot_count=")
                .append(scanSummary.scannedCellSlotCount())
                .append('\n')
                .append("mounted_cell_count=")
                .append(scanSummary.mountedCellCount())
                .append('\n')
                .append("unique_item_key_count=")
                .append(scanSummary.uniqueItemKeyCount())
                .append('\n')
                .append("duplicated_item_key_count=")
                .append(scanSummary.duplicatedItemKeyCount())
                .append('\n')
                .append("duplicated_cell_reference_count=")
                .append(scanSummary.duplicatedCellReferenceCount())
                .append("\n\n");
    }

    private static void appendEffectReviewSection(
            StringBuilder content,
            MergeNetworkSnapshot beforeSnapshot,
            MergeNetworkSnapshot afterSnapshot,
            SorterMoveExecutionResult executionResult) {
        long releasedBytes = beforeSnapshot.usedBytes() - afterSnapshot.usedBytes();
        long releasedAvailableGroupCount = afterSnapshot.availableGroupCount() - beforeSnapshot.availableGroupCount();

        content.append("[merge_effect_review]\n")
                .append("distribution_location_count_before=")
                .append(beforeSnapshot.totalDistributionLocationCount())
                .append('\n')
                .append("distribution_location_count_after=")
                .append(afterSnapshot.totalDistributionLocationCount())
                .append('\n')
                .append("distribution_location_count_delta=")
                .append(afterSnapshot.totalDistributionLocationCount() - beforeSnapshot.totalDistributionLocationCount())
                .append('\n')
                .append("occupied_internal_cell_count_before=")
                .append(beforeSnapshot.occupiedInternalCellCount())
                .append('\n')
                .append("occupied_internal_cell_count_after=")
                .append(afterSnapshot.occupiedInternalCellCount())
                .append('\n')
                .append("occupied_internal_cell_count_delta=")
                .append(afterSnapshot.occupiedInternalCellCount() - beforeSnapshot.occupiedInternalCellCount())
                .append('\n')
                .append("used_type_slot_count_before=")
                .append(beforeSnapshot.usedTypeSlotCount())
                .append('\n')
                .append("used_type_slot_count_after=")
                .append(afterSnapshot.usedTypeSlotCount())
                .append('\n')
                .append("used_type_slot_count_delta=")
                .append(afterSnapshot.usedTypeSlotCount() - beforeSnapshot.usedTypeSlotCount())
                .append('\n')
                .append("used_bytes_before=")
                .append(beforeSnapshot.usedBytes())
                .append('\n')
                .append("used_bytes_after=")
                .append(afterSnapshot.usedBytes())
                .append('\n')
                .append("released_bytes=")
                .append(releasedBytes)
                .append('\n')
                .append("available_group_count_before=")
                .append(beforeSnapshot.availableGroupCount())
                .append('\n')
                .append("available_group_count_after=")
                .append(afterSnapshot.availableGroupCount())
                .append('\n')
                .append("released_available_group_count=")
                .append(releasedAvailableGroupCount)
                .append('\n')
                .append("internal_storage_location_count_before=")
                .append(beforeSnapshot.internalStorageLocationCount())
                .append('\n')
                .append("internal_storage_location_count_after=")
                .append(afterSnapshot.internalStorageLocationCount())
                .append('\n')
                .append("external_storage_location_count_before=")
                .append(beforeSnapshot.externalStorageLocationCount())
                .append('\n')
                .append("external_storage_location_count_after=")
                .append(afterSnapshot.externalStorageLocationCount())
                .append('\n')
                .append("non_empty_storage_location_count_before=")
                .append(beforeSnapshot.nonEmptyStorageLocationCount())
                .append('\n')
                .append("non_empty_storage_location_count_after=")
                .append(afterSnapshot.nonEmptyStorageLocationCount())
                .append('\n')
                .append("moved_amount=")
                .append(executionResult.movedAmount())
                .append('\n')
                .append("changed_item_count=")
                .append(buildChangedItems(beforeSnapshot, afterSnapshot, executionResult).size())
                .append("\n\n");
    }

    private static void appendTopBenefitSection(
            StringBuilder content,
            MergeNetworkSnapshot beforeSnapshot,
            MergeNetworkSnapshot afterSnapshot,
            SorterMoveExecutionResult executionResult) {
        List<ChangedItemSummary> changedItems = buildChangedItems(beforeSnapshot, afterSnapshot, executionResult);

        content.append("[top_benefit_items]\n");
        if (changedItems.isEmpty()) {
            content.append("<none>\n\n");
            return;
        }

        int index = 1;
        for (ChangedItemSummary item : changedItems.stream().limit(TOP_BENEFIT_ITEM_LIMIT).toList()) {
            content.append(index++)
                    .append(". itemId=")
                    .append(item.itemId())
                    .append(" | name=")
                    .append(item.displayName())
                    .append(" | movedAmount=")
                    .append(item.movedAmount())
                    .append(" | beforeLocations=")
                    .append(item.before().locationCount())
                    .append(" | afterLocations=")
                    .append(item.after().locationCount())
                    .append(" | reducedLocations=")
                    .append(item.before().locationCount() - item.after().locationCount())
                    .append(" | beforeMix=")
                    .append(describeMixState(item.before()))
                    .append(" | afterMix=")
                    .append(describeMixState(item.after()))
                    .append(" | becameMoreConcentrated=")
                    .append(item.becameMoreConcentrated())
                    .append('\n');
        }
        content.append('\n');
    }

    private static void appendChangedItemSection(
            StringBuilder content,
            MergeNetworkSnapshot beforeSnapshot,
            MergeNetworkSnapshot afterSnapshot,
            SorterMoveExecutionResult executionResult) {
        List<ChangedItemSummary> changedItems = buildChangedItems(beforeSnapshot, afterSnapshot, executionResult);

        content.append("[item_merge_details]\n");
        if (changedItems.isEmpty()) {
            content.append("<none>\n\n");
            return;
        }

        int index = 0;
        for (ChangedItemSummary item : changedItems) {
            content.append("item[")
                    .append(index++)
                    .append("] itemId=")
                    .append(item.itemId())
                    .append(" | name=")
                    .append(item.displayName())
                    .append('\n')
                    .append("  delta: beforeLocations=")
                    .append(item.before().locationCount())
                    .append(" | afterLocations=")
                    .append(item.after().locationCount())
                    .append(" | reducedLocations=")
                    .append(item.before().locationCount() - item.after().locationCount())
                    .append(" | beforeAmount=")
                    .append(item.before().totalAmount())
                    .append(" | afterAmount=")
                    .append(item.after().totalAmount())
                    .append(" | movedAmount=")
                    .append(item.movedAmount())
                    .append(" | beforeMix=")
                    .append(describeMixState(item.before()))
                    .append(" | afterMix=")
                    .append(describeMixState(item.after()))
                    .append(" | becameMoreConcentrated=")
                    .append(item.becameMoreConcentrated())
                    .append('\n');

            appendLocationMap(content, "  moved_out_from", item.movedOutByLocation());
            appendLocationMap(content, "  merged_into", item.movedIntoByLocation());
            appendLocationMap(content, "  before", item.before().amountByLocation());
            appendLocationMap(content, "  after", item.after().amountByLocation());
        }
        content.append('\n');
    }

    private static void appendExecutedMoveSection(StringBuilder content, SorterMoveExecutionResult executionResult) {
        content.append("[executed_moves]\n");
        if (executionResult.moveResults().isEmpty()) {
            content.append("<none>\n\n");
            return;
        }

        int index = 0;
        for (var moveResult : executionResult.moveResults()) {
            var plannedMove = moveResult.plannedMove();
            content.append("move[")
                    .append(index++)
                    .append("] itemId=")
                    .append(itemId(plannedMove.key()))
                    .append(" | name=")
                    .append(plannedMove.key().getDisplayName().getString())
                    .append(" | requested=")
                    .append(plannedMove.amount())
                    .append(" | extracted=")
                    .append(moveResult.extractedAmount())
                    .append(" | inserted=")
                    .append(moveResult.insertedAmount())
                    .append(" | success=")
                    .append(moveResult.succeeded())
                    .append(" | source=")
                    .append(formatDriveCellReference(plannedMove.source()))
                    .append(" | destination=")
                    .append(formatDriveCellReference(plannedMove.destination()))
                    .append('\n');
        }
        content.append('\n');
    }

    private static void appendEnergyCostSection(StringBuilder content, SorterMoveExecutionResult executionResult) {
        var energyCost = executionResult.energyCost();
        content.append("[energy_cost]\n");
        if (energyCost == null) {
            content.append("enabled=false\n\n");
            return;
        }

        var breakdown = energyCost.breakdown();
        content.append("enabled=true\n")
                .append("total_cost=")
                .append(energyCost.totalCost())
                .append('\n')
                .append("move_count=")
                .append(energyCost.moveCount())
                .append('\n')
                .append("moved_amount=")
                .append(energyCost.movedAmount())
                .append('\n')
                .append("distinct_item_types=")
                .append(energyCost.distinctItemTypes())
                .append('\n')
                .append("average_distance=")
                .append(energyCost.averageDistance())
                .append('\n')
                .append("cost_breakdown: base=")
                .append(breakdown.baseCost())
                .append("|amount=")
                .append(breakdown.amountCost())
                .append("|type=")
                .append(breakdown.typeCost())
                .append("|distance=")
                .append(breakdown.distanceCost())
                .append("\n\n");
    }

    private static void appendLocationMap(StringBuilder content, String label, Map<String, Long> amountByLocation) {
        content.append(label).append(':').append('\n');
        if (amountByLocation.isEmpty()) {
            content.append("    <none>\n");
            return;
        }

        amountByLocation.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> content.append("    - ")
                        .append(entry.getKey())
                        .append(" amount=")
                        .append(entry.getValue())
                        .append('\n'));
    }

    private static List<ChangedItemSummary> buildChangedItems(
            MergeNetworkSnapshot beforeSnapshot,
            MergeNetworkSnapshot afterSnapshot,
            SorterMoveExecutionResult executionResult) {
        Map<String, ItemDistributionSnapshot> beforeItems = indexByItemId(beforeSnapshot.items());
        Map<String, ItemDistributionSnapshot> afterItems = indexByItemId(afterSnapshot.items());
        Map<String, ItemMoveAccumulator> moveAccumulators = new LinkedHashMap<>();

        for (var moveResult : executionResult.moveResults()) {
            if (moveResult.insertedAmount() <= 0) {
                continue;
            }

            var key = moveResult.plannedMove().key();
            String itemId = itemId(key);
            moveAccumulators.computeIfAbsent(itemId,
                    ignored -> new ItemMoveAccumulator(itemId, key.getDisplayName().getString()))
                    .addMove(moveResult.plannedMove().source(), moveResult.plannedMove().destination(),
                            moveResult.insertedAmount());
        }

        Set<String> orderedIds = new LinkedHashSet<>();
        orderedIds.addAll(moveAccumulators.keySet());
        orderedIds.addAll(beforeItems.keySet());
        orderedIds.addAll(afterItems.keySet());

        List<ChangedItemSummary> changedItems = new ArrayList<>();
        for (String itemId : orderedIds) {
            ItemDistributionSnapshot before = beforeItems.getOrDefault(itemId, ItemDistributionSnapshot.empty(itemId));
            ItemDistributionSnapshot after = afterItems.getOrDefault(itemId, ItemDistributionSnapshot.empty(itemId));
            ItemMoveAccumulator moves = moveAccumulators.get(itemId);
            long movedAmount = moves != null ? moves.movedAmount() : 0L;

            boolean changed = movedAmount > 0
                    || before.locationCount() != after.locationCount()
                    || before.internalLocationCount() != after.internalLocationCount()
                    || before.externalLocationCount() != after.externalLocationCount()
                    || !before.amountByLocation().equals(after.amountByLocation());
            if (!changed) {
                continue;
            }

            String displayName = firstNonBlank(
                    before.displayName(),
                    after.displayName(),
                    moves != null ? moves.displayName() : null,
                    itemId);
            changedItems.add(new ChangedItemSummary(
                    itemId,
                    displayName,
                    before,
                    after,
                    movedAmount,
                    moves != null ? moves.movedOutByLocation() : Map.of(),
                    moves != null ? moves.movedIntoByLocation() : Map.of()));
        }

        changedItems.sort(Comparator.comparingInt(ChangedItemSummary::locationReduction).reversed()
                .thenComparing(Comparator.comparingLong(ChangedItemSummary::movedAmount).reversed())
                .thenComparing(ChangedItemSummary::itemId));
        return changedItems;
    }

    private static Map<String, ItemDistributionSnapshot> indexByItemId(List<ItemDistributionSnapshot> items) {
        Map<String, ItemDistributionSnapshot> indexed = new LinkedHashMap<>();
        for (ItemDistributionSnapshot item : items) {
            indexed.put(item.itemId(), item);
        }
        return indexed;
    }

    private static CellStats inspectCellStats(StorageCell storageCell) {
        if (storageCell == null) {
            return null;
        }

        Long usedBytes = invokeLong(storageCell, "getUsedBytes");
        Long remainingItemTypes = invokeLong(storageCell, "getRemainingItemTypes");
        if (usedBytes == null || remainingItemTypes == null) {
            return null;
        }

        return new CellStats(usedBytes, remainingItemTypes);
    }

    private static Long invokeLong(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            if (value instanceof Number number) {
                return number.longValue();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return null;
        }
        return null;
    }

    private static String writeLog(String fileName, String content) {
        return ReportFileSupport.writeTextFile(
                LOG_DIR,
                fileName,
                content,
                log,
                "Failed to write sorter merge report {}"
        );
    }

    private static String describeMixState(ItemDistributionSnapshot snapshot) {
        if (snapshot.internalLocationCount() > 0 && snapshot.externalLocationCount() > 0) {
            return "external+cell_mixed";
        }
        if (snapshot.internalLocationCount() > 0) {
            return "cell_only";
        }
        if (snapshot.externalLocationCount() > 0) {
            return "external_only";
        }
        return "not_present";
    }

    private static String itemId(AEItemKey key) {
        return ReportFileSupport.itemId(key);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "<none>";
    }

    private static StringBuilder buildHeader(CommandSourceStack source, String commandName) {
        return ReportFileSupport.buildCommandHeader(source, commandName);
    }

    private static String formatDriveCellReference(DriveCellReference reference) {
        return ReportFileSupport.formatDriveCellReference(reference);
    }

    private static String formatBlockPos(net.minecraft.core.BlockPos pos) {
        return ReportFileSupport.formatBlockPos(pos);
    }

    private static String nullToPlaceholder(String value) {
        return ReportFileSupport.nullToPlaceholder(value);
    }

    public record MergeNetworkSnapshot(
            int networkNodeCount,
            int driveCount,
            int internalStorageLocationCount,
            int externalStorageLocationCount,
            int nonEmptyStorageLocationCount,
            int totalDistributionLocationCount,
            int occupiedInternalCellCount,
            long usedTypeSlotCount,
            long usedBytes,
            long availableGroupCount,
            long totalStoredAmount,
            List<ItemDistributionSnapshot> items) {
    }

    public record ItemDistributionSnapshot(
            String itemId,
            String displayName,
            long totalAmount,
            int locationCount,
            int internalLocationCount,
            int externalLocationCount,
            Map<String, Long> amountByLocation) {
        static ItemDistributionSnapshot empty(String itemId) {
            return new ItemDistributionSnapshot(itemId, itemId, 0L, 0, 0, 0, Map.of());
        }
    }

    private record CellStats(long usedBytes, long remainingItemTypes) {
    }

    private record ChangedItemSummary(
            String itemId,
            String displayName,
            ItemDistributionSnapshot before,
            ItemDistributionSnapshot after,
            long movedAmount,
            Map<String, Long> movedOutByLocation,
            Map<String, Long> movedIntoByLocation) {
        int locationReduction() {
            return before.locationCount() - after.locationCount();
        }

        boolean becameMoreConcentrated() {
            return locationReduction() > 0
                    || (before.internalLocationCount() > 0 && before.externalLocationCount() > 0
                            && !(after.internalLocationCount() > 0 && after.externalLocationCount() > 0));
        }
    }

    private static final class ItemDistributionAccumulator {
        private final AEItemKey key;
        private long totalAmount;
        private int internalLocationCount;
        private int externalLocationCount;
        private final Map<String, Long> amountByLocation = new LinkedHashMap<>();

        private ItemDistributionAccumulator(AEItemKey key) {
            this.key = Objects.requireNonNull(key);
        }

        private void addOccurrence(DriveCellReference reference, boolean external, long amount) {
            totalAmount += amount;
            String locationId = formatDriveCellReference(reference);
            amountByLocation.put(locationId, amount);
            if (external) {
                externalLocationCount++;
            } else {
                internalLocationCount++;
            }
        }

        private ItemDistributionSnapshot toSnapshot() {
            return new ItemDistributionSnapshot(
                    itemId(key),
                    key.getDisplayName().getString(),
                    totalAmount,
                    internalLocationCount + externalLocationCount,
                    internalLocationCount,
                    externalLocationCount,
                    Map.copyOf(amountByLocation));
        }
    }

    private static final class ItemMoveAccumulator {
        private final String itemId;
        private final String displayName;
        private long movedAmount;
        private final Map<String, Long> movedOutByLocation = new LinkedHashMap<>();
        private final Map<String, Long> movedIntoByLocation = new LinkedHashMap<>();

        private ItemMoveAccumulator(String itemId, String displayName) {
            this.itemId = itemId;
            this.displayName = displayName;
        }

        private void addMove(DriveCellReference source, DriveCellReference destination, long amount) {
            movedAmount += amount;
            movedOutByLocation.merge(formatDriveCellReference(source), amount, Long::sum);
            movedIntoByLocation.merge(formatDriveCellReference(destination), amount, Long::sum);
        }

        private long movedAmount() {
            return movedAmount;
        }

        private String displayName() {
            return displayName;
        }

        private Map<String, Long> movedOutByLocation() {
            return Map.copyOf(movedOutByLocation);
        }

        private Map<String, Long> movedIntoByLocation() {
            return Map.copyOf(movedIntoByLocation);
        }
    }
}
