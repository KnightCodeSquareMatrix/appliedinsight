package com.knightcode.appliedstoragesorter.ae2;

import org.jetbrains.annotations.Nullable;

import com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanSummary;

import appeng.api.networking.IGrid;
import net.minecraft.core.BlockPos;

public record SorterMeScanResult(
        boolean success,
        String userMessage,
        String status,
        @Nullable BlockPos targetPos,
        @Nullable String targetBlockId,
        boolean controllerDetected,
        @Nullable BlockPos controllerPos,
        boolean gridResolved,
        int nodeCount,
        int driveCount,
        int scannedCellSlotCount,
        int mountedCellCount,
        int uniqueItemKeyCount,
        int duplicatedItemKeyCount,
        int duplicatedCellReferenceCount,
        @Nullable String pivotDescription,
        @Nullable String dimensionId) {

    public static SorterMeScanResult noBlockHit(String dimensionId) {
        return new SorterMeScanResult(
                false,
                "Look directly at an ME Controller and try again.",
                "no_block_hit",
                null,
                null,
                false,
                null,
                false,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                dimensionId);
    }

    public static SorterMeScanResult wrongTarget(BlockPos targetPos, String targetBlockId, String dimensionId) {
        return new SorterMeScanResult(
                false,
                "You must be looking at an ME Controller.",
                "wrong_target_block",
                targetPos,
                targetBlockId,
                false,
                null,
                false,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                dimensionId);
    }

    public static SorterMeScanResult unresolvedGrid(BlockPos targetPos, String targetBlockId, BlockPos controllerPos,
            String dimensionId) {
        return new SorterMeScanResult(
                false,
                "Could not resolve the AE2 network from that controller. Check the sorter log.",
                "controller_without_grid",
                targetPos,
                targetBlockId,
                true,
                controllerPos,
                false,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                dimensionId);
    }

    public static SorterMeScanResult success(BlockPos targetPos, String targetBlockId, BlockPos controllerPos, IGrid grid,
            Ae2DriveScanSummary driveScanSummary, String pivotDescription, String dimensionId) {
        return new SorterMeScanResult(
                true,
                "AE2 network resolved. Drives=%d, mounted cells=%d, duplicated item keys=%d. Details written to logs/AppliedStorageSorter.log"
                        .formatted(
                                driveScanSummary.driveCount(),
                                driveScanSummary.mountedCellCount(),
                                driveScanSummary.duplicatedItemKeyCount()),
                "ok",
                targetPos,
                targetBlockId,
                true,
                controllerPos,
                true,
                grid.size(),
                driveScanSummary.driveCount(),
                driveScanSummary.scannedCellSlotCount(),
                driveScanSummary.mountedCellCount(),
                driveScanSummary.uniqueItemKeyCount(),
                driveScanSummary.duplicatedItemKeyCount(),
                driveScanSummary.duplicatedCellReferenceCount(),
                pivotDescription,
                dimensionId);
    }
}
