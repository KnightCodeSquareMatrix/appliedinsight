package com.knightcode.appliedstoragesorter.rule.filter;

import java.util.Objects;
import java.util.Set;

public record ItemMatchContext(
        String itemId,
        String modId,
        String displayName,
        Set<String> tags,
        boolean hasComponents,
        long totalAmount,
        String nbtData) {
    public ItemMatchContext {
        itemId = requireNonBlank(itemId, "itemId");
        modId = requireNonBlank(modId, "modId");
        displayName = displayName != null ? displayName : "";
        tags = Set.copyOf(Objects.requireNonNull(tags, "tags"));
        nbtData = nbtData != null ? nbtData : "";
    }

    public boolean hasNbtData() {
        return !nbtData.isEmpty();
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
