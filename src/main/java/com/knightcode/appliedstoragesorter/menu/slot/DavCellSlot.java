package com.knightcode.appliedstoragesorter.menu.slot;

import com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DavCellSlot extends SlotItemHandler {
    public DavCellSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
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
        return DavCellStack.isDavCell(stack) && super.mayPlace(stack);
    }
}
