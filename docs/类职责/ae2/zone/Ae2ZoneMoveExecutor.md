# Ae2ZoneMoveExecutor
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/zone/Ae2ZoneMoveExecutor.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
薄编排层：将 zone 分配计划转化为真实搬运操作。底层委托给 `ZoneMergePlanner` 生成 merge 操作，再通过 `SorterMoveOperation.execute()` 执行 extract→insert→rollback。

## 边界检查
边界健康。它不定义 route 规则，也不定义 zone 内部策略，只消费 plan 和 RuntimeTopology。

## 抽象检查
~300 行 → ~80 行。内联执行逻辑已提取到 `ZoneMergePlanner`，执行引擎已统一到 `SorterMoveOperation`。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveExecutionResult`
- `com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveOperation`
- `com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan`
- `appeng.api.networking.IGrid`
- `net.minecraft.core.HolderLookup.Provider`

## 维护备注
- 重构后已非常薄，可以考虑合并到 `SorterPlanService`，但保留独立的 API 入口有利于保持调用链清晰。
- 如果后续需要引入执行重试 / 事务 / 补偿机制，应在 `SorterMoveOperation.execute()` 中增强，而非在此处。
- `execute()` 和 `executeDetailed()` 方法新增 `HolderLookup.Provider registryAccess` 参数，用于传递给 `ZoneMergePlanner.plan()`。

## 近期重构 (2026-05-24)
- **删除内联执行逻辑**：原本 200+ 行的 `executePlannedMoves()` / `attemptMove()` / `ExecutionCollector` 全部移除
- **删除 `ItemAttemptResult` / `SkipReason` / `ItemFingerprint` / `ItemStackContext` / `ExecutionContext`**：这些内部数据结构已移至 `ZoneMergePlanner`
- **改为委托模式**：
  1. `ZoneMergePlanner.plan()` → 生成 `SorterMoveOperation`
  2. `operation.execute()` → 执行批量搬运
  3. 组装 `ZoneMoveExecutionDetailedResult` 返回
- 现在两种能力线共享同一执行引擎：
  - `/sorter merge` → `MergeMovePlanner` → `SorterMoveOperation.execute()`
  - `/sorter me planAndMove` → `ZoneMergePlanner` → `SorterMoveOperation.execute()`

## 近期变更 (2026-05-28)
- **新增 `registryAccess` 参数**：`execute()` 和 `executeDetailed()` 方法新增 `HolderLookup.Provider registryAccess` 参数，传递给 `ZoneMergePlanner.plan()`，用于在 `indexAssignments()` 中反序列化 assignment 的 NBT。
