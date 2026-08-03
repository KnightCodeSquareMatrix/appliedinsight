package com.knightcode.appliedstoragesorter.logging;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult;
import com.knightcode.appliedstoragesorter.ae2.dump.SorterNetworkDumpResult;
import com.knightcode.appliedstoragesorter.ae2.dump.SorterStorageAnalysisDumpResult;
import com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanSummary;
import com.knightcode.appliedstoragesorter.ae2.sort.PlannedMove;
import com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveExecutionResult;
import com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveOperation;

import net.minecraft.commands.CommandSourceStack;
import net.neoforged.fml.loading.FMLPaths;

/**
 * 命令级摘要日志记录器。
 * 负责向 {@code logs/AppliedStorageSorter.log} 追加写入命令级摘要。
 * <p>
 * 所有文本拼装委托给 {@link ReportFileSupport}，本类只负责组织日志内容。
 */
public final class SorterFileLogger {
    private static final Logger log = LoggerFactory.getLogger(SorterFileLogger.class);
    private static final Path LOG_PATH = FMLPaths.GAMEDIR.get().resolve("logs").resolve("AppliedStorageSorter.log");

    private SorterFileLogger() {
    }

    public static void logSorterMergeFailure(CommandSourceStack source, Ae2GridTargetResult result) {
        append(buildTargetFailureContent(source, "/sorter merge", result));
    }

