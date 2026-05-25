# HeuristicRoutingProfileGenerator
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/profilegen/HeuristicRoutingProfileGenerator.java`
- **包**: `com.knightcode.appliedstoragesorter.profilegen`
- **类型**: `class`
- **所属层**: 规则草案生成层

## 职责
根据 fallback 分析结果自动生成 draft zone/filter/route 组合。

## 边界检查
边界健康。它只生成规则草案，不越界改运行时。

## 抽象检查
没有过度抽象；“启发式生成器”接口化也是合理的。

## 主要协作者
- `com.knightcode.appliedstoragesorter.analysis.SorterDumpRoutingAnalyzer`
- `com.knightcode.appliedstoragesorter.analysis.SorterDumpRoutingSuggestionAnalyzer`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterCondition`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterField`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterOperator`
- `com.knightcode.appliedstoragesorter.rule.filter.ItemFilter`
- `com.knightcode.appliedstoragesorter.rule.route.RouteAction`
- `com.knightcode.appliedstoragesorter.rule.route.RouteRule`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfile`
- `com.knightcode.appliedstoragesorter.rule.zone.StorageZone`
- `com.knightcode.appliedstoragesorter.rule.zone.StorageZoneKind`

## 维护备注
- 生成规则的启发式目前写死在类中，随着策略增多可以再拆。
