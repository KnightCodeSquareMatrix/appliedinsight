# DavCellState
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/dav/cell/DavCellState.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.dav.cell`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
单个 DAV Cell 的运行时状态：`cellId`、`DavCellLedger`（喂入容量累计）、`Map<AEItemKey, Long>` 物品。NBT 序列化供 `DavCellSavedData` 持久化。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellLedger`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.WorldDavCellBackend`
