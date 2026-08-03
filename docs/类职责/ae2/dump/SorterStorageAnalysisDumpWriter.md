# SorterStorageAnalysisDumpWriter

| 维度 | 说明 |
|------|------|
| **包路径** | `ae2/dump/SorterStorageAnalysisDumpWriter.java` |
| **定位** | 存储分析 JSON dump 输出器 |
| **职责** | 调用 `Ae2StorageAnalyzer` 分析网格，将 `StorageAnalyzerReport` 序列化为 JSON 写入 `dumps/appliedinsight/storage-analysis-*.json`；同时输出 `latest` 版本供前端动态读取 |
| **关键方法** | `dump(Ae2GridTargetResult)` → `SorterStorageAnalysisDumpResult` |
| **输出文件** | `dumps/appliedinsight/storage-analysis-{slug}-{timestamp}.json` + `storage-analysis-{slug}-latest.json` |
| **依赖** | `Ae2StorageAnalyzer`、`StorageAnalyzerReport`、`ReportFileSupport`、Gson |
| **被谁使用** | `SorterStorageAnalysisService` |
| **边界** | 只负责序列化和文件写入，不负责网络推送 |
