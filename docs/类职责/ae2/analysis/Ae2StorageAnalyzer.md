# Ae2StorageAnalyzer

| 维度 | 说明 |
|------|------|
| **包路径** | `ae2/analysis/Ae2StorageAnalyzer.java` |
| **定位** | 存储网络宏观分析引擎 |
| **职责** | 扫描 AE2 网格的所有存储位置（drive + external storage bus），生成 `StorageAnalyzerReport`，包含：存储位置摘要、物品分布、健康指标、碎片化分析、可疑无限存储候选 |
| **关键方法** | `analyze(IGrid)` → `StorageAnalyzerReport` |
| **关键常量** | `TOP_KEY_COUNT_PER_STORAGE=5`、`FRAGMENTATION_SCORE_HIGH_THRESHOLD=2.5`、`HUGE_STORAGE_AMOUNT_THRESHOLD=1_000_000_000` |
| **健康标志** | `high_fragmentation`、`mixed_internal_external_distribution`、`external_amount_dominant`、`suspected_infinite_storage_present` 等 |
| **依赖** | `DriveMachineAccessor`、`CellCapacityInspector`、`Ae2ControllerTargetResolver` |
| **被谁使用** | `SorterStorageAnalysisDumpWriter`（离线 JSON 输出）、`SorterStorageAnalysisService`（在线网络推送） |
| **边界** | 只做观测和解释，不修改任何状态；不直接执行搬运 |
