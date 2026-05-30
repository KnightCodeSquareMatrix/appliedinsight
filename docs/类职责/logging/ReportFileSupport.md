# ReportFileSupport

| 维度 | 说明 |
|------|------|
| **包路径** | `logging/ReportFileSupport.java` |
| **定位** | 报告文件基础设施工具类 |
| **职责** | 提供日志/dump 目录解析、时间戳文件名生成、网络标识 slug、文件写入/追加、命令头格式化、坐标格式化等通用工具方法 |
| **关键方法** | `resolveLogDir(String)`、`resolveDumpDir(String)`、`timestampedFileName(String, String)`、`latestFileName(String, String)`、`networkSlug(String, BlockPos)`、`writeTextFile(Path, String, String, Logger, String)`、`appendToLogFile(Path, String, Logger, String)`、`relativizeGamePath(Path)`、`buildCommandHeader(CommandSourceStack, String)`、`formatDriveCellReference(DriveCellReference)` |
| **依赖** | `FMLPaths.GAMEDIR`、`DriveCellReference`、`AEItemKey` |
| **被谁使用** | `SorterFileLogger`、`SorterPlanFileLogger`、`SorterMergeReportFileLogger`、`SorterNetworkDumpWriter`、`SorterStorageAnalysisDumpWriter` |
| **边界** | 纯工具类，不持有状态 |
