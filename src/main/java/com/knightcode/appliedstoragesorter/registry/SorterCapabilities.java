package com.knightcode.appliedstoragesorter.registry;

import appeng.api.AECapabilities;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class SorterCapabilities {
    private SorterCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                SorterBlockEntities.DIGITAL_ASSET_VAULT.get(),
                (blockEntity, context) -> blockEntity);

        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                SorterBlockEntities.SORTER_COMMAND_BLOCK.get(),
                (blockEntity, context) -> blockEntity);
    }
}
