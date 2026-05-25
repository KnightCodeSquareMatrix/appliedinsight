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

import com.knightcode.appliedstoragesorter.rule.route.RoutingProfile;

public final class SorterDumpRoutingSuggestionAnalyzer {
    private static final int DEFAULT_TOP_LIMIT = 25;

    private SorterDumpRoutingSuggestionAnalyzer() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: SorterDumpRoutingSuggestionAnalyzer <profile.json> <dump.json> [output.txt]");
            System.exit(1);
        }

        Path profileFile = Path.of(args[0]);
        Path dumpFile = Path.of(args[1]);
        Path outputFile = args.length >= 3 ? Path.of(args[2]) : defaultOutputPath(dumpFile);

        RoutingProfile profile = com.knightcode.appliedstoragesorter.rule.route.RoutingProfileJsonCodec.load(profileFile);
        SorterDumpRoutingAnalyzer.AnalysisResult routingResult = SorterDumpRoutingAnalyzer.analyze(profile, dumpFile);
        SuggestionResult result = analyze(profile, routingResult);
        writeTextReport(result, outputFile);

        System.out.println("Loaded profile: " + profileFile);
        System.out.println("Analyzed dump: " + dumpFile);
        System.out.println("Wrote suggestion report: " + outputFile);
    }

    public static SuggestionResult analyze(RoutingProfile profile,
            SorterDumpRoutingAnalyzer.AnalysisResult routingResult) {
        Map<String, ModSuggestionAggregate> mods = new HashMap<>();
        List<SorterDumpRoutingAnalyzer.RoutedItem> fallbackItems = routingResult.fallbackItems();

        long totalFallbackAmount = 0L;
        for (var item : fallbackItems) {
            totalFallbackAmount += item.totalAmount();
            String modId = extractModId(item.itemId());
            mods.computeIfAbsent(modId, ModSuggestionAggregate::new).record(item);
        }

        List<ModSuggestionAggregate> topMods = mods.values().stream()
                .sorted(Comparator.comparingLong(ModSuggestionAggregate::totalAmount).reversed()
                        .thenComparing(Comparator.comparingInt(ModSuggestionAggregate::itemCount).reversed())
                        .thenComparing(ModSuggestionAggregate::modId))
                .toList();

        List<SorterDumpRoutingAnalyzer.RoutedItem> topFallbackItems = fallbackItems.stream()
                .sorted(Comparator.comparingLong(SorterDumpRoutingAnalyzer.RoutedItem::totalAmount).reversed()
                        .thenComparing(SorterDumpRoutingAnalyzer.RoutedItem::itemId))
                .limit(DEFAULT_TOP_LIMIT)
                .toList();

        List<String> recommendedRuleIdeas = buildRecommendedRuleIdeas(topMods, topFallbackItems, profile.defaultZoneId());

        return new SuggestionResult(
                profile.id(),
                routingResult.dumpFile(),
                fallbackItems.size(),
                totalFallbackAmount,
                topMods,
                topFallbackItems,
                recommendedRuleIdeas);
    }

    public static void writeTextReport(SuggestionResult result, Path outputFile) throws IOException {
        if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.write("Applied Storage Sorter Routing Suggestions\n");
            writer.write("========================================\n");
            writer.write("profileId=" + result.profileId() + "\n");
            writer.write("dumpFile=" + result.dumpFile() + "\n\n");

            writer.write("[summary]\n");
            writer.write("fallbackItemCount=" + result.fallbackItemCount() + "\n");
            writer.write("fallbackTotalAmount=" + result.fallbackTotalAmount() + "\n\n");

            writer.write("[top_fallback_mods]\n");
            for (int i = 0; i < Math.min(DEFAULT_TOP_LIMIT, result.topFallbackMods().size()); i++) {
                var mod = result.topFallbackMods().get(i);
                writer.write((i + 1) + ". " + mod.modId()
                        + " | itemCount=" + mod.itemCount()
                        + " | totalAmount=" + mod.totalAmount()
                        + " | topItem=" + mod.topItemId()
                        + " | topItemAmount=" + mod.topItemAmount()
                        + "\n");
            }
            writer.write("\n");

            writer.write("[top_fallback_items]\n");
            for (int i = 0; i < result.topFallbackItems().size(); i++) {
                var item = result.topFallbackItems().get(i);
                writer.write((i + 1) + ". " + item.itemId()
                        + " | name=" + item.displayName()
                        + " | amount=" + item.totalAmount()
                        + " | zone=" + item.zoneId()
                        + "\n");
            }
            writer.write("\n");

            writer.write("[recommended_rule_ideas]\n");
            for (int i = 0; i < result.recommendedRuleIdeas().size(); i++) {
                writer.write((i + 1) + ". " + result.recommendedRuleIdeas().get(i) + "\n");
            }
        }
    }

    private static List<String> buildRecommendedRuleIdeas(
            List<ModSuggestionAggregate> topMods,
            List<SorterDumpRoutingAnalyzer.RoutedItem> topFallbackItems,
            String defaultZoneId) {
        List<String> ideas = new ArrayList<>();

        for (int i = 0; i < Math.min(5, topMods.size()); i++) {
            var mod = topMods.get(i);
            if (mod.totalAmount() <= 0 || mod.itemCount() <= 1) {
                continue;
            }
            ideas.add("Consider a mod-based rule for '" + mod.modId()
                    + "' because it contributes " + mod.itemCount() + " fallback items and total amount "
                    + mod.totalAmount() + ". Suggested destination: a dedicated zone instead of default zone '"
                    + defaultZoneId + "'.");
        }

        for (int i = 0; i < Math.min(5, topFallbackItems.size()); i++) {
            var item = topFallbackItems.get(i);
            ideas.add("Consider an explicit item rule for '" + item.itemId()
                    + "' because it remains in fallback with amount " + item.totalAmount()
                    + ". Suggested destination: bulk/misc-specialized zone depending on gameplay intent.");
        }

        if (ideas.isEmpty()) {
            ideas.add("No fallback-based rule ideas were generated.");
        }

        return List.copyOf(ideas);
    }

    private static String extractModId(String itemId) {
        int separator = itemId.indexOf(':');
        if (separator <= 0) {
            return "<unknown>";
        }
        return itemId.substring(0, separator);
    }

    private static Path defaultOutputPath(Path dumpFile) {
        String fileName = dumpFile.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String baseName = dot >= 0 ? fileName.substring(0, dot) : fileName;
        return dumpFile.resolveSibling(baseName + ".routing-suggestions.txt");
    }

    public record SuggestionResult(
            String profileId,
            Path dumpFile,
            int fallbackItemCount,
            long fallbackTotalAmount,
            List<ModSuggestionAggregate> topFallbackMods,
            List<SorterDumpRoutingAnalyzer.RoutedItem> topFallbackItems,
            List<String> recommendedRuleIdeas) {
    }

    public static final class ModSuggestionAggregate {
        private final String modId;
        private int itemCount;
        private long totalAmount;
        private String topItemId = "<none>";
        private long topItemAmount;

        private ModSuggestionAggregate(String modId) {
            this.modId = modId;
        }

        private void record(SorterDumpRoutingAnalyzer.RoutedItem item) {
            itemCount++;
            totalAmount += item.totalAmount();
            if (item.totalAmount() > topItemAmount) {
                topItemAmount = item.totalAmount();
                topItemId = item.itemId();
            }
        }

        public String modId() {
            return modId;
        }

        public int itemCount() {
            return itemCount;
        }

        public long totalAmount() {
            return totalAmount;
        }

        public String topItemId() {
            return topItemId;
        }

        public long topItemAmount() {
            return topItemAmount;
        }
    }
}
