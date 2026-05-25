package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor;
import com.knightcode.appliedstoragesorter.ae2.sort.PlannedMove;
import com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveOperation;
import com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment;
import com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Zone merge planner — 将 zone 分配计划转化为可执行的 merge 操作。
 *
 * <p>替代 {@link Ae2ZoneMoveExecutor} 中原有的内联执行逻辑，
 * 与 {@link com.knightcode.appliedstoragesorter.ae2.sort.MergeMovePlanner} 互补：
 * <ul>
 *   <li>MergeMovePlanner: 全网络同类归并（{@code /sorter merge}）</li>
 *   <li>ZoneMergePlanner: 按 zone 治理规则的跨 zone 搬运（{@code /sorter me planAndMove}）</li>
 * </ul>
 *
 * <p>两者都产出 {@link SorterMoveOperation}，共享同一套执行引擎（extract → insert → rollback）。
 */
public final class ZoneMergePlanner {
    private static final int MAX_SAMPLE_MESSAGES = 100;

    private ZoneMergePlanner() {
    }

    /**
     * 根据 zone 分配计划生成 merge 操作。
     *
     * @param allocationPlan zone 分配计划
     * @param topology       运行时拓扑（含 zone/cell 结构）
     * @param grid           AE2 网络
     * @param maxTransfers   最大搬运次数限制
     * @return 包含 SorterMoveOperation 和诊断统计的 plan 结果
     */
    public static PlanResult plan(
            ZoneAllocationPlan allocationPlan,
            RuntimeTopology topology,
            IGrid grid,
            int maxTransfers) {
        Map<ItemFingerprint, ItemZoneAssignment> assignmentsByItem = indexAssignments(allocationPlan);
        var drives = DriveMachineAccessor.findSupportedDrives(grid);

        List<PlannedMove> plannedMoves = new ArrayList<>();
        List<SorterMoveOperation.ExecutableMove> executableMoves = new ArrayList<>();
        List<String> sampleMessages = new ArrayList<>();

        int scannedSourceCellCount = 0;
        int scannedStackCount = 0;
        int skippedNoAssignmentCount = 0;
        int skippedSameZoneCount = 0;
        int skippedMissingTargetZoneCount = 0;
        int skippedPlacementRejectedCount = 0;
        long totalPlannedAmount = 0;

        outer:
        for (var sourceDrive : drives) {
            String sourceZoneId = sourceDrive.getDeclaredZoneId().orElse(null);

            for (int slot = 0; slot < sourceDrive.cellCount(); slot++) {
                MEStorage sourceStorage = sourceDrive.getCellInventory(slot);
                if (sourceStorage == null) {
                    continue;
                }

                DriveCellReference sourceReference = createSourceReference(sourceDrive, slot);
                scannedSourceCellCount++;

                for (var entry : sourceStorage.getAvailableStacks()) {
                    if (plannedMoves.size() >= maxTransfers) {
                        addSample(sampleMessages, "Stopped because maxTransfers was reached: " + maxTransfers);
                        break outer;
                    }

                    if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEItemKey itemKey)) {
                        continue;
                    }

                    scannedStackCount++;

                    ItemFingerprint fingerprint = fingerprintOf(sourceDrive, itemKey);
                    ItemZoneAssignment assignment = assignmentsByItem.get(fingerprint);

                    if (assignment == null || !assignment.isMoveCandidate()) {
                        skippedNoAssignmentCount++;
                        addSample(sampleMessages, "No movable assignment for "
                                + describeItem(itemKey) + " at " + describeReference(sourceReference)
                                + " sourceZone=" + nullToPlaceholder(sourceZoneId));
                        continue;
                    }

                    String targetZoneId = assignment.targetZoneId();
                    if (targetZoneId == null || targetZoneId.equals(sourceZoneId)) {
                        skippedSameZoneCount++;
                        addSample(sampleMessages, "Skipping " + describeItem(itemKey)
                                + " at " + describeReference(sourceReference)
                                + " because targetZone=" + nullToPlaceholder(targetZoneId)
                                + " sourceZone=" + nullToPlaceholder(sourceZoneId));
                        continue;
                    }

                    RuntimeZone targetZone = topology.findZone(targetZoneId).orElse(null);
                    if (targetZone == null) {
                        skippedMissingTargetZoneCount++;
                        addSample(sampleMessages, "Missing runtime target zone " + targetZoneId
                                + " for " + describeItem(itemKey)
                                + " at " + describeReference(sourceReference));
                        continue;
                    }

                    ZonePlacementDecision placementDecision = targetZone.planPlacement(
                            sourceReference, itemKey, entry.getLongValue());
                    if (!placementDecision.accepted()) {
                        skippedPlacementRejectedCount++;
                        addSample(sampleMessages, "Placement rejected in zone " + targetZoneId
                                + " for " + describeItem(itemKey)
                                + " at " + describeReference(sourceReference)
                                + " reason=" + placementDecision.reason());
                        continue;
                    }

                    PlannedMove plannedMove = new PlannedMove(
                            itemKey, placementDecision.acceptedAmount(), sourceReference,
                            placementDecision.targetCell().reference());
                    SorterMoveOperation.ExecutableMove executableMove = new SorterMoveOperation.ExecutableMove(
                            plannedMove,
                            sourceDrive.actionHost(),
                            placementDecision.targetCell().actionHost(),
                            sourceStorage,
                            placementDecision.targetCell().storage());

                    plannedMoves.add(plannedMove);
                    executableMoves.add(executableMove);
                    totalPlannedAmount += placementDecision.acceptedAmount();
                }
            }
        }

        SorterMoveOperation operation = plannedMoves.isEmpty()
                ? SorterMoveOperation.empty()
                : SorterMoveOperation.of(plannedMoves, executableMoves, totalPlannedAmount);

        return new PlanResult(
                operation,
                drives.size(),
                scannedSourceCellCount,
                scannedStackCount,
                skippedNoAssignmentCount,
                skippedSameZoneCount,
                skippedMissingTargetZoneCount,
                skippedPlacementRejectedCount,
                sampleMessages);
    }

    private static DriveCellReference createSourceReference(DriveMachineAccessor.DriveMachine sourceDrive, int slot) {
        return new DriveCellReference(
                sourceDrive.blockPos().immutable(),
                slot,
                sourceDrive.attachedStoragePos().map(pos -> pos.immutable()).orElse(null),
                sourceDrive.attachmentSide().map(side -> side.getSerializedName()).orElse(null));
    }

    private static Map<ItemFingerprint, ItemZoneAssignment> indexAssignments(ZoneAllocationPlan plan) {
        Map<ItemFingerprint, ItemZoneAssignment> assignmentsByItem = new HashMap<>();
        for (ItemZoneAssignment assignment : plan.movableAssignments()) {
            assignmentsByItem.put(
                    new ItemFingerprint(assignment.itemId(), assignment.serializedStackNbt()), assignment);
        }
        return assignmentsByItem;
    }

    private static ItemFingerprint fingerprintOf(DriveMachineAccessor.DriveMachine drive, AEItemKey itemKey) {
        var stack = itemKey.getReadOnlyStack();
        var level = drive.blockEntity().getLevel();
        String serializedStackNbt = level != null ? stack.saveOptional(level.registryAccess()).toString() : "";
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return new ItemFingerprint(itemId, serializedStackNbt);
    }

    private static void addSample(List<String> sampleMessages, String message) {
        if (sampleMessages.size() < MAX_SAMPLE_MESSAGES) {
            sampleMessages.add(message);
        }
    }

    private static String describeItem(AEItemKey itemKey) {
        var stack = itemKey.getReadOnlyStack();
        return BuiltInRegistries.ITEM.getKey(stack.getItem()) + " x" + itemKey.getDisplayName().getString();
    }

    private static String describeReference(DriveCellReference reference) {
        String base = reference.drivePos().getX() + "," + reference.drivePos().getY() + "," + reference.drivePos().getZ()
                + "#slot=" + reference.slot();
        if (!reference.hasAttachedStorage()) {
            return base;
        }
        var attached = reference.attachedStoragePos();
        return base + "->attached=" + attached.getX() + "," + attached.getY() + "," + attached.getZ()
                + "@" + nullToPlaceholder(reference.attachmentDescription());
    }

    private static String nullToPlaceholder(String value) {
        return value != null ? value : "<none>";
    }

    private record ItemFingerprint(String itemId, String serializedStackNbt) {
    }

    /**
     * {@link #plan} 的返回结果。包含可执行的 {@link SorterMoveOperation} 和诊断统计数据。
     */
    public record PlanResult(
            SorterMoveOperation operation,
            int scannedSourceDriveCount,
            int scannedSourceCellCount,
            int scannedStackCount,
            int skippedNoAssignmentCount,
            int skippedSameZoneCount,
            int skippedMissingTargetZoneCount,
            int skippedPlacementRejectedCount,
            List<String> sampleMessages) {
    }
}
