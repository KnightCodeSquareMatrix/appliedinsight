# FilterUiMetadata
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterUiMetadata.java`
- **包**: `com.knightcode.appliedstoragesorter.rule.filter`
- **类型**: `record`
- **所属层**: 纯规则模型层

## 职责
定义前端 Query Builder 所需的字段、操作符、组合符元数据。

## 边界检查
边界健康。它明确处于前后端契约层，不侵入运行时。

## 抽象检查
没有过度抽象。

## 主要协作者
- 主要依赖为 JDK 或同文件内部成员。

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
