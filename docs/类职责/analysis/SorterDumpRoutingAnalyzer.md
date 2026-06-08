# SorterDumpRoutingAnalyzer
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/analysis/SorterDumpRoutingAnalyzer.java`
- **包**: `com.knightcode.appliedstoragesorter.analysis`
- **类型**: `class`
- **所属层**: 离线分析层

## 职责
用 RoutingProfile 对 dump 中的 items 做离线路由，并输出 zone 聚合和 fallback 分析。

## 边界检查
边界健康。它消费纯规则层和 dump 数据，没有直接依赖游戏运行时。

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
