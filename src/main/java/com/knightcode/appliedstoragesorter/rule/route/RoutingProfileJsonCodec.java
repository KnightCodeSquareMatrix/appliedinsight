package com.knightcode.appliedstoragesorter.rule.route;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.knightcode.appliedstoragesorter.rule.filter.FilterCombinator;
import com.knightcode.appliedstoragesorter.rule.filter.FilterCondition;
import com.knightcode.appliedstoragesorter.rule.filter.FilterExpression;
import com.knightcode.appliedstoragesorter.rule.filter.FilterField;
import com.knightcode.appliedstoragesorter.rule.filter.FilterGroup;
import com.knightcode.appliedstoragesorter.rule.filter.FilterMatchMode;
import com.knightcode.appliedstoragesorter.rule.filter.FilterOperator;
import com.knightcode.appliedstoragesorter.rule.filter.ItemFilter;
import com.knightcode.appliedstoragesorter.rule.zone.StorageZone;
import com.knightcode.appliedstoragesorter.rule.zone.StorageZoneKind;

public final class RoutingProfileJsonCodec {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private RoutingProfileJsonCodec() {
    }

    public static RoutingProfile load(Path inputFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(inputFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return fromJson(root);
        }
    }

    public static void write(RoutingProfile profile, Path outputFile) throws IOException {
        if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        try (Writer writer = Files.newBufferedWriter(outputFile)) {
            GSON.toJson(toJson(profile), writer);
        }
    }

    public static RoutingProfile fromJson(JsonObject root) {
        String id = getRequiredString(root, "id");
        String name = getRequiredString(root, "name");
        String description = getOptionalString(root, "description", "");
        boolean enabled = getOptionalBoolean(root, "enabled", true);
        String defaultZoneId = getRequiredString(root, "defaultZoneId");
        int version = getOptionalInt(root, "version", 1);

        List<StorageZone> zones = readZones(getRequiredArray(root, "zones"));
        List<ItemFilter> filters = readFilters(getRequiredArray(root, "filters"));
        List<RouteRule> routeRules = readRouteRules(getRequiredArray(root, "routeRules"));

        return new RoutingProfile(id, name, description, enabled, zones, filters, routeRules, defaultZoneId, version);
    }

    public static JsonObject toJson(RoutingProfile profile) {
        JsonObject root = new JsonObject();
        root.addProperty("id", profile.id());
        root.addProperty("name", profile.name());
        root.addProperty("description", profile.description());
        root.addProperty("enabled", profile.enabled());
        root.addProperty("defaultZoneId", profile.defaultZoneId());
        root.addProperty("version", profile.version());
        root.add("zones", writeZones(profile.zones()));
        root.add("filters", writeFilters(profile.filters()));
        root.add("routeRules", writeRouteRules(profile.routeRules()));
        return root;
    }

    public static RoutingProfile createExampleProfile() {
        return RoutingEngineExample.createExampleProfile();
    }

