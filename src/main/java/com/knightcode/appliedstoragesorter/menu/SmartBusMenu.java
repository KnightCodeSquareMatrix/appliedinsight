package com.knightcode.appliedstoragesorter.menu;

import appeng.menu.AEBaseMenu;
import com.knightcode.appliedstoragesorter.ae2.part.SmartBusPart;
import com.knightcode.appliedstoragesorter.block.SmartBusMode;
import com.knightcode.appliedstoragesorter.registry.SorterMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SmartBusMenu extends AEBaseMenu {

    private final SmartBusPart host;
    private final BlockPos blockPos;
    private final Direction side;

    public SmartBusMenu(int containerId, Inventory playerInventory, SmartBusPart host) {
        this(containerId, playerInventory, host.getBlockEntity().getBlockPos(), host.getSide(), host);
    }

    private SmartBusMenu(int containerId, Inventory playerInventory, BlockPos blockPos, Direction side,
            SmartBusPart host) {
        super(SorterMenus.SMART_BUS.get(), containerId, playerInventory, host);
        this.host = host;
        this.blockPos = blockPos;
        this.side = side;
    }

    public static SmartBusMenu fromNetwork(int containerId, Inventory playerInventory, BlockPos blockPos,
            Direction side) {
        SmartBusPart host = null;
        if (appeng.api.parts.PartHelper.getPart(playerInventory.player.level(), blockPos, side) instanceof SmartBusPart smartBusPart) {
            host = smartBusPart;
        }
        return new SmartBusMenu(containerId, playerInventory, blockPos, side, host);
    }

    public SmartBusPart getHost() {
        return host;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Direction getSide() {
        return side;
    }

    public SmartBusMode getMode() {
        return host != null ? host.getMode() : SmartBusMode.EMPTY;
    }

    public Component getModeDisplayName() {
        return getMode().getDisplayName();
    }

    public String getFilterJson() {
        return host != null ? host.getFilterJson() : null;
    }

    public boolean hasFilter() {
        return host != null && host.hasFilter();
    }

    public boolean hasValidFilter() {
        return host != null && host.hasValidFilter();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return host != null && host.getHost() != null && host.getHost().isInWorld()
                && host.getHost().getPart(side) == host;
    }
}
