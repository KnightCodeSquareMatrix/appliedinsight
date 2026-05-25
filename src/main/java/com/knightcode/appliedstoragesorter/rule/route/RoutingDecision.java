package com.knightcode.appliedstoragesorter.rule.route;

import java.util.List;
import java.util.Objects;

public record RoutingDecision(
        RoutingDecisionType decisionType,
        String finalZoneId,
        String matchedRuleId,
        String matchedFilterId,
        boolean fallbackUsed,
        List<String> matchedRuleIds,
        List<String> diagnosticMessages) {
    public RoutingDecision {
        decisionType = Objects.requireNonNull(decisionType, "decisionType");
        matchedRuleIds = List.copyOf(Objects.requireNonNull(matchedRuleIds, "matchedRuleIds"));
        diagnosticMessages = List.copyOf(Objects.requireNonNull(diagnosticMessages, "diagnosticMessages"));
    }

    public boolean hasFinalZone() {
        return finalZoneId != null && !finalZoneId.isBlank();
    }
}
