# GUI 视觉待优化清单

> **应用能源：洞察**（Applied Energistics: Insight）— 多模态模型与开发者 UI 验收文档。
> **最后更新**: 2026-06-08 · 0.9.0-beta

## AE2 对齐架构（Phase 1）

四个 mod GUI 均已迁移到 **AE2 `AEBaseScreen` + `ScreenStyle` JSON** 体系，但**所有主题资产放在 `appliedinsight:` 命名空间**，不覆盖 `assets/ae2/`。

| 层 | 类 / 资源 | 职责 |
|----|-----------|------|
| 主题 API | `GuiTheme` / `GuiThemes` / `AppliedDarkMatterV2Theme` / `Ae2VanillaTheme` / `GuiThemeProvider` | 提供 sprite、`states.png`、palette；Phase 2 mixin 注入点 |
| 样式加载 | `SorterStyleManager` | 平行于 AE2 `StyleManager`，从 `appliedinsight:screens/*.json` 加载 |
| Screen 基类 | `SorterBaseScreen` | `shouldAddToolbar() = false`；挂载 `ThemedVerticalButtonBar` + `ThemedOpenGuideButton` |
| 注册 | `SorterInitScreens` | 仿 AE2 `InitScreens.register`，构造时注入 `ScreenStyle` |
| 主题控件 | `ThemedAE2Button` / `ThemedVerticalButtonBar` / `ThemedOpenGuideButton` | 使用本 mod sprite，不读 `ae2:button` |

**Screen JSON 目录**: `src/main/resources/assets/appliedinsight/screens/`  
**纹理目录**: `src/main/resources/assets/appliedinsight/textures/gui/`  
**生成命令**: `python tools/generate_sorter_gui_assets.py`（P0 全套 dark_matter_v2 资产）

### 游戏内总体验收（必做）

- [ ] 四个 GUI 背景、按钮、左侧 GuideMe 竖栏均为 **dark_matter_v2** 深色主题
- [ ] 打开 AE2 原生 GUI（如 ME Drive）仍为 **原版灰蓝**，证明未污染 `ae2:` 资产
- [ ] GuideMe 帮助按钮可打开 `helpTopic` 对应文档页

---

## 当前 GUI 资产列表（P0）

| 资产 | 路径 | 尺寸 / 说明 |
|------|------|-------------|
| 按钮 sprite | `textures/gui/sprites/button*.png` + `.mcmeta` | AE2 九切片，主题色 |
| 左栏背景 | `textures/gui/sprites/vertical_buttons_bg.png` | GuideMe 竖栏底图 |
| 状态图集 | `textures/gui/states.png` | HELP 图标等（坐标与 AE2 `Icon` 对称） |
| 动态背景 | `textures/gui/background.png` | 宽屏九宫格（`generatedBackground` 备用） |
| SCB 背景 | `textures/gui/sorter_command_block.png` | **440×224**，JSON `background.srcRect` |
| DAV 背景 | `textures/gui/digital_asset_vault.png` | 256×256，有效 **226×238** |
| SmartBus 背景 | `textures/gui/smart_bus.png` | **400×280**，JSON `background.srcRect`；无物品栏大面板 |
| Palette | `screens/common/palette.json` | 与 `applied_dark_matter_v2` 对齐 |

| Screen | JSON | Menu |
|--------|------|------|
| SorterCommandBlock | `screens/sorter_command_block.json` | `SorterCommandBlockMenu`（`AEBaseMenu`） |
| DigitalAssetVault | `screens/digital_asset_vault.json` | `DigitalAssetVaultMenu` |
| SmartBus | `screens/smart_bus.json` | `SmartBusMenu` |

**自定义绘制钩子**: 子类覆盖 AE2 的 `drawBG` / `drawFG`（勿覆盖 `final` 的 `renderBg` / `renderLabels`）。

---

## Smart Bus GUI 布局契约 + 网页编辑工作流

> 无物品栏大面板；过滤器主编辑在 `tools/filter-editor/` 静态网页，游戏内负责展示、剪贴板同步与保存。

