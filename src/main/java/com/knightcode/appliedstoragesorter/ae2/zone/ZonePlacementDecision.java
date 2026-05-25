package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.Objects;

public record ZonePlacementDecision(
        String zoneId,
        boolean accepted,
        RuntimeCell targetCell,
        long acceptedAmount,
        String reason) {
    public ZonePlacementDecision {
        zoneId = requireNonBlank(zoneId, "zoneId");
        if (acceptedAmount < 0) {
            throw new IllegalArgumentException("acceptedAmount must not be negative");
        }
        reason = reason != null ? reason : "";

        if (accepted) {
            targetCell = Objects.requireNonNull(targetCell, "targetCell");
            if (acceptedAmount <= 0) {
                throw new IllegalArgumentException("accepted placement must have positive acceptedAmount");
            }
        } else {
            targetCell = null;
            acceptedAmount = 0L;
        }
    }

    public static ZonePlacementDecision accepted(String zoneId, RuntimeCell targetCell, long acceptedAmount) {
        return new ZonePlacementDecision(zoneId, true, targetCell, acceptedAmount, "");
    }

    public static ZonePlacementDecision rejected(String zoneId, String reason) {
        return new ZonePlacementDecision(zoneId, false, null, 0L, reason);
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
