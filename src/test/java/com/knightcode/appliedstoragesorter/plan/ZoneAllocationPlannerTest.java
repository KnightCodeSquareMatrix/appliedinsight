package com.knightcode.appliedstoragesorter.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.knightcode.appliedstoragesorter.rule.route.RoutingDecisionType;

class ZoneAllocationPlannerTest {
    @Test
    void classifiesDumpItemsIntoZonesFromProfile() throws Exception {
        Path profileFile = Path.of("src/main/resources/testfiles/routing-profile-example.json");
        Path dumpFile = Path.of("src/main/resources/testfiles/me-dump-20260517-033650.json");

        ZoneAllocationPlan plan = ZoneAllocationPlanner.plan(profileFile, dumpFile);

        assertEquals("default_profile", plan.profileId());
        assertEquals(1, plan.profileVersion());
        assertEquals("1", plan.dumpFormatVersion());
        assertEquals(630, plan.assignmentCount());
        assertEquals(630, plan.movableAssignments().size());

        long bulkCount = plan.assignments().stream()
                .filter(assignment -> "bulk".equals(assignment.targetZoneId()))
                .count();
        long miscCount = plan.assignments().stream()
                .filter(assignment -> "misc".equals(assignment.targetZoneId()))
                .count();

        assertEquals(15L, bulkCount);
        assertEquals(615L, miscCount);

        Map<String, ItemZoneAssignment> byItemId = plan.assignments().stream()
                .collect(Collectors.toMap(ItemZoneAssignment::itemId, Function.identity(), (left, right) -> left));

        ItemZoneAssignment cobblestone = byItemId.get("minecraft:cobblestone");
        assertNotNull(cobblestone);
        assertEquals("bulk", cobblestone.targetZoneId());
        assertEquals(RoutingDecisionType.ROUTED, cobblestone.decisionType());
        assertEquals("bulk_route", cobblestone.matchedRuleId());
        assertEquals(2147483647L, cobblestone.totalAmount());

        ItemZoneAssignment inferiumEssence = byItemId.get("mysticalagriculture:inferium_essence");
        assertNotNull(inferiumEssence);
        assertEquals("bulk", inferiumEssence.targetZoneId());
        assertEquals(RoutingDecisionType.ROUTED, inferiumEssence.decisionType());

        ItemZoneAssignment diamondPickaxe = byItemId.get("minecraft:diamond_pickaxe");
        assertNotNull(diamondPickaxe);
        assertEquals("misc", diamondPickaxe.targetZoneId());
        assertEquals(RoutingDecisionType.ROUTED, diamondPickaxe.decisionType());
        assertEquals("component_route", diamondPickaxe.matchedRuleId());
        assertTrue(diamondPickaxe.hasComponents());

        ItemZoneAssignment advancedControlCircuit = byItemId.get("mekanism:advanced_control_circuit");
        assertNotNull(advancedControlCircuit);
        assertEquals("misc", advancedControlCircuit.targetZoneId());
        assertEquals(RoutingDecisionType.FALLBACK, advancedControlCircuit.decisionType());
        assertTrue(!advancedControlCircuit.hasComponents());
    }
}
