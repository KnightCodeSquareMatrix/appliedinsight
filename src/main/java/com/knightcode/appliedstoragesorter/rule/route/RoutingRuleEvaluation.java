package com.knightcode.appliedstoragesorter.rule.route;

import java.util.List;
import java.util.Objects;

public record RoutingRuleEvaluation(
        String ruleId,
        String filterId,
        boolean matched,
        RouteAction action,
        String targetZoneId,
        String explanation) {
    public RoutingRuleEvaluation {
        ruleId = requireNonBlank(ruleId, "ruleId");
        filterId = requireNonBlank(filterId, "filterId");
        action = Objects.requireNonNull(action, "action");
        explanation = explanation != null ? explanation : "";
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
