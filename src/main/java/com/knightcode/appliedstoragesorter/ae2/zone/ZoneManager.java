package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;

import appeng.api.stacks.AEItemKey;

public abstract class ZoneManager {
    private final String zoneId;
    private final String zoneName;
    private final List<RuntimeCell> cells = new ArrayList<>();

    protected ZoneManager(String zoneId, String zoneName) {
        this.zoneId = requireNonBlank(zoneId, "zoneId");
        this.zoneName = requireNonBlank(zoneName, "zoneName");
    }

    public final String zoneId() {
        return zoneId;
    }

    public final String zoneName() {
        return zoneName;
    }

    public final List<RuntimeCell> cells() {
        return List.copyOf(cells);
    }

    public final int cellCount() {
        return cells.size();
    }

    public final boolean isEmpty() {
        return cells.isEmpty();
    }

    public final void addCell(RuntimeCell cell) {
        cells.add(requireCellForZone(cell));
    }

    public final boolean removeCell(DriveCellReference reference) {
        Objects.requireNonNull(reference, "reference");
        return cells.removeIf(cell -> cell.reference().equals(reference));
    }

    public final void replaceCell(RuntimeCell cell) {
        RuntimeCell validated = requireCellForZone(cell);
        removeCell(validated.reference());
        cells.add(validated);
    }

    public final ZonePlacementDecision planPlacement(
            DriveCellReference sourceReference,
            AEItemKey itemKey,
            long amount) {
        Objects.requireNonNull(sourceReference, "sourceReference");
        Objects.requireNonNull(itemKey, "itemKey");

        if (amount <= 0) {
            return ZonePlacementDecision.rejected(zoneId, "requested amount must be positive");
        }
        if (cells.isEmpty()) {
            return ZonePlacementDecision.rejected(zoneId, "zone has no runtime cells");
        }

        return doPlanPlacement(sourceReference, itemKey, amount, List.copyOf(cells));
    }

    protected abstract ZonePlacementDecision doPlanPlacement(
            DriveCellReference sourceReference,
            AEItemKey itemKey,
            long amount,
            List<RuntimeCell> cells);

    private RuntimeCell requireCellForZone(RuntimeCell cell) {
        Objects.requireNonNull(cell, "cell");
        if (!zoneId.equals(cell.zoneId())) {
            throw new IllegalArgumentException(
                    "runtime cell zoneId does not match manager zoneId: " + zoneId + " != " + cell.zoneId());
        }
        return cell;
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
