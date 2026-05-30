# SorterMoveExecutionResult
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/sort/SorterMoveExecutionResult.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.sort`
- **类型**: `record`
- **所属层**: AE2 集成层

## 职责
作为 merge 执行阶段的结果对象，承载整体执行摘要、每条 planned move 的实际 extracted / inserted 明细，
以及可选的电量消耗估算（`energyCost`）。

## 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `attemptedMoveCount` | `int` | 尝试执行的总 move 数 |
| `completedMoveCount` | `int` | 完全成功的 move 数（inserted == planned.amount）|
| `failedMoveCount` | `int` | 失败的 move 数 |
| `requestedAmount` | `long` | 计划搬运总量 |
| `movedAmount` | `long` | 实际成功搬运量 |
| `moveResults` | `List<MoveResult>` | 逐 move 执行明细 |
| `energyCost` | `@Nullable EnergyCostEstimate` | 电量消耗估算（默认 null，仅在 Config.ENERGY_COST_ENABLED=true 时非 null）|

## 方法

### `withEnergyCost(EnergyCostEstimate)`
创建一个新的 `SorterMoveExecutionResult` 实例，仅替换 `energyCost` 字段，其余字段保持不变。

由 `SorterMoveOperation.execute()` 在构建完基础结果后调用。

## 边界检查
边界健康。该类型明确属于 AE2 集成层，依赖 Minecraft/AE2 API 是合理的，并未反向污染规则层。

## 抽象检查
没有过度抽象；以值对象形式存在是合适的。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.sort.PlannedMove`
- `com.knightcode.appliedstoragesorter.ae2.sort.EnergyCostEstimate`
- 主要依赖为 JDK 或同文件内部成员。

## 维护备注
- 当前不仅包含 aggregate summary，还包含 `MoveResult` 明细，已成为 merge 复盘与详细日志的重要输入。
- `energyCost` 字段为 **nullable**，默认 `null` 保证向后兼容。
- 若后续继续扩展字段，优先保持 `MoveResult` 的语义稳定，避免影响日志解析与前端展示。

## 近期变更 (2026-05-25)
- **新增 `energyCost` 字段** — `@Nullable EnergyCostEstimate`，执行后电量消耗估算
- **新增 `withEnergyCost()` 方法** — 基于当前记录创建带电量消耗的新实例
