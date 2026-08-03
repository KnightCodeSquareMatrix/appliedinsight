package com.knightcode.appliedstoragesorter.registry;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public final class SorterCreativeTabs {
    private SorterCreativeTabs() {
    }

    public static void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(SorterItems.DIGITAL_ASSET_VAULT.get());
            event.accept(SorterItems.SMART_BUS.get());
            event.accept(SorterItems.SORTER_COMMAND_BLOCK.get());
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(SorterItems.DAV_CELL.get());
        }
    }
}
