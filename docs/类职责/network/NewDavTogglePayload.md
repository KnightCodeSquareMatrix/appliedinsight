# NewDavTogglePayload
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/network/NewDavTogglePayload.java`
- **包**: `com.knightcode.appliedstoragesorter.network`
- **类型**: `record`（`CustomPacketPayload`）
- **所属层**: 网络通信层

## 职责
客户端 → 服务端：切换 DAV 三个布尔设置。

| `settingId` 常量 | 设置 |
|------------------|------|
| `SETTING_MIGRATE_EXISTING` (1) | 导入现有库存 |
| `SETTING_AUTO_ACCEPT` (2) | 自动接收后续物品 |
| `SETTING_AUTO_EXPAND` (3) | 自动扩容 |

## 通道
- Type ID：`appliedinsight:new_dav_toggle`
- Payload：`BlockPos` + `int settingId`

## 主要协作者
- `com.knightcode.appliedstoragesorter.network.NewDavTogglePayloadHandler`
- `com.knightcode.appliedstoragesorter.client.screen.DigitalAssetVaultScreen`

## 维护备注
- Handler 校验玩家当前打开 `DigitalAssetVaultMenu` 且 pos 匹配，防止远程篡改。
