package com.knightcode.appliedstoragesorter.rule.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.knightcode.appliedstoragesorter.rule.zone.StorageZoneKind;

class RoutingProfileJsonCodecTest {
    @Test
    void loadsWebGeneratedFilterProfile() throws Exception {
        Path input = Path.of("src/main/resources/testfiles/web_generated_filter.json");

        RoutingProfile profile = RoutingProfileJsonCodec.load(input);

        assertEquals("minecraft-applied-storage-profile", profile.id());
        assertEquals("misc_zone", profile.defaultZoneId());
        assertEquals(4, profile.zones().size());
        assertEquals(4, profile.filters().size());
        assertEquals(4, profile.routeRules().size());

        assertEquals(StorageZoneKind.BULK, profile.getZone("zone_hvvks8ok").kind());
        assertEquals(RouteAction.ONLY_MARK, profile.sortedRouteRules().getLast().action());

        RouteRule firstRule = profile.sortedRouteRules().getFirst();
        assertEquals("route_rule_ah5p68xz", firstRule.id());
        assertEquals(RouteAction.ROUTE_TO_ZONE, firstRule.action());
        assertEquals("zone_hvvks8ok", firstRule.targetZoneId());

        assertNotNull(profile.getFilter("filter_m181n8jn"));
        assertTrue(profile.getFilter("filter_m181n8jn").enabled());
        assertFalse(profile.getZone("zone_hvvks8ok").allowAsDefault());
    }
}
