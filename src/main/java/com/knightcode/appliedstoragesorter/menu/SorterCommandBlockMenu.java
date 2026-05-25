package com.knightcode.appliedstoragesorter.menu;

import com.knightcode.appliedstoragesorter.blockentity.SorterCommandBlockEntity;
import com.knightcode.appliedstoragesorter.registry.SorterMenus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class SorterCommandBlockMenu extends AbstractContainerMenu {
    private final SorterCommandBlockEntity blockEntity;

    public SorterCommandBlockMenu(int containerId, Inventory playerInventory, SorterCommandBlockEntity blockEntity) {
        super(SorterMenus.SORTER_COMMAND_BLOCK_MENU.get(), containerId);
        this.blockEntity = blockEntity;
    }

    public SorterCommandBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
