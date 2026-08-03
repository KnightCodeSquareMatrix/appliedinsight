package com.knightcode.appliedstoragesorter.rule.filter;

import java.util.Objects;

public record FilterCondition(
        FilterField field,
        FilterOperator operator,
        String value) implements FilterExpression {
    public FilterCondition {
        field = Objects.requireNonNull(field, "field");
        operator = Objects.requireNonNull(operator, "operator");
        value = normalizeValue(operator, value);
    }

    private static String normalizeValue(FilterOperator operator, String value) {
        if (operator == FilterOperator.IS_TRUE || operator == FilterOperator.IS_FALSE) {
            return "";
        }

        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
        return value;
    }
}
