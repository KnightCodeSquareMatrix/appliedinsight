# SorterMoveExecutionResult
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/sort/SorterMoveExecutionResult.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.sort`
- **类型**: `record`
- **所属层**: AE2 集成层

## 职责
作为 merge 执行阶段的结果对象，承载整体执行摘要以及每条 planned move 的实际 extracted / inserted 明细。

## 边界检查
边界健康。该类型明确属于 AE2 集成层，依赖 Minecraft/AE2 API 是合理的，并未反向污染规则层。

## 抽象检查
没有过度抽象；以值对象/枚举形式存在是合适的。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.sort.PlannedMove`
- 主要依赖为 JDK 或同文件内部成员。

## 维护备注
- 当前不仅包含 aggregate summary，还包含 `MoveResult` 明细，已成为 merge 复盘与详细日志的重要输入。
- 若后续继续扩展字段，优先保持 `MoveResult` 的语义稳定，避免影响日志解析与前端展示。
