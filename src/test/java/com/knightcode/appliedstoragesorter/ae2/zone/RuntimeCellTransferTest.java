package com.knightcode.appliedstoragesorter.ae2.zone;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for the transfer result models added/modified during the result model stability refactor.
 *
 * <p>Covers: {@link ZoneMoveExecutionResult}, {@link ZoneMoveExecutionDetailedResult},
 * and the aggregator logic in {@link ZoneMoveExecutionDebugReport}.
 *
 * <p>Note: Tests for transfer outcome construction with nullable fields
 * require the Minecraft/AE2 runtime
 * ({@code net.minecraft.core.BlockPos}, {@code appeng.api.storage.MEStorage}, etc.)
 * and should be run in a game test environment (e.g. NeoForge {@code gameTestServer}).
 *
 * @see <a href="https://docs.neoforged.net/docs/gametests/">NeoForge GameTest docs</a>
 */
class RuntimeCellTransferTest {

    // ── ZoneMoveExecutionResult ───────────────────────────────────────────────

    @Test
    void executionResult_holdsPrimitiveCounts() {
        var result = new ZoneMoveExecutionResult(10, 7, 3, 512L, 448L, null);
        assertEquals(10, result.attemptedMoveCount());
        assertEquals(7, result.completedMoveCount());
        assertEquals(3, result.failedMoveCount());
        assertEquals(512L, result.requestedAmount());
        assertEquals(448L, result.movedAmount());
    }

    @Test
    void executionResult_zeroValues() {
        var result = new ZoneMoveExecutionResult(0, 0, 0, 0L, 0L, null);
        assertEquals(0, result.attemptedMoveCount());
        assertEquals(0, result.completedMoveCount());
        assertEquals(0, result.failedMoveCount());
    }

    @Test
    void executionResult_completedEqualsAttempted() {
        var result = new ZoneMoveExecutionResult(5, 5, 0, 320L, 320L, null);
        assertEquals(5, result.attemptedMoveCount());
        assertEquals(5, result.completedMoveCount());
        assertEquals(0, result.failedMoveCount());
    }

    // ── ZoneMoveExecutionDetailedResult ───────────────────────────────────────

    @Test
    void detailedResult_rejectsNullExecutionResult() {
        var debug = new ZoneMoveExecutionDebugReport(
                emptyTopology(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, java.util.List.of());
        assertThrows(NullPointerException.class,
                () -> new ZoneMoveExecutionDetailedResult(null, debug));
    }

    @Test
    void detailedResult_rejectsNullDebugReport() {
        var exec = new ZoneMoveExecutionResult(0, 0, 0, 0L, 0L, null);
        assertThrows(NullPointerException.class,
                () -> new ZoneMoveExecutionDetailedResult(exec, null));
    }

    @Test
    void detailedResult_wrapsBothParts() {
        var exec = new ZoneMoveExecutionResult(1, 1, 0, 64L, 64L, null);
        var debug = new ZoneMoveExecutionDebugReport(
                emptyTopology(), 2, 3, 10, 1, 0, 0, 0, 0, 0, 0, 0, java.util.List.of("ok"));
        var detailed = new ZoneMoveExecutionDetailedResult(exec, debug);

        assertSame(exec, detailed.executionResult());
        assertSame(debug, detailed.debugReport());
    }

    // ── ZoneMoveExecutionDebugReport ──────────────────────────────────────────

    @Test
    void debugReport_rejectsNullTopology() {
        assertThrows(NullPointerException.class,
                () -> new ZoneMoveExecutionDebugReport(
                        null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, java.util.List.of()));
    }

    @Test
    void debugReport_rejectsNullSampleMessages() {
        assertThrows(NullPointerException.class,
                () -> new ZoneMoveExecutionDebugReport(
                        emptyTopology(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null));
    }

    @Test
    void debugReport_defensivelyCopiesSampleMessages() {
        var mutable = new java.util.ArrayList<String>();
        mutable.add("a");
        var debug = new ZoneMoveExecutionDebugReport(
                emptyTopology(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, mutable);
        mutable.add("b"); // mutate original

        assertEquals(1, debug.sampleMessages().size());
        assertThrows(UnsupportedOperationException.class,
                () -> debug.sampleMessages().add("c"));
    }

    @Test
    void debugReport_failedMoveCount_isSumOfThree() {
        var debug = new ZoneMoveExecutionDebugReport(
                emptyTopology(), 0, 0, 0, 0, 0, 0, 0, 0,
                3,  // extractFailedCount
                2,  // partialInsertCount
                1,  // rollbackFailedCount
                java.util.List.of());
        assertEquals(6, debug.failedMoveCount()); // 3+2+1
    }

    @Test
    void debugReport_failedMoveCount_zeroWhenAllZero() {
        var debug = new ZoneMoveExecutionDebugReport(
                emptyTopology(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, java.util.List.of());
        assertEquals(0, debug.failedMoveCount());
    }

    @Test
    void debugReport_holdsScannedCounters() {
        var debug = new ZoneMoveExecutionDebugReport(
                emptyTopology(),
                5,   // scannedSourceDriveCount
                12,  // scannedSourceCellCount
                100, // scannedStackCount
                8,   // moveCandidateAssignmentCount
                2,   // skippedNoAssignmentCount
                1,   // skippedSameZoneCount
                0,   // skippedMissingTargetZoneCount
                3,   // skippedPlacementRejectedCount
                1,   // extractFailedCount
                2,   // partialInsertCount
                0,   // rollbackFailedCount
                java.util.List.of("sample1"));

        assertEquals(5, debug.scannedSourceDriveCount());
        assertEquals(12, debug.scannedSourceCellCount());
        assertEquals(100, debug.scannedStackCount());
        assertEquals(8, debug.moveCandidateAssignmentCount());
        assertEquals(2, debug.skippedNoAssignmentCount());
        assertEquals(1, debug.skippedSameZoneCount());
        assertEquals(0, debug.skippedMissingTargetZoneCount());
        assertEquals(3, debug.skippedPlacementRejectedCount());
        assertEquals(1, debug.extractFailedCount());
        assertEquals(2, debug.partialInsertCount());
        assertEquals(0, debug.rollbackFailedCount());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static RuntimeTopology emptyTopology() {
        return new RuntimeTopology(java.util.List.of(), java.util.List.of(), java.util.List.of(), false);
    }
}
