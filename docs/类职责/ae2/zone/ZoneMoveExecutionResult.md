# ZoneMoveExecutionResult
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/zone/ZoneMoveExecutionResult.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `record`
- **所属层**: AE2 集成层

## 职责
作为 zone 搬运执行阶段的聚合结果对象，承载整体执行摘要（与 `SorterMoveExecutionResult` 类似但精简）。
新增 `energyCost` 字段传播来自底层 `SorterMoveExecutionResult` 的电量消耗估算。

## 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `attemptedMoveCount` | `int` | 尝试执行的总 move 数 |
| `completedMoveCount` | `int` | 完全成功的 move 数 |
| `failedMoveCount` | `int` | 失败的 move 数 |
| `requestedAmount` | `long` | 计划搬运总量 |
| `movedAmount` | `long` | 实际成功搬运量 |
| `energyCost` | `@Nullable EnergyCostEstimate` | 电量消耗估算（从 SorterMoveExecutionResult 传播）|

## 方法

### `withEnergyCost(EnergyCostEstimate)`
创建一个新的 `ZoneMoveExecutionResult` 实例，仅替换 `energyCost` 字段。

## 边界检查
边界健康。仅依赖 `ae2.sort.EnergyCostEstimate`，属于 AE2 集成层内部引用。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.sort.EnergyCostEstimate`
- `com.knightcode.appliedstoragesorter.ae2.zone.Ae2ZoneMoveExecutor`（构造者）
- `com.knightcode.appliedstoragesorter.ae2.zone.ZoneMoveExecutionDetailedResult`（容器）

## 近期变更 (2026-05-25)
- **新增 `energyCost` 字段** — `@Nullable EnergyCostEstimate`，从底层 `SorterMoveExecutionResult.energyCost()` 传播
- **新增 `withEnergyCost()` 方法** — 保留其他字段不变，仅替换 `energyCost`
