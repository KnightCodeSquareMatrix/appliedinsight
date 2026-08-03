package com.knightcode.appliedstoragesorter.profilegen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.knightcode.appliedstoragesorter.analysis.SorterDumpRoutingAnalyzer;
import com.knightcode.appliedstoragesorter.analysis.SorterDumpRoutingSuggestionAnalyzer;
import com.knightcode.appliedstoragesorter.rule.filter.FilterCondition;
import com.knightcode.appliedstoragesorter.rule.filter.FilterField;
import com.knightcode.appliedstoragesorter.rule.filter.FilterOperator;
import com.knightcode.appliedstoragesorter.rule.filter.ItemFilter;
import com.knightcode.appliedstoragesorter.rule.route.RouteAction;
import com.knightcode.appliedstoragesorter.rule.route.RouteRule;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfile;
import com.knightcode.appliedstoragesorter.rule.zone.StorageZone;
import com.knightcode.appliedstoragesorter.rule.zone.StorageZoneKind;

public final class HeuristicRoutingProfileGenerator implements RoutingProfileGenerator {
    private static final String GENERATED_PRIORITY_ZONE_ID = "generated_priority_items";

    @Override
    public ProfileGenerationResult generate(ProfileGenerationRequest request) {
        RoutingProfile baseProfile = request.baseProfile();
        List<StorageZone> zones = new ArrayList<>(baseProfile.zones());
        List<ItemFilter> filters = new ArrayList<>(baseProfile.filters());
        List<RouteRule> routeRules = new ArrayList<>(baseProfile.routeRules());
        List<String> notes = new ArrayList<>();
        Set<String> coveredMods = new HashSet<>();
        Set<String> coveredItems = new HashSet<>();

        int nextPriority = routeRules.stream().mapToInt(RouteRule::priority).max().orElse(0) + 10;
        int addedModRules = 0;

        for (var mod : request.suggestionAnalysis().topFallbackMods()) {
            if (addedModRules >= request.maxSuggestedModRules()) {
                break;
            }
            if (mod.itemCount() < request.minimumModFallbackItemCount()) {
                continue;
            }

            String modId = mod.modId();
            if (hasExistingModRule(baseProfile, modId)) {
                notes.add("Skipped mod-based generation for '" + modId + "' because a matching mod filter already exists.");
                coveredMods.add(modId);
                continue;
            }

            String zoneId = "generated_mod_" + sanitizeId(modId);
            if (containsZoneId(zones, zoneId)) {
                notes.add("Skipped generated mod zone for '" + modId + "' because zone id already exists: " + zoneId);
                coveredMods.add(modId);
                continue;
            }

            String filterId = "generated_filter_mod_" + sanitizeId(modId);
            String ruleId = "generated_route_mod_" + sanitizeId(modId);

            zones.add(new StorageZone(zoneId, "Generated: " + modId, "Auto-generated zone candidate for mod " + modId,
                    true, StorageZoneKind.CUSTOM, false));
            filters.add(ItemFilter.allOf(
                    filterId,
                    "Generated mod filter: " + modId,
                    List.of(new FilterCondition(FilterField.MOD_ID, FilterOperator.EQUALS, modId))));
            routeRules.add(new RouteRule(ruleId, "Generated mod route: " + modId,
                    "Auto-generated from fallback suggestion analysis", true, nextPriority, filterId, zoneId, false,
                    RouteAction.ROUTE_TO_ZONE,
                    "Generated from fallback-heavy mod distribution."));

            coveredMods.add(modId);
            addedModRules++;
            nextPriority += 10;
            notes.add("Generated mod-based zone/filter/route for '" + modId + "'.");
        }

        int addedItemRules = 0;
        boolean priorityZoneCreated = containsZoneId(zones, GENERATED_PRIORITY_ZONE_ID);

        for (SorterDumpRoutingAnalyzer.RoutedItem item : request.suggestionAnalysis().topFallbackItems()) {
            if (addedItemRules >= request.maxSuggestedItemRules()) {
                break;
            }
            if (item.totalAmount() < request.minimumExplicitItemAmount()) {
                continue;
            }

            String modId = extractModId(item.itemId());
            if (coveredMods.contains(modId) || hasExistingItemRule(baseProfile, item.itemId())) {
                continue;
            }

            if (!priorityZoneCreated) {
                zones.add(new StorageZone(
                        GENERATED_PRIORITY_ZONE_ID,
                        "Generated Priority Items",
                        "Auto-generated zone candidate for high-value explicit item rules",
                        true,
                        StorageZoneKind.CUSTOM,
                        false));
                priorityZoneCreated = true;
                notes.add("Generated shared priority-items zone for explicit item rules.");
            }

            String filterId = "generated_filter_item_" + sanitizeId(item.itemId());
            String ruleId = "generated_route_item_" + sanitizeId(item.itemId());
            if (containsFilterId(filters, filterId) || containsRouteRuleId(routeRules, ruleId)) {
                continue;
            }

            filters.add(ItemFilter.allOf(
                    filterId,
                    "Generated item filter: " + item.itemId(),
                    List.of(new FilterCondition(FilterField.ITEM_ID, FilterOperator.EQUALS, item.itemId()))));
            routeRules.add(new RouteRule(ruleId, "Generated item route: " + item.itemId(),
                    "Auto-generated explicit item route", true, nextPriority, filterId, GENERATED_PRIORITY_ZONE_ID,
                    false, RouteAction.ROUTE_TO_ZONE,
                    "Generated from high-amount fallback item analysis."));

            coveredItems.add(item.itemId());
            addedItemRules++;
            nextPriority += 10;
            notes.add("Generated explicit item filter/route for '" + item.itemId() + "'.");
        }

        RoutingProfile generatedProfile = new RoutingProfile(
                baseProfile.id() + "_generated",
                baseProfile.name() + " (Generated Draft)",
                appendGeneratedSuffix(baseProfile.description()),
                baseProfile.enabled(),
                zones,
                filters,
                routeRules,
                baseProfile.defaultZoneId(),
                baseProfile.version() + 1);

        if (notes.isEmpty()) {
            notes.add("No generated changes were produced from the current fallback analysis.");
        }

        return new ProfileGenerationResult(generatedProfile, notes);
    }

