# SorterMergeReportFileLogger
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/logging/SorterMergeReportFileLogger.java`
- **包**: `com.knightcode.appliedstoragesorter.logging`
- **类型**: `class`
- **所属层**: 日志基础设施层

## 职责
为 `/sorter merge` 生成独立的详细复盘日志文件，负责采集合并前后网络快照，并输出：

- merge 效果复盘
- top benefit items
- 单物品 before / after 变更明细
- 实际 executed move 列表

输出位置为：

- `logs/appliedinsight/merge-*.log`

## 边界检查
边界健康。它专注于 merge 复盘日志，不参与 merge 规划本身，也不承担命令编排职责。

## 抽象检查
抽象适中。把 `/sorter merge` 的价值展示、前后对比和物品级明细从 `SorterFileLogger` 中拆出，可以避免总日志继续膨胀。

## 主要协作者
- `com.knightcode.appliedstoragesorter.appliedinsight`
- `com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult`
- `com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanSummary`
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference`
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor`
- `com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveExecutionResult`
- `com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveOperation`
- `appeng.api.networking.IGrid`
- `appeng.api.stacks.AEItemKey`
- `appeng.api.storage.cells.StorageCell`
- `net.minecraft.commands.CommandSourceStack`
- `net.neoforged.fml.loading.FMLPaths`

## 维护备注
- 该日志器的核心价值是“可展示”，后续若要给前端或 dashboard 消费，优先保证 section 名称、字段命名和 before/after/delta 语义稳定。
- 当前“释放可用组数”基于 AE2 cell 的 `remaining item types` 统计，若后续业务重新定义“可用组数”，应先统一口径再改字段说明。
