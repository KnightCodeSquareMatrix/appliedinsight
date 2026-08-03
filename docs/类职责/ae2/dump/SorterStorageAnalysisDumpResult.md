# SorterStorageAnalysisDumpResult

| 维度 | 说明 |
|------|------|
| **包路径** | `ae2/dump/SorterStorageAnalysisDumpResult.java` |
| **定位** | 存储分析 dump 结果（record） |
| **职责** | 承载 `SorterStorageAnalysisDumpWriter.dump()` 的返回结果，包含 dump 文件路径和关键统计摘要 |
| **字段** | `dumpFilePath`、`storageLocationCount`、`nonEmptyStorageLocationCount`、`internalStorageLocationCount`、`externalStorageLocationCount`、`uniqueKeyCount`、`duplicatedKeyCount`、`totalAmount`、`externalTotalAmount`、`fragmentationLevel`、`healthFlags` |
| **被谁使用** | `SorterStorageAnalysisDumpWriter`（输出）、`SorterStorageAnalysisService`（消费摘要） |
