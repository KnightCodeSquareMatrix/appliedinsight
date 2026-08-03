# ZoneAllocationPlannerAnalyzer
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/analysis/ZoneAllocationPlannerAnalyzer.java`
- **包**: `com.knightcode.appliedstoragesorter.analysis`
- **类型**: `class`
- **所属层**: 离线分析层

## 职责
把 ZoneAllocationPlan 渲染成便于人工检查的文本报告。

## 边界检查
边界健康，属于分析/报表层。

## 抽象检查
没有过度抽象。

## 主要协作者
- `com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment`
- `com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan`
- `com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlanner`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfile`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfileJsonCodec`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
