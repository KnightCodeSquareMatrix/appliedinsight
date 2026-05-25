package com.knightcode.appliedstoragesorter.rule.filter;

import java.util.List;
import java.util.Objects;

public record FilterFieldDefinition(
        String name,
        String label,
        String description,
        FilterValueType valueType,
        String valueEditorType,
        List<FilterOperator> supportedOperators) {
    public FilterFieldDefinition {
        name = requireNonBlank(name, "name");
        label = requireNonBlank(label, "label");
        description = description != null ? description : "";
        valueType = Objects.requireNonNull(valueType, "valueType");
        valueEditorType = requireNonBlank(valueEditorType, "valueEditorType");
        supportedOperators = List.copyOf(Objects.requireNonNull(supportedOperators, "supportedOperators"));
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
