# EnergyCostEstimate

| 维度 | 值 |
|------|-----|
| **包** | `com.knightcode.appliedstoragesorter.ae2.sort` |
| **类型** | `public record` |
| **职责** | 承载电量消耗计算结果的轻量值对象 |
| **定位** | 作为 `SorterMoveExecutionResult.energyCost()` 的 nullable 字段附着在操作结果上 |
| **能力线** | 公用（merge 和 zone 两条线共享） |

## 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalCost` | `long` | 总消耗 = α × N + β × log₂(1+A) + γ × log₂(1+T) + δ × log₂(1+avgDist) |
| `moveCount` | `int` | 搬运次数（N） |
| `movedAmount` | `long` | 实际成功搬运量（A） |
| `distinctItemTypes` | `int` | 涉及的物品种类数（T），对 `moveResults` 的 key 去重 |
| `averageDistance` | `int` | 平均曼哈顿距离（avgDist），单位：方块 |
| `breakdown` | `CostBreakdown` | 成本明细（base / amount / type / distance） |

## 内部类型 `CostBreakdown`

| 字段 | 类型 | 公式 |
|------|------|------|
| `baseCost` | `long` | α × N |
| `amountCost` | `long` | β × log₂(1 + A) |
| `typeCost` | `long` | γ × log₂(1 + T) |
| `distanceCost` | `long` | δ × log₂(1 + avgDist) |

## 设计约束

1. **纯 record，无业务逻辑** — 仅作为数据容器
2. **nullable** — 通过 `Config.ENERGY_COST_ENABLED` 控制，默认关闭
3. **信息报告型** — 不实际扣费
4. **无浮点运算** — 所有数值均为整型（`long` / `int`）

## 依赖关系

- 无外部依赖（纯 JDK record）
- 被 `EnergyCostCalculator.estimate()` 构造
- 被 `SorterMoveExecutionResult` 引用

## 相关文档

- [EnergyCostCalculator](./EnergyCostCalculator.md)
- [SorterMoveExecutionResult](./SorterMoveExecutionResult.md)
- 分析报告: [`docs/analysis/自动路由和merge电量消耗模型分析报告.md`](../../analysis/自动路由和merge电量消耗模型分析报告.md)
