# FileChunkPayload

| 维度 | 说明 |
|------|------|
| **包路径** | `network/FileChunkPayload.java` |
| **定位** | 大文件分块传输网络包（record） |
| **职责** | 承载文件分块数据：`fileId`（文件标识）、`chunkIndex`（当前块索引）、`totalChunks`（总块数）、`data`（二进制数据） |
| **字段** | `fileId` (`ResourceLocation`)、`chunkIndex` (`int`)、`totalChunks` (`int`)、`data` (`byte[]`) |
| **编解码** | 自定义 `StreamCodec`，使用 `FriendlyByteBuf` 读写 |
| **通道** | `appliedinsight:file_chunk` |
| **依赖** | NeoForge `CustomPacketPayload`、`StreamCodec` |
| **被谁使用** | `FileChunkedSender`（发送）、`FileChunkPayloadHandler`（接收） |
