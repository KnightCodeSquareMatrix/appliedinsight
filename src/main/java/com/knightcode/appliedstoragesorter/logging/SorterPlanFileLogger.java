package com.knightcode.appliedstoragesorter.logging;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult;
import com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanSummary;
import com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor;
import com.knightcode.appliedstoragesorter.ae2.zone.RuntimeCell;
import com.knightcode.appliedstoragesorter.ae2.zone.RuntimeTopology;
import com.knightcode.appliedstoragesorter.ae2.zone.RuntimeZone;
import com.knightcode.appliedstoragesorter.ae2.zone.ZoneMoveExecutionDetailedResult;
import com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment;
import com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfile;

import net.minecraft.commands.CommandSourceStack;

public final class SorterPlanFileLogger {
    private static final Logger log = LoggerFactory.getLogger(SorterPlanFileLogger.class);
    private static final Path LOG_DIR = ReportFileSupport.resolveLogDir("appliedstoragesorter");

    private SorterPlanFileLogger() {
    }

    public static String logPlan(
            CommandSourceStack source,
            Ae2GridTargetResult gridTarget,
            RoutingProfile profile,
            Ae2DriveScanSummary scanSummary,
            ZoneAllocationPlan plan,
            RuntimeTopology runtimeTopology) {
        String fileName = ReportFileSupport.timestampedFileName("plan-", ".log");
        return writeLog(fileName, buildPlanContent(
                source,
                "/sorter me plan",
                gridTarget,
                profile,
                scanSummary,
                plan,
                runtimeTopology,
                Optional.empty()));
    }

    public static String logPlanAndMove(
            CommandSourceStack source,
            Ae2GridTargetResult gridTarget,
            RoutingProfile profile,
            Ae2DriveScanSummary scanSummary,
            ZoneAllocationPlan plan,
            ZoneMoveExecutionDetailedResult moveResult) {
        String fileName = ReportFileSupport.timestampedFileName("plan-and-move-", ".log");
        return writeLog(fileName, buildPlanContent(
                source,
                "/sorter me planAndMove",
                gridTarget,
                profile,
                scanSummary,
                plan,
                moveResult.debugReport().runtimeTopology(),
                Optional.of(moveResult)));
    }

    private static String buildPlanContent(
            CommandSourceStack source,
            String commandName,
            Ae2GridTargetResult gridTarget,
            RoutingProfile profile,
            Ae2DriveScanSummary scanSummary,
            ZoneAllocationPlan plan,
            RuntimeTopology runtimeTopology,
            Optional<ZoneMoveExecutionDetailedResult> moveResult) {
        StringBuilder content = buildHeader(source, commandName)
                .append("target_block=")
                .append(nullToPlaceholder(gridTarget.targetBlockId()))
                .append('\n')
                .append("target_pos=")
                .append(formatBlockPos(gridTarget.targetPos()))
                .append('\n')
                .append("controller_pos=")
                .append(formatBlockPos(gridTarget.controllerPos()))
                .append('\n')
                .append("network_dimension=")
                .append(nullToPlaceholder(gridTarget.dimensionId()))
                .append('\n')
                .append("grid_resolved=")
                .append(gridTarget.gridResolved())
                .append('\n')
                .append("network_node_count=")
                .append(gridTarget.grid() != null ? gridTarget.grid().size() : 0)
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
                .append("profile.id=")
                .append(profile.id())
                .append('\n')
                .append("profile.name=")
                .append(profile.name())
                .append('\n')
                .append("profile.version=")
                .append(profile.version())
                .append('\n')
                .append("profile.defaultZoneId=")
                .append(profile.defaultZoneId())
                .append("\n\n");

        appendDriveSection(content, gridTarget);
        appendRuntimeZoneSection(content, runtimeTopology);
        appendPlanSummarySection(content, plan);
        appendPlanAssignmentsSection(content, plan);
        moveResult.ifPresent(result -> appendMoveSection(content, result));

        return content.toString();
    }

