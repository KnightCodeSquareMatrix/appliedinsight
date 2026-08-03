# StorageAnalyzerReport

| 维度 | 说明 |
|------|------|
| **包路径** | `ae2/analysis/StorageAnalyzerReport.java` |
| **定位** | 存储分析报告（record） |
| **职责** | 承载 `Ae2StorageAnalyzer` 的完整分析结果，包含 6 个内嵌 record 类型 |
| **字段** | `summary` (NetworkSummary)、`storageLocations`、`itemDistributions`、`healthFlags`、`mostFragmentedItems`、`largestStorages`、`mixedInternalExternalItems`、`suspectedSemanticCandidates` |
| **内嵌类型** | `NetworkSummary`（网络级统计）、`StorageLocationSummary`（单存储位置详情）、`ItemDistributionSummary`（物品分布）、`StorageSemanticCandidate`（可疑无限存储候选）、`KeyAmountSummary`（Top N 物品摘要） |
| **被谁使用** | `SorterStorageAnalysisDumpWriter`（序列化为 JSON）、`SorterAnalysisPayloadHandler`（客户端反序列化） |
| **JSON 契约** | 详见 `docs/日志与JSON字段契约.md` 第 8 节 |
