package com.knightcode.appliedstoragesorter.ae2.zone;

import org.jetbrains.annotations.Nullable;

import com.knightcode.appliedstoragesorter.ae2.sort.EnergyCostEstimate;

public record ZoneMoveExecutionResult(
        int attemptedMoveCount,
        int completedMoveCount,
        int failedMoveCount,
        long requestedAmount,
        long movedAmount,
        @Nullable EnergyCostEstimate energyCost) {

    public ZoneMoveExecutionResult withEnergyCost(@Nullable EnergyCostEstimate energyCost) {
        return new ZoneMoveExecutionResult(
                attemptedMoveCount, completedMoveCount, failedMoveCount,
                requestedAmount, movedAmount, energyCost);
    }
}
