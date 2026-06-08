package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.knightcode.appliedstoragesorter.ae2.scan.CellInfo;

public final class RuntimeTopology {
    private final Map<String, RuntimeZone> zonesById;
    private final List<CellInfo> unassignedCells;
    private final List<String> diagnostics;
    private final boolean degraded;

    public RuntimeTopology(
            Collection<RuntimeZone> zones,
            List<CellInfo> unassignedCells,
            List<String> diagnostics,
            boolean degraded) {
        Objects.requireNonNull(zones, "zones");
        Map<String, RuntimeZone> indexed = new LinkedHashMap<>();
        for (RuntimeZone zone : zones) {
            Objects.requireNonNull(zone, "zone");
            RuntimeZone previous = indexed.putIfAbsent(zone.zoneId(), zone);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate runtime zone id: " + zone.zoneId());
            }
        }
        this.zonesById = Map.copyOf(indexed);
        this.unassignedCells = List.copyOf(Objects.requireNonNull(unassignedCells, "unassignedCells"));
        this.diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics"));
        this.degraded = degraded;
    }

    public List<RuntimeZone> zones() {
        return List.copyOf(zonesById.values());
    }

    public Optional<RuntimeZone> findZone(String zoneId) {
        if (zoneId == null || zoneId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(zonesById.get(zoneId));
    }

    public List<CellInfo> unassignedCells() {
        return unassignedCells;
    }

    public List<CellInfo> allCells() {
        List<CellInfo> all = new ArrayList<>();
        for (RuntimeZone zone : zonesById.values()) {
            all.addAll(zone.cells());
        }
        all.addAll(unassignedCells);
        return List.copyOf(all);
    }

    public boolean isEmpty() {
        return zonesById.isEmpty() && unassignedCells.isEmpty();
    }

    public List<String> diagnostics() {
        return diagnostics;
    }

    public boolean degraded() {
        return degraded;
    }
}