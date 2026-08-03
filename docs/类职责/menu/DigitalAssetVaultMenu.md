# DigitalAssetVaultMenu

- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/menu/DigitalAssetVaultMenu.java`

- **包**: `com.knightcode.appliedstoragesorter.menu`

- **类型**: `class`

- **所属层**: 容器交互层



## 职责

DAV 容器菜单：注册喂料槽、DAV Cell 槽、玩家背包；通过 DataSlot 同步容量用量、开关状态、扩容 Cell 与状态行文案。



## 边界检查

边界健康。GUI 布局常量集中在本类；存储逻辑在 BlockEntity + `DavCellBackend`。



## 关键设计

- **SlotSemantic**

  - `appliedinsight_INPUT_CELL` — `StorageCellSlot`，喂入空 AE2 Cell

  - `appliedinsight_BUILT_IN_DAV_CELL` — `DavCellSlot`，可放入/取出 `DavCellItem`

- **布局常量**: `GUI_WIDTH=360`；右列吸收统计 Y（`ABSORPTION_*`）；左列开关与按钮坐标。

- **同步数据**: `absorbedCellCount`、`usedBytes`/`absorbedBytes`（总字节上限）、类型用量等，来源为 BlockEntity 对 backend 的查询；无 Cell 时容量同步为 0。

- **quickMoveStack**: DAV Cell 可 shift 进出 Cell 槽；喂料槽与 Cell 槽均可 shift 回玩家背包。



## 主要协作者

- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`

- `com.knightcode.appliedstoragesorter.menu.slot.DavCellSlot`

- `com.knightcode.appliedstoragesorter.menu.slot.StorageCellSlot`

- `assets/appliedinsight/screens/digital_asset_vault.json`



## 维护备注

- 管理卡槽 / 10 格 Drive 布局已废弃；勿按旧 Drive 文档改坐标。

- 修改槽位 semantic 或 index 时同步 Screen JSON 与 `DigitalAssetVaultScreen.renderTooltip` 的槽位类型判断。

