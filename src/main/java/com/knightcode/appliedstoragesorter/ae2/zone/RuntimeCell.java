package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.Objects;

import com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;

public final class RuntimeCell {
    private final DriveCellReference reference;
    private final String zoneId;
    private final String sourceBlockId;
    private final IActionHost actionHost;
    private final MEStorage storage;

    public RuntimeCell(
            DriveCellReference reference,
            String zoneId,
            String sourceBlockId,
            IActionHost actionHost,
            MEStorage storage) {
        this.reference = Objects.requireNonNull(reference, "reference");
        this.zoneId = requireNonBlank(zoneId, "zoneId");
        this.sourceBlockId = requireNonBlank(sourceBlockId, "sourceBlockId");
        this.actionHost = Objects.requireNonNull(actionHost, "actionHost");
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    public DriveCellReference reference() {
        return reference;
    }

    public String zoneId() {
        return zoneId;
    }

    public String sourceBlockId() {
        return sourceBlockId;
    }

    public IActionHost actionHost() {
        return actionHost;
    }

    public MEStorage storage() {
        return storage;
    }

    public boolean isSameCell(DriveCellReference otherReference) {
        return reference.equals(otherReference);
    }

    public long simulateAcceptedAmount(AEItemKey itemKey, long amount) {
        if (amount <= 0) {
            return 0L;
        }

        return storage.insert(
                itemKey,
                amount,
                Actionable.SIMULATE,
                IActionSource.ofMachine(actionHost));
    }

    public boolean containsExactItem(AEItemKey itemKey) {
        for (var entry : storage.getAvailableStacks()) {
            if (entry.getLongValue() > 0 && itemKey.equals(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    public int distinctItemKeyCount() {
        int count = 0;
        for (var entry : storage.getAvailableStacks()) {
            if (entry.getLongValue() > 0) {
                count++;
            }
        }
        return count;
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
