package com.knightcode.appliedstoragesorter.block;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum SmartBusMode implements StringRepresentable {
    EMPTY("empty"),
    IMPORT("import"),
    EXPORT("export");

    private final String name;

    SmartBusMode(String name) {
        this.name = name;
    }

    public SmartBusMode next() {
        return switch (this) {
            case EMPTY -> IMPORT;
            case IMPORT -> EXPORT;
            case EXPORT -> IMPORT;
        };
    }

    public Component getDisplayName() {
        return Component.translatable("smart_bus_mode.appliedinsight." + name);
    }

    public static SmartBusMode fromStoredName(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return EMPTY;
        }
        if ("storage".equalsIgnoreCase(storedName)) {
            return IMPORT;
        }
        for (SmartBusMode mode : values()) {
            if (mode.name().equalsIgnoreCase(storedName) || mode.name.equalsIgnoreCase(storedName)) {
                return mode;
            }
        }
        return EMPTY;
    }

    public static SmartBusMode fromNetworkOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            return ordinal == 3 ? IMPORT : EMPTY;
        }
        return values()[ordinal];
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
