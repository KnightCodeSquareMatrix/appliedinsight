package com.knightcode.appliedstoragesorter.ae2.analysis;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.knightcode.appliedstoragesorter.ae2.zone.RuntimeTopology;
import com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment;
import com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan;

public final class Ae2MoveAnalyzer {
    private Ae2MoveAnalyzer() {
    }

    public static RuntimeMoveAnalysisReport analyze(RuntimeTopology topology, ZoneAllocationPlan plan) {
        Objects.requireNonNull(topology, "topology");
        Objects.requireNonNull(plan, "plan");

        Set<String> blockingReasons = new LinkedHashSet<>();
        if (topology.isEmpty()) {
            blockingReasons.add("runtime topology has no declared zones");
        }

        for (ItemZoneAssignment assignment : plan.movableAssignments()) {
            String targetZoneId = assignment.targetZoneId();
            if (targetZoneId == null || targetZoneId.isBlank()) {
                blockingReasons.add("missing target zone for movable assignment: " + assignment.itemId());
                continue;
            }

            RuntimeMoveAnalysisReport zoneReport = analyzeZone(topology, targetZoneId);
            if (!zoneReport.fullyAdmissible()) {
                blockingReasons.addAll(zoneReport.blockingReasons());
            }
        }

        return new RuntimeMoveAnalysisReport(blockingReasons.isEmpty(), List.copyOf(new ArrayList<>(blockingReasons)));
    }

    private static RuntimeMoveAnalysisReport analyzeZone(RuntimeTopology topology, String zoneId) {
        List<String> reasons = new ArrayList<>();
        var targetZone = topology.findZone(zoneId).orElse(null);
        if (targetZone == null) {
            reasons.add("missing runtime target zone: " + zoneId);
            return new RuntimeMoveAnalysisReport(false, List.copyOf(reasons));
        }
        if (targetZone.isEmpty()) {
            reasons.add("runtime target zone has no runtime cells: " + zoneId);
        }
        return new RuntimeMoveAnalysisReport(reasons.isEmpty(), List.copyOf(reasons));
    }
}