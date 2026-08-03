# DavVaultCellBinding
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/dav/cell/DavVaultCellBinding.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.dav.cell`
- **类型**: `class`（静态绑定）
- **所属层**: AE2 集成层

## 职责
连接 DAV 方块与 cell 后端：
- `provisionInitialCellIfEmpty` — 新放置 DAV 首次 tick 且槽空时生成一张新 `DavCellItem`（**不会**在玩家手动取出后自动补回）。
- `prepareInstalledCell` — 槽内有合法 DAV Cell 时分配/校验 `dav_cell_id`（含合成得到的**空白** Cell，首次装入时 `assignNewCellId`）、剥离 legacy 账本、确保 SavedData 中存在对应 `DavCellState`。
- `resolveBackend` — 按槽内 Cell ID 返回 `DavCellBackend`；槽空则 null。
- `migrateLegacyData` — 将方块 NBT 旧库存迁入 `DavCellBackend`。

## 边界检查
边界健康。绑定逻辑集中于此，BlockEntity 不直接操作 `SavedData`。

## 主要协作者
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellBackends`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellStack`
