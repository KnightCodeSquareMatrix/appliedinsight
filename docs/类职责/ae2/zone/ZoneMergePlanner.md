# ZoneMergePlanner
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/ZoneMergePlanner.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
将 zone 分配计划转化为可执行的 merge 操作。扫描 source drives，匹配 plan assignment，通过 `RuntimeZone.planPlacement()` 确定 target cell，产出 `SorterMoveOperation`。

与 `MergeMovePlanner` 互补：
- `MergeMovePlanner`: 全网络同类归并（`/sorter merge`）
- `ZoneMergePlanner`: 按 zone 治理规则的跨 zone 搬运（`/sorter me planAndMove`）

两者都产出 `SorterMoveOperation`，共享同一套执行引擎。

## 边界检查
边界健康。不参与 route 规则决策，不定义 zone 策略。

## 抽象检查
单一职责。替代了 `Ae2ZoneMoveExecutor` 中原有的内联扫描/匹配/执行逻辑。

## 产出类型
- `ZoneMergePlanner.PlanResult` — 内部 record，包含 `SorterMoveOperation` 和诊断统计数据（scan counts, skip counts, sample messages）

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference`
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor`
- `com.knightcode.appliedstoragesorter.ae2.sort.PlannedMove`
- `com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveOperation`
- `com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment`
- `com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan`
- `appeng.api.networking.IGrid`
- `appeng.api.stacks.AEItemKey`
- `appeng.api.storage.MEStorage`

## 维护备注
- 与 `MergeMovePlanner` 共享 `SorterMoveOperation` 执行引擎，不要在此引入独立的 extract/insert 逻辑。
- 诊断数据通过 `PlanResult` 返回给 `Ae2ZoneMoveExecutor` 组装最终结果。
