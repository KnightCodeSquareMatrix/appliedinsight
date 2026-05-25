package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.Objects;

public record ZoneMoveExecutionDetailedResult(
        ZoneMoveExecutionResult executionResult,
        ZoneMoveExecutionDebugReport debugReport) {
    public ZoneMoveExecutionDetailedResult {
        executionResult = Objects.requireNonNull(executionResult, "executionResult");
        debugReport = Objects.requireNonNull(debugReport, "debugReport");
    }
}
