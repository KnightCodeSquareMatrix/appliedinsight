package com.knightcode.appliedstoragesorter.ae2.analysis;

import java.util.List;

public record RuntimeMoveAnalysisReport(
        boolean fullyAdmissible,
        List<String> blockingReasons) {
}