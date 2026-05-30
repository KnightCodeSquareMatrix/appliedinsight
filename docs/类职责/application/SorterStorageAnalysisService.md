# SorterStorageAnalysisService

| 维度 | 说明 |
|------|------|
| **包路径** | `application/SorterStorageAnalysisService.java` |
| **定位** | 存储分析应用服务 |
| **职责** | 编排 `/sorter me storageDump` 命令的完整执行流程：解析 grid 目标 → 调用 `SorterStorageAnalysisDumpWriter` 写出 JSON → 通过 `FileChunkedSender` + `SorterAnalysisPayload` 将分析报告推送给客户端 → 返回 `SorterStorageAnalysisCommandResult` |
| **关键方法** | `execute(CommandSourceStack)` → `SorterStorageAnalysisCommandResult`；`execute(CommandSourceStack, GridTargetResolver)` |
| **依赖** | `Ae2ControllerTargetResolver`、`GridTargetResolver`、`SorterStorageAnalysisDumpWriter`、`Ae2StorageAnalyzer`、`FileChunkedSender`、`SorterAnalysisPayload`、`SorterFileLogger` |
| **被谁使用** | `SorterCommands`（命令入口）、`SorterCommandPayloadHandler`（方块 GUI 入口） |
| **边界** | 不直接操作 AE2 网络；不执行搬运 |
