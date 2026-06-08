package com.knightcode.appliedstoragesorter.blockentity;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

/**
 * Block entities that should keep player-facing state on the picked-up block item.
 */
public interface ItemPersistable {
    void saveToItemStack(ItemStack stack, HolderLookup.Provider registries);

    void loadFromItemStack(ItemStack stack, HolderLookup.Provider registries);
}
