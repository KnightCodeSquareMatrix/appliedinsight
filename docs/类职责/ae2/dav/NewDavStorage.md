# NewDavStorage
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/dav/NewDavStorage.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.dav`
- **类型**: `class`（实现 `MEStorage`）
- **所属层**: AE2 集成层

## 职责
DAV 的 AE2 存储适配器。将 `DigitalAssetVaultBlockEntity` 经 `DavCellBackend` 暴露为网格可访问的 `MEStorage`：insert/extract/getAvailableStacks/isPreferredStorageFor。

## 边界检查
边界健康。所有状态变更委托 BlockEntity → `DavCellBackend`；本类不持久化、不决策 route/zone。

## 关键行为
- `isPreferredStorageFor`：仅在 **自动接收** 开启且 DAV 仍有字节/类型余量时返回 true
- 仅处理 `AEItemKey`（与 DAV 当前物品存储模型一致）

## 主要协作者
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`
- `com.knightcode.appliedstoragesorter.ae2.dav.NewDavStorageProvider`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellBackend`（间接，经 BlockEntity）

## 维护备注
- 类名保留 `NewDav` 前缀（历史命名）；方块 ID 已为 `digital_asset_vault`。
