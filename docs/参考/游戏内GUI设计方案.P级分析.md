# 游戏内 GUI 设计方案 — P 级分析

> **生成时间**: 2026-05-28
> **分析基准**: [`docs/参考/游戏内GUI设计方案.md`](游戏内GUI设计方案.md)
> **代码验证**: 基于 `src/` 目录当前代码状态
> **交叉参考**: [`docs/架构/FACTS.md`](../架构/FACTS.md) | [`docs/下一步计划.md`](../下一步计划.md) | [`docs/ai/AI_ENTRY.md`](../ai/AI_ENTRY.md)

---

## 1. 代码现状确认

| 项目 | 代码状态 | 文档描述 | 是否一致 |
|------|---------|---------|---------|
| `SorterCommandBlockScreen` | hopper 纹理，5 个 Button，无状态栏/反馈栏 | §2.1 hopper 占位，无反馈/状态 | ✅ 一致 |
| `DigitalAssetVaultScreen` | 自定义纹理，2×5 cell + MC 槽，无 zone 信息 | §2.2 基础功能，缺 zone 信息/健康提示 | ✅ 一致 |
| `SorterCommandPayload` | 仅 C→S 方向，5 个 buttonId 常量 | §5.2 已有 C→S，缺 S→C | ✅ 一致 |
| `SorterCommandResultPayload` | ❌ 不存在 | §5.2 标识为"需新增" | ✅ 一致 |
| `ClientPlanCache` / `ClientMergeCache` | ❌ 不存在 | §5.3 标识为"新增" | ✅ 一致 |
| `AnalysisResultScreen` / `PlanResultScreen` / `MergeResultScreen` | ❌ 不存在 | §4.3 规划中 | ✅ 一致 |
| 反馈方式 | 全部通过 `player.sendSystemMessage()` (chat) | §2.1/§4.3 描述一致 | ✅ 一致 |

**结论**：文档对当前代码状态的描述完全准确，无过时内容。

---

## 2. P 级总体评估

### 当前分配概览

```
P0 ── A. SorterCommandBlock 增强
   └── B. DAV Zone 信息增强

P1 ── C1. 分析结果查看器
   └── C2. 计划结果查看器

P2 ── C3. Merge 结果查看器

P3 ── D. 配置管理 GUI（Profile/Zone/绑定）
```

### 总体判断：**P 级分配总体合理，仅需微调。**

---

## 3. 逐项分析

### 3.1 A — SorterCommandBlock 增强（P0）✅ **维持 P0**

| 判断维度 | 分析 |
|---------|------|
| **依据** | FACT-041: SorterCommandBlock 是推荐给普通玩家的操作入口。它是玩家使用模组的主要触面。 |
| **用户影响** | 当前无反馈/无状态/hopper 纹理，体验极差。玩家点击按钮只能看到 chat 消息。 |
| **依赖关系** | 是 C1/C2 的基础（结果回传 Payload 也由 P0 建立）。 |
| **与 下一步计划 对齐** | "阶段 1: 完善傻瓜式游戏内交互" 首项。 |
| **结论** | **P0 正确，不应降级。** |

### 3.2 B — DAV Zone 信息增强（P0）⚠️ **可考虑调为 P1，但维持 P0 也合理**

| 判断维度 | 分析 |
|---------|------|
| **依据** | FACT-037~038: DAV 是管理入口。但 FACT-084: DAV 不是架构中心。 |
| **用户影响** | 当前 DAV 已有自定义纹理和槽位正常工作。缺少 zone 信息属于"锦上添花"而非"不能使用"。 |
| **依赖关系** | 依赖管理卡数据的稳定暴露。`DigitalAssetManagementCardItem` 已存在，但 zone 信息查询接口可能需额外工作。 |
| **建议** | ⚠️ **可降为 P1**，因为 DAV 当前已经基本可用（有纹理、有槽位、能操作）。Zone 信息面板是体验增强而不是功能缺失。但如果希望在下一版本给玩家完整的 DAV 体验，维持 P0 也无问题。 |
| **结论** | **维持 P0 或降为 P1 均可接受。建议根据团队资源决定。** |

### 3.3 C1 — 分析结果查看器（P1）✅ **维持 P1**

| 判断维度 | 分析 |
|---------|------|
| **依据** | 数据路径已建立：`SorterAnalysisPayload` + `ClientStorageAnalysisCache` 已在运行。 |
| **代码验证** | `ClientAnalysisPayloadHandler`、`ClientStorageAnalysisCache` 均已存在（[类职责](../类职责/client/ClientAnalysisPayloadHandler.md)）。 |
| **工作量** | 仅需新增 Screen 从缓存读取渲染，无后端改动。 |
| **结论** | **P1 合理**。P0 完成后即可实施。 |

### 3.4 C2 — 计划结果查看器（P1）✅ **维持 P1**

