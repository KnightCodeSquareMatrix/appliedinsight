package com.knightcode.appliedstoragesorter.ae2.sort;

import java.util.HashSet;
import java.util.Set;

import com.knightcode.appliedstoragesorter.Config;

import net.minecraft.core.BlockPos;

/**
 * 电量消耗计算器 — 纯静态工具类，从 {@link SorterMoveExecutionResult} 计算电量消耗。
 * <p>
 * 使用对数饱和公式（方案 B）：
 * <pre>
 * totalCost = floor(α × N + β × log₂(1 + A) + γ × log₂(1 + T) + δ × log₂(1 + avgDist))
 * </pre>
 * 其中：
 * <ul>
 *   <li>α (alpha) = 基础搬运费 — 从 {@code Config.ENERGY_COST_BASE_FEE} 读取</li>
 *   <li>β (beta)  = 数量饱和系数 — 从 {@code Config.ENERGY_COST_AMOUNT_COEFF} 读取</li>
 *   <li>γ (gamma) = 种类饱和系数 — 从 {@code Config.ENERGY_COST_TYPE_COEFF} 读取</li>
 *   <li>δ (delta) = 距离饱和系数 — 从 {@code Config.ENERGY_COST_DISTANCE_COEFF} 读取</li>
 * </ul>
 * <p>
 * log₂(1 + n) 使用位运算 {@code Long.SIZE - Long.numberOfLeadingZeros(n)} 高效实现，无浮点运算。
 * 距离使用曼哈顿距离：{@code |x₁-x₂| + |y₁-y₂| + |z₁-z₂|}。
 * <p>
 * 设计约束：
 * <ul>
 *   <li>纯静态方法，无实例状态</li>
 *   <li>不引入浮点数运算</li>
 *   <li>不依赖 {@code rule/} 层任何类</li>
 * </ul>
 */
public final class EnergyCostCalculator {

    private EnergyCostCalculator() {
    }

    /**
     * 从执行结果计算电量消耗估算。
     *
     * @param result 搬运执行结果（非 null）
     * @return 电量消耗估算值，如果 result 为空（无 moveResults）则返回全零估算
     */
    public static EnergyCostEstimate estimate(SorterMoveExecutionResult result) {
        if (result.moveResults() == null || result.moveResults().isEmpty()) {
            return emptyEstimate();
        }

        int moveCount = result.attemptedMoveCount();
        long movedAmount = result.movedAmount();
        int distinctItemTypes = countDistinctItemTypes(result);
        int totalDistance = sumDistances(result);
        int averageDistance = moveCount > 0 ? totalDistance / moveCount : 0;

        // 读取 Config 系数（DoubleValue → 浮点 → long 截断，无舍入）
        long alpha = doubleToLong(Config.ENERGY_COST_BASE_FEE.get());
        long beta = doubleToLong(Config.ENERGY_COST_AMOUNT_COEFF.get());
        long gamma = doubleToLong(Config.ENERGY_COST_TYPE_COEFF.get());
        long delta = doubleToLong(Config.ENERGY_COST_DISTANCE_COEFF.get());

        long baseCost = alpha * moveCount;
        long amountCost = beta * log2PlusOne(movedAmount);
        long typeCost = gamma * log2PlusOne(distinctItemTypes);
        long distanceCost = delta * log2PlusOne(averageDistance);

        long totalCost = baseCost + amountCost + typeCost + distanceCost;

        return new EnergyCostEstimate(
                totalCost,
                moveCount,
                movedAmount,
                distinctItemTypes,
                averageDistance,
                new EnergyCostEstimate.CostBreakdown(baseCost, amountCost, typeCost, distanceCost));
    }

    /**
     * 计算 log₂(1 + n) 的高效位运算实现。
     * <p>
     * 等价于 {@code (long) Math.floor(Math.log(1 + n) / Math.log(2))}，但无浮点运算。
     * 使用 {@link Long#numberOfLeadingZeros(long)} 实现。
     * <p>
     * 示例：
     * <ul>
     *   <li>log₂(1 + 0) = 0</li>
     *   <li>log₂(1 + 1) = 1</li>
     *   <li>log₂(1 + 7) = 3</li>
     *   <li>log₂(1 + 8) = 3</li>
     *   <li>log₂(1 + 15) = 4</li>
     * </ul>
     *
     * @param n 输入值（非负）
     * @return floor(log₂(1 + n))
     */
    static long log2PlusOne(long n) {
        if (n <= 0) {
            return 0;
        }
        // log2(x) = 63 - numberOfLeadingZeros(x)  for positive x in [1, Long.MAX_VALUE]
        // 我们要 floor(log2(1+n))
        // 如果 1+n 可能溢出，则限制为 Long.MAX_VALUE
        long x = (n == Long.MAX_VALUE) ? Long.MAX_VALUE : (n + 1);
        return Long.SIZE - 1 - Long.numberOfLeadingZeros(x);
    }

    /**
     * 计算曼哈顿距离。
     *
     * @param a 第一个坐标
     * @param b 第二个坐标
     * @return |x₁-x₂| + |y₁-y₂| + |z₁-z₂|
     */
    static int manhattanDistance(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX())
                + Math.abs(a.getY() - b.getY())
                + Math.abs(a.getZ() - b.getZ());
    }

    /**
     * 统计执行结果中涉及的物品种类数（按 PlannedMove.key 去重）。
     */
    private static int countDistinctItemTypes(SorterMoveExecutionResult result) {
        Set<appeng.api.stacks.AEItemKey> seen = new HashSet<>();
        for (var moveResult : result.moveResults()) {
            seen.add(moveResult.plannedMove().key());
        }
        return seen.size();
    }

    /**
     * 计算所有 move 的曼哈顿距离之和。
     */
    private static int sumDistances(SorterMoveExecutionResult result) {
        int total = 0;
        for (var moveResult : result.moveResults()) {
            var plannedMove = moveResult.plannedMove();
            total += manhattanDistance(
                    plannedMove.source().drivePos(),
                    plannedMove.destination().drivePos());
        }
        return total;
    }

    /**
     * 将 Config 的 {@code DoubleValue} 转为 {@code long}（截断取整）。
     */
    private static long doubleToLong(double value) {
        return (long) value;
    }

    /**
     * 当执行结果为空时返回全零估算。
     */
    private static EnergyCostEstimate emptyEstimate() {
        return new EnergyCostEstimate(
                0L, 0, 0L, 0, 0,
                new EnergyCostEstimate.CostBreakdown(0L, 0L, 0L, 0L));
    }
}
