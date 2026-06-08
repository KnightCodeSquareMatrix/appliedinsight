<p align="center">
  <img src="docs/assets/logo.png" alt="Applied Energistics: Insight" width="128" height="128"/>
</p>

<h1 align="center">Applied Energistics: Insight</h1>
<h3 align="center">应用能源：洞察</h3>

<p align="center">
  <b>See · converge · organize your ME network — without stacking more drives</b><br/>
  <b>看清 · 收敛 · 整理 ME 网络 — 不必无限加盘片</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-blue?logo=minecraft" alt="Minecraft 1.21.1"/>
  <img src="https://img.shields.io/badge/NeoForge-21.1.224-orange?logo=eclipseide" alt="NeoForge 21.1.224"/>
  <img src="https://img.shields.io/badge/AE2-19.2.17-brightgreen" alt="AE2 19.2.17"/>
  <img src="https://img.shields.io/badge/Java-21-red?logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/license-ARR-lightgrey" alt="License: ARR"/>
  <img src="https://img.shields.io/badge/version-0.9.2-yellow" alt="Version 0.9.2"/>
</p>

<p align="center">
  <a href="#中文">中文</a> · <a href="#english">English</a>
</p>

---

## 中文

> **30 秒速览**
>
> **应用能源：洞察**（Applied Energistics: Insight，社区常称 **AE 洞察**）面向 **ME 网络已经大到肉眼管不住** 的阶段 — 物品散满盘片、种类槽告急、开终端开始卡。
>
> - **数字资产库（DAV）** — 把分散库存收成 **一个存储后端**；自带 **2k 字节 / 126 种**，可吸收 Cell 自动扩容，并常能 **减轻 ME 终端打开时的枚举压力**
> - **智能总线（Smart Bus）** — JSON 过滤器 + 游戏内预设 + 离线网页编辑器，可解释、可编辑，精确控制进出
> - **命令执行块** — **分析 → 合并 →（可选）分区整理** 一键闭环，不用记命令
>
> **推荐循环：** 存储分析 → 整理合并 → 迁入 DAV → 网络再长大时重复

Mod ID: `appliedinsight` · MC 1.21.1 · NeoForge 21.1.224 · AE2 19.2.17

### 数字资产库 — 吸收 Cell，自动扩容

DAV 不是一格一格插 Cell 的驱动器，而是**放置即可用的集中存储池**（内置相当于两张空 1k Cell：**2048 字节 / 126 种**），并可将**空的 AE2 Storage Cell 吸收掉**，把 Cell 的字节上限和物品种类上限**叠加进同一池子**。

把散落物品收进 DAV，等于用 **一个 `MEStorage`** 替代大量盘片槽位（和慢速外部总线）的分别枚举 — 在繁忙网络上，**ME 终端打开往往更快**。

| 能力 | 说明 |
|------|------|
| **内置初始容量** | 无需先吸收 Cell；每个 DAV 固定提供 2048 字节与 126 种物品上限 |
| **Cell 吸收聚合** | 放入空 Storage Cell → Cell 消失，在基础容量之上继续叠加。多个 Cell 合成一个「大仓库」 |
| **导入现有库存** | 刚接入网络时，周期性把网络里散落的物品迁入 DAV |
| **自动接收后续物品** | 开启后 DAV 作为优先存储目标，新货主动进入 DAV |
| **自动扩容** | 字节或种类占用达到阈值（默认 80%）时，自动从网络提取空 Cell **或** 向 ME 自动合成提交任务，成品 Cell 吸收进 DAV |

**推荐流程：** 接入 ME 网络 → 开启自动接收 → （可选）导入库存 → （可选）吸收 Cell 进一步扩容 → 在扩容槽指定 Cell 类型并确认样板可用 → 开启自动扩容。之后容量紧张时 DAV 会自行扩容，无需反复手工造 Cell。

```
  [空 Cell] ──吸收──▶ [ DAV 存储池 ] ◀── 网络物品迁入
                           │
                     80% 阈值触发
                           ▼
              提取空 Cell / ME 自动合成 → 再吸收
```

