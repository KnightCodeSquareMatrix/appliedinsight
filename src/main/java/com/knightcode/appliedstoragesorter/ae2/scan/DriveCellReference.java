package com.knightcode.appliedstoragesorter.ae2.scan;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;

public record DriveCellReference(
        BlockPos drivePos,
        int slot,
        @Nullable BlockPos attachedStoragePos,
        @Nullable String attachmentDescription) {
    public DriveCellReference(BlockPos drivePos, int slot) {
        this(drivePos, slot, null, null);
    }

    public boolean hasAttachedStorage() {
        return attachedStoragePos != null;
    }

    /** True when this reference points at an ME Storage Bus-backed external inventory. */
    public boolean isExternalStorage() {
        return attachedStoragePos != null;
    }
}
