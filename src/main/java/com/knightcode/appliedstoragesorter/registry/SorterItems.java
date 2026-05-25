package com.knightcode.appliedstoragesorter.registry;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.item.DigitalAssetManagementCardItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SorterItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM,
            AppliedStorageSorter.MODID);

    public static final DeferredHolder<Item, DigitalAssetManagementCardItem> DIGITAL_ASSET_MANAGEMENT_CARD = ITEMS
            .register("digital_asset_management_card",
                    () -> new DigitalAssetManagementCardItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, BlockItem> DIGITAL_ASSET_VAULT = ITEMS.register("digital_asset_vault",
            () -> new BlockItem(SorterBlocks.DIGITAL_ASSET_VAULT.get(), new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> SORTER_COMMAND_BLOCK = ITEMS.register("sorter_command_block",
            () -> new BlockItem(SorterBlocks.SORTER_COMMAND_BLOCK.get(), new Item.Properties()));

    private SorterItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