    public static void logSorterMergePlan(CommandSourceStack source, Ae2GridTargetResult targetResult,
            Ae2DriveScanSummary scanSummary, SorterMoveOperation moveOperation) {
        var grid = targetResult.grid();
        var content = ReportFileSupport.buildCommandHeader(source, "/sorter merge preview")
                .append("scan_status=ok\n")
                .append("target_block=")
                .append(ReportFileSupport.nullToPlaceholder(targetResult.targetBlockId()))
                .append('\n')
                .append("target_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.targetPos()))
                .append('\n')
                .append("controller_detected=")
                .append(targetResult.controllerDetected())
                .append('\n')
                .append("controller_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.controllerPos()))
                .append('\n')
                .append("grid_resolved=")
                .append(targetResult.gridResolved())
                .append('\n')
                .append("network_dimension=")
                .append(ReportFileSupport.nullToPlaceholder(targetResult.dimensionId()))
                .append('\n')
                .append("network_node_count=")
                .append(grid != null ? grid.size() : 0)
                .append('\n')
                .append("network_drive_count=")
                .append(scanSummary.driveCount())
                .append('\n')
                .append("scanned_cell_slot_count=")
                .append(scanSummary.scannedCellSlotCount())
                .append('\n')
                .append("mounted_cell_count=")
                .append(scanSummary.mountedCellCount())
                .append('\n')
                .append("unique_item_key_count=")
                .append(scanSummary.uniqueItemKeyCount())
                .append('\n')
                .append("duplicated_item_key_count=")
                .append(scanSummary.duplicatedItemKeyCount())
                .append('\n')
                .append("duplicated_cell_reference_count=")
                .append(scanSummary.duplicatedCellReferenceCount())
                .append('\n')
                .append("network_pivot=")
                .append(grid != null ? ReportFileSupport.describeOwner(grid.getPivot().getOwner()) : "<none>")
                .append('\n')
                .append("planned_merge_count=")
                .append(moveOperation.plannedMoveCount())
                .append('\n')
                .append("planned_total_amount=")
                .append(moveOperation.totalPlannedAmount())
                .append('\n');

        int index = 0;
        for (PlannedMove plannedMove : moveOperation.plannedMoves()) {
            content.append("planned_merge[")
                    .append(index++)
                    .append("]=")
                    .append(plannedMove.key())
                    .append(" amount=")
                    .append(plannedMove.amount())
                    .append(" source=")
                    .append(ReportFileSupport.formatDriveCellReference(plannedMove.source()))
                    .append(" destination=")
                    .append(ReportFileSupport.formatDriveCellReference(plannedMove.destination()))
                    .append('\n');
        }

        content.append('\n');
        append(content.toString());
    }

    public static void logSorterMergeExecution(CommandSourceStack source, Ae2GridTargetResult targetResult,
            SorterMoveExecutionResult executionResult, String mergeReportPath) {
        var content = ReportFileSupport.buildCommandHeader(source, "/sorter merge execute")
                .append("scan_status=ok\n")
                .append("target_block=")
                .append(ReportFileSupport.nullToPlaceholder(targetResult.targetBlockId()))
                .append('\n')
                .append("target_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.targetPos()))
                .append('\n')
                .append("controller_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.controllerPos()))
                .append('\n')
                .append("attempted_merge_count=")
                .append(executionResult.attemptedMoveCount())
                .append('\n')
                .append("completed_merge_count=")
                .append(executionResult.completedMoveCount())
                .append('\n')
                .append("failed_merge_count=")
                .append(executionResult.failedMoveCount())
                .append('\n')
                .append("requested_merge_amount=")
                .append(executionResult.requestedAmount())
                .append('\n')
                .append("merged_amount=")
                .append(executionResult.movedAmount())
                .append('\n')
                .append("merge_report_file=")
                .append(ReportFileSupport.nullToPlaceholder(mergeReportPath))
                .append("\n\n");

        append(content.toString());
    }

    public static void logSorterMeDumpTargetFailure(CommandSourceStack source, Ae2GridTargetResult result) {
        append(buildTargetFailureContent(source, "/sorter me dump", result));
    }

    public static void logSorterMeStorageAnalysisTargetFailure(CommandSourceStack source, Ae2GridTargetResult result) {
        append(buildTargetFailureContent(source, "/sorter me storageDump", result));
    }

    public static void logSorterMeStorageAnalysis(CommandSourceStack source, Ae2GridTargetResult targetResult,
            SorterStorageAnalysisDumpResult dumpResult) {
        var grid = targetResult.grid();
        var content = ReportFileSupport.buildCommandHeader(source, "/sorter me storageDump")
                .append("scan_status=ok\n")
                .append("target_block=")
                .append(ReportFileSupport.nullToPlaceholder(targetResult.targetBlockId()))
                .append('\n')
                .append("target_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.targetPos()))
                .append('\n')
                .append("controller_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.controllerPos()))
                .append('\n')
                .append("network_node_count=")
                .append(grid != null ? grid.size() : 0)
                .append('\n')
                .append("storage_location_count=")
                .append(dumpResult.storageLocationCount())
                .append('\n')
                .append("unique_key_count=")
                .append(dumpResult.uniqueKeyCount())
                .append('\n')
                .append("total_amount=")
                .append(dumpResult.totalAmount())
                .append('\n')
                .append("dump_file=")
                .append(dumpResult.dumpFilePath())
                .append("\n\n");

        append(content.toString());
    }

    public static void logSorterMeStorageAnalysisFailure(CommandSourceStack source, Ae2GridTargetResult targetResult,
            IOException exception) {
        var content = ReportFileSupport.buildCommandHeader(source, "/sorter me storageDump")
                .append("scan_status=error\n")
                .append("target_block=")
                .append(ReportFileSupport.nullToPlaceholder(targetResult.targetBlockId()))
                .append('\n')
                .append("target_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.targetPos()))
                .append('\n')
                .append("controller_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.controllerPos()))
                .append('\n')
                .append("error=")
                .append(exception.getClass().getName())
                .append(": ")
                .append(ReportFileSupport.nullToPlaceholder(exception.getMessage()))
                .append("\n\n");

        append(content.toString());
    }

    public static void logSorterMeDump(CommandSourceStack source, Ae2GridTargetResult targetResult,
            Ae2DriveScanSummary scanSummary, SorterNetworkDumpResult dumpResult) {
        var grid = targetResult.grid();
        var content = ReportFileSupport.buildCommandHeader(source, "/sorter me dump")
                .append("scan_status=ok\n")
                .append("target_block=")
                .append(ReportFileSupport.nullToPlaceholder(targetResult.targetBlockId()))
                .append('\n')
                .append("target_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.targetPos()))
                .append('\n')
                .append("controller_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.controllerPos()))
                .append('\n')
                .append("network_node_count=")
                .append(grid != null ? grid.size() : 0)
                .append('\n')
                .append("network_drive_count=")
                .append(scanSummary.driveCount())
                .append('\n')
                .append("mounted_cell_count=")
                .append(scanSummary.mountedCellCount())
                .append('\n')
                .append("unique_item_key_count=")
                .append(scanSummary.uniqueItemKeyCount())
                .append('\n')
                .append("item_occurrence_count=")
                .append(dumpResult.itemOccurrenceCount())
                .append('\n')
                .append("dump_file=")
                .append(dumpResult.dumpFilePath())
                .append("\n\n");

        append(content.toString());
    }

    public static void logSorterMeDumpFailure(CommandSourceStack source, Ae2GridTargetResult targetResult,
            Ae2DriveScanSummary scanSummary, IOException exception) {
        var content = ReportFileSupport.buildCommandHeader(source, "/sorter me dump")
                .append("scan_status=error\n")
                .append("target_block=")
                .append(ReportFileSupport.nullToPlaceholder(targetResult.targetBlockId()))
                .append('\n')
                .append("target_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.targetPos()))
                .append('\n')
                .append("controller_pos=")
                .append(ReportFileSupport.formatBlockPos(targetResult.controllerPos()))
                .append('\n')
                .append("network_drive_count=")
                .append(scanSummary.driveCount())
                .append('\n')
                .append("mounted_cell_count=")
                .append(scanSummary.mountedCellCount())
                .append('\n')
                .append("error=")
                .append(exception.getClass().getName())
                .append(": ")
                .append(ReportFileSupport.nullToPlaceholder(exception.getMessage()))
                .append("\n\n");

        append(content.toString());
    }

    private static String buildTargetFailureContent(CommandSourceStack source, String commandTitle,
            Ae2GridTargetResult result) {
        return ReportFileSupport.buildCommandHeader(source, commandTitle)
                .append("scan_status=")
                .append(result.status())
                .append('\n')
                .append("target_block=")
                .append(ReportFileSupport.nullToPlaceholder(result.targetBlockId()))
                .append('\n')
                .append("target_pos=")
                .append(ReportFileSupport.formatBlockPos(result.targetPos()))
                .append('\n')
                .append("controller_detected=")
                .append(result.controllerDetected())
                .append('\n')
                .append("controller_pos=")
                .append(ReportFileSupport.formatBlockPos(result.controllerPos()))
                .append('\n')
                .append("grid_resolved=")
                .append(result.gridResolved())
                .append('\n')
                .append("network_dimension=")
                .append(ReportFileSupport.nullToPlaceholder(result.dimensionId()))
                .append("\n\n")
                .toString();
    }

    private static synchronized void append(String content) {
        ReportFileSupport.appendToLogFile(LOG_PATH, content, log, "Failed to append sorter log file {}");
    }
}
