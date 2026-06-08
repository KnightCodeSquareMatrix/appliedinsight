# SmartBusScreen
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/client/screen/SmartBusScreen.java`
- **包**: `com.knightcode.appliedstoragesorter.client.screen`
- **类型**: `class`
- **所属层**: 客户端展示层

## 职责
智能总线 GUI（400×280 大面板）：
- 顶栏：模式名 + 切换模式 / 打开网页编辑器
- 三枚**预设**按钮（`SmartBusFilterPresets`）
- **MultiLineEditBox**（360×166）编辑 filter JSON
- 实时摘要 + 校验反馈；粘贴/复制/保存/清除
- 发包：`SmartBusModePayload`、`SmartBusFilterPayload`

## 边界检查
边界健康。filter 解析复用 `FilterExpressionJsonCodec`；业务匹配在服务端 Part 执行。

## Screen 样式
- JSON：`assets/appliedinsight/screens/smart_bus.json`
- 背景：`textures/gui/smart_bus.png`
- 布局契约：[`GUI_VISUAL_CHECKLIST.md`](../../../ai/GUI_VISUAL_CHECKLIST.md) Smart Bus 章节

## 主要协作者
- `com.knightcode.appliedstoragesorter.menu.SmartBusMenu`
- `com.knightcode.appliedstoragesorter.client.filter.FilterEditorSupport`
- `com.knightcode.appliedstoragesorter.client.gui.SorterBaseScreen`

## 维护备注
- `inventoryLabelY = 10000` 隐藏原版物品栏标签（本 GUI 无玩家背包槽）。
