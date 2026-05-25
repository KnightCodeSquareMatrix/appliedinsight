package com.knightcode.appliedstoragesorter.plan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.knightcode.appliedstoragesorter.rule.filter.ItemMatchContext;
import com.knightcode.appliedstoragesorter.rule.route.RoutingDecision;
import com.knightcode.appliedstoragesorter.rule.route.RoutingEngine;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfile;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfileJsonCodec;

public final class ZoneAllocationPlanner {
    private ZoneAllocationPlanner() {
    }

    public static ZoneAllocationPlan plan(Path profileFile, Path dumpFile) throws IOException {
        return plan(RoutingProfileJsonCodec.load(profileFile), dumpFile);
    }

    public static ZoneAllocationPlan plan(RoutingProfile profile, Path dumpFile) throws IOException {
        String dumpFormatVersion = "unknown";
        String dumpGeneratedAt = "";
        List<ItemZoneAssignment> assignments = new ArrayList<>();

        try (var reader = Files.newBufferedReader(dumpFile); var json = new JsonReader(reader)) {
            json.beginObject();
            while (json.hasNext()) {
                switch (json.nextName()) {
                    case "formatVersion" -> dumpFormatVersion = nextNullableString(json, "unknown");
                    case "generatedAt" -> dumpGeneratedAt = nextNullableString(json, "");
                    case "items" -> readAssignments(json, profile, assignments);
                    default -> json.skipValue();
                }
            }
            json.endObject();
        }

        return new ZoneAllocationPlan(
                profile.id(),
                profile.version(),
                dumpFormatVersion,
                dumpGeneratedAt,
                assignments);
    }

    private static void readAssignments(JsonReader json, RoutingProfile profile, List<ItemZoneAssignment> assignments)
            throws IOException {
        json.beginArray();
        while (json.hasNext()) {
            DumpItem item = readItem(json);
            ItemMatchContext context = new ItemMatchContext(
                    item.itemId(),
                    item.modId(),
                    item.displayName(),
                    Set.copyOf(item.tags()),
                    item.hasComponents(),
                    item.totalAmount(),
                    item.serializedStackNbt());
            RoutingDecision decision = RoutingEngine.decide(profile, context);

            assignments.add(new ItemZoneAssignment(
                    item.itemId(),
                    item.modId(),
                    item.displayName(),
                    item.totalAmount(),
                    item.hasComponents(),
                    item.occurrenceCount(),
                    item.serializedStackNbt(),
                    decision.finalZoneId(),
                    decision.decisionType(),
                    decision.matchedRuleId(),
                    decision.matchedFilterId()));
        }
        json.endArray();
    }

    private static DumpItem readItem(JsonReader json) throws IOException {
        String itemId = "minecraft:air";
        String modId = "minecraft";
        String displayName = "";
        long totalAmount = 0L;
        int occurrenceCount = 0;
        boolean componentsPatchEmpty = true;
        String serializedStackNbt = "";
        List<String> tags = List.of();

        json.beginObject();
        while (json.hasNext()) {
            switch (json.nextName()) {
                case "itemId" -> itemId = nextNullableString(json, "minecraft:air");
                case "modId" -> modId = nextNullableString(json, "minecraft");
                case "displayName" -> displayName = nextNullableString(json, "");
                case "totalAmount" -> totalAmount = nextLong(json);
                case "occurrenceCount" -> occurrenceCount = nextInt(json);
                case "componentsPatchEmpty" -> componentsPatchEmpty = nextBoolean(json, true);
                case "serializedStackNbt" -> serializedStackNbt = nextNullableString(json, "");
                case "tags" -> tags = readStringArray(json);
                default -> json.skipValue();
            }
        }
        json.endObject();

        return new DumpItem(
                itemId,
                modId,
                displayName,
                totalAmount,
                occurrenceCount,
                !componentsPatchEmpty,
                serializedStackNbt,
                tags);
    }

    private static List<String> readStringArray(JsonReader json) throws IOException {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull();
            return List.of();
        }

        List<String> values = new ArrayList<>();
        json.beginArray();
        while (json.hasNext()) {
            values.add(nextNullableString(json, ""));
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

    private static int nextInt(JsonReader json) throws IOException {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull();
            return 0;
        }
        return json.nextInt();
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

    private record DumpItem(
            String itemId,
            String modId,
            String displayName,
            long totalAmount,
            int occurrenceCount,
            boolean hasComponents,
            String serializedStackNbt,
            List<String> tags) {
    }
}
