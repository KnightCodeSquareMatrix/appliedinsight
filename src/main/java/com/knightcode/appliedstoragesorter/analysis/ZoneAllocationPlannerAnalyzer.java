package com.knightcode.appliedstoragesorter.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment;
import com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan;
import com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlanner;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfile;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfileJsonCodec;

public final class ZoneAllocationPlannerAnalyzer {
    private ZoneAllocationPlannerAnalyzer() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: ZoneAllocationPlannerAnalyzer <profile.json> <dump.json> [output.txt]");
            System.exit(1);
        }

        Path profileFile = Path.of(args[0]);
        Path dumpFile = Path.of(args[1]);
        Path outputFile = args.length >= 3 ? Path.of(args[2]) : defaultOutputPath(dumpFile);

        RoutingProfile profile = RoutingProfileJsonCodec.load(profileFile);
        ZoneAllocationPlan plan = ZoneAllocationPlanner.plan(profile, dumpFile);
        writeTextReport(profile, dumpFile, plan, outputFile);

        System.out.println("Loaded profile: " + profileFile);
        System.out.println("Analyzed dump: " + dumpFile);
        System.out.println("Wrote report: " + outputFile);
    }

    public static void writeTextReport(RoutingProfile profile, Path dumpFile, ZoneAllocationPlan plan, Path outputFile)
            throws IOException {
        if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        Map<String, ZoneSummary> zoneSummaries = buildZoneSummaries(plan.assignments());
        Map<String, Integer> decisionCounts = buildDecisionCounts(plan.assignments());

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.write("Applied Energistics: Insight Zone Allocation Planner Report\n");
            writer.write("===================================================\n");
            writer.write("profileFile=" + profile.id() + " v" + profile.version() + "\n");
            writer.write("dumpFile=" + dumpFile + "\n");
            writer.write("dumpFormatVersion=" + plan.dumpFormatVersion() + "\n");
            writer.write("dumpGeneratedAt=" + plan.dumpGeneratedAt() + "\n\n");

            writer.write("[summary]\n");
            writer.write("assignmentCount=" + plan.assignmentCount() + "\n");
            writer.write("movableAssignmentCount=" + plan.movableAssignments().size() + "\n");
            writer.write("zoneCount=" + zoneSummaries.size() + "\n");
            writer.write("profileDefaultZone=" + profile.defaultZoneId() + "\n\n");

            writer.write("[decision_counts]\n");
            for (var entry : decisionCounts.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
            writer.write("\n");

            writer.write("[zone_summary]\n");
            for (ZoneSummary zoneSummary : zoneSummaries.values()) {
                writer.write("zone=" + zoneSummary.zoneId()
                        + " | itemCount=" + zoneSummary.itemCount()
                        + " | totalAmount=" + zoneSummary.totalAmount()
                        + " | componentItemCount=" + zoneSummary.componentItemCount()
                        + " | fallbackItemCount=" + zoneSummary.fallbackItemCount()
                        + "\n");
            }
            writer.write("\n");

            writer.write("[items_by_zone]\n");
            for (ZoneSummary zoneSummary : zoneSummaries.values()) {
                writer.write("zone=" + zoneSummary.zoneId() + "\n");
                int index = 1;
                for (ItemZoneAssignment assignment : zoneSummary.sortedAssignments()) {
                    writer.write("  " + index++
                            + ". itemId=" + assignment.itemId()
                            + " | name=" + assignment.displayName()
                            + " | amount=" + assignment.totalAmount()
                            + " | occurrences=" + assignment.occurrenceCount()
                            + " | hasComponents=" + assignment.hasComponents()
                            + " | decision=" + assignment.decisionType().name()
                            + " | matchedRule=" + valueOrPlaceholder(assignment.matchedRuleId())
                            + " | matchedFilter=" + valueOrPlaceholder(assignment.matchedFilterId())
                            + "\n");
                }
                writer.write("\n");
            }

            writer.write("[fallback_items]\n");
            int fallbackIndex = 1;
            for (ItemZoneAssignment assignment : plan.assignments().stream()
                    .filter(a -> a.decisionType().name().equals("FALLBACK"))
                    .sorted(Comparator.comparingLong(ItemZoneAssignment::totalAmount).reversed()
                            .thenComparing(ItemZoneAssignment::itemId))
                    .toList()) {
                writer.write(fallbackIndex++
                        + ". zone=" + valueOrPlaceholder(assignment.targetZoneId())
                        + " | itemId=" + assignment.itemId()
                        + " | name=" + assignment.displayName()
                        + " | amount=" + assignment.totalAmount()
                        + " | occurrences=" + assignment.occurrenceCount()
                        + " | hasComponents=" + assignment.hasComponents()
                        + "\n");
            }
        }
    }

    private static Map<String, ZoneSummary> buildZoneSummaries(List<ItemZoneAssignment> assignments) {
        Map<String, ZoneSummary> zoneSummaries = new LinkedHashMap<>();

        assignments.stream()
                .sorted(Comparator.comparing(ItemZoneAssignment::targetZoneId, Comparator.nullsLast(String::compareTo))
                        .thenComparing(Comparator.comparingLong(ItemZoneAssignment::totalAmount).reversed())
                        .thenComparing(ItemZoneAssignment::itemId))
                .forEach(assignment -> zoneSummaries
                        .computeIfAbsent(valueOrPlaceholder(assignment.targetZoneId()), ZoneSummary::new)
                        .add(assignment));

        return zoneSummaries;
    }

    private static Map<String, Integer> buildDecisionCounts(List<ItemZoneAssignment> assignments) {
        Map<String, Integer> decisionCounts = new LinkedHashMap<>();

        assignments.stream()
                .map(assignment -> assignment.decisionType().name())
                .sorted()
                .forEach(decisionType -> decisionCounts.merge(decisionType, 1, Integer::sum));

        return decisionCounts;
    }

    private static Path defaultOutputPath(Path dumpFile) {
        String fileName = dumpFile.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String baseName = dot >= 0 ? fileName.substring(0, dot) : fileName;
        return dumpFile.resolveSibling(baseName + ".zone-allocation-plan.txt");
    }

    private static String valueOrPlaceholder(String value) {
        return value == null || value.isBlank() ? "<none>" : value;
    }

    private static final class ZoneSummary {
        private final String zoneId;
        private int itemCount;
        private long totalAmount;
        private int componentItemCount;
        private int fallbackItemCount;
        private final List<ItemZoneAssignment> assignments = new java.util.ArrayList<>();

        private ZoneSummary(String zoneId) {
            this.zoneId = zoneId;
        }

        private void add(ItemZoneAssignment assignment) {
            itemCount++;
            totalAmount += assignment.totalAmount();
            if (assignment.hasComponents()) {
                componentItemCount++;
            }
            if (assignment.decisionType().name().equals("FALLBACK")) {
                fallbackItemCount++;
            }
            assignments.add(assignment);
        }

        private String zoneId() {
            return zoneId;
        }

        private int itemCount() {
            return itemCount;
        }

        private long totalAmount() {
            return totalAmount;
        }

        private int componentItemCount() {
            return componentItemCount;
        }

        private int fallbackItemCount() {
            return fallbackItemCount;
        }

        private List<ItemZoneAssignment> sortedAssignments() {
            return assignments.stream()
                    .sorted(Comparator.comparingLong(ItemZoneAssignment::totalAmount).reversed()
                            .thenComparing(ItemZoneAssignment::itemId))
                    .toList();
        }
    }
}