> 详细说明见 [`guidebook/digital-asset-vault.md`](guidebook/digital-asset-vault.md) · 游戏内 GuideMe 同名章节

### 智能总线 — JSON 过滤器，想搬什么搬什么

Smart Bus 是 AE2 线缆部件，在输入/输出总线基础上增加 **FilterExpression JSON 过滤器**。三种模式：**未配置** / **输入**（从背面库存拉货进网络）/ **输出**（从网络推货到背面）。Shift+右键或 GUI 切换。

**为什么比堆升级卡更灵活**

- **任意组合条件** — 按物品 ID、模组 ID、标签、耐久、数量等字段组合 `AND` / `OR`，支持嵌套分组
- **游戏内快速预设** — 「所有物品」「有耐久」「矿石」一键写入
- **离线网页编辑器** — 可视化建规则，复制 JSON 回游戏粘贴保存（`tools/filter-editor/`，无需联网）
- **高吞吐** — 默认每 tick 最多 12 组满 stack 传输（可在 `config/appliedinsight-server.toml` 调整）

**最小过滤器示例** — 只拉取 `ae2` 模组物品：

```json
{
  "combinator": "AND",
  "rules": [
    { "field": "MOD_ID", "operator": "EQUALS", "value": "ae2" }
  ]
}
```

**推荐工作流：** 打开 Smart Bus GUI → **网页编辑** → 在浏览器建规则 → **Copy for Game** → 回游戏 **粘贴** → **保存**。

> 详细说明见 [`guidebook/smart-bus.md`](guidebook/smart-bus.md)

### 命令执行块 — 分析 · 合并 · 整理

右键打开终端 GUI，无需记命令：

| 按钮 | 作用 |
|------|------|
| **存储分析** | 网络健康诊断：内外部存储分布、碎片化、DAV 容量概览 |
| **整理合并** | 对齐 AE2 语义：**cell→cell**、**cell→external**；外部存储总线**不作**合并源（不从抽屉/箱子拉货） |
| **规划并搬运** | （服主）按 `config/appliedinsight/profiles/` 路由配置分区整理 |

与 DAV 配合：合并减少碎片 → DAV 腾出种类空间 → 盘片更少、终端更轻快；分析界面单独标注 DAV，不参与传统 Drive 单元格统计。

> 详见 [`guidebook/sorter-command-block.md`](guidebook/sorter-command-block.md)

### 快速开始

**安装**

