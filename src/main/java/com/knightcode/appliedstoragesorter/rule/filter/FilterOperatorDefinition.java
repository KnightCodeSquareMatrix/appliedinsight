package com.knightcode.appliedstoragesorter.rule.filter;

import java.util.Objects;

public record FilterOperatorDefinition(
        String name,
        String label,
        String description,
        boolean unary) {
    public FilterOperatorDefinition {
        name = requireNonBlank(name, "name");
        label = requireNonBlank(label, "label");
        description = description != null ? description : "";
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
