# SorterMergeService
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/application/SorterMergeService.java`
- **包**: `com.knightcode.appliedstoragesorter.application`
- **类型**: `class`
- **所属层**: 应用服务层

## 职责
`/sorter merge` 的应用服务，串联解析 grid、前置扫描、merge planner、实际合并执行，以及 merge 前后快照与详细报告输出。

## 边界检查
边界健康。它负责快速 merge 用例编排，但不进入 profile / zone 主流程。

## 抽象检查
不过度抽象，职责非常直接。

## 主要协作者
- `com.knightcode.appliedstoragesorter.Config`
- `com.knightcode.appliedstoragesorter.ae2.Ae2ControllerTargetResolver`
- `com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanner`
- `com.knightcode.appliedstoragesorter.ae2.sort.MergeMovePlanner`
- `com.knightcode.appliedstoragesorter.application.SorterComponentHelper`
- `com.knightcode.appliedstoragesorter.application.result.SorterFeedbackResult`
- `com.knightcode.appliedstoragesorter.logging.SorterFileLogger`
- `com.knightcode.appliedstoragesorter.logging.SorterMergeReportFileLogger`
- `net.minecraft.commands.CommandSourceStack`

## 维护备注
- 它是刻意保留的低复杂度 merge 入口，未来适合直接接 GUI / 控制面板按钮。
- 当前除了写入 `appliedinsight.log` 摘要，还会生成 `logs/appliedinsight/merge-*.log` 详细复盘文件，后续若要接前端价值展示，优先扩展详细报告而不是继续堆总日志字段。
