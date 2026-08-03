# SorterPlanFileLogger
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/logging/SorterPlanFileLogger.java`
- **包**: `com.knightcode.appliedstoragesorter.logging`
- **类型**: `class`
- **所属层**: 日志基础设施层

## 职责
输出 /sorter me plan 与 /sorter me planAndMove 的详细日志文件。

## 边界检查
边界健康。日志渲染没有渗透回 planner 或 executor。

## 抽象检查
没有过度抽象，但内容组织已经较多。

## 主要协作者
- `com.knightcode.appliedstoragesorter.appliedinsight`
- `com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult`
- `com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanSummary`
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor`
- `com.knightcode.appliedstoragesorter.ae2.zone.RuntimeCell`
- `com.knightcode.appliedstoragesorter.ae2.zone.RuntimeTopology`
- `com.knightcode.appliedstoragesorter.ae2.zone.RuntimeZone`
- `com.knightcode.appliedstoragesorter.ae2.zone.ZoneMoveExecutionDebugReport`
- `com.knightcode.appliedstoragesorter.ae2.zone.ZoneMoveExecutionDetailedResult`
- `com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment`
- `com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfile`
- 其余依赖省略 4 项，以源码为准。

## 维护备注
- 如果日志格式继续扩展，可把各 section writer 拆成独立 helper。
