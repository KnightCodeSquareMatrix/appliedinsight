package com.knightcode.appliedstoragesorter.ae2;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.IGrid;
import net.minecraft.core.BlockPos;

public record Ae2GridTargetResult(
        boolean success,
        String userMessage,
        String status,
        @Nullable BlockPos targetPos,
        @Nullable String targetBlockId,
        boolean controllerDetected,
        @Nullable BlockPos controllerPos,
        boolean gridResolved,
        @Nullable IGrid grid,
        @Nullable String dimensionId) {

    public static Ae2GridTargetResult noBlockHit(String dimensionId) {
        return new Ae2GridTargetResult(false, "Look directly at an ME Controller and try again.", "no_block_hit",
                null, null, false, null, false, null, dimensionId);
    }

    public static Ae2GridTargetResult wrongTarget(BlockPos targetPos, String targetBlockId, String dimensionId) {
        return new Ae2GridTargetResult(false, "You must be looking at an ME Controller.", "wrong_target_block",
                targetPos, targetBlockId, false, null, false, null, dimensionId);
    }

    public static Ae2GridTargetResult unresolvedGrid(BlockPos targetPos, String targetBlockId, BlockPos controllerPos,
            String dimensionId) {
        return new Ae2GridTargetResult(false,
                "Could not resolve the AE2 network from that controller. Check the sorter log.",
                "controller_without_grid", targetPos, targetBlockId, true, controllerPos, false, null, dimensionId);
    }

    public static Ae2GridTargetResult success(BlockPos targetPos, String targetBlockId, BlockPos controllerPos,
            IGrid grid, String dimensionId) {
        return new Ae2GridTargetResult(true, "ok", "ok", targetPos, targetBlockId, true, controllerPos, true, grid,
                dimensionId);
    }

    /**
     * 从已接网方块实体的网格节点创建目标结果，绕过射线检测。
     * controllerPos 通过 GridNetworkLocator 从网格中扫描稳定标识，
     * 确保同一网络（无论从哪个入口触发）产生相同的标识坐标。
     */
    public static Ae2GridTargetResult fromNode(appeng.api.networking.IManagedGridNode node,
            BlockPos blockPos, String blockId, String dimensionId) {
        IGrid grid = node.getGrid();
        if (grid == null) {
            return new Ae2GridTargetResult(false,
                    "节点的网格尚未就绪，请稍后重试。", "node_grid_not_ready",
                    blockPos, blockId, false, null, false, null, dimensionId);
        }
        // 扫描网格获取稳定的控制器标识，替代直接使用节点自身坐标
        var anchorPos = GridNetworkLocator.locateAnchor(grid);
        return new Ae2GridTargetResult(true, "ok", "ok",
                blockPos, blockId, anchorPos != null, anchorPos != null ? anchorPos : blockPos, true, grid, dimensionId);
    }
}
