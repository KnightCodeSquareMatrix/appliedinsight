package com.knightcode.appliedstoragesorter.application.result;

public record NewDavMigrationResult(
        int plannedMoveCount,
        int attemptedMoveCount,
        int completedMoveCount,
        long movedAmount,
        boolean foundWork,
        String statusKey) {

    public static NewDavMigrationResult idle(String statusKey) {
        return new NewDavMigrationResult(0, 0, 0, 0L, false, statusKey);
    }
}
