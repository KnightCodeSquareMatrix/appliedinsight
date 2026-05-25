package com.knightcode.appliedstoragesorter.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.knightcode.appliedstoragesorter.Config;
import com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver;
import com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult;
import com.knightcode.appliedstoragesorter.ae2.GridTargetResolver;
import com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanner;
import com.knightcode.appliedstoragesorter.ae2.zone.Ae2ZoneMoveExecutor;
import com.knightcode.appliedstoragesorter.ae2.zone.LiveZoneAllocationPlanner;
import com.knightcode.appliedstoragesorter.ae2.zone.RuntimeTopology;
import com.knightcode.appliedstoragesorter.ae2.zone.RuntimeZoneRegistryBuilder;
import com.knightcode.appliedstoragesorter.application.result.SorterFeedbackResult;
import com.knightcode.appliedstoragesorter.logging.SorterPlanFileLogger;
import com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment;
import com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfileRepository;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SorterPlanService {
    private static final Logger log = LoggerFactory.getLogger(SorterPlanService.class);
    private static final RoutingProfileRepository PROFILE_REPOSITORY = new RoutingProfileRepository();

    private SorterPlanService() {
    }

    public static SorterFeedbackResult plan(CommandSourceStack source) {
        return execute(source, false, Ae2ControllerTargetResolver::resolveGridTarget);
    }

    public static SorterFeedbackResult planAndMove(CommandSourceStack source) {
        return execute(source, true, Ae2ControllerTargetResolver::resolveGridTarget);
    }

    public static SorterFeedbackResult execute(CommandSourceStack source, boolean executeMove, GridTargetResolver resolver) {
        if (!Config.ENABLE_SORTER.get()) {
            log.debug("Skip sorter plan because sorter is disabled.");
            return SorterFeedbackResult.failure("Applied Storage Sorter is disabled in the server config.");
        }
        if (source.getEntity() == null) {
            log.debug("Reject sorter plan because command source has no entity.");
            return SorterFeedbackResult.failure("This command must be run by a player.");
        }
        if (source.getServer() == null) {
            log.warn("Reject sorter plan because server is unavailable.");
            return SorterFeedbackResult.failure("Server is not available.");
        }

        var gridTarget = resolver.resolve(source);
        if (!gridTarget.success()) {
            log.warn("Sorter plan target resolution failed: {}", gridTarget.userMessage());
            return SorterFeedbackResult.failure(gridTarget.userMessage());
        }
        if (gridTarget.controllerPos() == null || gridTarget.dimensionId() == null || gridTarget.grid() == null) {
            log.warn("Sorter plan missing runtime network details after target resolution.");
            return SorterFeedbackResult.failure("Could not resolve runtime ME network details.");
        }

        try {
            NetworkBindingKey bindingKey = new NetworkBindingKey(gridTarget.dimensionId(), gridTarget.controllerPos());
            var profileId = new NetworkProfileBindingStore(source.getServer()).findProfileId(bindingKey).orElse(null);
            if (profileId == null) {
                return SorterFeedbackResult.failure(
                        "Current ME network is not bound to any global profile. Use /sorter me bindProfile <number> first.");
            }

            var storedProfile = PROFILE_REPOSITORY.findById(profileId).orElse(null);
            if (storedProfile == null) {
                log.warn("Sorter plan bound profile is missing: {}", profileId);
                return SorterFeedbackResult.failure("Bound profile is missing: " + profileId);
            }

            var scanSummary = Ae2DriveScanner.scan(gridTarget.grid());
            RuntimeTopology runtimeTopology = RuntimeZoneRegistryBuilder.buildTopology(gridTarget.grid());
            ZoneAllocationPlan plan = LiveZoneAllocationPlanner.plan(
                    storedProfile.profile(), runtimeTopology, source.getServer().registryAccess());
            log.info("Built sorter plan for profile {} (id={}) with {} assignment(s), executeMove={}",
                    storedProfile.profile().name(), storedProfile.profile().id(), plan.assignmentCount(), executeMove);
            var lines = new ArrayList<>(summarizePlan(storedProfile.profile().name(), storedProfile.profile().id(), plan));

            if (executeMove) {
                var detailedResult = Ae2ZoneMoveExecutor.executeDetailed(
                        gridTarget.grid(),
                        plan,
                        runtimeTopology,
                        Config.MAX_TRANSFERS_PER_OPERATION.get());
                lines.add(SorterComponentHelper.keyValue("move.attempted", String.valueOf(detailedResult.executionResult().attemptedMoveCount())));
                lines.add(SorterComponentHelper.keyValue("move.completed", String.valueOf(detailedResult.executionResult().completedMoveCount())));
                lines.add(SorterComponentHelper.keyValue("move.failed", String.valueOf(detailedResult.executionResult().failedMoveCount())));
                lines.add(SorterComponentHelper.keyValue("move.requestedAmount", String.valueOf(detailedResult.executionResult().requestedAmount())));
                lines.add(SorterComponentHelper.keyValue("move.movedAmount", String.valueOf(detailedResult.executionResult().movedAmount())));
                String logPath = SorterPlanFileLogger.logPlanAndMove(
                        source,
                        gridTarget,
                        storedProfile.profile(),
                        scanSummary,
                        plan,
                        detailedResult);
                log.info("Sorter planAndMove executed: moved {}/{} item units, detailedLog={}",
                        detailedResult.executionResult().movedAmount(),
                        detailedResult.executionResult().requestedAmount(),
                        logPath);
                lines.add(SorterComponentHelper.clickableFile("detailedLog", logPath));
            } else {
                String logPath = SorterPlanFileLogger.logPlan(
                        source,
                        gridTarget,
                        storedProfile.profile(),
                        scanSummary,
                        plan,
                        runtimeTopology);
                log.info("Sorter plan report written to {}", logPath);
                lines.add(SorterComponentHelper.clickableFile("detailedLog", logPath));
            }

            return SorterFeedbackResult.success(lines);
        } catch (IOException exception) {
            log.error("Failed to load sorter profile bindings or stored profile data.", exception);
            return SorterFeedbackResult.failure("Failed to load bound profile or bindings. Check server logs.");
        }
    }

    private static List<Component> summarizePlan(String profileName, String profileId, ZoneAllocationPlan plan) {
        List<Component> lines = new ArrayList<>();
        lines.add(SorterComponentHelper.keyValue("profile", profileName + " [id=" + profileId + "]"));
        lines.add(SorterComponentHelper.keyValue("assignmentCount", String.valueOf(plan.assignmentCount())));
        lines.add(SorterComponentHelper.keyValue("movableAssignmentCount", String.valueOf(plan.movableAssignments().size())));

        Map<String, List<ItemZoneAssignment>> assignmentsByZone = plan.movableAssignments().stream()
                .collect(Collectors.groupingBy(ItemZoneAssignment::targetZoneId));

        assignmentsByZone.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.nullsLast(String::compareTo)))
                .forEach(entry -> {
                    long totalAmount = entry.getValue().stream().mapToLong(ItemZoneAssignment::totalAmount).sum();
                    lines.add(SorterComponentHelper.line("zone=" + entry.getKey()
                            + " | movableItems=" + entry.getValue().size()
                            + " | totalAmount=" + totalAmount));
                });

        return lines;
    }
}
