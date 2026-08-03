package com.knightcode.appliedstoragesorter.rule.zone;

import java.util.Objects;

public record StorageZone(
        String id,
        String name,
        String description,
        boolean enabled,
        StorageZoneKind kind,
        boolean allowAsDefault) {
    public StorageZone {
        id = requireNonBlank(id, "id");
        name = requireNonBlank(name, "name");
        description = description != null ? description : "";
        kind = Objects.requireNonNull(kind, "kind");
    }

    public static StorageZone of(String id, String name, StorageZoneKind kind) {
        return new StorageZone(id, name, "", true, kind, false);
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
