# DigitalAssetVaultScreen
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/client/screen/DigitalAssetVaultScreen.java`
- **包**: `com.knightcode.appliedstoragesorter.client.screen`
- **类型**: `class`
- **所属层**: 客户端展示层

## 职责
数字资产库（DAV）客户端 GUI：继承 `SorterBaseScreen`（AE2 `AEBaseScreen` + 本 mod 主题），挂载三个开关、扩容 Cell 选择器，渲染右列吸收统计与左列状态/校验文字。

## 边界检查
边界健康。槽位坐标由 **Screen JSON** 定义；右列文字 Y 由 **`DigitalAssetVaultMenu` 常量**定义；本类只负责左列控件与自定义标签。

## 关键设计

### Screen 样式（AE2 ScreenStyle）
- **主文件**: `assets/appliedinsight/screens/digital_asset_vault.json`
- **玩家背包**: `screens/common/new_dav_player_inventory.json`（通过 `includes` 合并；文件名保留历史前缀）
- **背景**: `textures/gui/digital_asset_vault.png`，256×256，`srcRect` 有效区域 **226×238**
- **Cell 输入槽**: semantic `appliedinsight_INPUT_CELL`，坐标 **(167, 44)**
- **玩家背包**: 行 y=162/180/198，快捷栏 y=220，x=30

### 双列垂直分区（GUI 本地 px）

| 列 | Y 范围 | 内容 | 定义位置 |
|----|--------|------|----------|
| 共用 | 0–20 | 标题带 | Screen JSON `dialog_title` |
| **右列** | 28 | 标题「吸收 Cell」 | Menu `ABSORPTION_TITLE_Y` |
| **右列** | 44 | Cell 输入槽 | Screen JSON |
| **右列** | 68, 82, 96 | 吸收统计三行 | Menu `ABSORPTION_STAT_*_Y` |
| **左列** | 26, 48, 70 | 三个开关 | Screen `TOGGLE_ROW_*` |
| **左列** | 94 / 104 | 扩容 Cell ID 标签 / EditBox | Screen 常量 |
| **左列** | 124 | 校验指示 | `INDICATOR_Y` |
| **左列** | 142 | 状态行 | `STATUS_Y` |
| 共用 | 157 | 「物品栏」标签 | Screen JSON |
| 共用 | 162+ | 玩家背包 + 快捷栏 | Screen JSON |

### 主题
- 基类 `SorterBaseScreen` → `themedTextColor()` / `themedMutedTextColor()`
- 开关 `NewDavToggleButton`（AE2Button 风格；类名保留 `NewDav` 前缀）

## 主要协作者
- `com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu`
- `com.knightcode.appliedstoragesorter.client.gui.SorterBaseScreen`
- `com.knightcode.appliedstoragesorter.network.NewDavTogglePayload`
- `com.knightcode.appliedstoragesorter.network.NewDavSetExpansionCellPayload`
- `assets/appliedinsight/screens/digital_asset_vault.json`

## 维护备注
修改布局时同步：`digital_asset_vault.json` → `new_dav_player_inventory.json` → Menu 常量 → PNG → 本类左列 Y → [`GUI_VISUAL_CHECKLIST.md`](../../../ai/GUI_VISUAL_CHECKLIST.md)。
