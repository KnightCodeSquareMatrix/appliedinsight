# DigitalAssetVaultBlockEntity
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/blockentity/DigitalAssetVaultBlockEntity.java`
- **包**: `com.knightcode.appliedstoragesorter.blockentity`
- **类型**: `class`
- **所属层**: 方块实体层

## 职责
DAV 核心方块实体：ME 网格节点、`NewDavStorage` 提供方、喂料槽与 DAV Cell 槽、导入/自动接收/自动扩容逻辑。**不**在方块 NBT 中持久化物品库存；通过槽内 `DavCellItem` 的 ID 解析 `DavCellBackend` 读写世界级存储。

## 边界检查
边界健康。存储实现委托 `ae2.dav.cell`；本类负责 AE2 集成、GUI 同步与方块级配置（开关、扩容 Cell 选择）。

## 关键设计
- **DAV Cell 槽**: `builtInCellInventory`（1 格），仅 `DavCellItem` + `dav_cell_id`；可取出/更换。槽空时 `backendOrNull()` 为 null，ME 不暴露存储；`onBuiltInCellChanged` 刷新网格并设 `NO_DAV_CELL` 状态。
- **首次放置**: `onFirstTickReady` → `provisionInitialCellIfEmpty`（仅槽空时生成新 Cell）。
- **喂料槽**: `inputInventory`，吸收空 AE2 Storage Cell 容量 → 当前 backend 的 `absorbCapacity(...)`。
- **存储路径**: `backendOrNull()` → `DavVaultCellBinding.resolveBackend` → `DavCellBackends` → `DavCellSavedData`。
- **方块 NBT**: 开关、扩容配置、槽内 DAV Cell 物品（仅 ID）；旧档 `StoredItems` / `Absorbed*` 在 `ensureDavCellReady` 时一次性 `importLegacy` 进 SavedData。
- **多 DAV 共享**: 多个方块插入相同 `dav_cell_id` 的 Cell → 同一 `DavCellBackend`（末影箱式访问点，非复制库存）。
- **管理卡**: 已移除（beta）；勿在本类恢复 ZoneCard 槽。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.dav.NewDavStorage`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellBackend`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavVaultCellBinding`
- `com.knightcode.appliedstoragesorter.application.NewDavMigrationService`
- `com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu`

## 维护备注
- 新增存储字段时优先扩展 `DavCellBackend` / `DavCellState`，避免写回方块 NBT。
- `showMigrateToSqlTbd()` 为 SQL 迁移占位；实现 H2 后端时接 `DavCellBackends` 分发与迁移服务。
