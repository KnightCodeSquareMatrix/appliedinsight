package com.knightcode.appliedstoragesorter.application;

import java.util.List;

import com.knightcode.appliedstoragesorter.Config;
import com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver;
import com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult;
import com.knightcode.appliedstoragesorter.ae2.GridTargetResolver;
import com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanner;
import com.knightcode.appliedstoragesorter.ae2.sort.MergeMovePlanner;
import com.knightcode.appliedstoragesorter.application.result.SorterFeedbackResult;
import com.knightcode.appliedstoragesorter.logging.SorterFileLogger;
import com.knightcode.appliedstoragesorter.logging.SorterMergeReportFileLogger;

import net.minecraft.commands.CommandSourceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SorterMergeService {
    private static final Logger log = LoggerFactory.getLogger(SorterMergeService.class);

    private SorterMergeService() {
    }

    public static SorterFeedbackResult execute(CommandSourceStack source) {
        return execute(source, Ae2ControllerTargetResolver::resolveGridTarget);
    }

    public static SorterFeedbackResult execute(CommandSourceStack source, GridTargetResolver resolver) {
        if (!Config.ENABLE_SORTER.get()) {
            log.debug("Skip sorter merge because sorter is disabled.");
            return SorterFeedbackResult.failure("Applied Storage Sorter is disabled in the server config.");
        }
        if (source.getEntity() == null) {
            log.debug("Reject sorter merge because command source has no entity.");
            return SorterFeedbackResult.failure("This command must be run by a player.");
        }
        var gridTarget = resolver.resolve(source);
        if (!gridTarget.success()) {
            log.warn("Sorter merge target resolution failed: {}", gridTarget.userMessage());
            SorterFileLogger.logSorterMergeFailure(source, gridTarget);
            return SorterFeedbackResult.failure(gridTarget.userMessage());
        }

        var grid = gridTarget.grid();
        var beforeScanSummary = Ae2DriveScanner.scan(grid);
        var beforeSnapshot = SorterMergeReportFileLogger.captureSnapshot(grid);
        var moveOperation = MergeMovePlanner.plan(grid, Config.MAX_TRANSFERS_PER_OPERATION.get());
        log.info("Planned sorter merge for {} drive(s) with {} move candidate(s).",
                beforeScanSummary.driveCount(), moveOperation.plannedMoveCount());
        SorterFileLogger.logSorterMergePlan(source, gridTarget, beforeScanSummary, moveOperation);

        var executionResult = moveOperation.execute();
        var afterScanSummary = Ae2DriveScanner.scan(grid);
        var afterSnapshot = SorterMergeReportFileLogger.captureSnapshot(grid);
        var mergeReportPath = SorterMergeReportFileLogger.logMergeReport(
                source,
                gridTarget,
                beforeScanSummary,
                beforeSnapshot,
                moveOperation,
                executionResult,
                afterScanSummary,
                afterSnapshot);
        log.info("Sorter merge executed: moved {}/{} item units, report={}",
                executionResult.movedAmount(), executionResult.requestedAmount(), mergeReportPath);
        SorterFileLogger.logSorterMergeExecution(source, gridTarget, executionResult, mergeReportPath);

        return SorterFeedbackResult.success(List.of(
                SorterComponentHelper.keyValue("plannedMergeCount", String.valueOf(moveOperation.plannedMoveCount())),
                SorterComponentHelper.keyValue("mergedAmount", executionResult.movedAmount() + "/" + executionResult.requestedAmount()),
                SorterComponentHelper.clickableFile("mergeReport", mergeReportPath)));
    }
}
