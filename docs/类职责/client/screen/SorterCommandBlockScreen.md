# SorterCommandBlockScreen

> **最后更新**: 2026-05-29

| 维度 | 说明 |
|------|------|
| **包路径** | `client/screen/SorterCommandBlockScreen.java` |
| **定位** | 命令执行方块 GUI 屏幕 |
| **职责** | 渲染 5 个命令按钮（ME 扫描网络、存储分析、生成计划、规划并搬运、整理合并）+ 1 个 GuideME "?" 帮助按钮，点击后通过 `SorterCommandPayload` 发送到服务端或打开 GuideME 教程 |
| **关键方法** | `init()` 中使用 5 个 `AE2Button`（`appeng.client.gui.widgets.AE2Button`），以绝对坐标（`leftPos+28, topPos+20/44/68/92/116`）布局，每个绑定对应的 `SorterCommandPayload` 常量；另加 1 个 `OpenGuideButton` 在右上角，点击后通过 `GuidesCommon.openGuide()` 打开教程首页 |
| **依赖** | `SorterCommandBlockMenu`、`SorterCommandPayload`、`PacketDistributor`、`AE2Button`、`OpenGuideButton`、`GuideItem`、`GuidesCommon`、`PageAnchor` |
| **被谁使用** | 客户端注册（`SorterClientRegistrations`） |
| **纹理** | `appliedstoragesorter:textures/gui/sorter_command_block.png`（176×145 自定义纹理） |
| **边界** | 纯客户端类，不包含任何服务端逻辑；仅负责 UI 渲染与事件派发；GuideME 集成使用 AE2 的 `OpenGuideButton`（`Icon.HELP`）和 `GuidesCommon.openGuide()` API |
