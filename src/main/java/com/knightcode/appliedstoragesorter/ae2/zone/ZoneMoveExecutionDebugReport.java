package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.List;
import java.util.Objects;

public record ZoneMoveExecutionDebugReport(
        RuntimeTopology runtimeTopology,
        int scannedSourceDriveCount,
        int scannedSourceCellCount,
        int scannedStackCount,
        int moveCandidateAssignmentCount,
        int skippedNoAssignmentCount,
        int skippedSameZoneCount,
        int skippedMissingTargetZoneCount,
        int skippedPlacementRejectedCount,
        int extractFailedCount,
        int partialInsertCount,
        int rollbackFailedCount,
        List<String> sampleMessages) {
    public ZoneMoveExecutionDebugReport {
        runtimeTopology = Objects.requireNonNull(runtimeTopology, "runtimeTopology");
        sampleMessages = List.copyOf(Objects.requireNonNull(sampleMessages, "sampleMessages"));
    }

    /**
     * 总执行失败计数（向后兼容，等于三级失败计数之和）。
     */
    public int failedMoveCount() {
        return extractFailedCount + partialInsertCount + rollbackFailedCount;
    }
}
