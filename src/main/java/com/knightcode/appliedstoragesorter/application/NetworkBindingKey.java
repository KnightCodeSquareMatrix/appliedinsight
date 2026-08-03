package com.knightcode.appliedstoragesorter.application;

import java.util.Objects;

import net.minecraft.core.BlockPos;

public record NetworkBindingKey(String dimensionId, BlockPos controllerPos) {
    public NetworkBindingKey {
        dimensionId = requireNonBlank(dimensionId, "dimensionId");
        controllerPos = Objects.requireNonNull(controllerPos, "controllerPos").immutable();
    }

    public String asStorageKey() {
        return dimensionId + "|" + controllerPos.getX() + "," + controllerPos.getY() + "," + controllerPos.getZ();
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
