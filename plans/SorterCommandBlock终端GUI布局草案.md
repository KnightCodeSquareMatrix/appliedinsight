# Sorter Command Block 终端 GUI 布局草案

> **生成时间**: 2026-05-28
> **阶段**: P0 结构敲定
> **目标**: 先固定游戏内 `SorterCommandBlock` GUI 的信息分区，不提前扩充 analysis 语义，不进入贴图绘制细节

---

## 1. 设计结论

`SorterCommandBlock` 不再定位为“小面板 + 五个按钮”，而是定位为：

> **一个 AE2 风格的宽屏终端（Sorter Terminal）**

核心布局采用：

- **左列：操作入口**
- **中列：存储网络分析总览**
- **右列：分析细节与提示**
- **底栏：操作反馈**

该布局更适合 16:9 屏幕，也符合当前界面不需要为 JEI 预留两侧空间的前提。

---

## 2. 当前代码约束

相关现状：

- [`SorterCommandBlockScreen.java`](../src/main/java/com/knightcode/appliedinsight/client/screen/SorterCommandBlockScreen.java)
  - 当前是 `176x145` 小面板
  - 已有 5 个按钮
  - 当前没有终端式信息分区
- [`SorterCommandPayload.java`](../src/main/java/com/knightcode/appliedinsight/network/SorterCommandPayload.java)
  - 当前已有 5 个操作码
- [`SorterStorageAnalysisService.java`](../src/main/java/com/knightcode/appliedinsight/application/SorterStorageAnalysisService.java)
  - 已能生成 analysis report
  - 已能把报告推送到客户端
- [`SorterAnalysisPayloadHandler.java`](../src/main/java/com/knightcode/appliedinsight/network/SorterAnalysisPayloadHandler.java)
  - 客户端已有 analysis 缓存
- [`StorageAnalyzerReport.java`](../src/main/java/com/knightcode/appliedinsight/ae2/analysis/StorageAnalyzerReport.java)
  - 已定义 summary / health flags / fragmented items / largest storages / suspected semantic candidates 等结构

关键判断：

> **当前 GUI 的中列和右列，应直接消费现有 analysis 数据模型，而不是为了填界面去新增后端分析字段。**

---

## 3. 布局总图

