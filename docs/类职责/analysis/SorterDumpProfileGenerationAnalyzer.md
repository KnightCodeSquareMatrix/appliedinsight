# SorterDumpProfileGenerationAnalyzer
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/analysis/SorterDumpProfileGenerationAnalyzer.java`
- **包**: `com.knightcode.appliedstoragesorter.analysis`
- **类型**: `class`
- **所属层**: 离线分析层

## 职责
串联 base profile、routing analysis、suggestion analysis 和生成器，产出 draft profile 及备注。

## 边界检查
边界健康。它是离线应用脚本，不进入主运行时。

## 抽象检查
没有过度抽象。

## 主要协作者
- `com.knightcode.appliedstoragesorter.profilegen.HeuristicRoutingProfileGenerator`
- `com.knightcode.appliedstoragesorter.profilegen.ProfileGenerationRequest`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfileJsonCodec`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
