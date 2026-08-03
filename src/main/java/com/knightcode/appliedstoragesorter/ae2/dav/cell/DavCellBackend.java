package com.knightcode.appliedstoragesorter.ae2.dav.cell;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import java.util.Map;
import java.util.UUID;

public interface DavCellBackend {
    UUID cellId();

    DavCellLedger ledger();

    long totalBytes();

    long totalTypeCapacity();

    long usedBytes();

    long usedTypeCapacity();

    boolean wouldInsertNewType(AEItemKey key);

    long insert(AEItemKey key, long amount, Actionable mode);

    long extract(AEItemKey key, long amount, Actionable mode);

    void writeAvailableStacks(KeyCounter out);

    void absorbCapacity(long addedBytes, long addedTypes);

    void importLegacy(DavCellLedger legacyLedger, Map<AEItemKey, Long> legacyItems);
}
