# WorldDavCellBackend
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/dav/cell/WorldDavCellBackend.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.dav.cell`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
`DavCellBackend` 的 SavedData 实现：读写 `DavCellState`（物品 map + ledger），变更时回调 `SavedData.setDirty`。

## 边界检查
边界健康。无 AE2 网格依赖；容量上限含 `DavCellConstants` 内置 2048B / 126 types。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellState`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellSavedData`
