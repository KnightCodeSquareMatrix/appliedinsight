package com.knightcode.appliedstoragesorter.registry;

import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.block.DigitalAssetVaultBlock;
import com.knightcode.appliedstoragesorter.block.SorterCommandBlock;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SorterBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK,
            AppliedStorageSorter.MODID);

    public static final DeferredHolder<Block, DigitalAssetVaultBlock> DIGITAL_ASSET_VAULT = BLOCKS
            .register("digital_asset_vault", DigitalAssetVaultBlock::new);

    public static final DeferredHolder<Block, SorterCommandBlock> SORTER_COMMAND_BLOCK = BLOCKS
            .register("sorter_command_block",
                    () -> new SorterCommandBlock(BlockBehaviour.Properties.of()
                            .strength(3.0F, 6.0F)
                            .requiresCorrectToolForDrops()));

    private SorterBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