| 区域 | 坐标 (local) | 尺寸 / 说明 |
|------|--------------|-------------|
| 标题 | (8, 6) | JSON `dialog_title` |
| 模式行（横向） | (16, 22) → 宽至 (220) | `模式` + 模式名 + Shift 提示 |
| 切换 / 网页编辑 | (228, 21) / (306, 21) | 72×18 / 80×18 |
| JSON 标签 | (16, 42) | drawString |
| 预设按钮 | (14, 52) 起 | 80×16 ×3，间距 6 |
| MultiLineEditBox | (14, 70) | **360×166** |
| 解析摘要 | (16, 242) | 实时校验 |
| 状态行 | (16, 254) | 保存/粘贴反馈 |
| 粘贴/复制/保存/删除 | (14, 262) | 52/52/60/48 × 16 |

**网页编辑器（离线）**: `./gradlew exportFilterEditorMetadata` → `tools/filter-editor/metadata.js`；双击 `index.html` 即可。游戏内默认解压到 `config/appliedinsight/filter-editor/` 并以 `file://` 打开（`filterEditorUrl` 留空）。

---

## 数字资产库 DAV GUI 布局契约

> 类职责详见 [`DigitalAssetVaultScreen`](../类职责/client/screen/DigitalAssetVaultScreen.md) 与 [`DigitalAssetVaultMenu`](../类职责/menu/DigitalAssetVaultMenu.md)。

### 双列结构（自上而下）

**左列（x=14，宽 106）** — 开关与扩容配置  
**右列（x=134，宽 84）** — **标题 → Cell 槽位 → 吸收统计**（垂直堆叠，勿把统计挤到槽位上）

### 坐标表（GUI 本地坐标，px）

| 区域 | Y | X | 定义位置 |
|------|---|---|----------|
| **左列** 开关 1–3 | 26, 48, 70 | 14 | Screen `init` |
| **左列** EditBox 标签 | 94 | 14 | Screen `renderCustomLabels` |
| **左列** EditBox | 104 | 14 | Screen `init` |
| **左列** 校验指示 | 124 | 14 | Screen `renderCellIndicator` |
| **左列** 状态行 | 142 | 14 | Screen `renderCustomLabels` |
| 列分隔线 | 22–110 | 128 | PNG / 纹理脚本 |
| **右列** 标题「吸收 Cell」 | **28** | **140** | Menu `ABSORPTION_*` + Screen |
| **右列** Cell 输入槽 | **44** | **167**（右列居中） | Screen JSON `appliedinsight_INPUT_CELL` |
| **右列** 统计 Cell | **68** | 140 | Menu `ABSORPTION_STAT_1_Y` |
| **右列** 统计 字节 | **82** | 140 | Menu `ABSORPTION_STAT_2_Y` |
| **右列** 统计 类型 | **96** | 140 | Menu `ABSORPTION_STAT_3_Y` |
| 「物品栏」标签 | **157** | 30 | Screen JSON `player_inventory_title` |
| 背包三行 | **162, 180, 198** | 30 + col×18 | Screen JSON `PLAYER_INVENTORY` |
| 快捷栏 | **220** | 30 + col×18 | Screen JSON `PLAYER_HOTBAR` |

**右列 X 基准**: `RIGHT_SECTION_X = 134`，文字 `ABSORPTION_TEXT_X = 140`（section 内边距 +6）。  
**Cell 槽 X**: `134 + (84 - 18) / 2 = 167`。

### 布局真相源（修改时必须同步）

| 内容 | 真相源 |
|------|--------|
| 背景 blit / 有效区域 | `screens/digital_asset_vault.json` → `background.srcRect` (226×238) |
| Cell 槽、玩家背包槽 | 上述 Screen JSON + `common/new_dav_player_inventory.json` |
| 右列标题与三行统计 Y | `DigitalAssetVaultMenu` 常量 `ABSORPTION_*` |
| 左列开关 / EditBox / 状态 | `DigitalAssetVaultScreen` 本地常量 |
| 槽位背景像素 | `tools/generate_new_dav_texture.py` → 运行脚本重写 PNG |

### 游戏内验收清单

