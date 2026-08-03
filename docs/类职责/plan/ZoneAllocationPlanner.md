# ZoneAllocationPlanner
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/plan/ZoneAllocationPlanner.java`
- **包**: `com.knightcode.appliedstoragesorter.plan`
- **类型**: `class`
- **所属层**: 规划模型层

## 职责
离线 planner：读取 dump items，用 RoutingEngine 计算 item -> zone，再产出 ZoneAllocationPlan。

## 边界检查
边界健康。它是纯规则层/离线分析层对象，不依赖 Minecraft 运行时。

## 抽象检查
没有过度抽象。

## 主要协作者
- `com.google.gson.stream.JsonReader`
- `com.google.gson.stream.JsonToken`
- `com.knightcode.appliedstoragesorter.rule.filter.ItemMatchContext`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingDecision`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingEngine`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfile`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfileJsonCodec`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
