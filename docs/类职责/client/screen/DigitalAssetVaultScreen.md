# DigitalAssetVaultScreen
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/client/screen/DigitalAssetVaultScreen.java`
- **包**: `com.knightcode.appliedstoragesorter.client.screen`
- **类型**: `class`
- **所属层**: 客户端展示层

## 职责
数字资产库（DAV）客户端 GUI：继承 `SorterBaseScreen`，挂载三个开关、「立即扩容」「迁移 SQL（占位）」按钮、扩容 Cell 选择器；渲染右列容量统计与状态行。

## 边界检查
边界健康。槽位坐标由 Screen JSON 定义；右列文字 Y 由 `DigitalAssetVaultMenu` 常量定义；本类负责左列控件、自定义标签与**槽位 tooltip 覆盖**。

## 关键设计

### Screen 样式（AE2 ScreenStyle）
- **主文件**: `assets/appliedinsight/screens/digital_asset_vault.json`
- **玩家背包**: `screens/common/new_dav_player_inventory.json`
- **背景**: `textures/gui/digital_asset_vault.png`，384×320，`srcRect` 有效区域 **360×262**
- **DAV Cell 槽**: semantic `appliedinsight_BUILT_IN_DAV_CELL`，(230, 46)，可放入/取出
- **喂料槽**: semantic `appliedinsight_INPUT_CELL`，(258, 46)

### Tooltip（2026-06-09）
- **禁止**在 `render()` 里对槽位二次 `renderTooltip`（会与 `AEBaseScreen` 物品 tooltip 叠在同一坐标）。
- 覆盖 `renderTooltip()`：
  - 悬停 **DavCellSlot**（有物品）→ `drawTooltip()` 分层显示：标题、说明、容量段（来自 Menu 同步数据）、F3 下 Cell ID。
  - 悬停 **空 DavCellSlot** → 提示插入 DAV Cell 以连接后端。
  - 悬停 **空 StorageCellSlot**（喂料槽）→ 仅显示喂料说明。
  - 其余槽位 → `super.renderTooltip()`（标准物品 tooltip）。

### 左列控件
- 三个 `NewDavToggleButton`（导入 / 自动接收 / 自动扩容）
- `ThemedAE2Button`：立即扩容、Migrate to SQL（占位，服务端仅设 `MIGRATE_TO_SQL_TBD` 状态）
- `ExpansionCellPickerWidget`：扩容 Cell 类型选择

## 主要协作者
- `com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu`
- `com.knightcode.appliedstoragesorter.menu.slot.DavCellSlot`
- `com.knightcode.appliedstoragesorter.menu.slot.StorageCellSlot`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellStack`
- `com.knightcode.appliedstoragesorter.client.format.ByteUnitFormatter`
- `assets/appliedinsight/screens/digital_asset_vault.json`

## 维护备注
修改布局时同步：`digital_asset_vault.json` → Menu 常量 → PNG → 本类 → [`GUI_VISUAL_CHECKLIST.md`](../../../ai/GUI_VISUAL_CHECKLIST.md)。新增 lang key 时同步 `en_us.json` / `zh_cn.json`。
