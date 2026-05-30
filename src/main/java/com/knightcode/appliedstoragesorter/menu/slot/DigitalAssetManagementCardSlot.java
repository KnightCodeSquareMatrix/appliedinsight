package com.knightcode.appliedstoragesorter.menu.slot;

import com.knightcode.appliedstoragesorter.registry.SorterItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DigitalAssetManagementCardSlot extends SlotItemHandler {
    public DigitalAssetManagementCardSlot(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Directly check item type; the underlying IItemHandler/AE filter also
        // validates this, but we check first to provide clear, independent logic.
        return stack.is(SorterItems.DIGITAL_ASSET_MANAGEMENT_CARD.get());
    }

    @Override
    public boolean mayPickup(Player player) {
        // Cards can be freely extracted from the vault
        return true;
    }
}
