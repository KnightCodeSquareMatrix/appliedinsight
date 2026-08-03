# SorterAnalysisPayloadHandler

| 维度 | 说明 |
|------|------|
| **包路径** | `network/SorterAnalysisPayloadHandler.java` |
| **定位** | 客户端存储分析报告处理器 |
| **职责** | 接收 `SorterAnalysisPayload` 通知后，通过 `FileChunkPayloadHandler` 注册回调接收完整 JSON，反序列化为 `StorageAnalyzerReport` 并缓存 |
| **关键方法** | `handle(SorterAnalysisPayload, IPayloadContext)`；`getReport()` → `StorageAnalyzerReport`；`getRawJson()` → `String`；`hasData()` → `boolean`；`clear()` |
| **依赖** | `SorterAnalysisPayload`、`FileChunkPayloadHandler`、`StorageAnalyzerReport`、Gson |
| **被谁使用** | `ClientStorageAnalysisCache`（便捷入口） |
| **边界** | 纯客户端处理器；缓存可被 Screen 消费 |
