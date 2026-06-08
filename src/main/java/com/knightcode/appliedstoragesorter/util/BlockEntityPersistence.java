package com.knightcode.appliedstoragesorter.util;

import com.knightcode.appliedstoragesorter.blockentity.ItemPersistable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class BlockEntityPersistence {
    private BlockEntityPersistence() {
    }

    public static ItemStack enrichCloneItemStack(LevelReader level, BlockPos pos, BlockState state, ItemStack stack) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            saveBlockEntityToItem(blockEntity, stack, level.registryAccess());
        }
        return stack;
    }

    public static void loadPlacedBlock(Level level, BlockPos pos, ItemStack stack) {
        if (level.isClientSide()) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            loadBlockEntityFromItem(blockEntity, stack, level.registryAccess());
            blockEntity.setChanged();
        }
    }

    public static void saveBlockEntityToItem(BlockEntity blockEntity, ItemStack stack,
            HolderLookup.Provider registries) {
        if (blockEntity instanceof ItemPersistable persistable) {
            persistable.saveToItemStack(stack, registries);
            return;
        }
        var tag = blockEntity.saveCustomOnly(registries);
        if (tag.isEmpty()) {
            return;
        }
        BlockEntity.addEntityType(tag, blockEntity.getType());
        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
    }

    public static void loadBlockEntityFromItem(BlockEntity blockEntity, ItemStack stack,
            HolderLookup.Provider registries) {
        if (blockEntity instanceof ItemPersistable persistable) {
            persistable.loadFromItemStack(stack, registries);
            return;
        }
        var blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData != null) {
            blockEntity.loadCustomOnly(blockEntityData.copyTag(), registries);
        }
    }

    @Nullable
    public static BlockEntity getBlockEntity(LevelReader level, BlockPos pos) {
        return level.getBlockEntity(pos);
    }
}
