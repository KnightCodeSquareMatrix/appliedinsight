package com.knightcode.appliedstoragesorter.plan;

import java.util.Objects;

import com.knightcode.appliedstoragesorter.rule.route.RoutingDecisionType;

public record ItemZoneAssignment(
        String itemId,
        String modId,
        String displayName,
        long totalAmount,
        boolean hasComponents,
        int occurrenceCount,
        String serializedStackNbt,
        String targetZoneId,
        RoutingDecisionType decisionType,
        String matchedRuleId,
        String matchedFilterId) {
    public ItemZoneAssignment {
        itemId = requireNonBlank(itemId, "itemId");
        modId = requireNonBlank(modId, "modId");
        displayName = displayName != null ? displayName : "";
        if (totalAmount < 0) {
            throw new IllegalArgumentException("totalAmount must not be negative");
        }
        if (occurrenceCount < 0) {
            throw new IllegalArgumentException("occurrenceCount must not be negative");
        }
        serializedStackNbt = serializedStackNbt != null ? serializedStackNbt : "";
        targetZoneId = normalize(targetZoneId);
        decisionType = Objects.requireNonNull(decisionType, "decisionType");
        matchedRuleId = normalize(matchedRuleId);
        matchedFilterId = normalize(matchedFilterId);
    }

    public boolean isMoveCandidate() {
        return targetZoneId != null
                && (decisionType == RoutingDecisionType.ROUTED || decisionType == RoutingDecisionType.FALLBACK);
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
