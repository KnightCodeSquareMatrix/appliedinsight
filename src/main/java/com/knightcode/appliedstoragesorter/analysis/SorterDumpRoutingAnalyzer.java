package com.knightcode.appliedstoragesorter.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.knightcode.appliedstoragesorter.rule.filter.ItemMatchContext;
import com.knightcode.appliedstoragesorter.rule.route.RoutingDecision;
import com.knightcode.appliedstoragesorter.rule.route.RoutingEngine;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfile;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfileJsonCodec;

public final class SorterDumpRoutingAnalyzer {
    private static final int DEFAULT_TOP_ITEMS_PER_ZONE = 20;

    private SorterDumpRoutingAnalyzer() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: SorterDumpRoutingAnalyzer <profile.json> <dump.json> [output.txt]");
            System.exit(1);
        }

        Path profileFile = Path.of(args[0]);
        Path dumpFile = Path.of(args[1]);
        Path outputFile = args.length >= 3 ? Path.of(args[2]) : defaultOutputPath(dumpFile);

        RoutingProfile profile = RoutingProfileJsonCodec.load(profileFile);
        AnalysisResult result = analyze(profile, dumpFile);
        writeTextReport(result, outputFile);

        System.out.println("Loaded profile: " + profileFile);
        System.out.println("Analyzed dump: " + dumpFile);
        System.out.println("Wrote report: " + outputFile);
    }

    public static AnalysisResult analyze(RoutingProfile profile, Path dumpFile) throws IOException {
        int totalItems = 0;
        long totalAmount = 0L;
        int fallbackItemCount = 0;

        Map<String, ZoneAggregate> zones = new HashMap<>();
        Map<String, Integer> decisionCounts = new HashMap<>();
        List<RoutedItem> fallbackItems = new ArrayList<>();

        try (var reader = Files.newBufferedReader(dumpFile); var json = new JsonReader(reader)) {
            json.beginObject();
            while (json.hasNext()) {
                String name = json.nextName();
                if (!"items".equals(name)) {
                    json.skipValue();
                    continue;
                }

                json.beginArray();
                while (json.hasNext()) {
                    DumpItem item = readItem(json);
                    totalItems++;
                    totalAmount += item.totalAmount();

                    ItemMatchContext context = new ItemMatchContext(
                            item.itemId(),
                            item.modId(),
                            item.displayName(),
                            Set.copyOf(item.tags()),
                            !item.componentsPatchEmpty(),
                            item.totalAmount(),
                            item.serializedStackNbt());

                    RoutingDecision decision = RoutingEngine.decide(profile, context);
                    String decisionType = decision.decisionType().name();
                    decisionCounts.merge(decisionType, 1, Integer::sum);

                    String zoneId = decision.finalZoneId() != null ? decision.finalZoneId() : "<none>";
                    zones.computeIfAbsent(zoneId, ZoneAggregate::new)
                            .record(item, decision);

                    if (decision.fallbackUsed()) {
                        fallbackItemCount++;
                        fallbackItems.add(new RoutedItem(item.itemId(), item.displayName(), item.totalAmount(), zoneId,
                                decisionType, List.copyOf(decision.matchedRuleIds())));
                    }
                }
                json.endArray();
            }
            json.endObject();
        }

        List<ZoneAggregate> zoneList = zones.values().stream()
                .sorted(Comparator.comparingLong(ZoneAggregate::totalAmount).reversed()
                        .thenComparing(ZoneAggregate::zoneId))
                .toList();

        List<RoutedItem> sortedFallbackItems = fallbackItems.stream()
                .sorted(Comparator.comparingLong(RoutedItem::totalAmount).reversed()
                        .thenComparing(RoutedItem::itemId))
                .toList();

        return new AnalysisResult(
                profile.id(),
                dumpFile,
                totalItems,
                totalAmount,
                fallbackItemCount,
                Map.copyOf(decisionCounts),
                zoneList,
                sortedFallbackItems);
    }

    public static void writeTextReport(AnalysisResult result, Path outputFile) throws IOException {
        if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.write("Applied Energistics: Insight Dump Routing Analysis\n");
            writer.write("============================================\n");
            writer.write("profileId=" + result.profileId() + "\n");
            writer.write("dumpFile=" + result.dumpFile() + "\n\n");

            writer.write("[summary]\n");
            writer.write("totalItems=" + result.totalItems() + "\n");
            writer.write("totalAmount=" + result.totalAmount() + "\n");
            writer.write("fallbackItemCount=" + result.fallbackItemCount() + "\n\n");

            writer.write("[decision_counts]\n");
            result.decisionCounts().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> writeLine(writer, entry.getKey() + "=" + entry.getValue()));
            writer.write("\n");

            writer.write("[zones]\n");
            for (ZoneAggregate zone : result.zones()) {
                writer.write("zone=" + zone.zoneId() + " | itemCount=" + zone.itemCount() + " | totalAmount="
                        + zone.totalAmount() + " | fallbackItemCount=" + zone.fallbackItemCount() + "\n");
                List<RoutedItem> topItems = zone.topItems().stream()
                        .sorted(Comparator.comparingLong(RoutedItem::totalAmount).reversed()
                                .thenComparing(RoutedItem::itemId))
                        .limit(DEFAULT_TOP_ITEMS_PER_ZONE)
                        .toList();
                for (int i = 0; i < topItems.size(); i++) {
                    RoutedItem item = topItems.get(i);
                    writer.write("  " + (i + 1) + ". " + item.itemId()
                            + " | name=" + item.displayName()
                            + " | amount=" + item.totalAmount()
                            + " | decision=" + item.decisionType()
                            + " | matchedRules=" + item.matchedRuleIds()
                            + "\n");
                }
            }
            writer.write("\n");

            writer.write("[fallback_items]\n");
            for (int i = 0; i < Math.min(DEFAULT_TOP_ITEMS_PER_ZONE, result.fallbackItems().size()); i++) {
                RoutedItem item = result.fallbackItems().get(i);
                writer.write((i + 1) + ". " + item.itemId()
                        + " | name=" + item.displayName()
                        + " | amount=" + item.totalAmount()
                        + " | zone=" + item.zoneId()
                        + " | decision=" + item.decisionType()
                        + " | matchedRules=" + item.matchedRuleIds()
                        + "\n");
            }
        }
    }

    private static void writeLine(BufferedWriter writer, String line) {
        try {
            writer.write(line + "\n");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static DumpItem readItem(JsonReader json) throws IOException {
        String itemId = "<unknown>";
        String modId = "<unknown>";
        String displayName = "<unknown>";
        long totalAmount = 0L;
        boolean componentsPatchEmpty = true;
        String serializedStackNbt = "";
        List<String> tags = List.of();

        json.beginObject();
        while (json.hasNext()) {
            switch (json.nextName()) {
                case "itemId" -> itemId = nextNullableString(json, "<unknown>");
                case "modId" -> modId = nextNullableString(json, "<unknown>");
                case "displayName" -> displayName = nextNullableString(json, "<unknown>");
                case "totalAmount" -> totalAmount = nextLong(json);
                case "componentsPatchEmpty" -> componentsPatchEmpty = nextBoolean(json, true);
                case "serializedStackNbt" -> serializedStackNbt = nextNullableString(json, "");
                case "tags" -> tags = readStringArray(json);
                default -> json.skipValue();
            }
        }
        json.endObject();

        return new DumpItem(itemId, modId, displayName, totalAmount, componentsPatchEmpty, serializedStackNbt, tags);
    }

    private static List<String> readStringArray(JsonReader json) throws IOException {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull();
            return List.of();
        }

        List<String> values = new ArrayList<>();
        json.beginArray();
        while (json.hasNext()) {
            values.add(nextNullableString(json, "<null>"));
        }
        json.endArray();
        return List.copyOf(values);
    }

    private static long nextLong(JsonReader json) throws IOException {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull();
            return 0L;
        }
        return json.nextLong();
    }

    private static boolean nextBoolean(JsonReader json, boolean defaultValue) throws IOException {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull();
            return defaultValue;
        }
        return json.nextBoolean();
    }

    private static String nextNullableString(JsonReader json, String defaultValue) throws IOException {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull();
            return defaultValue;
        }
        return json.nextString();
    }

    private static Path defaultOutputPath(Path dumpFile) {
        String fileName = dumpFile.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String baseName = dot >= 0 ? fileName.substring(0, dot) : fileName;
        return dumpFile.resolveSibling(baseName + ".routing-analysis.txt");
    }

    public record AnalysisResult(
            String profileId,
            Path dumpFile,
            int totalItems,
            long totalAmount,
            int fallbackItemCount,
            Map<String, Integer> decisionCounts,
            List<ZoneAggregate> zones,
            List<RoutedItem> fallbackItems) {
    }

    private record DumpItem(
            String itemId,
            String modId,
            String displayName,
            long totalAmount,
            boolean componentsPatchEmpty,
            String serializedStackNbt,
            List<String> tags) {
    }

    public static final class ZoneAggregate {
        private final String zoneId;
        private int itemCount;
        private long totalAmount;
        private int fallbackItemCount;
        private final List<RoutedItem> topItems = new ArrayList<>();

        private ZoneAggregate(String zoneId) {
            this.zoneId = zoneId;
        }

        private void record(DumpItem item, RoutingDecision decision) {
            itemCount++;
            totalAmount += item.totalAmount();
            if (decision.fallbackUsed()) {
                fallbackItemCount++;
            }
            topItems.add(new RoutedItem(item.itemId(), item.displayName(), item.totalAmount(), zoneId,
                    decision.decisionType().name(), List.copyOf(decision.matchedRuleIds())));
        }

        public String zoneId() {
            return zoneId;
        }

        public int itemCount() {
            return itemCount;
        }

        public long totalAmount() {
            return totalAmount;
        }

        public int fallbackItemCount() {
            return fallbackItemCount;
        }

        public List<RoutedItem> topItems() {
            return List.copyOf(topItems);
        }
    }

    public record RoutedItem(
            String itemId,
            String displayName,
            long totalAmount,
            String zoneId,
            String decisionType,
            List<String> matchedRuleIds) {
    }
}
