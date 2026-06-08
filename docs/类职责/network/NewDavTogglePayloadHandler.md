# NewDavTogglePayloadHandler
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/network/NewDavTogglePayloadHandler.java`
- **包**: `com.knightcode.appliedstoragesorter.network`
- **类型**: `final class`
- **所属层**: 网络通信层

## 职责
处理 `NewDavTogglePayload`：在服务端切换 `DigitalAssetVaultBlockEntity` 上对应布尔字段；migrate 切换时写 INFO 日志。

## 边界检查
边界健康。仅 toggle 布尔值，不含业务规划；迁移执行仍由 BlockEntity tick 调用 `NewDavMigrationService`。

## 主要协作者
- `com.knightcode.appliedstoragesorter.network.NewDavTogglePayload`
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`
- `com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu`

## 维护备注
- 在 `AppliedStorageSorter.registerPayloads` 注册 play-to-server。
