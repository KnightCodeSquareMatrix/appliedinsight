package com.knightcode.appliedstoragesorter.rule.route;

import java.util.List;
import java.util.Objects;

import com.knightcode.appliedstoragesorter.rule.filter.ItemMatchContext;

public record RoutingExplanation(
        ItemMatchContext context,
        RoutingDecision decision,
        List<RoutingRuleEvaluation> evaluations) {
    public RoutingExplanation {
        context = Objects.requireNonNull(context, "context");
        decision = Objects.requireNonNull(decision, "decision");
        evaluations = List.copyOf(Objects.requireNonNull(evaluations, "evaluations"));
    }
}
