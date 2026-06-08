package com.knightcode.appliedstoragesorter.rule.filter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class FilterExpressionJsonCodec {

    private static final int MAX_TOTAL_RULES = 256;
    private static final Set<FilterOperator> STRING_OPERATORS = Set.of(
            FilterOperator.EQUALS, FilterOperator.CONTAINS, FilterOperator.IN, FilterOperator.REGEX);
    private static final Set<FilterOperator> LONG_OPERATORS = Set.of(
            FilterOperator.EQUALS, FilterOperator.GREATER_OR_EQUAL, FilterOperator.LESS_OR_EQUAL);
    private static final Set<FilterOperator> BOOLEAN_OPERATORS = Set.of(
            FilterOperator.IS_TRUE, FilterOperator.IS_FALSE);
    private static final Set<FilterOperator> NBT_OPERATORS = Set.of(
            FilterOperator.EQUALS, FilterOperator.CONTAINS, FilterOperator.REGEX);

    private FilterExpressionJsonCodec() {
    }

    public static FilterExpression parse(String json) {
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("Filter JSON must be an object");
        }
        FilterExpression expression = fromJson(root.getAsJsonObject());
        List<String> errors = new ArrayList<>();
        validate(expression, 0, errors);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
        return expression;
    }

    public static FilterExpression fromJson(JsonObject root) {
        if (root.has("root") && root.get("root").isJsonObject()) {
            return readExpression(root.getAsJsonObject("root"));
        }
        return readExpression(root);
    }

    public static JsonObject toJson(FilterExpression expression) {
        return writeExpression(expression);
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

    private static FilterCondition readCondition(JsonObject condition) {
        return new FilterCondition(
                FilterField.valueOf(getRequiredString(condition, "field")),
                FilterOperator.valueOf(getRequiredString(condition, "operator")),
                getOptionalNullableString(condition, "value"));
    }

    private static JsonObject writeExpression(FilterExpression expression) {
        if (expression instanceof FilterCondition condition) {
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

    // ─── Semantic validation ──────────────────────────────────

    private static int validate(FilterExpression expression, int depth, List<String> errors) {
        if (depth > 32) {
            if (errors.isEmpty()) {
                errors.add("Filter is nested too deeply (max 32 levels)");
            }
            return 0;
        }
        if (expression instanceof FilterCondition condition) {
            validateCondition(condition, errors);
            return 1;
        }
        FilterGroup group = (FilterGroup) expression;
        int count = 0;
        for (FilterExpression child : group.rules()) {
            int childCount = validate(child, depth + 1, errors);
            count += childCount;
            if (count > MAX_TOTAL_RULES && errors.stream().noneMatch(e -> e.contains("too many"))) {
                errors.add("Filter has too many conditions (max " + MAX_TOTAL_RULES + ")");
            }
        }
        return count;
    }

    private static void validateCondition(FilterCondition condition, List<String> errors) {
        FilterField field = condition.field();
        FilterOperator operator = condition.operator();
        String value = condition.value();

        Set<FilterOperator> allowed = allowedOperators(field);
        if (!allowed.contains(operator)) {
            errors.add("Field '" + field.name() + "' does not support operator '"
                    + operator.name() + "'; allowed: " + allowed);
            return;
        }

        if (operator == FilterOperator.REGEX && value != null && !value.isBlank()) {
            try {
                Pattern.compile(value);
            } catch (PatternSyntaxException e) {
                errors.add("Field '" + field.name() + "' has invalid regex pattern: " + e.getMessage());
            }
        }

        if (field == FilterField.TOTAL_AMOUNT && value != null && !value.isBlank()) {
            try {
                Long.parseLong(value);
            } catch (NumberFormatException e) {
                errors.add("Field 'TOTAL_AMOUNT' requires a numeric value, got: '" + value + "'");
            }
        }
    }

    private static Set<FilterOperator> allowedOperators(FilterField field) {
        return switch (field) {
            case ITEM_ID, MOD_ID, TAG, DISPLAY_NAME -> STRING_OPERATORS;
            case TOTAL_AMOUNT -> LONG_OPERATORS;
            case HAS_COMPONENTS -> BOOLEAN_OPERATORS;
            case NBT_PATH -> NBT_OPERATORS;
        };
    }

    // ─── JSON helpers ─────────────────────────────────────────

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

    private static String getOptionalNullableString(JsonObject object, String memberName) {
        JsonElement element = object.get(memberName);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }
}
