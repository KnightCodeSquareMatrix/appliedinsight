# SorterTerminalLayout
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/client/screen/SorterTerminalLayout.java`
- **包**: `com.knightcode.appliedstoragesorter.client.screen`
- **类型**: `record`（package-private）
- **所属层**: 客户端展示层

## 职责
SorterCommandBlock **440×224 宽屏终端**的几何布局真相源。`of(leftPos, topPos, guideW, guideH)` 计算：
- 三列内容区（左 120 / 中 140 / 右 148）
- 左列三按钮矩形（分析 / 整理 / 合并）
- GuideMe 竖栏背景与按钮锚点
- 标题与状态灯 X 坐标

## 常量
- `PANEL_WIDTH = 440`，`PANEL_HEIGHT = 224`

## 主要协作者
- `com.knightcode.appliedstoragesorter.client.screen.GuiRect`
- `com.knightcode.appliedstoragesorter.client.screen.SorterCommandBlockScreen`

## 维护备注
- 修改 SCB 布局时**先改本 record**，再改 Screen 绘制与 `sorter_command_block.json`（若槽位/背景相关）。
