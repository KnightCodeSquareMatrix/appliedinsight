package com.knightcode.appliedstoragesorter.ae2.sort;

/**
 * 承载电量消耗计算结果的轻量值对象。
 * <p>
 * 由 {@link EnergyCostCalculator} 在 {@link SorterMoveOperation#execute()} 执行后计算，
 * 作为 {@link SorterMoveExecutionResult#energyCost()} 的 nullable 字段附着在操作结果上。
 * <p>
 * 设计原则：
 * <ul>
 *   <li>纯 record，无业务逻辑</li>
 *   <li>通过 {@code Config.ENERGY_COST_ENABLED} 控制启用/禁用，默认关闭（向后兼容）</li>
 *   <li>信息报告型，不实际扣费</li>
 * </ul>
 *
 * @param totalCost         总消耗（各维度加权求和后取整）
 * @param moveCount         搬运次数（N）
 * @param movedAmount       实际成功搬运量（A）
 * @param distinctItemTypes 涉及的物品种类数（T），对 {@code moveResults} 的 key 去重
 * @param averageDistance   平均曼哈顿距离（avgDist），单位：方块
 * @param breakdown         成本明细
 */
public record EnergyCostEstimate(
        long totalCost,
        int moveCount,
        long movedAmount,
        int distinctItemTypes,
        int averageDistance,
        CostBreakdown breakdown) {

    /**
     * 电量消耗的维度明细。
     *
     * @param baseCost     基础搬运费 = α × N
     * @param amountCost   数量维度成本 = β × log₂(1 + A)
     * @param typeCost     种类维度成本 = γ × log₂(1 + T)
     * @param distanceCost 距离维度成本 = δ × log₂(1 + avgDist)
     */
    public record CostBreakdown(
            long baseCost,
            long amountCost,
            long typeCost,
            long distanceCost) {
    }
}
