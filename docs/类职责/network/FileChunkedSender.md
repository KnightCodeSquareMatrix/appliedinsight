# FileChunkedSender

| 维度 | 说明 |
|------|------|
| **包路径** | `network/FileChunkedSender.java` |
| **定位** | 服务端大文件分块发送工具 |
| **职责** | 将文件切分为 128KB 的小块，通过 `FileChunkPayload` 依次发送给指定玩家 |
| **关键方法** | `sendFile(ServerPlayer, ResourceLocation, Path)` |
| **常量** | `CHUNK_SIZE = 128 * 1024`（128KB） |
| **依赖** | `FileChunkPayload`、`PacketDistributor` |
| **被谁使用** | `SorterStorageAnalysisService`（推送分析报告 JSON） |
| **边界** | 只负责分块发送，不关心文件内容格式 |
