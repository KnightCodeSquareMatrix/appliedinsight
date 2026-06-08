package com.knightcode.appliedstoragesorter.rule.filter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;

public final class SmartBusFilterPresets {
    private static final Gson PRETTY_GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public enum Preset {
        ALL_ITEMS("all_items", allItems()),
        DURABILITY_ITEMS("durability_items", durabilityItems()),
        ORES("ores", ores());

        private final String id;
        private final FilterExpression expression;

        Preset(String id, FilterExpression expression) {
            this.id = id;
            this.expression = expression;
        }

        public String id() {
            return id;
        }

        public FilterExpression expression() {
            return expression;
        }

        public String translationKey() {
            return "screen.appliedinsight.smart_bus.preset." + id;
        }

        public String tooltipKey() {
            return translationKey() + ".tooltip";
        }

        public String toFilterJson() {
            return PRETTY_GSON.toJson(FilterExpressionJsonCodec.toJson(expression));
        }
    }

    private SmartBusFilterPresets() {
    }

    private static FilterExpression allItems() {
        return FilterGroup.and(List.of(
                new FilterCondition(FilterField.ITEM_ID, FilterOperator.REGEX, ".+")));
    }

    private static FilterExpression durabilityItems() {
        return FilterGroup.or(List.of(
                new FilterCondition(
                        FilterField.NBT_PATH,
                        FilterOperator.REGEX,
                        "components.minecraft:damage||.*"),
                new FilterCondition(
                        FilterField.NBT_PATH,
                        FilterOperator.REGEX,
                        "components.minecraft:max_damage||.*")));
    }

    private static FilterExpression ores() {
        return FilterGroup.or(List.of(
                new FilterCondition(FilterField.TAG, FilterOperator.CONTAINS, "c:ores"),
                new FilterCondition(FilterField.TAG, FilterOperator.REGEX, ".+:ores/.*"),
                new FilterCondition(FilterField.ITEM_ID, FilterOperator.REGEX, ".*_ore$")));
    }
}
