# RoutingEngineExample
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/rule/route/RoutingEngineExample.java`
- **包**: `com.knightcode.appliedstoragesorter.rule.route`
- **类型**: `class`
- **所属层**: 纯规则模型层

## 职责
纯 Java 示例入口，用一份手工 profile 演示 RoutingEngine 的用法。

## 边界检查
边界健康，完全独立于 Minecraft/AE2。

## 抽象检查
不是抽象层，而是示例代码。

## 主要协作者
- `com.knightcode.appliedstoragesorter.rule.filter.FilterCondition`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterGroup`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterField`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterOperator`
- `com.knightcode.appliedstoragesorter.rule.filter.ItemFilter`
- `com.knightcode.appliedstoragesorter.rule.filter.ItemMatchContext`
- `com.knightcode.appliedstoragesorter.rule.zone.StorageZone`
- `com.knightcode.appliedstoragesorter.rule.zone.StorageZoneKind`

## 维护备注
- 适合作为规则层回归示例，但如果以后加入正式测试，可以把这里的构造样例迁移到测试代码。
