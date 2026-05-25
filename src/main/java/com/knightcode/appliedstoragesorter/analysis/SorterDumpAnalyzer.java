package com.knightcode.appliedstoragesorter.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public final class SorterDumpAnalyzer {
    private static final int DEFAULT_TOP_ITEM_LIMIT = 100;
    private static final int DEFAULT_TOP_MOD_LIMIT = 50;
    private static final int DEFAULT_TOP_TAG_LIMIT = 50;
    private static final int DEFAULT_COMPONENT_ITEM_LIMIT = 50;
    private static final long DEFAULT_BULK_THRESHOLD = 4096L;

    private SorterDumpAnalyzer() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: SorterDumpAnalyzer <input.json> [output.txt]");
            System.exit(1);
        }

        Path input = Path.of(args[0]);
        Path output = args.length >= 2 ? Path.of(args[1]) : defaultOutputPath(input);

        var result = analyze(input);
        writeTextReport(result, output);

        System.out.println("Analyzed dump: " + input);
        System.out.println("Wrote report: " + output);
    }

    public static AnalysisResult analyze(Path inputFile) throws IOException {
        DumpSummary summary = null;
        String generatedAt = "<unknown>";
        int itemCount = 0;
        long totalAmountAcrossItems = 0;
        int componentItemCount = 0;
        int taggedItemCount = 0;
        int bulkCandidateCount = 0;

        Map<String, ModAggregate> modAggregates = new HashMap<>();
        Map<String, TagAggregate> tagAggregates = new HashMap<>();
        PriorityQueue<ItemAggregate> topItems = minHeapByAmount(DEFAULT_TOP_ITEM_LIMIT);
        PriorityQueue<ItemAggregate> topComponentItems = minHeapByAmount(DEFAULT_COMPONENT_ITEM_LIMIT);

        try (var reader = Files.newBufferedReader(inputFile); var json = new JsonReader(reader)) {
            json.beginObject();
            while (json.hasNext()) {
                String name = json.nextName();
                switch (name) {
                    case "generatedAt" -> generatedAt = nextNullableString(json, "<unknown>");
                    case "summary" -> summary = readSummary(json);
                    case "items" -> {
                        json.beginArray();
                        while (json.hasNext()) {
                            ItemAggregate item = readItem(json);
                            itemCount++;
                            totalAmountAcrossItems += item.totalAmount();

                            if (item.componentsPatchEmpty()) {
                                modAggregates.computeIfAbsent(item.modId(), ignored -> new ModAggregate(item.modId()))
                                        .recordPlainItem(item.totalAmount());
                            } else {
                                componentItemCount++;
                                pushTop(topComponentItems, item, DEFAULT_COMPONENT_ITEM_LIMIT);
                                modAggregates.computeIfAbsent(item.modId(), ignored -> new ModAggregate(item.modId()))
                                        .recordComponentItem(item.totalAmount());
                            }

                            if (!item.tags().isEmpty()) {
                                taggedItemCount++;
                            }

                            if (item.totalAmount() >= DEFAULT_BULK_THRESHOLD) {
                                bulkCandidateCount++;
                            }

                            modAggregates.computeIfAbsent(item.modId(), ignored -> new ModAggregate(item.modId()))
                                    .recordItem(item.totalAmount(), item.occurrenceCount());

                            for (String tag : item.tags()) {
                                tagAggregates.computeIfAbsent(tag, ignored -> new TagAggregate(tag))
                                        .recordItem(item.totalAmount());
                            }

                            pushTop(topItems, item, DEFAULT_TOP_ITEM_LIMIT);
                        }
                        json.endArray();
                    }
                    default -> json.skipValue();
                }
            }
            json.endObject();
        }

        return new AnalysisResult(
                inputFile,
                generatedAt,
                summary != null ? summary : DumpSummary.empty(),
                itemCount,
                totalAmountAcrossItems,
                componentItemCount,
                taggedItemCount,
                bulkCandidateCount,
                toSortedDescending(topItems),
                toSortedMods(modAggregates),
                toSortedTags(tagAggregates),
                toSortedDescending(topComponentItems));
    }

    public static void writeTextReport(AnalysisResult result, Path outputFile) throws IOException {
        Files.createDirectories(outputFile.getParent() != null ? outputFile.getParent() : Path.of("."));

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.write("Applied Storage Sorter Dump Analysis\n");
            writer.write("===================================\n");
            writer.write("input=" + result.inputFile() + "\n");
            writer.write("generatedAt=" + result.generatedAt() + "\n");
            writer.write("\n");

            writer.write("[summary]\n");
            writer.write("networkNodeCount=" + result.summary().networkNodeCount() + "\n");
            writer.write("driveCount=" + result.summary().driveCount() + "\n");
            writer.write("scannedCellSlotCount=" + result.summary().scannedCellSlotCount() + "\n");
            writer.write("mountedCellCount=" + result.summary().mountedCellCount() + "\n");
            writer.write("uniqueItemKeyCount=" + result.summary().uniqueItemKeyCount() + "\n");
            writer.write("duplicatedItemKeyCount=" + result.summary().duplicatedItemKeyCount() + "\n");
            writer.write("duplicatedCellReferenceCount=" + result.summary().duplicatedCellReferenceCount() + "\n");
            writer.write("itemOccurrenceCount=" + result.summary().itemOccurrenceCount() + "\n");
            writer.write("\n");

            writer.write("[derived]\n");
            writer.write("itemCount=" + result.itemCount() + "\n");
            writer.write("totalAmountAcrossItems=" + result.totalAmountAcrossItems() + "\n");
            writer.write("componentItemCount=" + result.componentItemCount() + "\n");
            writer.write("taggedItemCount=" + result.taggedItemCount() + "\n");
            writer.write("bulkCandidateCount(threshold=" + DEFAULT_BULK_THRESHOLD + ")=" + result.bulkCandidateCount() + "\n");
            writer.write("averageAmountPerItem=" + formatDouble(result.averageAmountPerItem()) + "\n");
            writer.write("\n");

            writer.write("[top_items_by_amount]\n");
            writeTopItems(writer, result.topItems());
            writer.write("\n");

            writer.write("[top_mods_by_total_amount]\n");
            writeTopMods(writer, result.topMods(), DEFAULT_TOP_MOD_LIMIT);
            writer.write("\n");

            writer.write("[top_tags_by_item_count]\n");
            writeTopTags(writer, result.topTags(), DEFAULT_TOP_TAG_LIMIT);
            writer.write("\n");

            writer.write("[items_with_components]\n");
            writeTopItems(writer, result.topComponentItems());
        }
    }

    private static void writeTopItems(BufferedWriter writer, List<ItemAggregate> items) throws IOException {
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            writer.write((i + 1) + ". " + item.itemId()
                    + " | name=" + item.displayName()
                    + " | mod=" + item.modId()
                    + " | amount=" + item.totalAmount()
                    + " | occurrences=" + item.occurrenceCount()
                    + " | componentsPatchEmpty=" + item.componentsPatchEmpty()
                    + " | tagCount=" + item.tags().size()
                    + "\n");
        }
    }

    private static void writeTopMods(BufferedWriter writer, List<ModAggregate> mods, int limit) throws IOException {
        for (int i = 0; i < Math.min(limit, mods.size()); i++) {
            var mod = mods.get(i);
            writer.write((i + 1) + ". " + mod.modId()
                    + " | totalAmount=" + mod.totalAmount()
                    + " | itemCount=" + mod.itemCount()
                    + " | componentItemCount=" + mod.componentItemCount()
                    + " | plainItemCount=" + mod.plainItemCount()
                    + " | totalOccurrences=" + mod.totalOccurrences()
                    + "\n");
        }
    }

    private static void writeTopTags(BufferedWriter writer, List<TagAggregate> tags, int limit) throws IOException {
        for (int i = 0; i < Math.min(limit, tags.size()); i++) {
            var tag = tags.get(i);
            writer.write((i + 1) + ". " + tag.tag()
                    + " | itemCount=" + tag.itemCount()
                    + " | totalAmount=" + tag.totalAmount()
                    + "\n");
        }
    }

    private static DumpSummary readSummary(JsonReader json) throws IOException {
        int networkNodeCount = 0;
        int driveCount = 0;
        int scannedCellSlotCount = 0;
        int mountedCellCount = 0;
        int uniqueItemKeyCount = 0;
        int duplicatedItemKeyCount = 0;
        int duplicatedCellReferenceCount = 0;
        int itemOccurrenceCount = 0;

        json.beginObject();
        while (json.hasNext()) {
            switch (json.nextName()) {
                case "networkNodeCount" -> networkNodeCount = nextInt(json);
                case "driveCount" -> driveCount = nextInt(json);
                case "scannedCellSlotCount" -> scannedCellSlotCount = nextInt(json);
                case "mountedCellCount" -> mountedCellCount = nextInt(json);
                case "uniqueItemKeyCount" -> uniqueItemKeyCount = nextInt(json);
                case "duplicatedItemKeyCount" -> duplicatedItemKeyCount = nextInt(json);
                case "duplicatedCellReferenceCount" -> duplicatedCellReferenceCount = nextInt(json);
                case "itemOccurrenceCount" -> itemOccurrenceCount = nextInt(json);
                default -> json.skipValue();
            }
        }
        json.endObject();

        return new DumpSummary(
                networkNodeCount,
                driveCount,
                scannedCellSlotCount,
                mountedCellCount,
                uniqueItemKeyCount,
                duplicatedItemKeyCount,
                duplicatedCellReferenceCount,
                itemOccurrenceCount);
    }

    private static ItemAggregate readItem(JsonReader json) throws IOException {
        String itemId = "<unknown>";
        String modId = "<unknown>";
        String displayName = "<unknown>";
        long totalAmount = 0;
        int occurrenceCount = 0;
        boolean componentsPatchEmpty = true;
        List<String> tags = List.of();

        json.beginObject();
        while (json.hasNext()) {
            switch (json.nextName()) {
                case "itemId" -> itemId = nextNullableString(json, "<unknown>");
                case "modId" -> modId = nextNullableString(json, "<unknown>");
                case "displayName" -> displayName = nextNullableString(json, "<unknown>");
                case "totalAmount" -> totalAmount = nextLong(json);
                case "occurrenceCount" -> occurrenceCount = nextInt(json);
                case "componentsPatchEmpty" -> componentsPatchEmpty = nextBoolean(json, true);
                case "tags" -> tags = readStringArray(json);
                default -> json.skipValue();
            }
        }
        json.endObject();

        return new ItemAggregate(itemId, modId, displayName, totalAmount, occurrenceCount, componentsPatchEmpty, tags);
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

    private static int nextInt(JsonReader json) throws IOException {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull();
            return 0;
        }
        return json.nextInt();
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

    private static PriorityQueue<ItemAggregate> minHeapByAmount(int limit) {
        return new PriorityQueue<>(limit, Comparator
                .comparingLong(ItemAggregate::totalAmount)
                .thenComparing(ItemAggregate::itemId));
    }

    private static void pushTop(PriorityQueue<ItemAggregate> heap, ItemAggregate value, int limit) {
        if (heap.size() < limit) {
            heap.offer(value);
            return;
        }

        ItemAggregate smallest = heap.peek();
        if (smallest != null && compareByAmountThenId(value, smallest) > 0) {
            heap.poll();
            heap.offer(value);
        }
    }

    private static int compareByAmountThenId(ItemAggregate left, ItemAggregate right) {
        int amountCompare = Long.compare(left.totalAmount(), right.totalAmount());
        if (amountCompare != 0) {
            return amountCompare;
        }
        return left.itemId().compareTo(right.itemId());
    }

    private static List<ItemAggregate> toSortedDescending(PriorityQueue<ItemAggregate> heap) {
        List<ItemAggregate> values = new ArrayList<>(heap);
        values.sort(Comparator
                .comparingLong(ItemAggregate::totalAmount)
                .reversed()
                .thenComparing(ItemAggregate::itemId));
        return List.copyOf(values);
    }

    private static List<ModAggregate> toSortedMods(Map<String, ModAggregate> values) {
        return values.values().stream()
                .sorted(Comparator.comparingLong(ModAggregate::totalAmount).reversed()
                        .thenComparing(ModAggregate::modId))
                .toList();
    }

    private static List<TagAggregate> toSortedTags(Map<String, TagAggregate> values) {
        return values.values().stream()
                .sorted(Comparator.comparingInt(TagAggregate::itemCount).reversed()
                        .thenComparingLong(TagAggregate::totalAmount).reversed()
                        .thenComparing(TagAggregate::tag))
                .toList();
    }

    private static String formatDouble(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static Path defaultOutputPath(Path input) {
        String fileName = input.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String baseName = dot >= 0 ? fileName.substring(0, dot) : fileName;
        return input.resolveSibling(baseName + ".analysis.txt");
    }

    public record AnalysisResult(
            Path inputFile,
            String generatedAt,
            DumpSummary summary,
            int itemCount,
            long totalAmountAcrossItems,
            int componentItemCount,
            int taggedItemCount,
            int bulkCandidateCount,
            List<ItemAggregate> topItems,
            List<ModAggregate> topMods,
            List<TagAggregate> topTags,
            List<ItemAggregate> topComponentItems) {
        public double averageAmountPerItem() {
            return itemCount > 0 ? (double) totalAmountAcrossItems / itemCount : 0.0D;
        }
    }

    public record DumpSummary(
            int networkNodeCount,
            int driveCount,
            int scannedCellSlotCount,
            int mountedCellCount,
            int uniqueItemKeyCount,
            int duplicatedItemKeyCount,
            int duplicatedCellReferenceCount,
            int itemOccurrenceCount) {
        private static DumpSummary empty() {
            return new DumpSummary(0, 0, 0, 0, 0, 0, 0, 0);
        }
    }

    public record ItemAggregate(
            String itemId,
            String modId,
            String displayName,
            long totalAmount,
            int occurrenceCount,
            boolean componentsPatchEmpty,
            List<String> tags) {
    }

    public static final class ModAggregate {
        private final String modId;
        private long totalAmount;
        private int itemCount;
        private int componentItemCount;
        private int plainItemCount;
        private long totalOccurrences;

        private ModAggregate(String modId) {
            this.modId = modId;
        }

        private void recordItem(long amount, int occurrences) {
            totalAmount += amount;
            itemCount++;
            totalOccurrences += occurrences;
        }

        private void recordComponentItem(long amount) {
            componentItemCount++;
        }

        private void recordPlainItem(long amount) {
            plainItemCount++;
        }

        public String modId() {
            return modId;
        }

        public long totalAmount() {
            return totalAmount;
        }

        public int itemCount() {
            return itemCount;
        }

        public int componentItemCount() {
            return componentItemCount;
        }

        public int plainItemCount() {
            return plainItemCount;
        }

        public long totalOccurrences() {
            return totalOccurrences;
        }
    }

    public static final class TagAggregate {
        private final String tag;
        private int itemCount;
        private long totalAmount;

        private TagAggregate(String tag) {
            this.tag = tag;
        }

        private void recordItem(long amount) {
            itemCount++;
            totalAmount += amount;
        }

        public String tag() {
            return tag;
        }

        public int itemCount() {
            return itemCount;
        }

        public long totalAmount() {
            return totalAmount;
        }
    }
}
