package com.knightcode.appliedstoragesorter.ae2.scan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;

public final class Ae2DriveScanner {
    private Ae2DriveScanner() {
    }

    public static Ae2DriveScanSummary scan(IGrid grid) {
        Map<AEItemKey, DriveCellReference> firstCellByKey = new HashMap<>();
        Set<AEItemKey> duplicatedKeys = new HashSet<>();

        int scannedCellSlotCount = 0;
        int mountedCellCount = 0;
        int duplicatedCellReferenceCount = 0;

        var drives = DriveMachineAccessor.findSupportedDrives(grid);
        for (var drive : drives) {
            for (int slot = 0; slot < drive.cellCount(); slot++) {
                scannedCellSlotCount++;

                var storage = drive.getCellInventory(slot);
                if (storage == null) {
                    continue;
                }

                mountedCellCount++;
                var reference = new DriveCellReference(
                        drive.blockPos().immutable(),
                        slot,
                        drive.attachedStoragePos().map(pos -> pos.immutable()).orElse(null),
                        drive.attachmentSide().map(side -> side.getSerializedName()).orElse(null));

                for (var entry : storage.getAvailableStacks()) {
                    if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEItemKey itemKey)) {
                        continue;
                    }

                    var previous = firstCellByKey.putIfAbsent(itemKey, reference);
                    if (previous != null) {
                        duplicatedKeys.add(itemKey);
                        duplicatedCellReferenceCount++;
                    }
                }
            }
        }

        return new Ae2DriveScanSummary(
                drives.size(),
                scannedCellSlotCount,
                mountedCellCount,
                firstCellByKey.size(),
                duplicatedKeys.size(),
                duplicatedCellReferenceCount);
    }
}
