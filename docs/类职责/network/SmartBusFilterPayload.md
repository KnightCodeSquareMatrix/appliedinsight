# SmartBusFilterPayload
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/network/SmartBusFilterPayload.java`
- **包**: `com.knightcode.appliedstoragesorter.network`
- **类型**: `record`（`CustomPacketPayload`）
- **所属层**: 网络通信层

## 职责
客户端 → 服务端：保存 Smart Bus 过滤器 JSON。Payload 含 `BlockPos`、`Direction side`、filter 字符串（空串视为 null）。

## 通道
- Type ID：`appliedinsight:smart_bus_filter`
- Handler：`SmartBusFilterPayload.handle`（同文件静态方法）

## 边界检查
边界健康。Handler 通过 `IPartHost.getPart(side)` 定位 `SmartBusPart` 并 `setFilterJson`；不在 payload 层解析 filter 语义。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.part.SmartBusPart`
- `com.knightcode.appliedstoragesorter.client.screen.SmartBusScreen`

## 维护备注
- 大 JSON 仍受 Minecraft UTF 包长度限制；极端 filter 需压缩或分块（当前未实现）。