- [ ] 右列顺序清晰：**「吸收 Cell」→ 槽位 → Cell / 字节 / 类型**，统计不与槽位重叠
- [ ] Cell 输入槽物品对齐 PNG 槽位背景（167, 44）
- [ ] 三个开关完整可见，不覆盖「物品栏」标签与背包第一行
- [ ] 状态行（y=142）在背包上方，不画在物品图标上
- [ ] 背包 27 格 + 快捷栏 9 格对齐槽位背景
- [ ] EditBox 与背包无垂直重叠

---

## 贴图错位排查经验（2026-06-08）

### 症状
- 开关按钮压在背包第一行上
- 「物品栏」标签被按钮遮挡
- 状态文字叠在物品图标上
- **右列统计（Cell / 字节 / 类型）贴在 Cell 槽位上或挤在标题旁**

### 根因（三类，常同时存在）

1. **Menu / Screen / JSON 垂直分区冲突**  
   背包槽 Y 与开关/EditBox Y 共用同一 band。仅把槽位改为与 PNG 一致，而 Screen 控件仍在更高位置，必然重叠。

2. **右列未按「标题 → 槽 → 统计」堆叠**  
   统计 Y 设在槽位 band（如 y=50）会与槽位（y=44–62）重叠。统计必须放在槽位下方（当前 y≥68）。

3. **`blit` / ScreenStyle UV 除数错误**  
   256×256 的 PNG 若 UV 按 226 除，背景缩放与槽位坐标系不一致。须在 Screen JSON 声明 `textureWidth/Height: 256`，`srcRect` 与有效内容一致。

### 正确修复顺序

1. **先固定逻辑分区**：控件区 `< 156`（背包区分割线），背包从 `162` 起
2. **右列**：定标题 Y → 槽位 Y（标题下留空）→ 统计 Y（槽位下留空）
3. **再改 Screen JSON 槽位** 与 **PNG 槽位背景**（同一组坐标）
4. **Java 标签 Y** 与 Menu 常量、Screen JSON **成对提交**

### 反模式（勿再犯）

| 反模式 | 后果 |
|--------|------|
| 「PNG 槽在 y=116，所以 JSON 也改 116」 | 忽略 Screen 控件，开关压背包 |
| 只改 Java 标签 Y 不改 JSON / PNG | 文字与槽位背景错位 |
| 只改 PNG 不改 Screen JSON | 点击区域与视觉不一致 |
| 统计 Y 与槽位 Y 相差 < 18px | 右列信息挤在一起 |
| ScreenStyle 未声明 256×256 纹理尺寸 | 背景拉伸 |

---

## 待优化点

### 1. SorterCommandBlock GUI
- 背景由 JSON 指向 `sorter_command_block.png`（440×224）；分析/诊断列表在 `drawBG` 叠加绘制
- 三枚操作按钮使用 `ThemedAE2Button`（`appliedinsight:button` sprite）
- 左侧 `ThemedVerticalButtonBar` + GuideMe；标题栏状态灯在 `drawFG`

### 2. Phase 2（未实施）
- Mixin 包 `com.knightcode.appliedstoragesorter.mixin` 已建空配置
- 目标：`AE2Button` / `VerticalButtonBar` / `Icon` / `BackgroundGenerator` 经 `GuiThemeProvider` 重定向

### 3. 方块纹理（block）
- DAV：`digital_asset_vault` 自有 block 模型（`textures/block/new_dav/` 等）
- SCB：自有 front 纹理 + AE2 drive 基底（临时方案）
- `dark_matter_controller`：未注册，材质测试保留

## 如何协助

1. 打开游戏截图 → 对照本文「新版 DAV」坐标表与验收清单
2. 若错位，先查 **Screen JSON / Menu 常量 / PNG** 三者 Y 是否一致，再查 `background.srcRect` 与纹理尺寸
3. 确定方案后同步修改 Screen JSON + Java 标签 + PNG（`python tools/generate_new_dav_texture.py`），并更新 [`DigitalAssetVaultScreen`](../类职责/client/screen/DigitalAssetVaultScreen.md) 维护备注