    private static void appendDriveSection(StringBuilder content, Ae2GridTargetResult gridTarget) {
        content.append("[supported_drives]\n");
        if (gridTarget.grid() == null) {
            content.append("<no_grid>\n\n");
            return;
        }

        List<DriveMachineAccessor.DriveMachine> drives = DriveMachineAccessor.findSupportedDrives(gridTarget.grid());
        if (drives.isEmpty()) {
            content.append("<none>\n\n");
            return;
        }

        int index = 0;
        for (var drive : drives) {
            int mountedCells = 0;
            for (int slot = 0; slot < drive.cellCount(); slot++) {
                if (drive.getCellInventory(slot) != null) {
                    mountedCells++;
                }
            }

            content.append("drive[")
                    .append(index++)
                    .append("] blockId=")
                    .append(drive.blockId())
                    .append(" | pos=")
                    .append(formatBlockPos(drive.blockPos()))
                    .append(" | attachedStoragePos=")
                    .append(drive.attachedStoragePos().map(SorterPlanFileLogger::formatBlockPos).orElse("<none>"))
                    .append(" | attachmentSide=")
                    .append(drive.attachmentSide().map(Enum::name).orElse("<none>"))
                    .append(" | externalStorageBus=")
                    .append(drive.isExternalStorageBus())
                    .append(" | declaredZoneId=")
                    .append(drive.getDeclaredZoneId().orElse("<none>"))
                    .append(" | cellCount=")
                    .append(drive.cellCount())
                    .append(" | mountedCellCount=")
                    .append(mountedCells)
                    .append('\n');
        }
        content.append('\n');
    }

    private static void appendRuntimeZoneSection(StringBuilder content, RuntimeTopology runtimeTopology) {
        content.append("[runtime_zones]\n");
        if (runtimeTopology.isEmpty()) {
            content.append("<none>\n\n");
            return;
        }

        for (RuntimeZone zone : runtimeTopology.zones().stream()
                .sorted(Comparator.comparing(RuntimeZone::zoneId))
                .toList()) {
            content.append("zone=")
                    .append(zone.zoneId())
                    .append(" | name=")
                    .append(zone.zoneName())
                    .append(" | cellCount=")
                    .append(zone.cellCount())
                    .append('\n');

            for (RuntimeCell cell : zone.cells()) {
                content.append("  cell=")
                        .append(formatBlockPos(cell.reference().drivePos()))
                        .append("#slot=")
                        .append(cell.reference().slot())
                        .append(" | attachedStoragePos=")
                        .append(formatBlockPos(cell.reference().attachedStoragePos()))
                        .append(" | attachment=")
                        .append(valueOrPlaceholder(cell.reference().attachmentDescription()))
                        .append(" | sourceBlockId=")
                        .append(cell.sourceBlockId())
                        .append(" | distinctItemKeyCount=")
                        .append(cell.distinctItemKeyCount())
                        .append('\n');
            }
        }
        content.append('\n');
    }

