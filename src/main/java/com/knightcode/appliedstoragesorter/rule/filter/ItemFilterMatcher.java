package com.knightcode.appliedstoragesorter.rule.filter;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class ItemFilterMatcher {
    private ItemFilterMatcher() {
    }

    public static boolean matches(ItemFilter filter, ItemMatchContext context) {
        if (!filter.enabled()) {
            return false;
        }

        return matches(filter.root(), context);
    }

    public static boolean matches(FilterExpression expression, ItemMatchContext context) {
        if (expression instanceof FilterCondition condition) {
            return matches(condition, context);
        }

        FilterGroup group = (FilterGroup) expression;
        if (group.rules().isEmpty()) {
            return true;
        }

        return switch (group.combinator()) {
            case AND -> group.rules().stream().allMatch(rule -> matches(rule, context));
            case OR -> group.rules().stream().anyMatch(rule -> matches(rule, context));
        };
    }

    public static boolean matches(FilterCondition condition, ItemMatchContext context) {
        return switch (condition.field()) {
            case ITEM_ID -> matchString(context.itemId(), condition.operator(), condition.value());
            case MOD_ID -> matchString(context.modId(), condition.operator(), condition.value());
            case TAG -> matchTags(context, condition);
            case DISPLAY_NAME -> matchString(context.displayName(), condition.operator(), condition.value());
            case HAS_COMPONENTS -> matchBoolean(context.hasComponents(), condition.operator());
            case TOTAL_AMOUNT -> matchLong(context.totalAmount(), condition.operator(), condition.value());
            case NBT_PATH -> matchNbtPath(context, condition);
        };
    }

    // ─── NBT_PATH ────────────────────────────────────────────
    //
    // NBT_PATH 的 value 使用 "||" 分隔符编码路径和比较值：
    //   value = "nbtPath||comparisonValue"
    //
    // 示例：
    //   REGEX:   "components.apotheosis:rarity.value||epic|mythic"
    //            → 提取路径值，用正则 "epic|mythic" 匹配
    //   EQUALS:  "components.apotheosis:rarity.value||epic"
    //            → 提取路径值，与 "epic" 比较
    //
    // 如果 value 不包含 "||"，整条作为 NBT 路径，比较值为空。
    // 这种情况下仅 CONTAINS 可能有意义（检查路径名本身）。

    private static final String NBT_PATH_SEPARATOR = "||";

    private static boolean matchNbtPath(ItemMatchContext context, FilterCondition condition) {
        if (!context.hasNbtData()) {
            return false;
        }

        String value = condition.value();
        String path;
        String comparison;

        int sep = value.indexOf(NBT_PATH_SEPARATOR);
        if (sep >= 0) {
            path = value.substring(0, sep);
            comparison = value.substring(sep + NBT_PATH_SEPARATOR.length());
        } else {
            path = value;
            comparison = value;
        }

        String extracted = NbtPathExtractor.extract(context.nbtData(), path);
        if (extracted == null) {
            return false;
        }

        return switch (condition.operator()) {
            case EQUALS -> equalsIgnoreCase(extracted, comparison);
            case CONTAINS -> containsIgnoreCase(extracted, comparison);
            case REGEX -> matchRegex(extracted, comparison);
            default -> false;
        };
    }

    // ─── Tags ────────────────────────────────────────────────

    private static boolean matchTags(ItemMatchContext context, FilterCondition condition) {
        return switch (condition.operator()) {
            case EQUALS, CONTAINS -> context.tags().stream()
                    .anyMatch(tag -> equalsIgnoreCase(tag, condition.value()) || containsIgnoreCase(tag, condition.value()));
            case IN -> parseCsv(condition.value()).stream().anyMatch(candidate -> context.tags().stream()
                    .anyMatch(tag -> equalsIgnoreCase(tag, candidate)));
            case REGEX -> context.tags().stream().anyMatch(tag -> matchRegex(tag, condition.value()));
            default -> false;
        };
    }

    // ─── String ──────────────────────────────────────────────

    private static boolean matchString(String actualValue, FilterOperator operator, String expectedValue) {
        return switch (operator) {
            case EQUALS -> equalsIgnoreCase(actualValue, expectedValue);
            case CONTAINS -> containsIgnoreCase(actualValue, expectedValue);
            case IN -> parseCsv(expectedValue).stream().anyMatch(candidate -> equalsIgnoreCase(actualValue, candidate));
            case REGEX -> matchRegex(actualValue, expectedValue);
            default -> false;
        };
    }

    // ─── Boolean ─────────────────────────────────────────────

    private static boolean matchBoolean(boolean actualValue, FilterOperator operator) {
        return switch (operator) {
            case IS_TRUE -> actualValue;
            case IS_FALSE -> !actualValue;
            default -> false;
        };
    }

    // ─── Long ────────────────────────────────────────────────

    private static boolean matchLong(long actualValue, FilterOperator operator, String rawExpectedValue) {
        long expectedValue = Long.parseLong(rawExpectedValue);
        return switch (operator) {
            case EQUALS -> actualValue == expectedValue;
            case GREATER_OR_EQUAL -> actualValue >= expectedValue;
            case LESS_OR_EQUAL -> actualValue <= expectedValue;
            default -> false;
        };
    }

    // ─── Regex ───────────────────────────────────────────────

    private static boolean matchRegex(String input, String pattern) {
        if (input == null || pattern == null || pattern.isBlank()) {
            return false;
        }
        try {
            return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(input).find();
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    // ─── Helpers ─────────────────────────────────────────────

    private static boolean equalsIgnoreCase(String left, String right) {
        return left.equalsIgnoreCase(right);
    }

    private static boolean containsIgnoreCase(String left, String right) {
        return left.toLowerCase(Locale.ROOT).contains(right.toLowerCase(Locale.ROOT));
    }

    private static java.util.List<String> parseCsv(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