    private static boolean hasExistingModRule(RoutingProfile profile, String modId) {
        return profile.filters().stream().anyMatch(filter -> filter.allConditions().stream().anyMatch(condition ->
                condition.field() == FilterField.MOD_ID
                        && condition.operator() == FilterOperator.EQUALS
                        && modId.equalsIgnoreCase(condition.value())));
    }

    private static boolean hasExistingItemRule(RoutingProfile profile, String itemId) {
        return profile.filters().stream().anyMatch(filter -> filter.allConditions().stream().anyMatch(condition ->
                condition.field() == FilterField.ITEM_ID
                        && condition.operator() == FilterOperator.EQUALS
                        && itemId.equalsIgnoreCase(condition.value())));
    }

    private static boolean containsZoneId(List<StorageZone> zones, String zoneId) {
        return zones.stream().anyMatch(zone -> zone.id().equals(zoneId));
    }

    private static boolean containsFilterId(List<ItemFilter> filters, String filterId) {
        return filters.stream().anyMatch(filter -> filter.id().equals(filterId));
    }

    private static boolean containsRouteRuleId(List<RouteRule> routeRules, String ruleId) {
        return routeRules.stream().anyMatch(rule -> rule.id().equals(ruleId));
    }

    private static String sanitizeId(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replace(':', '_')
                .replace('-', '_')
                .replace('/', '_')
                .replace(' ', '_');
    }

    private static String extractModId(String itemId) {
        int separator = itemId.indexOf(':');
        if (separator <= 0) {
            return "<unknown>";
        }
        return itemId.substring(0, separator);
    }

    private static String appendGeneratedSuffix(String description) {
        if (description == null || description.isBlank()) {
            return "Auto-generated draft profile based on fallback routing analysis.";
        }
        return description + " | Auto-generated draft profile based on fallback routing analysis.";
    }
}
