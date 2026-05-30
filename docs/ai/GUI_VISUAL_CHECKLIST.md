# GUI 视觉待优化清单

> 此文档给多模态模型（如 GPT-4o）使用，用于协助 UI 纹理设计和验收。
> 生成时间: 2026-05-30

## 当前 GUI 资产列表

| 资产 | 路径 | 状态 |
|------|------|------|
| SCB 背景纹理 | `textures/gui/sorter_command_block.png` (176×145) | 已完成（AE2 色系极简风） |
| SCB 按钮 | 无纹理，运行时使用 `ae2:button` sprite | 已完成（AE2Button） |
| DAV 背景纹理 | `textures/gui/digital_asset_vault.png` (256×256) | 已完成（复用 AE2 drive 外观） |

## 待优化点

### 1. SorterCommandBlock GUI
- 当前 5 个按钮使用 AE2 的 `ae2:button` sprite（200×20, nine_slice border=3）
- 背景使用 AE2 色系纯色 `#cbccd4`
- 按钮坐标：x=28, y=20/44/68/92/116, 120×20（面板 176×145）
- **问题**：之前我添加的装饰线有偏移，目前 v2 已全部去掉，只留纯色背景

### 2. DigitalAssetVault GUI
- 当前复用 AE2 drive 纹理，尺寸 256×256
- 分三部分：标题区(y=0~84) + 背包区(y=84~201) + 卡槽区(y=201~223)
- 5 行 cell 槽 + 1 个管理卡槽

### 3. 方块纹理（block）
- DAV 方块：复用 `ae2:block/drive`（AE2 drive 外观）
- SCB 方块：复用 `ae2:block/drive/drive_front`（临时）

## 如何协助

1. 打开游戏截图 → 发给 GPT 看
2. GPT 给出 px 级修正建议
3. 把确定好的方案告诉我 → 我写 Python 脚本生成 + 代码实现
