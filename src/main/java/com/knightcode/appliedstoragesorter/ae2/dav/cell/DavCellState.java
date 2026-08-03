package com.knightcode.appliedstoragesorter.ae2.dav.cell;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class DavCellState {
    private static final String STORED_ITEMS_TAG = "StoredItems";
    private static final String STORED_KEY_TAG = "Key";
    private static final String STORED_AMOUNT_TAG = "Amount";
    private static final String LEDGER_CELL_COUNT_TAG = "AbsorbedCellCount";
    private static final String LEDGER_BYTES_TAG = "AbsorbedBytes";
    private static final String LEDGER_TYPES_TAG = "AbsorbedTypeCapacity";

    private final UUID cellId;
    private DavCellLedger ledger = DavCellLedger.EMPTY;
    private final Map<AEItemKey, Long> storedItems = new LinkedHashMap<>();

    public DavCellState(UUID cellId) {
        this.cellId = cellId;
    }

    public UUID cellId() {
        return cellId;
    }

    public DavCellLedger ledger() {
        return ledger;
    }

    public Map<AEItemKey, Long> storedItems() {
        return storedItems;
    }

    public void setLedger(DavCellLedger ledger) {
        this.ledger = ledger != null ? ledger : DavCellLedger.EMPTY;
    }

    public void absorbCapacity(long addedBytes, long addedTypes) {
        ledger = ledger.addAbsorbed(addedBytes, addedTypes);
    }

    public long usedBytes() {
        return storedItems.values().stream().mapToLong(Long::longValue).sum();
    }

    public int usedTypeCapacity() {
        return storedItems.size();
    }

    public boolean wouldInsertNewType(AEItemKey key) {
        return !storedItems.containsKey(key);
    }

    public void importLegacy(DavCellLedger legacyLedger, Map<AEItemKey, Long> legacyItems) {
        if (legacyLedger != null) {
            ledger = legacyLedger;
        }
        if (legacyItems != null && !legacyItems.isEmpty()) {
            storedItems.clear();
            legacyItems.forEach((key, amount) -> {
                if (amount != null && amount > 0) {
                    storedItems.put(key, amount);
                }
            });
        }
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        tag.putUUID("CellId", cellId);
        tag.putLong(LEDGER_CELL_COUNT_TAG, ledger.absorbedCellCount());
        tag.putLong(LEDGER_BYTES_TAG, ledger.absorbedBytes());
        tag.putLong(LEDGER_TYPES_TAG, ledger.absorbedTypeCapacity());
        var list = new ListTag();
        storedItems.forEach((key, amount) -> {
            var entry = new CompoundTag();
            entry.put(STORED_KEY_TAG, key.toTagGeneric(registries));
            entry.putLong(STORED_AMOUNT_TAG, amount);
            list.add(entry);
        });
        tag.put(STORED_ITEMS_TAG, list);
        return tag;
    }

    public static DavCellState load(CompoundTag tag, HolderLookup.Provider registries) {
        var cellId = tag.getUUID("CellId");
        var state = new DavCellState(cellId);
        state.ledger = new DavCellLedger(
                tag.getLong(LEDGER_CELL_COUNT_TAG),
                tag.getLong(LEDGER_BYTES_TAG),
                tag.getLong(LEDGER_TYPES_TAG));
        var list = tag.getList(STORED_ITEMS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            var entry = list.getCompound(i);
            var key = AEKey.fromTagGeneric(registries, entry.getCompound(STORED_KEY_TAG));
            long amount = entry.getLong(STORED_AMOUNT_TAG);
            if (key instanceof AEItemKey itemKey && amount > 0) {
                state.storedItems.put(itemKey, amount);
            }
        }
        return state;
    }
}
