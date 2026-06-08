# ZonePlacementDecision
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/zone/ZonePlacementDecision.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `record`
- **所属层**: AE2 集成层

## 职责
表达一次 zone 放置判断的结果：接受/拒绝、目标 cell、接受数量和原因。

## 边界检查
边界健康；是 zone 决策层和执行层之间的协议对象。

## 抽象检查
不过度抽象，accepted/rejected 工厂方法也很合适。

## 主要协作者
- 主要依赖为 JDK 或同文件内部成员。

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
