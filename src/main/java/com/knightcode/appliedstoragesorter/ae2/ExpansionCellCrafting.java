package com.knightcode.appliedstoragesorter.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Resolves expansion-cell crafting patterns and network storage matches.
 *
 * <p>AE2 patterns may register outputs with components/NBT that differ from a bare
 * {@link AEItemKey#of(Item)}. Exact {@link ICraftingService#isCraftable} checks miss those.
 */
public final class ExpansionCellCrafting {
    private ExpansionCellCrafting() {
    }

    public static boolean hasCraftingPattern(ICraftingService craftingService, AEItemKey desired) {
        return resolveCraftingKey(craftingService, desired) != null;
    }

    public static AEItemKey resolveCraftingKey(ICraftingService craftingService, AEItemKey desired) {
        if (craftingService == null || desired == null) {
            return null;
        }
        if (!craftingService.getCraftingFor(desired).isEmpty()) {
            return desired;
        }
        var fuzzy = craftingService.getFuzzyCraftable(desired,
                candidate -> candidate instanceof AEItemKey itemKey && itemKey.getItem() == desired.getItem());
        if (fuzzy instanceof AEItemKey itemKey) {
            return itemKey;
        }
        for (var craftable : craftingService.getCraftables(AEItemKey.filter())) {
            if (craftable instanceof AEItemKey itemKey && itemKey.getItem() == desired.getItem()) {
                return itemKey;
            }
        }
        return null;
    }

    public static long countInNetwork(IGrid grid, Item item) {
        var storageService = grid.getStorageService();
        if (storageService == null) {
            return 0;
        }
        return countInNetwork(storageService, item);
    }

    public static long countInNetwork(IStorageService storageService, Item item) {
        if (storageService == null) {
            return 0;
        }
        long total = 0;
        for (var entry : storageService.getCachedInventory()) {
            if (entry.getKey() instanceof AEItemKey itemKey && itemKey.getItem() == item) {
                total += entry.getLongValue();
            }
        }
        return total;
    }

    public static long extractOneFromNetwork(IGrid grid, Item item) {
        var stack = extractOneStackFromNetwork(grid, item);
        return stack.isEmpty() ? 0L : stack.getCount();
    }

    public static ItemStack extractOneStackFromNetwork(IGrid grid, Item item) {
        var storageService = grid.getStorageService();
        if (storageService == null) {
            return ItemStack.EMPTY;
        }
        for (var entry : storageService.getCachedInventory()) {
            if (!(entry.getKey() instanceof AEItemKey itemKey)) {
                continue;
            }
            if (itemKey.getItem() != item || entry.getLongValue() <= 0) {
                continue;
            }
            long extracted = storageService.getInventory().extract(itemKey, 1, Actionable.MODULATE, null);
            if (extracted <= 0) {
                return ItemStack.EMPTY;
            }
            return itemKey.toStack((int) extracted);
        }
        return ItemStack.EMPTY;
    }
}
