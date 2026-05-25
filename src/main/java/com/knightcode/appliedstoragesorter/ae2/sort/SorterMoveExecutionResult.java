package com.knightcode.appliedstoragesorter.ae2.sort;

import java.util.List;

public record SorterMoveExecutionResult(
        int attemptedMoveCount,
        int completedMoveCount,
        int failedMoveCount,
        long requestedAmount,
        long movedAmount,
        List<MoveResult> moveResults) {
    public record MoveResult(
            PlannedMove plannedMove,
            long extractedAmount,
            long insertedAmount) {
        public boolean succeeded() {
            return insertedAmount == plannedMove.amount();
        }
    }
}
