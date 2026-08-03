package com.knightcode.appliedstoragesorter.ae2.sort;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public record SorterMoveExecutionResult(
        int attemptedMoveCount,
        int completedMoveCount,
        int failedMoveCount,
        long requestedAmount,
        long movedAmount,
        List<MoveResult> moveResults,
        @Nullable EnergyCostEstimate energyCost) {

    /**
     * 使用给定的电量消耗估算创建新的执行结果（保留其他字段不变）。
     * <p>
     * 由 {@link SorterMoveOperation#execute()} 在构建完基础结果后调用，
     * 避免污染原始构造逻辑。
     *
     * @param energyCost 电量消耗估算（可为 null）
     * @return 包含电量消耗的新执行结果
     */
    public SorterMoveExecutionResult withEnergyCost(@Nullable EnergyCostEstimate energyCost) {
        return new SorterMoveExecutionResult(
                attemptedMoveCount, completedMoveCount, failedMoveCount,
                requestedAmount, movedAmount, moveResults, energyCost);
    }

    public record MoveResult(
            PlannedMove plannedMove,
            long extractedAmount,
            long insertedAmount) {
        public boolean succeeded() {
            return insertedAmount == plannedMove.amount();
        }
    }
}
