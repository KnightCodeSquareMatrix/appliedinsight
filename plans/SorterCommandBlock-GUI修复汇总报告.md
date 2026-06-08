# SorterCommandBlock GUI 修复汇总报告

> 生成时间: 2026-05-30
> 范围: 本次 `SorterCommandBlock` 终端 GUI 修复

## 1. 背景

本次修复基于根目录截图 `无标题1.png` 暴露出的几个直接问题：

1. 标题显示为翻译 key：`block.appliedinsight.sorter_command_block`
2. 右上角状态显示为 `ME 离线`，但该 GUI 实际只有在节点在线时才能打开
3. 左列最后一个按钮 `整理合并` 掉入底栏，布局越界
4. 右侧占位说明仍混有临时英文稿，GUI 文案未完整接入本地化
5. 文档仍描述旧版 `176x145` 纹理面板，与当前宽屏终端实现脱节

---

## 2. 根因分析

### 2.1 在线状态来源错误

- 服务端在 [`SorterCommandBlockEntity.openMenu()`](../src/main/java/com/knightcode/appliedinsight/blockentity/SorterCommandBlockEntity.java) 中只有在 `isNodeOnline()` 为真时才允许打开菜单
- 但客户端界面在 [`SorterCommandBlockScreen`](../src/main/java/com/knightcode/appliedinsight/client/screen/SorterCommandBlockScreen.java) 中直接读取客户端方块实体的 `isNodeOnline()`
- 菜单客户端实例是按 `BlockPos` 从客户端世界重建的，不应拿它作为服务端权威状态

结论：

> GUI 顶栏状态缺少服务端同步快照，导致“能打开 GUI 但显示离线”的假状态。

### 2.2 左列布局仍是草稿坐标

- 按钮布局使用 `buttonY + buttonStep * n` 的硬编码方式
- `整理合并` 使用了过大的纵向偏移，直接压进了底栏区域

结论：

> 按钮坐标没有按分组锚点布局，导致左列下部越界。

### 2.3 标题与文案兜底不足

- 屏幕直接消费上游传入的 `title`
- 文案大量使用 `Component.literal(...)`
- 右侧提示区仍保留临时混合文案

结论：

> 当前终端 GUI 缺少完整的 screen 级本地化和显示兜底。

### 2.4 文档与实现发生漂移

- 当前代码已是 `352x196` 的运行时代码绘制终端
- 相关文档仍描述旧版 `176x145` GUI 纹理方案

结论：

> 视觉验收和后续维护会被旧文档误导。

---

## 3. 本次修改内容

### 3.1 菜单同步服务端在线状态

修改文件：

- [`src/main/java/com/knightcode/appliedinsight/menu/SorterCommandBlockMenu.java`](../src/main/java/com/knightcode/appliedinsight/menu/SorterCommandBlockMenu.java)
- [`src/main/java/com/knightcode/appliedinsight/blockentity/SorterCommandBlockEntity.java`](../src/main/java/com/knightcode/appliedinsight/blockentity/SorterCommandBlockEntity.java)
- [`src/main/java/com/knightcode/appliedinsight/registry/SorterMenus.java`](../src/main/java/com/knightcode/appliedinsight/registry/SorterMenus.java)

修改点：

1. 在 `SorterCommandBlockMenu` 中新增 `nodeOnlineSnapshot`
2. `openMenu()` 写入 `BlockPos + boolean`
3. `SorterMenus` 客户端菜单工厂改为读取 `BlockPos + boolean`
4. Screen 顶栏状态改为消费 `menu.isNodeOnlineSnapshot()`

效果：

- 修复右上角状态与实际可打开条件不一致的问题
- 避免客户端直接依赖本地方块实体的瞬时状态

### 3.2 重排左列按钮坐标

修改文件：

- [`src/main/java/com/knightcode/appliedinsight/client/screen/SorterCommandBlockScreen.java`](../src/main/java/com/knightcode/appliedinsight/client/screen/SorterCommandBlockScreen.java)

修改点：

1. 拆分为 `observationButtonY`、`planningButtonY`、`mergeButtonY`
2. `整理合并` 按钮改为单独锚定到“快速整理”分组
3. `OpenGuideButton` 改为显式定位到右上角

效果：

- 修复按钮掉入底栏的问题
- 左列分区与终端布局草案保持一致

### 3.3 补全 Screen 级本地化

修改文件：

