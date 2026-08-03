# DavCellBackend
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/dav/cell/DavCellBackend.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.dav.cell`
- **类型**: `interface`
- **所属层**: AE2 集成层

## 职责
DAV 虚拟存储盘的后端契约：按 `cellId` 提供容量账本、物品 insert/extract、可用栈枚举与 legacy 导入。方块实体与 ME 适配器只依赖本接口，便于替换 `SavedData` / SQL 等实现。

## 边界检查
边界健康。接口在 `ae2.dav.cell`，不反向依赖 `application/`。

## 实现
- **当前**: `WorldDavCellBackend`（内存 `DavCellState` + `DavCellSavedData` 持久化）
- **规划**: SQL/H2 实现 + `DavCellBackends` 可配置分发

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellLedger`
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`（消费者）
