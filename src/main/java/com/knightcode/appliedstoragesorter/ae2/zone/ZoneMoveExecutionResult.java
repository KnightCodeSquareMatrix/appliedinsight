package com.knightcode.appliedstoragesorter.ae2.zone;

public record ZoneMoveExecutionResult(
        int attemptedMoveCount,
        int completedMoveCount,
        int failedMoveCount,
        long requestedAmount,
        long movedAmount) {
}
