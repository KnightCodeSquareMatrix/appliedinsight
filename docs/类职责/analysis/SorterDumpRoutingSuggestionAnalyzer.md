# SorterDumpRoutingSuggestionAnalyzer
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/analysis/SorterDumpRoutingSuggestionAnalyzer.java`
- **包**: `com.knightcode.appliedstoragesorter.analysis`
- **类型**: `class`
- **所属层**: 离线分析层

## 职责
基于 fallback 路由结果生成规则建议，帮助扩展 profile。

## 边界检查
边界健康。它是明确的“分析建议层”，没有越界写回运行时。

## 抽象检查
抽象适中。

## 主要协作者
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfile`

## 维护备注
- 建议策略目前是启发式文本输出，后续如果要更正式地生成候选规则，可以继续和 profilegen 包协作。
