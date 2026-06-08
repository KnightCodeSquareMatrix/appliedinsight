# SmartBusModePayload
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/network/SmartBusModePayload.java`
- **包**: `com.knightcode.appliedstoragesorter.network`
- **类型**: `record`（`CustomPacketPayload`）
- **所属层**: 网络通信层

## 职责
客户端 → 服务端：请求 Smart Bus **循环切换模式**（`SmartBusPart.cycleMode()`）。Payload 含 `BlockPos` + `Direction side`。

## 通道
- Type ID：`appliedinsight:smart_bus_mode`
- Handler：`SmartBusModePayload.handle`（同文件静态方法）

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.part.SmartBusPart`
- `com.knightcode.appliedstoragesorter.client.screen.SmartBusScreen`

## 维护备注
- GUI「切换模式」按钮与 Shift+扳手均最终改变同一 `SmartBusMode` 状态。