```text
┌──────────────────────────────────────────────────────────────────────────────────────┐
│ Sorter Terminal                                        [ME 在线]               [?]  │
├───────────────┬────────────────────────────────┬────────────────────────────────────┤
│ 操作面板      │ 存储网络分析                   │ 分析细节 / 提示                     │
│               │                                │                                    │
│ [扫描网络]    │ 网络概览                       │ 健康标志                           │
│ [存储分析]    │ - 节点数                       │ - health flags                     │
│               │ - 存储位置数                   │                                    │
│ [生成计划]    │ - 非空/内部/外部               │ 疑似语义节点                       │
│ [规划并搬运]  │ - 唯一物品数                   │ - suspected semantic candidates    │
│               │ - 总物量                       │                                    │
│ [整理合并]    │ - 碎片等级                     │ Top 列表                           │
│               │                                │ - largest storages                 │
│ [打开指南]    │                                │ - most fragmented items            │
│               │                                │                                    │
├───────────────┴────────────────────────────────┴────────────────────────────────────┤
│ 操作反馈：当前动作 / 最近结果 / 错误提示 / 简短状态消息                             │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. 左列：操作入口

左列只承担“动作入口”，不承担分析语义。

### 4.1 分组

#### 网络观测
- 扫描网络
- 存储分析

#### 规划执行
- 生成计划
- 规划并搬运

#### 快速整理
- 整理合并

#### 帮助入口
- 打开指南

### 4.2 对应现有代码

| UI 按钮 | 操作码 / 行为 |
|--------|---------------|
| 扫描网络 | `SorterCommandPayload.CMD_ME_DUMP` |
| 存储分析 | `SorterCommandPayload.CMD_ME_STORAGE_DUMP` |
| 生成计划 | `SorterCommandPayload.CMD_ME_PLAN` |
| 规划并搬运 | `SorterCommandPayload.CMD_ME_PLAN_AND_MOVE` |
| 整理合并 | `SorterCommandPayload.CMD_MERGE` |
| 打开指南 | `OpenGuideButton` |

### 4.3 设计原则

- 左列保持克制，不塞额外统计
- 每个按钮都应有清晰分区和用途
- 第一版不新增更多按钮，避免终端失焦

---

## 5. 中列：存储网络分析总览

中列是终端的**主视图**，只放 overview，不放长列表。

### 5.1 数据来源

直接读取 `StorageAnalyzerReport.summary()`。

### 5.2 建议展示字段

- 网络节点数 `networkNodeCount`
- 存储位置数 `storageLocationCount`
- 非空存储数 `nonEmptyStorageLocationCount`
- 内部存储数 `internalStorageLocationCount`
- 外部存储数 `externalStorageLocationCount`
- 唯一物品数 `uniqueKeyCount`
- 重复键数 `duplicatedKeyCount`
- 总物量 `totalAmount`
- 碎片分数 `fragmentationScore`
- 碎片等级 `fragmentationLevel`

### 5.3 中列定位

中列回答的问题是：

- 这个网络大不大？
- 存储结构偏内部还是偏外部？
- 物品分布是否健康？
- 当前是否存在明显碎片化问题？

也就是说：

> **中列负责“一眼看全局”。**

---

## 6. 右列：分析细节与提示

右列是中列的补充解释区，不承担主 overview。

### 6.1 第一优先级：健康标志

数据来源：`StorageAnalyzerReport.healthFlags()`

用途：
- 显示当前网络的重要健康提示
- 作为玩家是否要进一步治理的第一判断点

### 6.2 第二优先级：疑似语义节点

数据来源：`StorageAnalyzerReport.suspectedSemanticCandidates()`

用途：
- 展示疑似无限容器等高价值候选
- 为后续 dashboard / semantic review 铺路

### 6.3 第三优先级：Top 列表

数据来源：
- `largestStorages()`
- `mostFragmentedItems()`

用途：
- 帮助玩家快速理解“哪里最大”
- 帮助玩家快速理解“什么最碎”

### 6.4 第一版控量原则

为了防止右列过满，第一版建议：

- `healthFlags`：显示前 3 条
- `suspectedSemanticCandidates`：显示前 2~3 条
- `largestStorages`：显示前 2~3 条
- `mostFragmentedItems`：显示前 2~3 条

结论：

> **右列是轻量解释区，不是完整 dashboard。**

---

## 7. 底栏：操作反馈

底栏只放短反馈，不抢 analysis 主体位置。

### 7.1 第一版职责

- 当前动作
- 最近一次操作状态
- 错误提示
- 简短说明文字

### 7.2 当前代码现实

当前 `SorterCommandPayloadHandler` 会把结果通过 `sendSystemMessage` 发到聊天栏，尚未建立专门的 GUI 结构化反馈缓存。

因此第一版底栏应保守处理：

- 点击按钮后显示本地“已发送请求”
- 若收到 analysis 数据，显示“分析已更新”
- 对在线状态、失败状态做单行反馈

### 7.3 后续增强

后续如需要把 `plan/merge/dump` 的完整多行反馈显示到 GUI 中，再单独补：

- Screen 专用反馈 payload
- 或客户端反馈缓存

当前不作为 P0 前提。

---

## 8. 与 analysis 的边界

当前阶段有一个重要约束：

> **不为了 GUI 填内容而扩充 analysis。**

原因：

- analysis 日后会成为前端和游戏内分析界面的共同消费对象
- 具体还要分析什么，应由玩家反馈逐步驱动
- 现在先把已有 analysis 字段读出来、看懂、展示好，比提前发明更多指标更重要

因此当前 GUI 只围绕现有字段：

- `summary`
- `healthFlags`
- `largestStorages`
- `mostFragmentedItems`
- `suspectedSemanticCandidates`

必要时可以晚一些再接：

- `mixedInternalExternalItems`
- 更细的 item distribution 视图

---

## 9. 视觉方向约束

虽然本文不进入正式 GUI 绘制，但视觉方向已明确：

- 保持 AE2 风格
- 做成“新终端”，不是普通容器小窗
- 利用宽屏横向空间
- 不为 JEI 预留左右空白
- 强调分区、终端感、信息秩序感

不做的事：

- 不做偏现代网页的纯扁平 UI
- 不做大面积高饱和配色
- 不在第一版加入过多动态可视化

---

## 10. 当前敲定项

### 已敲定

- `SorterCommandBlock` GUI 定位为宽屏终端
- 主体采用三列布局
- 左列为操作入口
- 中列为 analysis summary 总览
- 右列为 analysis 细节与提示
- 底栏为短反馈
- 第一版不扩充 analysis 语义

### 暂未敲定

- 最终像素尺寸
- 每列精确宽度
- 每个分析块的标题与文案
- 是否加入小图标/状态灯
- 贴图绘制细节

---

## 11. 下一步落地范围

进入实现时，优先顺序应为：

1. 调整 `SorterCommandBlockScreen` 的宽高与分区坐标
2. 先把三列文本结构画出来
3. 接入 `ClientStorageAnalysisCache` 的 analysis 数据展示
4. 最后再做 AE2 风格贴图细化

这保证第一版先验证信息架构，再投入绘制成本。
