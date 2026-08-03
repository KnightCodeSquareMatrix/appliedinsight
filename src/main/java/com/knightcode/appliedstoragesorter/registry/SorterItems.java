package com.knightcode.appliedstoragesorter.registry;

import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.ae2.part.SmartBusPart;
import com.knightcode.appliedstoragesorter.item.DavCellItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SorterItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM,
            AppliedStorageSorter.MODID);

    public static final DeferredHolder<Item, BlockItem> DIGITAL_ASSET_VAULT = ITEMS.register("digital_asset_vault",
            () -> new BlockItem(SorterBlocks.DIGITAL_ASSET_VAULT.get(), new Item.Properties()));

    public static final DeferredHolder<Item, DavCellItem> DAV_CELL = ITEMS.register("dav_cell",
            () -> new DavCellItem(new Item.Properties()));

    public static final DeferredHolder<Item, PartItem<SmartBusPart>> SMART_BUS = ITEMS.register("smart_bus",
            () -> new PartItem<>(new Item.Properties(), SmartBusPart.class, SmartBusPart::new));

    public static final DeferredHolder<Item, BlockItem> SORTER_COMMAND_BLOCK = ITEMS.register("sorter_command_block",
            () -> new BlockItem(SorterBlocks.SORTER_COMMAND_BLOCK.get(), new Item.Properties()));

    private SorterItems() {
    }

    public static void register(IEventBus eventBus) {
        PartModels.registerModels(
                ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "part/smart_bus_empty"),
                ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "part/smart_bus_import"),
                ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "part/smart_bus_export"),
                ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "part/smart_bus_debug"),
                ResourceLocation.fromNamespaceAndPath(AppliedStorageSorter.MODID, "part/smart_bus_debug_square"));
        ITEMS.register(eventBus);
    }
}