| 判断维度 | 分析 |
|---------|------|
| **依据** | 依赖 `SorterCommandResultPayload`（S→C），该 Payload 由 P0 任务 1.4 提供。 |
| **依赖链** | P0 (1.4) → 创建 Payload → P1 (C2) → 创建 Screen。顺序合理。 |
| **工作量** | 需新建 Payload handler + cache + screen。但 Payload 结构已在 §5.2 设计完成。 |
| **结论** | **P1 合理**。与 P0 有清晰的依赖链，不会阻塞。 |

### 3.5 C3 — Merge 结果查看器（P2）✅ **维持 P2**

| 判断维度 | 分析 |
|---------|------|
| **依据** | Merge 是简化路径（FACT-066~070），不与 profile/zone 主线混用。 |
| **用户影响** | Merge 当前通过 chat 返回结果，玩家可接受。结果查看器是改善而非必要。 |
| **工作量** | 需新增 Payload + cache + screen。 |
| **结论** | **P2 合理**。不应提前。 |

### 3.6 D — 配置管理 GUI（P3）✅ **维持 P3**

| 判断维度 | 分析 |
|---------|------|
| **依据** | 与 下一步计划.md 对齐：HTTP API + H2 数据库的 dashboard 将承载完整配置管理能力。 |
| **分工原则** | §7 明确：游戏内 GUI 适合"日常操作 + 快速状态"，配置管理更适合外部 Dashboard。 |
| **结论** | **P3 合理**。不应提前。远期再议。 |

---

## 4. 调整建议

### 建议 1（推荐）：任务 2.3 从 P1 移至 P0

**原文位置**：§6 阶段 2 → 任务 2.3 "Cell 容量着色（DAV 槽位）"

**理由**：
- Cell 容量着色（绿/黄/红边框）是 DAV 增强（P0-B）**最直观的视觉部分**。Zone 信息面板（P0-B）是文字信息，而 Cell 着色是图形信号，用户一眼就能看到。
- 如果 P0-B 要做 DAV 增强，顺带做 Cell 着色几乎零额外成本（数据来源 `CellCapacityInspector` 已存在）。
- 当前 P0-B 只包含"Zone 信息面板"，而 Cell 着色反而在 P1，逻辑上不连贯。

**修改后**：

```
P0 ── A. SorterCommandBlock 增强
   └── B. DAV Zone 信息增强 + Cell 容量着色

P1 ── C1. 分析结果查看器
   └── C2. 计划结果查看器

P2 ── C3. Merge 结果查看器
   └── (原 P1 任务 2.3 移至 P0)
```

### 建议 2（可选）：P0 新增"按钮执行状态"任务

**理由**：
- §4.1.4 交互流程提到"按钮变灰 '执行中...'"，但 §6 阶段 1 任务列表未包含此任务。
- 这是一个基本的 UX 要求：不给执行中反馈，玩家会以为按钮没反应。

**建议新增**：

```
1.6 按钮执行状态管理（禁用/恢复/进度指示） | 小 | 无
```

---

## 5. 依赖关系图

```
                    ┌──────────────────────────────────┐
                    │      SorterCommandResultPayload   │
                    │      （P0 任务 1.4 新增）           │
                    └──────────┬───────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          ▼                    ▼                     ▼
┌──────────────────┐  ┌─────────────────┐  ┌───────────────┐
│ P0 任务 1.3      │  │ P1 C2           │  │ P2 C3         │
│ 底部反馈栏        │  │ 计划结果查看器    │  │ Merge 结果查看器│
│（复用 Payload）    │  │（新建 Screen）   │  │（新建 Payload+ │
└──────────────────┘  └─────────────────┘  │ Screen）       │
                                            └───────────────┘

P0-B (DAV 增强) ── 独立路径，只需管理卡数据即可 ── 无阻塞
P1 C1 (分析查看器) ── 独立路径，ClientStorageAnalysisCache 已就绪
P3 (配置管理) ── 完全独立，与上列无依赖
```

---

## 6. Modern UI 依赖评估（2026-05-28）

### 6.1 查询结论

