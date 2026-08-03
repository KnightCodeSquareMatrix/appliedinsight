package com.knightcode.appliedstoragesorter.rule.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ItemFilter(
        String id,
        String name,
        String description,
        boolean enabled,
        FilterExpression root) {
    public ItemFilter {
        id = requireNonBlank(id, "id");
        name = requireNonBlank(name, "name");
        description = description != null ? description : "";
        root = Objects.requireNonNull(root, "root");
    }

    public static ItemFilter allOf(String id, String name, List<FilterCondition> conditions) {
        return new ItemFilter(id, name, "", true, FilterGroup.and(conditions));
    }

    public static ItemFilter anyOf(String id, String name, List<FilterCondition> conditions) {
        return new ItemFilter(id, name, "", true, FilterGroup.or(conditions));
    }

    public List<FilterCondition> allConditions() {
        List<FilterCondition> conditions = new ArrayList<>();
        collectConditions(root, conditions);
        return List.copyOf(conditions);
    }

    private static void collectConditions(FilterExpression expression, List<FilterCondition> sink) {
        if (expression instanceof FilterCondition condition) {
            sink.add(condition);
            return;
        }

        FilterGroup group = (FilterGroup) expression;
        for (FilterExpression child : group.rules()) {
            collectConditions(child, sink);
        }
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