    private static void appendPlanSummarySection(StringBuilder content, ZoneAllocationPlan plan) {
        content.append("[plan_summary]\n")
                .append("assignmentCount=")
                .append(plan.assignmentCount())
                .append('\n')
                .append("movableAssignmentCount=")
                .append(plan.movableAssignments().size())
                .append('\n');

        Map<String, List<ItemZoneAssignment>> assignmentsByZone = plan.assignments().stream()
                .collect(Collectors.groupingBy(assignment -> valueOrPlaceholder(assignment.targetZoneId())));

        assignmentsByZone.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    long totalAmount = entry.getValue().stream().mapToLong(ItemZoneAssignment::totalAmount).sum();
                    long movableCount = entry.getValue().stream().filter(ItemZoneAssignment::isMoveCandidate).count();
                    content.append("zone=")
                            .append(entry.getKey())
                            .append(" | itemCount=")
                            .append(entry.getValue().size())
                            .append(" | movableItemCount=")
                            .append(movableCount)
                            .append(" | totalAmount=")
                            .append(totalAmount)
                            .append('\n');
                });
        content.append('\n');
    }

    private static void appendPlanAssignmentsSection(StringBuilder content, ZoneAllocationPlan plan) {
        content.append("[plan_assignments]\n");
        int index = 0;
        for (ItemZoneAssignment assignment : plan.assignments().stream()
                .sorted(Comparator.comparing(ItemZoneAssignment::targetZoneId, Comparator.nullsLast(String::compareTo))
                        .thenComparing(Comparator.comparingLong(ItemZoneAssignment::totalAmount).reversed())
                        .thenComparing(ItemZoneAssignment::itemId))
                .toList()) {
            content.append("assignment[")
                    .append(index++)
                    .append("] itemId=")
                    .append(assignment.itemId())
                    .append(" | name=")
                    .append(assignment.displayName())
                    .append(" | targetZoneId=")
                    .append(valueOrPlaceholder(assignment.targetZoneId()))
                    .append(" | decision=")
                    .append(assignment.decisionType().name())
                    .append(" | matchedRuleId=")
                    .append(valueOrPlaceholder(assignment.matchedRuleId()))
                    .append(" | matchedFilterId=")
                    .append(valueOrPlaceholder(assignment.matchedFilterId()))
                    .append(" | totalAmount=")
                    .append(assignment.totalAmount())
                    .append(" | occurrenceCount=")
                    .append(assignment.occurrenceCount())
                    .append(" | hasComponents=")
                    .append(assignment.hasComponents())
                    .append(" | moveCandidate=")
                    .append(assignment.isMoveCandidate())
                    .append('\n');
        }
        content.append('\n');
    }

    private static void appendMoveSection(StringBuilder content, ZoneMoveExecutionDetailedResult moveResult) {
        var executionResult = moveResult.executionResult();
        var debugReport = moveResult.debugReport();

        content.append("[move_summary]\n")
                .append("attemptedMoveCount=")
                .append(executionResult.attemptedMoveCount())
                .append('\n')
                .append("completedMoveCount=")
                .append(executionResult.completedMoveCount())
                .append('\n')
                .append("failedMoveCount=")
                .append(executionResult.failedMoveCount())
                .append('\n')
                .append("requestedAmount=")
                .append(executionResult.requestedAmount())
                .append('\n')
                .append("movedAmount=")
                .append(executionResult.movedAmount())
                .append('\n')
                .append("scannedSourceDriveCount=")
                .append(debugReport.scannedSourceDriveCount())
                .append('\n')
                .append("scannedSourceCellCount=")
                .append(debugReport.scannedSourceCellCount())
                .append('\n')
                .append("scannedStackCount=")
                .append(debugReport.scannedStackCount())
                .append('\n')
                .append("moveCandidateAssignmentCount=")
                .append(debugReport.moveCandidateAssignmentCount())
                .append('\n')
                .append("skippedNoAssignmentCount=")
                .append(debugReport.skippedNoAssignmentCount())
                .append('\n')
                .append("skippedSameZoneCount=")
                .append(debugReport.skippedSameZoneCount())
                .append('\n')
                .append("skippedMissingTargetZoneCount=")
                .append(debugReport.skippedMissingTargetZoneCount())
                .append('\n')
                .append("skippedPlacementRejectedCount=")
                .append(debugReport.skippedPlacementRejectedCount())
                .append('\n')
                .append("extractFailedCount=")
                .append(debugReport.extractFailedCount())
                .append('\n')
                .append("partialInsertCount=")
                .append(debugReport.partialInsertCount())
                .append('\n')
                .append("rollbackFailedCount=")
                .append(debugReport.rollbackFailedCount())
                .append("\n\n");

        content.append("[move_samples]\n");
        if (debugReport.sampleMessages().isEmpty()) {
            content.append("<none>\n\n");
            return;
        }

        for (int i = 0; i < debugReport.sampleMessages().size(); i++) {
            content.append(i + 1)
                    .append(". ")
                    .append(debugReport.sampleMessages().get(i))
                    .append('\n');
        }
        content.append('\n');
    }

    private static String writeLog(String fileName, String content) {
        return ReportFileSupport.writeTextFile(
                LOG_DIR,
                fileName,
                content,
                log,
                "Failed to write sorter plan log {}"
        );
    }

    private static StringBuilder buildHeader(CommandSourceStack source, String commandName) {
        return ReportFileSupport.buildCommandHeader(source, commandName);
    }

    private static String nullToPlaceholder(String value) {
        return ReportFileSupport.nullToPlaceholder(value);
    }

    private static String formatBlockPos(net.minecraft.core.BlockPos pos) {
        return ReportFileSupport.formatBlockPos(pos);
    }

    private static String valueOrPlaceholder(String value) {
        return ReportFileSupport.valueOrPlaceholder(value);
    }
}
