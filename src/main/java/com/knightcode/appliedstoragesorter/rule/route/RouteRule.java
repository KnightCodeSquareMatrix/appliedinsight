package com.knightcode.appliedstoragesorter.rule.route;

import java.util.Objects;

public record RouteRule(
        String id,
        String name,
        String description,
        boolean enabled,
        int priority,
        String filterId,
        String targetZoneId,
        boolean continueOnMatch,
        RouteAction action,
        String explanation) {
    public RouteRule {
        id = requireNonBlank(id, "id");
        name = requireNonBlank(name, "name");
        description = description != null ? description : "";
        filterId = requireNonBlank(filterId, "filterId");
        targetZoneId = requireNonBlank(targetZoneId, "targetZoneId");
        action = Objects.requireNonNull(action, "action");
        explanation = explanation != null ? explanation : "";
    }

    public static RouteRule routeToZone(String id, String name, int priority, String filterId, String targetZoneId) {
        return new RouteRule(id, name, "", true, priority, filterId, targetZoneId, false, RouteAction.ROUTE_TO_ZONE, "");
    }

    public boolean isFallbackRule() {
        return action == RouteAction.FALLBACK;
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
