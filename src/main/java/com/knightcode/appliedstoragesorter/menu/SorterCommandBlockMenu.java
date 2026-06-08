package com.knightcode.appliedstoragesorter.menu;

import appeng.menu.AEBaseMenu;
import com.knightcode.appliedstoragesorter.blockentity.SorterCommandBlockEntity;
import com.knightcode.appliedstoragesorter.registry.SorterMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SorterCommandBlockMenu extends AEBaseMenu {
    private final SorterCommandBlockEntity blockEntity;
    private final SorterCommandBlockEntity.NetworkStatus networkStatusSnapshot;

    public SorterCommandBlockMenu(int containerId, Inventory playerInventory, SorterCommandBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, blockEntity.getNetworkStatus());
    }

    public SorterCommandBlockMenu(int containerId, Inventory playerInventory, SorterCommandBlockEntity blockEntity,
            SorterCommandBlockEntity.NetworkStatus networkStatusSnapshot) {
        super(SorterMenus.SORTER_COMMAND_BLOCK_MENU.get(), containerId, playerInventory, blockEntity);
        this.blockEntity = blockEntity;
        this.networkStatusSnapshot = networkStatusSnapshot;
    }

    public static SorterCommandBlockMenu fromNetwork(int containerId, Inventory playerInventory, BlockPos pos,
            SorterCommandBlockEntity.NetworkStatus networkStatusSnapshot) {
        Level level = playerInventory.player.level();
        if (!(level.getBlockEntity(pos) instanceof SorterCommandBlockEntity blockEntity)) {
            throw new IllegalStateException("Expected SorterCommandBlockEntity at " + pos);
        }
        return new SorterCommandBlockMenu(containerId, playerInventory, blockEntity, networkStatusSnapshot);
    }

    public SorterCommandBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public SorterCommandBlockEntity.NetworkStatus getNetworkStatusSnapshot() {
        return networkStatusSnapshot;
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