Modern UI（[`icyllis/modern-ui`](https://github.com/icyllis/modern-ui)）经调查后判定：**不兼容，不建议引入**。

| 维度 | 结论 |
|------|------|
| Minecraft 1.21.1 支持 | ❌ **不支持**。Modern UI 3.x 最后覆盖至 1.20.4，未发布 1.21.x 适配 |
| NeoForge 支持 | ❌ **不存在**。仅发布 Forge 变体（`ModernUI-Forge`），无 NeoForge artifact |
| Maven 坐标 | `icyllis.modernui:ModernUI-Core:3.10.1` / `ModernUI-Forge:3.10.1`，仓库 `https://maven.izzel.io/releases` |
| 项目当前引用 | 无。`build.gradle` 及全项目文档中无任何 Modern UI 提及 |

### 6.2 "手搓太繁琐"的缓解方案

你的担忧是合理的。Vanilla GUI（`AbstractContainerScreen`）手写确实繁琐。以下是**不引入外部依赖**的实用性缓解方案：

#### 方案 A：只提取 `SorterTextures.java`（推荐，当前阶段唯一需要的）

当前项目仅 2 个 Screen（共 134 行），提取完整 widget 包为时过早。**只做一件事**：

```java
// 新增：client/gui/SorterTextures.java
// 职责：统一管理所有 GUI 纹理引用
public final class SorterTextures {
    public static final ResourceLocation DAV_VAULT = ResourceLocation.parse(
            "appliedinsight:textures/gui/digital_asset_vault.png");
    // P0 时新增
    public static final ResourceLocation COMMAND_BLOCK = ResourceLocation.parse(
            "appliedinsight:textures/gui/sorter_command_block.png");
    // P1 时新增
    public static final ResourceLocation RESULT_VIEWER = ResourceLocation.parse(
            "appliedinsight:textures/gui/result_viewer.png");
    private SorterTextures() {}
}
```

**收益**：改纹理路径只需改一个文件，不会散落在各 Screen 中。

**不做什么**（当前不值得）：
- 不提取 `SorterButton` — 5 个 Button 不值得抽象
- 不提取 `StatusBarWidget` — 还不知道 P0 实现后长什么样
- 不提前建 `widget/` 包 — 违反 AI_ENTRY §1.3"不要为未来猜想引入大框架"

#### 方案 B：等 C1/C2/C3 写完后再提取分页组件

C1/C2/C3 三个结果查看器共享同一个分页列表组件，仅数据源不同：

```java
// 统一的结果查看器，传入不同数据源
new ResultViewerScreen(
    title,
    new AnalysisDataProvider(),   // C1: ClientStorageAnalysisCache
    new PlanDataProvider(),       // C2: ClientPlanCache
    new MergeDataProvider()       // C3: ClientMergeCache
);
```

#### 方案 C：外部 Dashboard 承载复杂 UI

对于表格、规则编辑、历史趋势等真正复杂的 UI，**外部 React Dashboard** 是正确方向（§7 已明确分工），游戏内 GUI 只负责"日常操作 + 快速状态"。

### 6.3 推荐策略

```
P0 ── SorterCommandBlock + DAV：Vanilla 自绘纹理 + 组件工具类 ✅ 零依赖
P1 ── 结果查看器：Vanilla 分页列表组件 ✅ 零依赖
P3 ── 配置管理 GUI → 交由外部 Dashboard 实现（HTTP API + React）
```

---

## 8. 关键风险

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| P0 任务 1.2（状态栏）依赖后端暴露新接口 | 若后端查询接口未准备，状态栏无法实现 | 优先实现最小化查询（网络连接状态 + 绑定的 profile 名） |
| P0-B 管理卡数据 API 未稳定 | DAV Zone 信息面板无法渲染 | 先做实 Cell 容量着色（`CellCapacityInspector` 已就绪），Zone 信息延后 |
| P0 与 P1 边界模糊 | 开发中可能把结果查看器提前到 P0 | 严格按 §4 设计实现，底部栏只显示摘要，详情链接打开 P1 的 Screen |

---

## 9. 最终 P 级排序

```
P0 (当前) ─────────────────────────────────────────────────
   A. SorterCommandBlock 增强
      ├── 1.1 自定义纹理
      ├── 1.2 状态栏（网络状态 + 绑定信息）
      ├── 1.3 底部反馈栏（上次结果摘要）
      ├── 1.4 SorterCommandResultPayload（S→C）
      ├── 1.5 按钮执行状态管理（新增建议） ← 新增
      └── (1.6 原 1.4 为 Payload 纯实现)
   B. DAV Zone 信息增强
      ├── Zone 信息面板
      └── Cell 容量着色（原 P1 任务 2.3，建议移入） ← 调整

P1 (短中期) ──────────────────────────────────────────────
   C1. 分析结果查看器 Screen（数据已就绪）
   C2. 计划结果查看器 Screen（依赖 P0 的 Payload）
   B'. DAV 快捷操作入口（若 P0 未做）

P2 (中期) ────────────────────────────────────────────────
   C3. Merge 结果查看器 Screen

P3 (远期) ────────────────────────────────────────────────
   D. 配置管理 GUI（Profile/Zone/绑定管理器）
      → 建议优先在外部 Dashboard 实现
```

---

## 10. 结论

| 结论 | 说明 |
|------|------|
| **现有 P 级基本合理** | A=P0, C1/C2=P1, C3=P2, D=P3 的分配符合项目架构和用户优先级 |
| **建议调整项** | 1. Cell 容量着色(任务 2.3)从 P1 → P0（与 DAV 增强逻辑连贯）<br>2. 新增"按钮执行状态管理"任务到 P0（基本 UX 需求） |
| **可选调整项** | DAV Zone 信息增强(P0-B)可考虑降为 P1，如果资源紧张 |
| **无架构风险** | 所有 GUI 设计均遵循 FACT-050(FACT-051(FACT-041~042)等架构约束 |
