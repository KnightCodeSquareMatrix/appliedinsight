# FilterMatchMode
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/rule/filter/FilterMatchMode.java`
- **包**: `com.knightcode.appliedstoragesorter.rule.filter`
- **类型**: `enum`
- **所属层**: 纯规则模型层

## 职责
提供 FilterMatchMode 所在子域使用的有限枚举值，避免魔法字符串扩散。

## 边界检查
边界健康。该类型位于纯规则/离线层，没有直接依赖 Minecraft 或 AE2 运行时。

## 抽象检查
没有过度抽象；以值对象/枚举形式存在是合适的。

## 主要协作者
- 主要依赖为 JDK 或同文件内部成员。

## 维护备注
- 当前更像稳定的数据/常量定义，无需进一步拆分。