1. 从 [Releases](https://github.com/KnightCodeSquareMatrix/appliedinsight/releases) 下载 `appliedinsight-0.9.2.jar`
2. 放入实例 `mods/` 目录（需已安装 AE2）
3. 进游戏合成 **数字资产库**、**智能总线**、**命令执行块**，接入 ME 网络即可

**从源码构建**

```bash
git clone https://github.com/KnightCodeSquareMatrix/appliedinsight.git
cd appliedinsight
./gradlew build
./gradlew runClient
```

产物位于 `build/libs/`。

**玩家指南**

| 文档 | 内容 |
|------|------|
| [`guidebook/getting-started.md`](guidebook/getting-started.md) | 分析 → 合并 → 接入 DAV 推荐流程 |
| [`guidebook/digital-asset-vault.md`](guidebook/digital-asset-vault.md) | DAV 吸收规则、三开关、自动扩容 |
| [`guidebook/smart-bus.md`](guidebook/smart-bus.md) | 过滤器语法、网页编辑器、预设 |
| [`guidebook/sorter-command-block.md`](guidebook/sorter-command-block.md) | 终端 GUI 三栏布局 |
| [`CHANGELOG.md`](CHANGELOG.md) | 版本变更与迁移说明 |

**开发者**

| 文档 | 说明 |
|------|------|
| [`docs/参考/DEVELOPER_QUICKSTART.md`](docs/参考/DEVELOPER_QUICKSTART.md) | 环境搭建与构建 |
| [`docs/架构/FACTS.md`](docs/架构/FACTS.md) | 权威架构事实库 |
| [`docs/参考/COMMANDS_REFERENCE.md`](docs/参考/COMMANDS_REFERENCE.md) | 完整命令与配置 |
| [`docs/ai/AI_ENTRY.md`](docs/ai/AI_ENTRY.md) | AI 协作者入口 |

离线分析工具链（dump 分析、路由建议、profile 生成等）见 [`docs/参考/DEVELOPER_GUIDE.md`](docs/参考/DEVELOPER_GUIDE.md)。

**技术栈**

| 技术 | 版本 |
|------|------|
| Minecraft + NeoForge | 1.21.1 / 21.1.224 |
| Applied Energistics 2 | 19.2.17 |
| Java | 21 |

可选客户端集成：JEI、EMI（DAV 扩容 Cell 拖拽、Smart Bus 过滤器编辑）。

**许可证：** All Rights Reserved.

---

## English

> **30-second overview**
>
> **Applied Energistics: Insight** (Mod ID `appliedinsight`, often called **AE Insight**) is for when your ME network is **too big to eyeball** — scattered stacks, full type slots, and sluggish terminal opens.
>
> - **Digital Asset Vault (DAV)** — Consolidates stock into **one storage backend** with **2k bytes / 126 types** built in, cell absorption, auto-expand — and often **reduces ME terminal enumeration cost** on busy grids
> - **Smart Bus** — JSON filters with in-game presets and an offline web editor — explainable, editable import/export control
> - **Sorter Command Block** — **Analyze → merge → (optional) zone sort** in one GUI, no commands to memorize
>
> **Recommended loop:** Storage Analysis → Merge → move stock into DAV → repeat as the network grows.

Mod ID: `appliedinsight` · MC 1.21.1 · NeoForge 21.1.224 · AE2 19.2.17

### Digital Asset Vault — absorb cells, auto-expand

DAV is not a slot-by-slot drive. It is a **ready-to-use centralized storage pool** (built-in baseline equivalent to two empty 1k cells: **2048 bytes / 126 types**) that can **absorb empty AE2 Storage Cells** and **stack their byte and type limits into one pool**.

Pulling scattered items into DAV replaces many per-slot `getAvailableStacks()` calls with **one** ME backend — on large networks, **ME terminals often open faster**.

| Capability | Description |
|------------|-------------|
| **Built-in baseline** | No cell required to start; every DAV provides 2048 bytes and 126 item types |
| **Cell absorption** | Insert an empty Storage Cell → the cell is consumed and its limits are added on top of the baseline |
| **Import existing stock** | When first connected, periodically pulls scattered network items into the DAV |
| **Auto-accept new items** | When enabled, the DAV becomes a preferred storage target for incoming items |
| **Auto-expand** | At the usage threshold (default 80%), pulls empty cells from the network **or** submits ME autocrafting jobs; finished cells are absorbed into the DAV |

**Recommended workflow:** Connect to the ME network → enable auto-accept → (optional) import existing stock → (optional) absorb cells for more capacity → set expansion cell type and confirm patterns → enable auto-expand. After that, the DAV grows itself when space gets tight.

```
  [Empty Cell] ──absorb──▶ [ DAV pool ] ◀── items from network
                                │
                          80% threshold
                                ▼
                 pull empty cell / ME autocraft → absorb again
```

> See [`guidebook/digital-asset-vault.md`](guidebook/digital-asset-vault.md) · in-game GuideMe chapter with the same name

### Smart Bus — JSON filters, move exactly what you want

Smart Bus is an AE2 cable part that adds a **FilterExpression JSON filter** on top of import/export buses. Three modes: **unconfigured** / **import** (pull from the back-side inventory into the network) / **export** (push from the network to the back side). Toggle with Shift+right-click or the GUI.

**Why it is more flexible than stacking upgrade cards**

- **Composable conditions** — Combine item ID, mod ID, tags, durability, stack size, and more with `AND` / `OR` and nested groups
- **In-game presets** — One-click templates for "all items", "has durability", "ores"
- **Offline web editor** — Build rules visually, copy JSON back into the game (`tools/filter-editor/`, no internet required)
- **High throughput** — Up to 12 full stacks per tick by default (configurable in `config/appliedinsight-server.toml`)

**Minimal filter example** — import only items from the `ae2` mod:

```json
{
  "combinator": "AND",
  "rules": [
    { "field": "MOD_ID", "operator": "EQUALS", "value": "ae2" }
  ]
}
```

**Recommended workflow:** Open Smart Bus GUI → **Web Editor** → build rules in the browser → **Copy for Game** → **Paste** in-game → **Save**.

> See [`guidebook/smart-bus.md`](guidebook/smart-bus.md)

### Sorter Command Block — analyze · merge · sort

Right-click to open the terminal GUI — no commands to memorize:

| Button | Action |
|--------|--------|
| **Storage Analysis** | Network health report: internal/external storage split, fragmentation, DAV capacity overview |
| **Merge** | AE2-aligned: **cell → cell** and **cell → external** only; external storage buses are **never** merge sources |
| **Plan & Move** | (Server operators) Zone-based sorting driven by `config/appliedinsight/profiles/` routing profiles |

Works well with DAV: merge cuts fragmentation → DAV regains type headroom → fewer scattered cells → snappier terminals. The analysis UI labels DAV separately and does not count it as a traditional drive cell.

> See [`guidebook/sorter-command-block.md`](guidebook/sorter-command-block.md)

### Quick start

**Install**

1. Download `appliedinsight-0.9.2.jar` from [Releases](https://github.com/KnightCodeSquareMatrix/appliedinsight/releases)
2. Drop it into your instance `mods/` folder (AE2 required)
3. Craft the **Digital Asset Vault**, **Smart Bus**, and **Sorter Command Block**, then connect them to your ME network

**Build from source**

```bash
git clone https://github.com/KnightCodeSquareMatrix/appliedinsight.git
cd appliedinsight
./gradlew build
./gradlew runClient
```

Output JARs are in `build/libs/`.

**Player guides**

| Document | Contents |
|----------|----------|
| [`guidebook/getting-started.md`](guidebook/getting-started.md) | Recommended analyze → merge → DAV workflow |
| [`guidebook/digital-asset-vault.md`](guidebook/digital-asset-vault.md) | DAV absorption rules, toggles, auto-expand |
| [`guidebook/smart-bus.md`](guidebook/smart-bus.md) | Filter syntax, web editor, presets |
| [`guidebook/sorter-command-block.md`](guidebook/sorter-command-block.md) | Terminal GUI layout |
| [`CHANGELOG.md`](CHANGELOG.md) | Version history and migration notes |

**For developers**

| Document | Description |
|----------|-------------|
| [`docs/参考/DEVELOPER_QUICKSTART.md`](docs/参考/DEVELOPER_QUICKSTART.md) | Environment setup and build |
| [`docs/架构/FACTS.md`](docs/架构/FACTS.md) | Authoritative architecture fact sheet |
| [`docs/参考/COMMANDS_REFERENCE.md`](docs/参考/COMMANDS_REFERENCE.md) | Full command and config reference |
| [`docs/ai/AI_ENTRY.md`](docs/ai/AI_ENTRY.md) | Entry point for AI collaborators |

Offline analysis tooling (dump analysis, routing suggestions, profile generation, etc.) is documented in [`docs/参考/DEVELOPER_GUIDE.md`](docs/参考/DEVELOPER_GUIDE.md).

**Tech stack**

| Component | Version |
|-----------|---------|
| Minecraft + NeoForge | 1.21.1 / 21.1.224 |
| Applied Energistics 2 | 19.2.17 |
| Java | 21 |

Optional client integrations: JEI and EMI (DAV expansion cell drag-and-drop, Smart Bus filter editing).

**License:** All Rights Reserved.

---

<p align="center">
  <i>Applied Energistics: Insight · 应用能源：洞察</i><br/>
  <i>See · converge · organize — explainable AE2 storage for networks that outgrew eyeballing</i><br/>
  <i>看清 · 收敛 · 整理 — 为「大到肉眼管不住」的 AE2 网络而生</i>
</p>
