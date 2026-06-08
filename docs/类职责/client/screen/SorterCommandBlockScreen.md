# SorterCommandBlockScreen

> **最后更新**: 2026-05-30

| 维度 | 说明 |
|------|------|
| **包路径** | `client/screen/SorterCommandBlockScreen.java` |
| **定位** | 命令执行方块 GUI 屏幕 |
| **职责** | 渲染宽屏三栏终端：左列 3 个命令按钮（分析当前网络、以绑定配置整理、碎片物品合并）+ 顶栏 GuideME 帮助按钮；中列展示 3 组存储概览（存储节点 / 内部存储 / 外部存储）；右列展示 health flags / semantic candidates / top 列表；底栏展示短反馈 |
| **关键方法** | `init()` 中通过 `SorterTerminalLayout` 放置 3 个 `AE2Button` 和 `OpenGuideButton`；`renderBg()` 复用同一份布局对象绘制终端面板与三列分区；`renderAnalysisPanels()` 聚合 `StorageAnalyzerReport` 为概览区块与诊断列表；`containerTick()` 监听新的 analysis JSON 到达并刷新底栏反馈；`renderLabels()` 使用菜单同步过来的 `nodeOnlineSnapshot` 渲染顶栏状态 |
| **依赖** | `SorterCommandBlockMenu`、`SorterCommandPayload`、`PacketDistributor`、`AE2Button`、`OpenGuideButton`、`GuideItem`、`GuidesCommon`、`PageAnchor`、`CompactNumberFormatter` |
| **被谁使用** | 客户端注册（`SorterClientRegistrations`） |
| **纹理** | 当前实现为代码绘制的 `440×224` 宽屏终端；仓库中的 `textures/gui/sorter_command_block.png` 属于旧版小面板资产，未被当前屏幕实现消费 |
| **边界** | 纯客户端类，不包含任何服务端命令逻辑；仅负责 UI 渲染、已同步状态展示与事件派发；GuideME 集成使用 AE2 的 `OpenGuideButton` 和 `GuidesCommon.openGuide()` API |
