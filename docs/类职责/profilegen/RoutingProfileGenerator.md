# RoutingProfileGenerator
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/profilegen/RoutingProfileGenerator.java`
- **包**: `com.knightcode.appliedstoragesorter.profilegen`
- **类型**: `interface`
- **所属层**: 规则草案生成层

## 职责
定义 RoutingProfileGenerator 对应的最小协作契约。

## 边界检查
边界健康。该类型位于纯规则/离线层，没有直接依赖 Minecraft 或 AE2 运行时。

## 抽象检查
抽象粒度适中；它表达的是当前真实存在的协作契约，而不是面向想象需求的设计。

## 主要协作者
- 主要依赖为 JDK 或同文件内部成员。

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
