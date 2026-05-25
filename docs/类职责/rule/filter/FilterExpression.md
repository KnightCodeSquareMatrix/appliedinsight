# FilterExpression
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterExpression.java`
- **包**: `com.knightcode.appliedstoragesorter.rule.filter`
- **类型**: `type`
- **所属层**: 纯规则模型层

## 职责
承载过滤规则子域中的表达式、定义或求值输入。

## 边界检查
边界健康。该类型位于纯规则/离线层，没有直接依赖 Minecraft 或 AE2 运行时。

## 抽象检查
整体没有过度抽象，职责保持在可理解范围内。

## 主要协作者
- 主要依赖为 JDK 或同文件内部成员。

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
