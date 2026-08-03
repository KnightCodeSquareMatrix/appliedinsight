package com.knightcode.appliedstoragesorter.ae2.dav.cell;

import com.knightcode.appliedstoragesorter.registry.SorterItems;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

/**
 * DAV Cell is a player-facing pseudo-cell: it looks and sits like an AE2 storage cell, but is not one.
 * The item only stores {@code dav_cell_id}; when a DAV runs, that ID resolves to a {@link DavCellBackend}
 * in {@link DavCellSavedData}. Capacity, ledger, and stored items never live on the item stack.
 */
public final class DavCellStack {
    private static final String LEGACY_ABSORBED_CELL_COUNT = "dav_absorbed_cell_count";
    private static final String LEGACY_ABSORBED_BYTES = "dav_absorbed_bytes";
    private static final String LEGACY_ABSORBED_TYPE_CAPACITY = "dav_absorbed_type_capacity";

    private DavCellStack() {
    }

    public static ItemStack createEmpty() {
        return new ItemStack(SorterItems.DAV_CELL.get());
    }

    public static ItemStack createNew() {
        var stack = createEmpty();
        assignNewCellId(stack);
        return stack;
    }

    public static ItemStack createWithCellId(UUID cellId) {
        var stack = createEmpty();
        var tag = new CompoundTag();
        tag.putUUID(DavCellConstants.CUSTOM_DATA_CELL_ID, cellId);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static boolean isBlank(ItemStack stack) {
        return isDavCell(stack) && getCellId(stack) == null;
    }

    public static void assignNewCellId(ItemStack stack) {
        var tag = new CompoundTag();
        tag.putUUID(DavCellConstants.CUSTOM_DATA_CELL_ID, UUID.randomUUID());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isDavCell(ItemStack stack) {
        return !stack.isEmpty() && stack.is(SorterItems.DAV_CELL);
    }

    @Nullable
    public static UUID getCellId(ItemStack stack) {
        if (!isDavCell(stack)) {
            return null;
        }
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        var tag = customData.copyTag();
        if (!tag.hasUUID(DavCellConstants.CUSTOM_DATA_CELL_ID)) {
            return null;
        }
        return tag.getUUID(DavCellConstants.CUSTOM_DATA_CELL_ID);
    }

    public static boolean hasLegacyItemLedger(ItemStack stack) {
        if (!isDavCell(stack)) {
            return false;
        }
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        var tag = customData.copyTag();
        return tag.contains(LEGACY_ABSORBED_CELL_COUNT)
                || tag.contains(LEGACY_ABSORBED_BYTES)
                || tag.contains(LEGACY_ABSORBED_TYPE_CAPACITY);
    }

    public static DavCellLedger readLegacyItemLedger(ItemStack stack) {
        if (!hasLegacyItemLedger(stack)) {
            return DavCellLedger.EMPTY;
        }
        var tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        return new DavCellLedger(
                tag.getLong(LEGACY_ABSORBED_CELL_COUNT),
                tag.getLong(LEGACY_ABSORBED_BYTES),
                tag.getLong(LEGACY_ABSORBED_TYPE_CAPACITY));
    }

    public static void stripToIdentityOnly(ItemStack stack) {
        UUID cellId = getCellId(stack);
        if (!isDavCell(stack) || cellId == null) {
            return;
        }
        var tag = new CompoundTag();
        tag.putUUID(DavCellConstants.CUSTOM_DATA_CELL_ID, cellId);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

}
