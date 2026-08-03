package com.knightcode.appliedstoragesorter.ae2.dav.cell;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import java.util.Map;
import java.util.UUID;

public final class WorldDavCellBackend implements DavCellBackend {
    private final DavCellState state;
    private final Runnable onDirty;

    public WorldDavCellBackend(DavCellState state, Runnable onDirty) {
        this.state = state;
        this.onDirty = onDirty;
    }

    @Override
    public UUID cellId() {
        return state.cellId();
    }

    @Override
    public DavCellLedger ledger() {
        return state.ledger();
    }

    @Override
    public long totalBytes() {
        return state.ledger().totalBytes();
    }

    @Override
    public long totalTypeCapacity() {
        return state.ledger().totalTypeCapacity();
    }

    @Override
    public long usedBytes() {
        return state.usedBytes();
    }

    @Override
    public long usedTypeCapacity() {
        return state.usedTypeCapacity();
    }

    @Override
    public boolean wouldInsertNewType(AEItemKey key) {
        return state.wouldInsertNewType(key);
    }

    @Override
    public long insert(AEItemKey key, long amount, Actionable mode) {
        if (amount <= 0) {
            return 0;
        }
        long freeBytes = Math.max(0, totalBytes() - usedBytes());
        long freeTypes = Math.max(0, totalTypeCapacity() - usedTypeCapacity());
        boolean newType = wouldInsertNewType(key);
        if (newType && freeTypes <= 0) {
            return 0;
        }
        long accepted = Math.min(amount, freeBytes);
        if (accepted <= 0) {
            return 0;
        }
        if (mode == Actionable.MODULATE) {
            state.storedItems().merge(key, accepted, Math::addExact);
            markDirty();
        }
        return accepted;
    }

    @Override
    public long extract(AEItemKey key, long amount, Actionable mode) {
        if (amount <= 0) {
            return 0;
        }
        long stored = state.storedItems().getOrDefault(key, 0L);
        long extracted = Math.min(amount, stored);
        if (extracted <= 0) {
            return 0;
        }
        if (mode == Actionable.MODULATE) {
            long remaining = stored - extracted;
            if (remaining > 0) {
                state.storedItems().put(key, remaining);
            } else {
                state.storedItems().remove(key);
            }
            markDirty();
        }
        return extracted;
    }

    @Override
    public void writeAvailableStacks(KeyCounter out) {
        state.storedItems().forEach(out::add);
    }

    @Override
    public void absorbCapacity(long addedBytes, long addedTypes) {
        state.absorbCapacity(addedBytes, addedTypes);
        markDirty();
    }

    @Override
    public void importLegacy(DavCellLedger legacyLedger, Map<AEItemKey, Long> legacyItems) {
        state.importLegacy(legacyLedger, legacyItems);
        markDirty();
    }

    private void markDirty() {
        onDirty.run();
    }
}
