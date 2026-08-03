package com.knightcode.appliedstoragesorter.ae2.scan;

import com.knightcode.appliedstoragesorter.ae2.CellCapacityInspector.CellCapacity;

import appeng.api.networking.security.IActionHost;
import appeng.api.storage.MEStorage;

/**
 * Pure-data snapshot of one storage cell inside the topology.
 *
 * <p>Zero behavior — just identity, capacity metadata, and a live
 * {@link MEStorage} handle for on-demand item enumeration via
 * {@link MEStorage#getAvailableStacks()}. No per-item data is cached.</p>
 *
 * <p>Replaces {@code RuntimeCell} as part of ADR-012.</p>
 *
 * @see DriveCellReference
 */
public record CellInfo(
        DriveCellReference reference,
        String zoneId,
        String sourceBlockId,
        IActionHost actionHost,
        MEStorage storage,
        String cellItemId,
        CellCapacity capacity) {

    public CellInfo {
        if (reference == null) {
            throw new NullPointerException("reference");
        }
        if (zoneId == null || zoneId.isBlank()) {
            throw new IllegalArgumentException("zoneId must not be blank");
        }
        if (sourceBlockId == null || sourceBlockId.isBlank()) {
            throw new IllegalArgumentException("sourceBlockId must not be blank");
        }
        if (actionHost == null) {
            throw new NullPointerException("actionHost");
        }
        if (storage == null) {
            throw new NullPointerException("storage");
        }
    }

    /** Convenience: does this cell have usable capacity info? */
    public boolean hasCapacity() {
        return capacity != null && capacity.totalBytes() > 0;
    }
}
