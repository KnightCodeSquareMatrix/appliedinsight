package com.knightcode.appliedstoragesorter.rule.route;

import java.util.List;
import java.util.Set;

import com.knightcode.appliedstoragesorter.rule.filter.FilterCondition;
import com.knightcode.appliedstoragesorter.rule.filter.FilterGroup;
import com.knightcode.appliedstoragesorter.rule.filter.FilterField;
import com.knightcode.appliedstoragesorter.rule.filter.FilterOperator;
import com.knightcode.appliedstoragesorter.rule.filter.ItemFilter;
import com.knightcode.appliedstoragesorter.rule.filter.ItemMatchContext;
import com.knightcode.appliedstoragesorter.rule.zone.StorageZone;
import com.knightcode.appliedstoragesorter.rule.zone.StorageZoneKind;

public final class RoutingEngineExample {
    private RoutingEngineExample() {
    }

    public static void main(String[] args) {
        var profile = createExampleProfile();
        var simpleContext = new ItemMatchContext(
                "minecraft:cobblestone",
                "minecraft",
                "Cobblestone",
                Set.of("minecraft:stone_crafting_materials"),
                false,
                12000L,
                "");

        var explanation = RoutingEngine.explain(profile, simpleContext);
        System.out.println("Decision type: " + explanation.decision().decisionType());
        System.out.println("Final zone: " + explanation.decision().finalZoneId());
        System.out.println("Matched rules: " + explanation.decision().matchedRuleIds());
        System.out.println("Diagnostics: " + explanation.decision().diagnosticMessages());
    }

    public static RoutingProfile createExampleProfile() {
        var bulkZone = new StorageZone("bulk", "Bulk", "Large-volume items", true, StorageZoneKind.BULK, false);
        var miscZone = new StorageZone("misc", "Misc", "Default catch-all zone", true, StorageZoneKind.MISC, true);

        var bulkFilter = new ItemFilter(
                "bulk_filter",
                "Bulk threshold",
                "",
                true,
                FilterGroup.and(List.of(new FilterCondition(FilterField.TOTAL_AMOUNT, FilterOperator.GREATER_OR_EQUAL, "4096"))));

        var componentFilter = new ItemFilter(
                "component_filter",
                "Component-safe items",
                "",
                true,
                FilterGroup.and(List.of(new FilterCondition(FilterField.HAS_COMPONENTS, FilterOperator.IS_TRUE, null))));

        return new RoutingProfile(
                "default_profile",
                "Default Profile",
                "Example routing profile for standalone Java testing",
                true,
                List.of(bulkZone, miscZone),
                List.of(bulkFilter, componentFilter),
                List.of(
                        RouteRule.routeToZone("bulk_route", "Route bulk items", 10, "bulk_filter", "bulk"),
                        new RouteRule("component_route", "Route component items", "", true, 20, "component_filter", "misc", false,
                                RouteAction.ROUTE_TO_ZONE, "Components are routed conservatively.")),
                "misc",
                1);
    }
}
