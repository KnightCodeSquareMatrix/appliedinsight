# DavCellBackends
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/dav/cell/DavCellBackends.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.dav.cell`
- **类型**: `class`（静态工厂）
- **所属层**: AE2 集成层

## 职责
按 `DavCellItem` 上的 UUID 或裸 `cellId` 解析 `DavCellBackend`。当前固定走 `DavCellSavedData.openBackend`；换后端时主要改此类。

## 边界检查
边界健康。仅服务端 `ServerLevel` 可解析；客户端返回 null。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellSavedData`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellStack`
