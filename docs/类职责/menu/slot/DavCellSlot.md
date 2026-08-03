# DavCellSlot
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/menu/slot/DavCellSlot.java`
- **包**: `com.knightcode.appliedstoragesorter.menu.slot`
- **类型**: `class`
- **所属层**: 容器交互层

## 职责
DAV Cell 专用物品槽：仅接受 `DavCellItem`，堆叠上限 1，支持正常取出与放入。槽内 Cell 的 `dav_cell_id` 经 `DavVaultCellBinding.resolveBackend` 决定 BlockEntity 连接的 `DavCellBackend`。

## 边界检查
边界健康。槽位规则与 `BuiltInDavCellFilter`（BlockEntity 侧）一致；存储逻辑不在本类。

## 主要协作者
- `com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellStack`
- `com.knightcode.appliedstoragesorter.client.screen.DigitalAssetVaultScreen`（tooltip 按 `DavCellSlot` 类型分支）
