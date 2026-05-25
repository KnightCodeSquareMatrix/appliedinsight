package com.knightcode.appliedstoragesorter.blockentity;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import com.knightcode.appliedstoragesorter.item.DigitalAssetManagementCardItem;
import com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu;
import com.knightcode.appliedstoragesorter.registry.SorterBlockEntities;
import com.knightcode.appliedstoragesorter.registry.SorterItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DigitalAssetVaultBlockEntity extends DriveBlockEntity {
    private static final String CARD_INVENTORY_TAG = "davCardInv";

    private final AppEngInternalInventory cardInventory = new AppEngInternalInventory(this, 1, 1);

    public DigitalAssetVaultBlockEntity(BlockPos pos, BlockState blockState) {
        super(SorterBlockEntities.DIGITAL_ASSET_VAULT.get(), pos, blockState);
        this.cardInventory.setFilter(new DigitalAssetManagementCardFilter());
    }

    public InternalInventory getCardInventory() {
        return cardInventory;
    }

    public ItemStack getManagementCard() {
        return cardInventory.getStackInSlot(0);
    }

    public boolean hasDeclaredZone() {
        return getDeclaredZoneId().isPresent();
    }

    public Optional<String> getDeclaredZoneId() {
        return DigitalAssetManagementCardItem.getZoneId(getManagementCard());
    }

    public Optional<String> getDeclaredZoneName() {
        return DigitalAssetManagementCardItem.getZoneName(getManagementCard());
    }

    @Override
    public Component getName() {
        return getBlockState().getBlock().getName();
    }

    public void openMenu(Player player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, menuPlayer) -> new DigitalAssetVaultMenu(containerId, playerInventory,
                        this),
                getName()),
                buffer -> buffer.writeBlockPos(getBlockPos()));
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        cardInventory.readFromNBT(data, CARD_INVENTORY_TAG, registries);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        cardInventory.writeToNBT(data, CARD_INVENTORY_TAG, registries);
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);

        var card = getManagementCard();
        if (!card.isEmpty()) {
            drops.add(card.copy());
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        cardInventory.setItemDirect(0, ItemStack.EMPTY);
    }

    private static class DigitalAssetManagementCardFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return stack.is(SorterItems.DIGITAL_ASSET_MANAGEMENT_CARD.get());
        }
    }
}