- [`src/main/resources/assets/appliedinsight/lang/zh_cn.json`](../src/main/resources/assets/appliedinsight/lang/zh_cn.json)
- [`src/main/resources/assets/appliedinsight/lang/en_us.json`](../src/main/resources/assets/appliedinsight/lang/en_us.json)
- [`src/main/java/com/knightcode/appliedinsight/client/screen/SorterCommandBlockScreen.java`](../src/main/java/com/knightcode/appliedinsight/client/screen/SorterCommandBlockScreen.java)

修改点：

1. 为 section、group、button、status、feedback、analysis、details、empty state 增加独立 lang key
2. 将原先大量 `Component.literal(...)` 的固定 GUI 文案切换为 `Component.translatable(...)`
3. Screen 标题改为 `SCREEN_TITLE` 兜底渲染

效果：

- 修复标题显示翻译 key 的问题
- GUI 文案不再依赖硬编码中文
- 中英文资源齐全，后续扩展更稳定

### 3.4 优化右侧文本换行与反馈栏

修改文件：

- [`src/main/java/com/knightcode/appliedinsight/client/screen/SorterCommandBlockScreen.java`](../src/main/java/com/knightcode/appliedinsight/client/screen/SorterCommandBlockScreen.java)

修改点：

1. 新增 `drawWrappedText()`，统一处理列表和提示文本换行
2. 底栏反馈改为 `Component + FeedbackTone`
3. 在 `containerTick()` 中监听新的 analysis JSON 到达，自动将底栏反馈切换为“分析结果已更新”

效果：

- 右列文本不会再按固定 20px 粗暴推进
- 底栏反馈语义更清晰
- “存储分析”按钮触发后能给出更准确的本地 UI 反馈

### 3.5 修正文档漂移

修改文件：

- [`docs/类职责/client/screen/SorterCommandBlockScreen.md`](../docs/类职责/client/screen/SorterCommandBlockScreen.md)
- [`docs/类职责/block/SorterCommandBlock.md`](../docs/类职责/block/SorterCommandBlock.md)
- [`docs/ai/GUI_VISUAL_CHECKLIST.md`](../docs/ai/GUI_VISUAL_CHECKLIST.md)

修改点：

1. 将 `SorterCommandBlockScreen` 文档更新为当前三栏终端实现
2. 明确 `sorter_command_block.png` 为旧版小面板资产，当前未被该 screen 消费
3. 让视觉检查文档以当前 `352x196` 代码绘制终端为准

效果：

- 降低后续设计、验收和 AI 协作时的误判成本

---

## 4. 涉及文件清单

### 代码

- `src/main/java/com/knightcode/appliedinsight/client/screen/SorterCommandBlockScreen.java`
- `src/main/java/com/knightcode/appliedinsight/menu/SorterCommandBlockMenu.java`
- `src/main/java/com/knightcode/appliedinsight/blockentity/SorterCommandBlockEntity.java`
- `src/main/java/com/knightcode/appliedinsight/registry/SorterMenus.java`

### 本地化资源

- `src/main/resources/assets/appliedinsight/lang/zh_cn.json`
- `src/main/resources/assets/appliedinsight/lang/en_us.json`

### 文档

- `docs/类职责/client/screen/SorterCommandBlockScreen.md`
- `docs/类职责/block/SorterCommandBlock.md`
- `docs/ai/GUI_VISUAL_CHECKLIST.md`

---

## 5. 验证结果

已执行：

```powershell
.\gradlew.bat compileJava
```

结果：

- `BUILD SUCCESSFUL`

说明：

- 当前已确认编译通过
- 尚未实际启动 Minecraft 客户端进行最终视觉验收

---

## 6. 当前仍保留的事项

1. 根目录旧截图对应的问题已从代码层修复，但仍需游戏内复看确认最终像素效果
2. `src/main/resources/assets/appliedinsight/textures/gui/sorter_command_block.png` 仍保留在仓库中，当前未删除；它现在是旧版资产，不是当前 screen 的实际背景来源
3. 如果后续决定重新回到纹理方案，需要单独做“代码绘制终端”与“纹理 GUI”二选一收口，避免再次出现双轨描述

---

## 7. 一句话总结

> 这次修改的核心不是“调了几个坐标”，而是把 `SorterCommandBlock` GUI 从一个状态来源不可靠、布局仍带草稿痕迹、文案未完整本地化的半成品，收口成了一个服务端状态可同步、布局分区明确、文案可维护、文档与实现基本一致的宽屏终端界面。