    private static List<StorageZone> readZones(JsonArray array) {
        List<StorageZone> zones = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject zone = element.getAsJsonObject();
            zones.add(new StorageZone(
                    getRequiredString(zone, "id"),
                    getRequiredString(zone, "name"),
                    getOptionalString(zone, "description", ""),
                    getOptionalBoolean(zone, "enabled", true),
                    StorageZoneKind.valueOf(getRequiredString(zone, "kind")),
                    getOptionalBoolean(zone, "allowAsDefault", false)));
        }
        return zones;
    }

    private static List<ItemFilter> readFilters(JsonArray array) {
        List<ItemFilter> filters = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject filter = element.getAsJsonObject();
            filters.add(new ItemFilter(
                    getRequiredString(filter, "id"),
                    getRequiredString(filter, "name"),
                    getOptionalString(filter, "description", ""),
                    getOptionalBoolean(filter, "enabled", true),
                    readFilterRoot(filter)));
        }
        return filters;
    }

    private static FilterExpression readFilterRoot(JsonObject filter) {
        JsonElement rootElement = filter.get("root");
        if (rootElement != null && !rootElement.isJsonNull()) {
            return readExpression(rootElement.getAsJsonObject());
        }

        FilterMatchMode legacyMatchMode = FilterMatchMode.valueOf(getRequiredString(filter, "matchMode"));
        List<FilterCondition> legacyConditions = readConditions(getRequiredArray(filter, "conditions"));
        return switch (legacyMatchMode) {
            case ALL -> FilterGroup.and(legacyConditions);
            case ANY -> FilterGroup.or(legacyConditions);
        };
    }

    private static FilterExpression readExpression(JsonObject object) {
        if (object.has("field")) {
            return readCondition(object);
        }

        FilterCombinator combinator = FilterCombinator.valueOf(getRequiredString(object, "combinator"));
        List<FilterExpression> rules = new ArrayList<>();
        for (JsonElement element : getRequiredArray(object, "rules")) {
            rules.add(readExpression(element.getAsJsonObject()));
        }
        return new FilterGroup(combinator, rules);
    }

    private static List<FilterCondition> readConditions(JsonArray array) {
        List<FilterCondition> conditions = new ArrayList<>();
        for (JsonElement element : array) {
            conditions.add(readCondition(element.getAsJsonObject()));
        }
        return conditions;
    }

    private static FilterCondition readCondition(JsonObject condition) {
        return new FilterCondition(
                FilterField.valueOf(getRequiredString(condition, "field")),
                FilterOperator.valueOf(getRequiredString(condition, "operator")),
                getOptionalNullableString(condition, "value"));
    }

    private static List<RouteRule> readRouteRules(JsonArray array) {
        List<RouteRule> routeRules = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject rule = element.getAsJsonObject();
            routeRules.add(new RouteRule(
                    getRequiredString(rule, "id"),
                    getRequiredString(rule, "name"),
                    getOptionalString(rule, "description", ""),
                    getOptionalBoolean(rule, "enabled", true),
                    getOptionalInt(rule, "priority", 0),
                    getRequiredString(rule, "filterId"),
                    getRequiredString(rule, "targetZoneId"),
                    getOptionalBoolean(rule, "continueOnMatch", false),
                    RouteAction.valueOf(getRequiredString(rule, "action")),
                    getOptionalString(rule, "explanation", "")));
        }
        return routeRules;
    }

    private static JsonArray writeZones(List<StorageZone> zones) {
        JsonArray array = new JsonArray();
        for (StorageZone zone : zones) {
            JsonObject object = new JsonObject();
            object.addProperty("id", zone.id());
            object.addProperty("name", zone.name());
            object.addProperty("description", zone.description());
            object.addProperty("enabled", zone.enabled());
            object.addProperty("kind", zone.kind().name());
            object.addProperty("allowAsDefault", zone.allowAsDefault());
            array.add(object);
        }
        return array;
    }

    private static JsonArray writeFilters(List<ItemFilter> filters) {
        JsonArray array = new JsonArray();
        for (ItemFilter filter : filters) {
            JsonObject object = new JsonObject();
            object.addProperty("id", filter.id());
            object.addProperty("name", filter.name());
            object.addProperty("description", filter.description());
            object.addProperty("enabled", filter.enabled());
            object.add("root", writeExpression(filter.root()));
            array.add(object);
        }
        return array;
    }

    private static JsonObject writeExpression(FilterExpression expression) {
        if (expression instanceof FilterCondition condition) {
            return writeCondition(condition);
        }

        FilterGroup group = (FilterGroup) expression;
        JsonObject object = new JsonObject();
        object.addProperty("combinator", group.combinator().name());
        JsonArray rules = new JsonArray();
        for (FilterExpression child : group.rules()) {
            rules.add(writeExpression(child));
        }
        object.add("rules", rules);
        return object;
    }

    private static JsonObject writeCondition(FilterCondition condition) {
        JsonObject object = new JsonObject();
        object.addProperty("field", condition.field().name());
        object.addProperty("operator", condition.operator().name());
        if (condition.value() == null || condition.value().isBlank()) {
            object.add("value", null);
        } else {
            object.addProperty("value", condition.value());
        }
        return object;
    }

    private static JsonArray writeRouteRules(List<RouteRule> routeRules) {
        JsonArray array = new JsonArray();
        for (RouteRule rule : routeRules) {
            JsonObject object = new JsonObject();
            object.addProperty("id", rule.id());
            object.addProperty("name", rule.name());
            object.addProperty("description", rule.description());
            object.addProperty("enabled", rule.enabled());
            object.addProperty("priority", rule.priority());
            object.addProperty("filterId", rule.filterId());
            object.addProperty("targetZoneId", rule.targetZoneId());
            object.addProperty("continueOnMatch", rule.continueOnMatch());
            object.addProperty("action", rule.action().name());
            object.addProperty("explanation", rule.explanation());
            array.add(object);
        }
        return array;
    }

    private static JsonArray getRequiredArray(JsonObject object, String memberName) {
        JsonElement element = object.get(memberName);
        if (element == null || element.isJsonNull()) {
            throw new IllegalArgumentException("Missing required array field: " + memberName);
        }
        return element.getAsJsonArray();
    }

    private static String getRequiredString(JsonObject object, String memberName) {
        JsonElement element = object.get(memberName);
        if (element == null || element.isJsonNull()) {
            throw new IllegalArgumentException("Missing required string field: " + memberName);
        }
        return element.getAsString();
    }

    private static String getOptionalString(JsonObject object, String memberName, String defaultValue) {
        JsonElement element = object.get(memberName);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        return element.getAsString();
    }

    private static String getOptionalNullableString(JsonObject object, String memberName) {
        JsonElement element = object.get(memberName);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }

    private static boolean getOptionalBoolean(JsonObject object, String memberName, boolean defaultValue) {
        JsonElement element = object.get(memberName);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        return element.getAsBoolean();
    }

    private static int getOptionalInt(JsonObject object, String memberName, int defaultValue) {
        JsonElement element = object.get(memberName);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        return element.getAsInt();
    }
}
