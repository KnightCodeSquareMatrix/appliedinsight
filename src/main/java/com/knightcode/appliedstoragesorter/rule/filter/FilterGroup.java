package com.knightcode.appliedstoragesorter.rule.filter;

import java.util.List;
import java.util.Objects;

public record FilterGroup(
        FilterCombinator combinator,
        List<FilterExpression> rules) implements FilterExpression {
    public FilterGroup {
        combinator = Objects.requireNonNull(combinator, "combinator");
        rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
    }

    public static FilterGroup and(List<? extends FilterExpression> rules) {
        return new FilterGroup(FilterCombinator.AND, List.copyOf(rules));
    }

    public static FilterGroup or(List<? extends FilterExpression> rules) {
        return new FilterGroup(FilterCombinator.OR, List.copyOf(rules));
    }
}
