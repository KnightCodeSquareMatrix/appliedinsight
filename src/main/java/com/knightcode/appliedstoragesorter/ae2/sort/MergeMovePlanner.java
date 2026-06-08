package com.knightcode.appliedstoragesorter.ae2.sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor.DriveMachine;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;

/**
 * Plans fragment-consolidation moves for {@code /sorter merge}.
 *
 * <p>Only two transfer shapes are allowed (AE2-aligned):
 * <ol>
 *   <li><b>cell → cell</b> — scatter across internal drives/DAV into the first internal cell</li>
 *   <li><b>cell → external</b> — push from internal cells into an existing external storage bus slot
 *       when the bus already holds the same item (bulk sink), if no internal duplicate target exists</li>
 * </ol>
 * External inventories are never merge sources.
 */
public final class MergeMovePlanner {
    private MergeMovePlanner() {
    }

    public static SorterMoveOperation plan(IGrid grid, int maxTransfers) {
        if (maxTransfers <= 0) {
            return SorterMoveOperation.empty();
        }

        List<PlannedMove> plannedMoves = new ArrayList<>();
        List<SorterMoveOperation.ExecutableMove> executableMoves = new ArrayList<>();
        Map<AEItemKey, CellContext> firstInternalCellByKey = new HashMap<>();
        Map<AEItemKey, CellContext> firstExternalCellByKey = new HashMap<>();
        List<CellContext> internalSources = new ArrayList<>();
        long totalPlannedAmount = 0;

        for (DriveMachine drive : DriveMachineAccessor.findSupportedDrives(grid)) {
            for (int slot = 0; slot < drive.cellCount(); slot++) {
                MEStorage storage = drive.getCellInventory(slot);
                if (storage == null) {
                    continue;
                }

                CellContext context = toContext(drive, slot, storage);
                if (MergeEndpointPolicy.isMergeSource(drive)) {
                    internalSources.add(context);
                }

                for (var entry : storage.getAvailableStacks()) {
                    if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEItemKey itemKey)) {
                        continue;
                    }

                    if (MergeEndpointPolicy.isInternalDestination(drive)) {
                        firstInternalCellByKey.putIfAbsent(itemKey, context);
                    } else if (MergeEndpointPolicy.isExternalDestination(drive)) {
                        firstExternalCellByKey.putIfAbsent(itemKey, context);
                    }
                }
            }
        }

        for (CellContext sourceContext : internalSources) {
            for (var entry : sourceContext.storage().getAvailableStacks()) {
                if (plannedMoves.size() >= maxTransfers) {
                    return SorterMoveOperation.of(plannedMoves, executableMoves, totalPlannedAmount);
                }

                if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEItemKey itemKey)) {
                    continue;
                }

                CellContext destinationContext = resolveDestination(
                        sourceContext,
                        itemKey,
                        firstInternalCellByKey,
                        firstExternalCellByKey);
                if (destinationContext == null) {
                    continue;
                }

                long plannedAmount = destinationContext.storage().insert(
                        itemKey,
                        entry.getLongValue(),
                        Actionable.SIMULATE,
                        IActionSource.ofMachine(destinationContext.drive()));

                if (plannedAmount <= 0) {
                    continue;
                }

                PlannedMove plannedMove = new PlannedMove(
                        itemKey,
                        plannedAmount,
                        sourceContext.reference(),
                        destinationContext.reference());
                plannedMoves.add(plannedMove);
                executableMoves.add(SorterMoveOperation.executableMove(
                        plannedMove,
                        sourceContext.drive(),
                        destinationContext.drive(),
                        sourceContext.storage(),
                        destinationContext.storage()));
                totalPlannedAmount += plannedAmount;
            }
        }

        return SorterMoveOperation.of(plannedMoves, executableMoves, totalPlannedAmount);
    }

    private static CellContext resolveDestination(
            CellContext sourceContext,
            AEItemKey itemKey,
            Map<AEItemKey, CellContext> firstInternalCellByKey,
            Map<AEItemKey, CellContext> firstExternalCellByKey) {
        CellContext internalDestination = firstInternalCellByKey.get(itemKey);
        if (internalDestination != null && !sameCell(sourceContext, internalDestination)) {
            return internalDestination;
        }

        CellContext externalDestination = firstExternalCellByKey.get(itemKey);
        if (externalDestination != null && !sameCell(sourceContext, externalDestination)) {
            return externalDestination;
        }

        return null;
    }

    private static boolean sameCell(CellContext left, CellContext right) {
        return left.reference().equals(right.reference());
    }

    private static CellContext toContext(DriveMachine drive, int slot, MEStorage storage) {
        DriveCellReference reference = new DriveCellReference(
                drive.blockPos().immutable(),
                slot,
                drive.attachedStoragePos().map(pos -> pos.immutable()).orElse(null),
                drive.attachmentSide().map(side -> side.getSerializedName()).orElse(null));
        return new CellContext(reference, drive.actionHost(), storage);
    }

    private record CellContext(
            DriveCellReference reference,
            IActionHost drive,
            MEStorage storage) {
    }
}
