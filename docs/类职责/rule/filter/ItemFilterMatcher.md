# ItemFilterMatcher
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/rule/filter/ItemFilterMatcher.java`
- **包**: `com.knightcode.appliedstoragesorter.rule.filter`
- **类型**: `class`
- **所属层**: 纯规则模型层

## 职责
执行 filter 表达式树匹配，把 ItemMatchContext 与 FilterCondition/FilterGroup 做求值。

## 边界检查
边界健康。它是纯规则层逻辑，不依赖游戏运行时。

## 抽象检查
没有过度抽象；当前 operator 集规模下直接 switch 很清晰。

## 主要协作者
- 主要依赖为 JDK 或同文件内部成员。

## 维护备注
- 数值解析异常会直接冒出 NumberFormatException，调用方需保证配置合法。
- `NBT_PATH`：`value` 格式为 `path||comparison`（见 `docs/日志与JSON字段契约.md` §9.10）；耐久组件路径见 §9.10 表格。
