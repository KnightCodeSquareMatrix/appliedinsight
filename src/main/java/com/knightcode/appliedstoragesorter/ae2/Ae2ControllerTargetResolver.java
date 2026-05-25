package com.knightcode.appliedstoragesorter.ae2;

import com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanner;

import appeng.blockentity.networking.ControllerBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class Ae2ControllerTargetResolver {
    private static final double PICK_RANGE = 8.0D;

    private Ae2ControllerTargetResolver() {
    }

    public static Ae2GridTargetResult resolveGridTarget(CommandSourceStack source) {
        var player = source.getPlayer();
        var level = source.getLevel();
        var dimensionId = level.dimension().location().toString();

        if (player == null) {
            return Ae2GridTargetResult.noBlockHit(dimensionId);
        }

        var hitResult = player.pick(PICK_RANGE, 0.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return Ae2GridTargetResult.noBlockHit(dimensionId);
        }

        var blockHitResult = (BlockHitResult) hitResult;
        var targetPos = blockHitResult.getBlockPos();
        var blockEntity = level.getBlockEntity(targetPos);
        var targetBlockId = BuiltInRegistries.BLOCK.getKey(level.getBlockState(targetPos).getBlock()).toString();

        if (!(blockEntity instanceof ControllerBlockEntity controller)) {
            return Ae2GridTargetResult.wrongTarget(targetPos, targetBlockId, dimensionId);
        }

        var grid = controller.getMainNode().getGrid();
        if (grid == null) {
            return Ae2GridTargetResult.unresolvedGrid(targetPos, targetBlockId, controller.getBlockPos(), dimensionId);
        }

        return Ae2GridTargetResult.success(targetPos, targetBlockId, controller.getBlockPos(), grid, dimensionId);
    }

    public static SorterMeScanResult resolve(CommandSourceStack source) {
        var gridTarget = resolveGridTarget(source);
        if (!gridTarget.success()) {
            return switch (gridTarget.status()) {
                case "no_block_hit" -> SorterMeScanResult.noBlockHit(gridTarget.dimensionId());
                case "wrong_target_block" -> SorterMeScanResult.wrongTarget(gridTarget.targetPos(),
                        gridTarget.targetBlockId(), gridTarget.dimensionId());
                default -> SorterMeScanResult.unresolvedGrid(gridTarget.targetPos(), gridTarget.targetBlockId(),
                        gridTarget.controllerPos(), gridTarget.dimensionId());
            };
        }

        var grid = gridTarget.grid();
        var driveScanSummary = Ae2DriveScanner.scan(grid);
        var pivotDescription = describeOwner(grid.getPivot().getOwner());
        return SorterMeScanResult.success(gridTarget.targetPos(), gridTarget.targetBlockId(), gridTarget.controllerPos(),
                grid, driveScanSummary, pivotDescription, gridTarget.dimensionId());
    }

    private static String describeOwner(Object owner) {
        if (owner instanceof BlockEntity blockEntity) {
            return owner.getClass().getName() + "@" + formatBlockPos(blockEntity.getBlockPos());
        }

        return owner.getClass().getName();
    }

    public static String formatBlockPos(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
