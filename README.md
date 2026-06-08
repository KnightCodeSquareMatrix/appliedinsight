<p align="center">
  <img src="docs/assets/logo.png" alt="Applied Energistics: Insight" width="128" height="128"/>
</p>

<h1 align="center">Applied Energistics: Insight</h1>

<p align="center">
  <b>应用能源：洞察 · AE2 存储聚合、智能过滤与网络整理</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-blue?logo=minecraft" alt="Minecraft 1.21.1"/>
  <img src="https://img.shields.io/badge/NeoForge-21.1.224-orange?logo=eclipseide" alt="NeoForge 21.1.224"/>
  <img src="https://img.shields.io/badge/AE2-19.2.17-brightgreen" alt="AE2 19.2.17"/>
  <img src="https://img.shields.io/badge/Java-21-red?logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/license-ARR-lightgrey" alt="License: ARR"/>
  <img src="https://img.shields.io/badge/version-0.9.1-yellow" alt="Version 0.9.1"/>
</p>

---

> **30 秒速览**
>
> **应用能源：洞察**（Applied Energistics: Insight，社区常称 **AE 洞察**）是 AE2 的存储扩展模组。
>
> - **数字资产库（DAV）** — 自带 **2k 字节 / 126 种**初始容量，并可将多个 Storage Cell **吸收聚合成更大存储池**；容量吃紧时**自动提取或合成新 Cell 并吸收**，仓库自己「长大」
> - **智能总线（Smart Bus）** — 带 **JSON 自定义过滤器** 的输入/输出总线；游戏内预设 + 离线网页编辑器，精确控制什么货进、什么货出
> - **命令执行块** — 存储分析、碎片合并、（可选）规则路由整理的一键终端

Mod ID: `appliedinsight` · MC 1.21.1 · NeoForge 21.1.224 · AE2 19.2.17

---

## 数字资产库 — 吸收 Cell，自动扩容

DAV 不是一格一格插 Cell 的驱动器，而是**放置即可用的集中存储池**（内置相当于两张空 1k Cell：**2048 字节 / 126 种**），并可将**空的 AE2 Storage Cell 吸收掉**，把 Cell 的字节上限和物品种类上限**叠加进同一池子**。

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

---

## 智能总线 — JSON 过滤器，想搬什么搬什么

Smart Bus 是 AE2 线缆部件，在输入/输出总线基础上增加 **FilterExpression JSON 过滤器**。三种模式：**未配置** / **输入**（从背面库存拉货进网络）/ **输出**（从网络推货到背面）。Shift+右键或 GUI 切换。

### 为什么比堆升级卡更灵活

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

---

## 命令执行块 — 分析 · 合并 · 整理

右键打开终端 GUI，无需记命令：

| 按钮 | 作用 |
|------|------|
| **存储分析** | 网络健康诊断：内外部存储分布、碎片化、DAV 容量概览 |
| **整理合并** | 同类物品归并压实，释放物品种类槽位 |
| **规划并搬运** | （服主）按 `config/appliedinsight/profiles/` 路由配置分区整理 |

与 DAV 配合：合并减少碎片 → DAV 腾出种类空间；分析界面单独标注 DAV，不参与传统 Drive 单元格统计。

> 详见 [`guidebook/sorter-command-block.md`](guidebook/sorter-command-block.md)

---

## 快速开始

### 安装

1. 从 [Releases](https://github.com/KnightCodeSquareMatrix/appliedinsight/releases) 下载 `appliedinsight-0.9.1.jar`
2. 放入实例 `mods/` 目录（需已安装 AE2）
3. 进游戏合成 **数字资产库**、**智能总线**、**命令执行块**，接入 ME 网络即可

### 从源码构建

```bash
git clone https://github.com/KnightCodeSquareMatrix/appliedinsight.git
cd appliedinsight
./gradlew build
./gradlew runClient
```

产物位于 `build/libs/`。

### 玩家指南

| 文档 | 内容 |
|------|------|
| [`guidebook/getting-started.md`](guidebook/getting-started.md) | 分析 → 合并 → 接入 DAV 推荐流程 |
| [`guidebook/digital-asset-vault.md`](guidebook/digital-asset-vault.md) | DAV 吸收规则、三开关、自动扩容 |
| [`guidebook/smart-bus.md`](guidebook/smart-bus.md) | 过滤器语法、网页编辑器、预设 |
| [`guidebook/sorter-command-block.md`](guidebook/sorter-command-block.md) | 终端 GUI 三栏布局 |
| [`CHANGELOG.md`](CHANGELOG.md) | 版本变更与迁移说明 |

---

## 开发者

| 文档 | 说明 |
|------|------|
| [`docs/参考/DEVELOPER_QUICKSTART.md`](docs/参考/DEVELOPER_QUICKSTART.md) | 环境搭建与构建 |
| [`docs/架构/FACTS.md`](docs/架构/FACTS.md) | 权威架构事实库 |
| [`docs/参考/COMMANDS_REFERENCE.md`](docs/参考/COMMANDS_REFERENCE.md) | 完整命令与配置 |
| [`docs/ai/AI_ENTRY.md`](docs/ai/AI_ENTRY.md) | AI 协作者入口 |

离线分析工具链（dump 分析、路由建议、profile 生成等）见 [`docs/参考/DEVELOPER_GUIDE.md`](docs/参考/DEVELOPER_GUIDE.md)。

---

## 技术栈

| 技术 | 版本 |
|------|------|
| Minecraft + NeoForge | 1.21.1 / 21.1.224 |
| Applied Energistics 2 | 19.2.17 |
| Java | 21 |

可选客户端集成：JEI、EMI（DAV 扩容 Cell 拖拽、Smart Bus 过滤器编辑）。

---

## 许可证

All Rights Reserved.

---

<p align="center">
  <i>Applied Energistics: Insight · 应用能源：洞察 — 聚合存储 · 精确过滤 · 可解释的 AE2 网络维护</i>
</p>
