package com.knightcode.appliedstoragesorter.plan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record ZoneAllocationPlan(
        String profileId,
        int profileVersion,
        String dumpFormatVersion,
        String dumpGeneratedAt,
        List<ItemZoneAssignment> assignments) {
    public ZoneAllocationPlan {
        profileId = requireNonBlank(profileId, "profileId");
        dumpFormatVersion = requireNonBlank(dumpFormatVersion, "dumpFormatVersion");
        dumpGeneratedAt = normalizeTimestamp(dumpGeneratedAt);
        assignments = List.copyOf(Objects.requireNonNull(assignments, "assignments"));
    }

    public int assignmentCount() {
        return assignments.size();
    }

    public List<ItemZoneAssignment> movableAssignments() {
        return assignments.stream()
                .filter(ItemZoneAssignment::isMoveCandidate)
                .toList();
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static String normalizeTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return LocalDateTime.now().toString();
        }
        return value;
    }
}
