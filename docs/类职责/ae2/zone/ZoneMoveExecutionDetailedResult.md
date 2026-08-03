# ZoneMoveExecutionDetailedResult
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/zone/ZoneMoveExecutionDetailedResult.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `record`
- **所属层**: AE2 集成层

## 职责
作为轻量值对象/结果对象承载 ZoneMoveExecutionDetailedResult 对应的数据快照。

## 边界检查
边界健康。该类型明确属于 AE2 集成层，依赖 Minecraft/AE2 API 是合理的，并未反向污染规则层。

## 抽象检查
没有过度抽象；以值对象/枚举形式存在是合适的。

## 主要协作者
- 主要依赖为 JDK 或同文件内部成员。

## 维护备注
- 当前更像稳定的数据/常量定义，无需进一步拆分。
