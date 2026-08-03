package com.knightcode.appliedstoragesorter.menu.slot;

import appeng.api.storage.StorageCells;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class StorageCellSlot extends SlotItemHandler {
    public StorageCellSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return StorageCells.isCellHandled(stack) && super.mayPlace(stack);
    }
}
