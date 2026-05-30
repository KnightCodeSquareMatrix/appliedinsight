# SorterStorageAnalysisCommandResult

| 维度 | 说明 |
|------|------|
| **包路径** | `application/result/SorterStorageAnalysisCommandResult.java` |
| **定位** | `/sorter me storageDump` 命令结果（record） |
| **职责** | 承载存储分析命令的返回结果，包含成功标志、反馈消息行、dump 文件路径、存储位置数和唯一 key 数 |
| **字段** | `success`、`lines` (`List<Component>`)、`dumpFilePath`、`storageLocationCount`、`uniqueKeyCount` |
| **工厂方法** | `failure(String)`、`success(List<Component>, String, int, int)` |
| **被谁使用** | `SorterStorageAnalysisService` 的输出 |
