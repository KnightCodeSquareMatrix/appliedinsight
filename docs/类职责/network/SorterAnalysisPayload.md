# SorterAnalysisPayload

| 维度 | 说明 |
|------|------|
| **包路径** | `network/SorterAnalysisPayload.java` |
| **定位** | 存储分析通知网络包（record） |
| **职责** | 通知客户端有新的存储分析报告可用，携带 `fileId` 供客户端注册 `FileCompleteCallback` 接收完整数据 |
| **字段** | `fileId` (`ResourceLocation`) |
| **编解码** | 使用 `ResourceLocation.STREAM_CODEC` |
| **通道** | `appliedstoragesorter:analysis_notify` |
| **依赖** | NeoForge `CustomPacketPayload`、`StreamCodec` |
| **被谁使用** | `SorterStorageAnalysisService`（发送）、`SorterAnalysisPayloadHandler`（接收） |
