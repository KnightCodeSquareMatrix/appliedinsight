package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.Objects;

import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;

import appeng.api.networking.security.IActionHost;
import appeng.api.storage.MEStorage;

/**
 * Result of placing one item into a zone.
 * ADR-012: uses DriveCellReference + MEStorage instead of RuntimeCell.
 */
public record ZonePlacementDecision(
        String zoneId,
        boolean accepted,
        DriveCellReference targetReference,
        IActionHost targetActionHost,
        MEStorage targetStorage,
        long acceptedAmount,
        String reason) {

    public ZonePlacementDecision {
        zoneId = requireNonBlank(zoneId, "zoneId");
        if (acceptedAmount < 0) {
            throw new IllegalArgumentException("acceptedAmount must not be negative");
        }
        reason = reason != null ? reason : "";

        if (accepted) {
            targetReference = Objects.requireNonNull(targetReference, "targetReference");
            targetActionHost = Objects.requireNonNull(targetActionHost, "targetActionHost");
            targetStorage = Objects.requireNonNull(targetStorage, "targetStorage");
            if (acceptedAmount <= 0) {
                throw new IllegalArgumentException("accepted placement must have positive acceptedAmount");
            }
        } else {
            targetReference = null;
            targetActionHost = null;
            targetStorage = null;
            acceptedAmount = 0L;
        }
    }

    public static ZonePlacementDecision accepted(String zoneId, DriveCellReference ref,
                                                  IActionHost host, MEStorage storage, long amount) {
        return new ZonePlacementDecision(zoneId, true, ref, host, storage, amount, "");
    }

    public static ZonePlacementDecision rejected(String zoneId, String reason) {
        return new ZonePlacementDecision(zoneId, false, null, null, null, 0L, reason);
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
