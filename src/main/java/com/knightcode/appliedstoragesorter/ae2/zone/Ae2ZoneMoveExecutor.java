package com.knightcode.appliedstoragesorter.ae2.zone;

import java.util.ArrayList;
import java.util.List;

import com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveExecutionResult;
import com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveOperation;
import com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan;

import appeng.api.networking.IGrid;
import net.minecraft.core.HolderLookup;

/**
 * Zone move executor — 将 zone 分配计划转化为真实的搬运操作。
 *
 * <p>底层委托给 {@link ZoneMergePlanner} 生成 merge 操作，
 * 再通过 {@link SorterMoveOperation#execute()} 执行 extract→insert→rollback。
 *
 * <p>这意味着 zone 搬运的底层执行语义与 {@code /sorter merge} 一致，
 * 共享同一套批量执行引擎。
 */
public final class Ae2ZoneMoveExecutor {
    private Ae2ZoneMoveExecutor() {
    }

    public static ZoneMoveExecutionResult execute(IGrid grid, ZoneAllocationPlan plan, RuntimeTopology topology, int maxTransfers, HolderLookup.Provider registryAccess) {
        return executeDetailed(grid, plan, topology, maxTransfers, registryAccess).executionResult();
    }

    public static ZoneMoveExecutionDetailedResult executeDetailed(IGrid grid, ZoneAllocationPlan plan, RuntimeTopology topology, int maxTransfers, HolderLookup.Provider registryAccess) {
        if (maxTransfers <= 0) {
            return buildResult(topology, "maxTransfers <= 0");
        }
        if (plan.movableAssignments().isEmpty()) {
            return buildResult(topology, "plan has no movable assignments");
        }
        if (topology == null || topology.isEmpty()) {
            return buildResult(topology, "runtime topology is empty or null");
        }

        // 1. ZoneMergePlanner 根据 plan + topology 生成 merge 操作 + 诊断统计
        ZoneMergePlanner.PlanResult planResult = ZoneMergePlanner.plan(plan, topology, grid, maxTransfers, registryAccess);

        // 2. 通过 SorterMoveOperation.execute() 执行批量搬运
        //    execute() 内部已根据 Config.ENERGY_COST_ENABLED 计算电量消耗并附着
        SorterMoveExecutionResult execResult = planResult.operation().execute();

        // 3. 统计失败分类
        int extractFailedCount = 0;
        int partialInsertCount = 0;
        int rollbackFailedCount = 0;
        int moveCandidateAssignmentCount = 0;

        for (SorterMoveExecutionResult.MoveResult moveResult : execResult.moveResults()) {
            moveCandidateAssignmentCount++;
            if (moveResult.succeeded()) {
                continue;
            }
            if (moveResult.extractedAmount() <= 0) {
                extractFailedCount++;
            } else {
                // 提取成功但插入不完全 — 此时 SorterMoveOperation 已经回滚
                partialInsertCount++;
            }
        }

        // 4. 组装详细结果（传播电量消耗）
        List<String> sampleMessages = new ArrayList<>(planResult.sampleMessages());

        return new ZoneMoveExecutionDetailedResult(
                new ZoneMoveExecutionResult(
                        execResult.attemptedMoveCount(),
                        execResult.completedMoveCount(),
                        execResult.failedMoveCount(),
                        execResult.requestedAmount(),
                        execResult.movedAmount(),
                        execResult.energyCost()),      // ← 传播 SorterMoveExecutionResult 的 energyCost
                new ZoneMoveExecutionDebugReport(
                        topology,
                        planResult.scannedSourceDriveCount(),
                        planResult.scannedSourceCellCount(),
                        planResult.scannedStackCount(),
                        moveCandidateAssignmentCount,
                        planResult.skippedNoAssignmentCount(),
                        planResult.skippedSameZoneCount(),
                        planResult.skippedMissingTargetZoneCount(),
                        planResult.skippedPlacementRejectedCount(),
                        extractFailedCount,
                        partialInsertCount,
                        rollbackFailedCount,
                        sampleMessages));
    }

    private static ZoneMoveExecutionDetailedResult buildResult(RuntimeTopology topology, String summaryMessage) {
        List<String> sampleMessages = new ArrayList<>();
        sampleMessages.add(summaryMessage);
        return new ZoneMoveExecutionDetailedResult(
                new ZoneMoveExecutionResult(0, 0, 0, 0, 0, null),
                new ZoneMoveExecutionDebugReport(
                        topology != null ? topology : new RuntimeTopology(List.of(), List.of(), List.of(), true),
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, sampleMessages));
    }
}
