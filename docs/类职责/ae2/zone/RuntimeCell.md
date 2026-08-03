# RuntimeCell
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/zone/RuntimeCell.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
统一表达运行时可操作 cell，持有引用、来源方块、AE2 action host、读写 storage 和原始 cell。

## 边界检查
边界基本健康。它是运行时层对象，没有回流到规则层；当前可接受其承担一次最小搬运事务原语（如 extract -> insert -> rollback），但不应继续承载更复杂的失败原因体系、trace / metrics、analyzer 解释或 zone 策略判断。
## 抽象检查
抽象恰当，没有做成大而空的“存储节点体系”。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference`
- `appeng.api.config.Actionable`
- `appeng.api.networking.security.IActionHost`
- `appeng.api.networking.security.IActionSource`
- `appeng.api.stacks.AEItemKey`
- `appeng.api.storage.MEStorage`
- `appeng.api.storage.cells.StorageCell`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
- 搬运执行已统一由 `SorterMoveOperation` 承担，`RuntimeCell` 不再参与 extract/insert/rollback 事务。

## 近期清理 (2026-05-24)
- **删除 `transferFrom(SourceContext, AEItemKey, long)`** — 死代码，搬运执行已统一由 `SorterMoveOperation` 引擎管理
- **删除 `SourceContext`** — 仅被 `transferFrom` 使用
- **删除 `TransferOutcome`** — 仅被 `transferFrom` 使用
- 执行职责已从 `RuntimeCell` 转移至 `SorterMoveOperation`，`RuntimeCell` 回归为纯数据模型（持有引用 + storage 能力）
