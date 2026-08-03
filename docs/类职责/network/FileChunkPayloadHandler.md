# FileChunkPayloadHandler

| 维度 | 说明 |
|------|------|
| **包路径** | `network/FileChunkPayloadHandler.java` |
| **定位** | 客户端分块重组处理器 |
| **职责** | 接收 `FileChunkPayload`，按 `fileId` 缓存分块到 `ChunkSession`，全部到达后重组为完整字节数组并触发 `FileCompleteCallback` |
| **关键方法** | `handle(FileChunkPayload, IPayloadContext)`；`registerCallback(ResourceLocation, FileCompleteCallback)`；`unregisterCallback(ResourceLocation)` |
| **内部类型** | `FileCompleteCallback`（函数式接口：`onComplete(String fileName, byte[] data)`）；`ChunkSession`（分块缓存 + 计数） |
| **依赖** | `FileChunkPayload` |
| **被谁使用** | `SorterAnalysisPayloadHandler`（注册回调接收分析报告） |
| **边界** | 纯客户端处理器；使用 `ConcurrentHashMap` 支持并发传输 |
