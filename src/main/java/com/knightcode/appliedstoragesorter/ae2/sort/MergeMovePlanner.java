package com.knightcode.appliedstoragesorter.ae2.sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;

public final class MergeMovePlanner {
    private MergeMovePlanner() {
    }

    public static SorterMoveOperation plan(IGrid grid, int maxTransfers) {
        if (maxTransfers <= 0) {
            return SorterMoveOperation.empty();
        }

        List<PlannedMove> plannedMoves = new ArrayList<>();
        List<SorterMoveOperation.ExecutableMove> executableMoves = new ArrayList<>();
        Map<AEItemKey, CellContext> firstCellByKey = new HashMap<>();
        long totalPlannedAmount = 0;

        for (var drive : DriveMachineAccessor.findSupportedDrives(grid)) {
            for (int slot = 0; slot < drive.cellCount(); slot++) {
                var sourceStorage = drive.getCellInventory(slot);
                if (sourceStorage == null) {
                    continue;
                }

                var sourceReference = new DriveCellReference(
                        drive.blockPos().immutable(),
                        slot,
                        drive.attachedStoragePos().map(pos -> pos.immutable()).orElse(null),
                        drive.attachmentSide().map(side -> side.getSerializedName()).orElse(null));
                var sourceContext = new CellContext(sourceReference, drive.actionHost(), sourceStorage);

                for (var entry : sourceStorage.getAvailableStacks()) {
                    if (plannedMoves.size() >= maxTransfers) {
                        return SorterMoveOperation.of(plannedMoves, executableMoves, totalPlannedAmount);
                    }

                    if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEItemKey itemKey)) {
                        continue;
                    }

                    var destinationContext = firstCellByKey.putIfAbsent(itemKey, sourceContext);
                    if (destinationContext == null || destinationContext.reference.equals(sourceReference)) {
                        continue;
                    }

                    var plannedAmount = destinationContext.storage.insert(
                            itemKey,
                            entry.getLongValue(),
                            Actionable.SIMULATE,
                            IActionSource.ofMachine(destinationContext.drive));

                    if (plannedAmount <= 0) {
                        continue;
                    }

                    var plannedMove = new PlannedMove(itemKey, plannedAmount, sourceReference, destinationContext.reference);
                    plannedMoves.add(plannedMove);
                    executableMoves.add(SorterMoveOperation.executableMove(
                            plannedMove,
                            drive.actionHost(),
                            destinationContext.drive,
                            sourceStorage,
                            destinationContext.storage));
                    totalPlannedAmount += plannedAmount;
                }
            }
        }

        return SorterMoveOperation.of(plannedMoves, executableMoves, totalPlannedAmount);
    }

    private record CellContext(
            DriveCellReference reference,
            IActionHost drive,
            MEStorage storage) {
    }
}
