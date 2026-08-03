package com.knightcode.appliedstoragesorter.rule.route;

import java.util.ArrayList;
import java.util.List;

import com.knightcode.appliedstoragesorter.rule.filter.ItemFilterMatcher;
import com.knightcode.appliedstoragesorter.rule.filter.ItemMatchContext;

public final class RoutingEngine {
    private RoutingEngine() {
    }

    public static RoutingDecision decide(RoutingProfile profile, ItemMatchContext context) {
        return explain(profile, context).decision();
    }

    public static RoutingExplanation explain(RoutingProfile profile, ItemMatchContext context) {
        if (!profile.enabled()) {
            return new RoutingExplanation(
                    context,
                    new RoutingDecision(
                            RoutingDecisionType.NO_MATCH,
                            null,
                            null,
                            null,
                            false,
                            List.of(),
                            List.of("Routing profile is disabled.")),
                    List.of());
        }

        List<RoutingRuleEvaluation> evaluations = new ArrayList<>();
        List<String> matchedRuleIds = new ArrayList<>();
        List<String> diagnostics = new ArrayList<>();

        String finalZoneId = null;
        String matchedRuleId = null;
        String matchedFilterId = null;
        RoutingDecisionType decisionType = null;
        boolean fallbackUsed = false;

        for (var rule : profile.sortedRouteRules()) {
            if (!rule.enabled()) {
                evaluations.add(new RoutingRuleEvaluation(
                        rule.id(),
                        rule.filterId(),
                        false,
                        rule.action(),
                        rule.targetZoneId(),
                        appendExplanation(rule.explanation(), "Rule disabled.")));
                continue;
            }

            var filter = profile.getFilter(rule.filterId());
            boolean matched = ItemFilterMatcher.matches(filter, context);
            evaluations.add(new RoutingRuleEvaluation(
                    rule.id(),
                    rule.filterId(),
                    matched,
                    rule.action(),
                    rule.targetZoneId(),
                    rule.explanation()));

            if (!matched) {
                continue;
            }

            matchedRuleIds.add(rule.id());

            if (decisionType == null) {
                matchedRuleId = rule.id();
                matchedFilterId = rule.filterId();
                finalZoneId = rule.targetZoneId();
                fallbackUsed = rule.isFallbackRule();
                decisionType = switch (rule.action()) {
                    case ROUTE_TO_ZONE -> RoutingDecisionType.ROUTED;
                    case REJECT -> RoutingDecisionType.REJECTED;
                    case ONLY_MARK -> RoutingDecisionType.MARKED_ONLY;
                    case FALLBACK -> RoutingDecisionType.FALLBACK;
                };
            }

            if (!rule.continueOnMatch()) {
                diagnostics.add("Stopped after matched rule: " + rule.id());
                break;
            }
        }

        if (decisionType == null) {
            diagnostics.add("No route rule matched. Using default zone: " + profile.defaultZoneId());
            decisionType = RoutingDecisionType.FALLBACK;
            fallbackUsed = true;
            finalZoneId = profile.defaultZoneId();
        }

        return new RoutingExplanation(
                context,
                new RoutingDecision(
                        decisionType,
                        finalZoneId,
                        matchedRuleId,
                        matchedFilterId,
                        fallbackUsed,
                        matchedRuleIds,
                        diagnostics),
                evaluations);
    }

    private static String appendExplanation(String base, String suffix) {
        if (base == null || base.isBlank()) {
            return suffix;
        }
        return base + " " + suffix;
    }
}
