# DavCellSavedData
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/dav/cell/DavCellSavedData.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.dav.cell`
- **类型**: `class`（`SavedData`）
- **所属层**: AE2 集成层

## 职责
维度级持久化：ID `appliedinsight_dav_cells`，`Map<UUID, DavCellState>`。任一 cell 变更 `setDirty` 后，世界保存时序列化**该维度全部** cell 状态。

## 边界检查
边界健康。不参与 ME 网络或 route；仅存储。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellState`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.WorldDavCellBackend`

## 维护备注
- DAV 数量极大时，存档写入粒度为「整文件」；换 SQL 后端时可缓解。
- 迁移到 SQL 后本类可保留为默认实现或仅作导入源。
