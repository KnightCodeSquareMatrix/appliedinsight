package com.knightcode.appliedstoragesorter.rule.filter;

import java.util.List;
import java.util.Objects;

public record FilterCombinatorDefinition(
        String name,
        String label) {
    public FilterCombinatorDefinition {
        name = requireNonBlank(name, "name");
        label = requireNonBlank(label, "label");
    }

    public static List<FilterCombinatorDefinition> defaults() {
        return List.of(
                new FilterCombinatorDefinition(FilterCombinator.AND.name(), "and"),
                new FilterCombinatorDefinition(FilterCombinator.OR.name(), "or"));
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
