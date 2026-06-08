# ProfileGenerationRequest
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/profilegen/ProfileGenerationRequest.java`
- **包**: `com.knightcode.appliedstoragesorter.profilegen`
- **类型**: `record`
- **所属层**: 规则草案生成层

## 职责
作为轻量值对象/结果对象承载 ProfileGenerationRequest 对应的数据快照。

## 边界检查
边界健康。该类型位于纯规则/离线层，没有直接依赖 Minecraft 或 AE2 运行时。

## 抽象检查
没有过度抽象；以值对象/枚举形式存在是合适的。

## 主要协作者
- `com.knightcode.appliedstoragesorter.analysis.SorterDumpRoutingAnalyzer`
- `com.knightcode.appliedstoragesorter.analysis.SorterDumpRoutingSuggestionAnalyzer`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfile`

## 维护备注
- 当前更像稳定的数据/常量定义，无需进一步拆分。
