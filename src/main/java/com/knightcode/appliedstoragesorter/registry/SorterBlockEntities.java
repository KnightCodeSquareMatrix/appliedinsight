package com.knightcode.appliedstoragesorter.registry;

import appeng.blockentity.AEBaseBlockEntity;
import com.knightcode.appliedstoragesorter.AppliedStorageSorter;
import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;
import com.knightcode.appliedstoragesorter.blockentity.SorterCommandBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SorterBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE, AppliedStorageSorter.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DigitalAssetVaultBlockEntity>> DIGITAL_ASSET_VAULT = BLOCK_ENTITY_TYPES
            .register("digital_asset_vault", () -> {
                var block = SorterBlocks.DIGITAL_ASSET_VAULT.get();
                BlockEntityType.BlockEntitySupplier<DigitalAssetVaultBlockEntity> supplier = DigitalAssetVaultBlockEntity::new;
                var type = BlockEntityType.Builder.of(supplier, block).build(null);
                AEBaseBlockEntity.registerBlockEntityItem(type, SorterItems.DIGITAL_ASSET_VAULT.get());
                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SorterCommandBlockEntity>> SORTER_COMMAND_BLOCK = BLOCK_ENTITY_TYPES
            .register("sorter_command_block", () -> {
                var block = SorterBlocks.SORTER_COMMAND_BLOCK.get();
                BlockEntityType.BlockEntitySupplier<SorterCommandBlockEntity> supplier = SorterCommandBlockEntity::new;
                var type = BlockEntityType.Builder.of(supplier, block).build(null);
                AEBaseBlockEntity.registerBlockEntityItem(type, SorterItems.SORTER_COMMAND_BLOCK.get());
                return type;
            });

    private SorterBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
