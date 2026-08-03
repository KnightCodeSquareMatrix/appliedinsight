package com.knightcode.appliedstoragesorter.rule.route;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.knightcode.appliedstoragesorter.rule.filter.ItemFilter;
import com.knightcode.appliedstoragesorter.rule.zone.StorageZone;

public record RoutingProfile(
        String id,
        String name,
        String description,
        boolean enabled,
        List<StorageZone> zones,
        List<ItemFilter> filters,
        List<RouteRule> routeRules,
        String defaultZoneId,
        int version) {
    public RoutingProfile {
        id = requireNonBlank(id, "id");
        name = requireNonBlank(name, "name");
        description = description != null ? description : "";
        zones = List.copyOf(Objects.requireNonNull(zones, "zones"));
        filters = List.copyOf(Objects.requireNonNull(filters, "filters"));
        routeRules = List.copyOf(Objects.requireNonNull(routeRules, "routeRules"));
        defaultZoneId = requireNonBlank(defaultZoneId, "defaultZoneId");

        ensureUniqueZoneIds(zones);
        ensureUniqueFilterIds(filters);
        ensureUniqueRouteRuleIds(routeRules);
        ensureDefaultZoneExists(zones, defaultZoneId);
    }

    public List<RouteRule> sortedRouteRules() {
        return routeRules.stream()
                .sorted(Comparator.comparingInt(RouteRule::priority).thenComparing(RouteRule::id))
                .toList();
    }

    public StorageZone getZone(String zoneId) {
        return zones.stream()
                .filter(zone -> zone.id().equals(zoneId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown zone id: " + zoneId));
    }

    public ItemFilter getFilter(String filterId) {
        return filters.stream()
                .filter(filter -> filter.id().equals(filterId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown filter id: " + filterId));
    }

    private static void ensureUniqueZoneIds(List<StorageZone> zones) {
        long distinctCount = zones.stream().map(StorageZone::id).distinct().count();
        if (distinctCount != zones.size()) {
            throw new IllegalArgumentException("zones must have unique ids");
        }
    }

    private static void ensureUniqueFilterIds(List<ItemFilter> filters) {
        long distinctCount = filters.stream().map(ItemFilter::id).distinct().count();
        if (distinctCount != filters.size()) {
            throw new IllegalArgumentException("filters must have unique ids");
        }
    }

    private static void ensureUniqueRouteRuleIds(List<RouteRule> routeRules) {
        long distinctCount = routeRules.stream().map(RouteRule::id).distinct().count();
        if (distinctCount != routeRules.size()) {
            throw new IllegalArgumentException("routeRules must have unique ids");
        }
    }

    private static void ensureDefaultZoneExists(List<StorageZone> zones, String defaultZoneId) {
        boolean exists = zones.stream().anyMatch(zone -> zone.id().equals(defaultZoneId));
        if (!exists) {
            throw new IllegalArgumentException("defaultZoneId must refer to an existing zone");
        }
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
