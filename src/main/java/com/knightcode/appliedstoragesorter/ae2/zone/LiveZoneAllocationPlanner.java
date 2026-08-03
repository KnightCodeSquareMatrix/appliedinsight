package com.knightcode.appliedstoragesorter.ae2.zone;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment;
import com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan;
import com.knightcode.appliedstoragesorter.rule.filter.ItemMatchContext;
import com.knightcode.appliedstoragesorter.rule.route.RoutingDecision;
import com.knightcode.appliedstoragesorter.rule.route.RoutingEngine;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfile;

import appeng.api.stacks.AEItemKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public final class LiveZoneAllocationPlanner {
    private LiveZoneAllocationPlanner() {
    }

    public static ZoneAllocationPlan plan(RoutingProfile profile, RuntimeTopology topology, HolderLookup.Provider registries) {
        Map<AEItemKey, LiveItemBuilder> itemsByKey = new LinkedHashMap<>();

        for (var cell : topology.allCells()) {
            var storage = cell.storage();
            if (storage == null) {
                continue;
            }

            for (var entry : storage.getAvailableStacks()) {
                if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEItemKey itemKey)) {
                    continue;
                }

                itemsByKey.computeIfAbsent(
                        itemKey,
                        key -> LiveItemBuilder.fromKey(key, registries))
                        .addOccurrence(entry.getLongValue());
            }
        }

        List<ItemZoneAssignment> assignments = itemsByKey.values().stream()
                .map(builder -> toAssignment(profile, builder.toItemData()))
                .toList();

        return new ZoneAllocationPlan(
                profile.id(),
                profile.version(),
                "live-grid",
                LocalDateTime.now().toString(),
                assignments);
    }

    private static ItemZoneAssignment toAssignment(RoutingProfile profile, LiveItem item) {
        ItemMatchContext context = new ItemMatchContext(
                item.itemId(),
                item.modId(),
                item.displayName(),
                Set.copyOf(item.tags()),
                item.hasComponents(),
                item.totalAmount(),
                item.serializedStackNbt());
        RoutingDecision decision = RoutingEngine.decide(profile, context);

        return new ItemZoneAssignment(
                item.itemId(),
                item.modId(),
                item.displayName(),
                item.totalAmount(),
                item.hasComponents(),
                item.occurrenceCount(),
                item.serializedStackNbt(),
                decision.finalZoneId(),
                decision.decisionType(),
                decision.matchedRuleId(),
                decision.matchedFilterId());
    }

    private record LiveItem(
            String itemId,
            String modId,
            String displayName,
            long totalAmount,
            int occurrenceCount,
            boolean hasComponents,
            String serializedStackNbt,
            List<String> tags) {
    }

    private static final class LiveItemBuilder {
        private final String itemId;
        private final String modId;
        private final String displayName;
        private final boolean hasComponents;
        private final String serializedStackNbt;
        private final List<String> tags;
        private long totalAmount;
        private int occurrenceCount;

        private LiveItemBuilder(
                String itemId,
                String modId,
                String displayName,
                boolean hasComponents,
                String serializedStackNbt,
                List<String> tags) {
            this.itemId = itemId;
            this.modId = modId;
            this.displayName = displayName;
            this.hasComponents = hasComponents;
            this.serializedStackNbt = serializedStackNbt;
            this.tags = tags;
        }

        private static LiveItemBuilder fromKey(
                AEItemKey key,
                net.minecraft.core.HolderLookup.Provider registries) {
            ItemStack stack = key.getReadOnlyStack();
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            String modId = stack.getItem().builtInRegistryHolder().key().location().getNamespace();
            List<String> tags = stack.getTags().map(TagKey::location).map(Object::toString).sorted().toList();
            String serializedStackNbt = stack.saveOptional(registries).toString();

            return new LiveItemBuilder(
                    itemId,
                    modId,
                    key.getDisplayName().getString(),
                    !stack.isComponentsPatchEmpty(),
                    serializedStackNbt,
                    tags);
        }

        private void addOccurrence(long amount) {
            totalAmount += amount;
            occurrenceCount++;
        }

        private LiveItem toItemData() {
            return new LiveItem(
                    itemId,
                    modId,
                    displayName,
                    totalAmount,
                    occurrenceCount,
                    hasComponents,
                    serializedStackNbt,
                    tags);
        }
    }
}
