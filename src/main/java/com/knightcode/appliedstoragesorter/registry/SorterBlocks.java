package com.knightcode.appliedstoragesorter.registry;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import appeng.block.AEBaseBlock;
import com.knightcode.appliedstoragesorter.block.DigitalAssetVaultBlock;
import com.knightcode.appliedstoragesorter.block.SorterCommandBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SorterBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK,
            AppliedStorageSorter.MODID);

    public static final DeferredHolder<Block, DigitalAssetVaultBlock> DIGITAL_ASSET_VAULT = BLOCKS
            .register("digital_asset_vault",
                    () -> new DigitalAssetVaultBlock(AEBaseBlock.metalProps()));

    public static final DeferredHolder<Block, SorterCommandBlock> SORTER_COMMAND_BLOCK = BLOCKS
            .register("sorter_command_block",
                    () -> new SorterCommandBlock(AEBaseBlock.metalProps()));

    private SorterBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
