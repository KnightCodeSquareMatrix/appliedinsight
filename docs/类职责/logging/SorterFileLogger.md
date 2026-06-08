# SorterFileLogger
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/logging/SorterFileLogger.java`
- **包**: `com.knightcode.appliedstoragesorter.logging`
- **类型**: `class`
- **所属层**: 日志基础设施层

## 职责
为 `/sorter merge`、`/sorter me dump`、`/sorter me storageDump` 提供单文件命令摘要日志输出，覆盖以下路径：

- `/sorter merge`：failure / preview / execution
- `/sorter me dump`：success / failure
- `/sorter me storageDump`：success / failure（`logSorterMeStorageAnalysis` / `logSorterMeStorageAnalysisFailure`）

每条日志通过 `ReportFileSupport.buildCommandHeader()` 生成统一命令头，输出到 `logs/appliedinsight/` 下的单文件。

## 边界检查
边界健康，位于 logging 基础设施层。`SorterFileLogger` 只负责命令级摘要，不参与详细的 merge 复盘（由 `SorterMergeReportFileLogger` 负责）。

## 抽象检查
没有过度抽象；当前把 merge / dump / storageDump 三条命令的摘要日志集中到一个轻量日志器是合理的。

## 主要协作者
- `com.knightcode.appliedstoragesorter.appliedinsight`
- `com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver`
- `com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult`
- `com.knightcode.appliedstoragesorter.ae2.dump.SorterNetworkDumpResult`
- `com.knightcode.appliedstoragesorter.ae2.dump.SorterStorageAnalysisDumpResult`
- `com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanSummary`
- `com.knightcode.appliedstoragesorter.ae2.sort.PlannedMove`
- `com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveExecutionResult`
- `com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveOperation`
- `com.knightcode.appliedstoragesorter.logging.ReportFileSupport`
- `net.minecraft.commands.CommandSourceStack`
- `net.minecraft.core.BlockPos`
- `net.minecraft.world.entity.Entity`
- `net.neoforged.fml.loading.FMLPaths`

## 维护备注
- 现在 merge / dump / storageDump 已经各自拥有更准确的日志标题，merge 日志也统一改成 preview / execute / merge_count 语义，后续继续保持命令语义和日志语义一致即可。
- `/sorter merge` 的详细前后对比明细已迁移到独立的 `SorterMergeReportFileLogger` 文件日志中，这里更适合保留"单次命令摘要 + 报告文件路径"。
- `/sorter me storageDump` 的成功日志包含 scan_status、cell 类型分布等摘要信息；失败日志记录异常堆栈和 scan_status=error。
