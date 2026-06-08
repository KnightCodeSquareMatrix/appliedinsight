# NewDavSetExpansionCellPayload
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/network/NewDavSetExpansionCellPayload.java`
- **包**: `com.knightcode.appliedstoragesorter.network`
- **类型**: `record`（`CustomPacketPayload`）
- **所属层**: 网络通信层

## 职责
客户端 → 服务端：设置 DAV 自动扩容目标 Cell 的物品 ID 字符串（来自 EditBox 或 JEI/EMI ghost item）。

## 通道
- Type ID：`appliedinsight:new_dav_set_expansion_cell`
- Payload：`BlockPos` + UTF-8 `cellId`

## 主要协作者
- `com.knightcode.appliedstoragesorter.network.NewDavSetExpansionCellPayloadHandler`
- `com.knightcode.appliedstoragesorter.client.gui.widget.ExpansionCellPickerWidget`

## 维护备注
- 空字符串表示清除 expansion cell 配置。
