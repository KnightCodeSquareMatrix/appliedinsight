package com.knightcode.appliedstoragesorter.ae2.dav.cell;

import appeng.api.stacks.AEItemKey;
import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class DavVaultCellBinding {
    public static final String BUILT_IN_CELL_INVENTORY_TAG = "BuiltInCellInventory";

    private DavVaultCellBinding() {
    }

    public static void provisionInitialCellIfEmpty(DigitalAssetVaultBlockEntity owner) {
        var inventory = owner.getBuiltInCellInventory();
        if (!inventory.getStackInSlot(0).isEmpty()) {
            return;
        }
        inventory.setItemDirect(0, DavCellStack.createNew());
        prepareInstalledCell(owner);
    }

    public static void prepareInstalledCell(DigitalAssetVaultBlockEntity owner) {
        var inventory = owner.getBuiltInCellInventory();
        var stack = inventory.getStackInSlot(0);
        if (stack.isEmpty() || !DavCellStack.isDavCell(stack)) {
            return;
        }
        boolean inventoryChanged = false;
        if (DavCellStack.getCellId(stack) == null) {
            DavCellStack.assignNewCellId(stack);
            inventoryChanged = true;
        }
        if (owner.getLevel() instanceof ServerLevel serverLevel) {
            UUID cellId = DavCellStack.getCellId(stack);
            if (cellId != null) {
                var backend = DavCellBackends.getOrCreate(serverLevel, cellId);
                boolean hadLegacyItemLedger = DavCellStack.hasLegacyItemLedger(stack);
                migrateLegacyItemLedgerIntoBackend(stack, backend);
                if (hadLegacyItemLedger) {
                    DavCellStack.stripToIdentityOnly(stack);
                    inventoryChanged = true;
                }
            }
        }
        if (inventoryChanged) {
            inventory.setItemDirect(0, stack);
            owner.setChanged();
        }
    }

    @Nullable
    public static DavCellBackend resolveBackend(DigitalAssetVaultBlockEntity owner) {
        if (owner.getLevel() == null || owner.getLevel().isClientSide()) {
            return null;
        }
        var stack = owner.getBuiltInCellInventory().getStackInSlot(0);
        if (stack.isEmpty() || !DavCellStack.isDavCell(stack)) {
            return null;
        }
        prepareInstalledCell(owner);
        return DavCellBackends.resolve(owner.getLevel(), owner.getBuiltInCellInventory().getStackInSlot(0));
    }

    public static void migrateLegacyData(
            DigitalAssetVaultBlockEntity owner,
            Map<AEItemKey, Long> legacyItems,
            long legacyAbsorbedCellCount,
            long legacyAbsorbedBytes,
            long legacyAbsorbedTypeCapacity) {
        if (owner.getLevel() == null || owner.getLevel().isClientSide()) {
            return;
        }
        boolean hasLegacy = legacyAbsorbedCellCount > 0
                || legacyAbsorbedBytes > 0
                || legacyAbsorbedTypeCapacity > 0
                || (legacyItems != null && !legacyItems.isEmpty());
        if (!hasLegacy) {
            return;
        }
        prepareInstalledCell(owner);
        var backend = resolveBackend(owner);
        if (backend == null) {
            return;
        }
        var ledger = new DavCellLedger(legacyAbsorbedCellCount, legacyAbsorbedBytes, legacyAbsorbedTypeCapacity);
        Map<AEItemKey, Long> items = legacyItems != null ? legacyItems : Map.of();
        backend.importLegacy(ledger, items);
        owner.setChanged();
    }

    private static void migrateLegacyItemLedgerIntoBackend(ItemStack stack, DavCellBackend backend) {
        if (!DavCellStack.hasLegacyItemLedger(stack)) {
            return;
        }
        var legacy = DavCellStack.readLegacyItemLedger(stack);
        if (legacy.equals(DavCellLedger.EMPTY)) {
            return;
        }
        if (backend.ledger().equals(DavCellLedger.EMPTY) && backend.usedBytes() == 0) {
            backend.importLegacy(legacy, Map.of());
        }
    }
}
