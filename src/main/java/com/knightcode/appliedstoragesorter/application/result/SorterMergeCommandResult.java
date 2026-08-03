package com.knightcode.appliedstoragesorter.application.result;

public record SorterMergeCommandResult(
        boolean success,
        String message,
        int plannedMoveCount,
        long requestedAmount,
        long movedAmount) {
    public static SorterMergeCommandResult failure(String message) {
        return new SorterMergeCommandResult(false, message, 0, 0L, 0L);
    }

    public static SorterMergeCommandResult success(String message, int plannedMoveCount, long requestedAmount,
            long movedAmount) {
        return new SorterMergeCommandResult(true, message, plannedMoveCount, requestedAmount, movedAmount);
    }
}
