# SorterFileLogger
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/logging/SorterFileLogger.java`
- **包**: `com.knightcode.appliedstoragesorter.logging`
- **类型**: `class`
- **所属层**: 日志基础设施层

## 职责
为 `/sorter merge` 与 `/sorter me dump` 提供单文件日志输出，并记录 merge 预览、实际合并执行、详细 merge report 路径与 dump 的关键阶段。

## 边界检查
边界健康，位于 logging 基础设施层。

## 抽象检查
没有过度抽象；当前把 merge 与 dump 日志集中到一个轻量日志器是合理的。

## 主要协作者
- `com.knightcode.appliedstoragesorter.AppliedStorageSorter`
- `com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver`
- `com.knightcode.appliedstoragesorter.ae2.Ae2GridTargetResult`
- `com.knightcode.appliedstoragesorter.ae2.dump.SorterNetworkDumpResult`
- `com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanSummary`
- `com.knightcode.appliedstoragesorter.ae2.sort.PlannedMove`
- `com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveExecutionResult`
- `com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveOperation`
- `net.minecraft.commands.CommandSourceStack`
- `net.minecraft.core.BlockPos`
- `net.minecraft.world.entity.Entity`
- `net.neoforged.fml.loading.FMLPaths`

## 维护备注
- 现在 merge 与 dump 已经各自拥有更准确的日志标题，merge 日志也统一改成 preview / execute / merge_count 语义，后续继续保持命令语义和日志语义一致即可。
- `/sorter merge` 的详细前后对比明细已迁移到独立的 `SorterMergeReportFileLogger` 文件日志中，这里更适合保留“单次命令摘要 + 报告文件路径”。
