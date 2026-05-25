package com.knightcode.appliedstoragesorter.block;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;

import com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DigitalAssetVaultBlock extends AEBaseEntityBlock<DigitalAssetVaultBlockEntity> {
    public DigitalAssetVaultBlock() {
        super(metalProps());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof DigitalAssetVaultBlockEntity blockEntity) {
            if (!level.isClientSide) {
                blockEntity.openMenu(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.full();
    }
}
