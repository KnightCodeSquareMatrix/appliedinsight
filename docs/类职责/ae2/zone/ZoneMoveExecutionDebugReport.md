# ZoneMoveExecutionDebugReport
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/ZoneMoveExecutionDebugReport.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `record`
- **所属层**: AE2 集成层

## 职责
作为轻量值对象/结果对象承载 ZoneMoveExecutionDebugReport 对应的数据快照。2026-05-24 新增三级失败计数器用于诊断细化。

## 边界检查
边界健康。该类型明确属于 AE2 集成层，依赖 Minecraft/AE2 API 是合理的，并未反向污染规则层。

## 抽象检查
没有过度抽象；以值对象/枚举形式存在是合适的。

## 主要协作者
- 主要依赖为 JDK 或同文件内部成员。

## 维护备注
- 当前更像稳定的数据/常量定义，无需进一步拆分。

## 字段说明

### 三级失败计数器（2026-05-24 新增）
| 字段 | 类型 | 含义 |
|---|---|---|
| `extractFailedCount` | `int` | extract 返回 0，物品未被取出 |
| `partialInsertCount` | `int` | 只插入了部分物品，但回滚成功 |
| `rollbackFailedCount` | `int` | 插入部分成功 + 回滚不完整，有数据丢失风险 |

### 向后兼容
增加了 `failedMoveCount()` 计算访问器：
```java
public int failedMoveCount() {
    return extractFailedCount + partialInsertCount + rollbackFailedCount;
}
```
`ZoneMoveExecutionResult.failedMoveCount` 字段保持不变，依赖该字段的日志代码无需改动。
