package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.knightcode.appliedstoragesorter.ae2.CellCapacityInspector;
import com.knightcode.appliedstoragesorter.ae2.CellCapacityInspector.CellCapacity;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;
import com.knightcode.appliedstoragesorter.ae2.scan.CellInfo;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor;

import appeng.api.networking.IGrid;
import appeng.api.storage.MEStorage;
import net.minecraft.core.BlockPos;

public final class RuntimeZoneRegistryBuilder {
    private RuntimeZoneRegistryBuilder() {
    }

    public static RuntimeTopology buildTopology(IGrid grid) {
        Map<String, RuntimeZone> zonesById = new LinkedHashMap<>();
        List<CellInfo> unassignedCells = new ArrayList<>();

        for (var drive : DriveMachineAccessor.findSupportedDrives(grid)) {
            String zoneId = drive.getDeclaredZoneId().orElse(null);

            for (int slot = 0; slot < drive.cellCount(); slot++) {
                MEStorage storage = drive.getCellInventory(slot);
                if (storage == null) {
                    continue;
                }

                // Extract cell identity and capacity metadata (ADR-012)
                String cellItemId = drive.getCellItemId(slot).orElse(null);
                CellCapacity capacity = null;
                if (!drive.isExternalStorageBus()) {
                    var originalCell = drive.getOriginalCellInventory(slot);
                    if (originalCell != null) {
                        capacity = CellCapacityInspector.inspect(originalCell);
                    }
                }

                CellInfo cell = new CellInfo(
                        new DriveCellReference(
                                drive.blockPos().immutable(),
                                slot,
                                drive.attachedStoragePos().map(BlockPos::immutable).orElse(null),
                                drive.attachmentSide().map(side -> side.getSerializedName()).orElse(null)),
                        zoneId != null && !zoneId.isBlank() ? zoneId : "__unassigned__",
                        drive.blockId(),
                        drive.actionHost(),
                        storage,
                        cellItemId,
                        capacity);

                if (zoneId == null || zoneId.isBlank()) {
                    unassignedCells.add(cell);
                } else {
                    RuntimeZone zone = zonesById.computeIfAbsent(zoneId, RuntimeZoneRegistryBuilder::createZone);
                    zone.addCell(cell);
                }
            }
        }

        return new RuntimeTopology(zonesById.values(), unassignedCells, java.util.List.of(), zonesById.isEmpty());
    }

    private static RuntimeZone createZone(String zoneId) {
        return new RuntimeZone(zoneId, zoneId);
    }
}
