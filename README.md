<p align="center">
  <img src="docs/assets/logo.png" alt="Applied Storage Sorter" width="128" height="128"/>
</p>

<h1 align="center">Applied Storage Sorter</h1>

<p align="center">
  <b>AE2 存储治理模组 — 自动整理 · 规则路由 · 存储观测</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-blue?logo=minecraft" alt="Minecraft 1.21.1"/>
  <img src="https://img.shields.io/badge/NeoForge-21.1.224-orange?logo=eclipseide" alt="NeoForge 21.1.224"/>
  <img src="https://img.shields.io/badge/AE2-19.2.17-brightgreen" alt="AE2 19.2.17"/>
  <img src="https://img.shields.io/badge/Java-21-red?logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Gradle-9.2.1-blue?logo=gradle" alt="Gradle 9.2.1"/>
  <img src="https://img.shields.io/badge/license-ARR-lightgrey" alt="License: ARR"/>
  <img src="https://img.shields.io/badge/status-active--development-yellow" alt="Status: Active Development"/>
</p>

---

> **30 秒速览**：Applied Storage Sorter 是一个 AE2 存储治理模组。
> - **自动整理**：放置 [`SorterCommandBlock`](docs/类职责/block/SorterCommandBlock.md) 点击"整理合并"按钮即可 — 同类物品归并压实，无需配置
> - **规则路由**：放置 [`SorterCommandBlock`](docs/类职责/block/SorterCommandBlock.md) 点击"规划并搬运"按钮 — 玩家定义存储区+过滤器，规则化整理
> - **宏观观测**：点击"存储分析"按钮 — 存储健康诊断，dashboard 快照
> - **技术栈**：Minecraft 1.21.1 + NeoForge + Java 21 + AE2
> - **快速开始**：放入 `mods/` 目录，确保有 ME Controller，放置 [`SorterCommandBlock`](docs/类职责/block/SorterCommandBlock.md) 右键打开 GUI 即可

---

> **Applied Storage Sorter** 是一个 Minecraft NeoForge 模组，专注于 AE2 内部存储治理。
> 提供自动整理（同类物品归并压实）和规则路由（玩家自定义存储区与过滤器）两条能力线，附带完整的离线分析工具链和可扩展的规则引擎。

---

## 📋 目录

- [快速开始](#-快速开始)
- [命令速查](#-命令速查)
- [架构概览](#-架构概览)
- [当前实现状态](#-当前实现状态)
- [离线分析工具链](#-离线分析工具链)
- [下一步阅读（按角色）](#-下一步阅读按角色)
- [技术栈](#-技术栈)
- [许可证](#-许可证)

---

## 🚀 快速开始

### 前置要求

| 依赖 | 版本 |
|------|------|
| Minecraft 1.21.1 + NeoForge 21.1.224+ + AE2 19.2.17+ + Java 21+ | |

### 安装

1. 从 [Releases](https://github.com/your-repo/appliedstoragesorter/releases) 下载最新 JAR
2. 放入 Minecraft 实例的 `mods/` 目录
3. 启动游戏，确保 AE2 网络中有 **ME Controller**

### 从源码构建

```bash
git clone https://github.com/your-repo/appliedstoragesorter.git
cd appliedstoragesorter
./gradlew build          # 编译（不运行测试）
./gradlew runClient      # 启动 Minecraft 客户端
```

构建产物在 `build/libs/` 下。

---

## ⌨️ 命令速查

> **💡 普通玩家推荐使用 [`SorterCommandBlock`](docs/类职责/block/SorterCommandBlock.md)（命令方块）的 GUI 界面**，右键点击按钮即可操作，无需记忆命令。
> 以下命令为**开发者模式入口**，主要用于调试和开发场景，需通过聊天框输入。

| 命令 | 功能 | 输出 |
|------|------|------|
| `/sorter merge preview` | 预览自动整理效果 | 控制台 + merge report 日志 |
| `/sorter merge execute` | 执行自动整理 | 控制台 + merge report 日志 |
| `/sorter me dump` | 导出当前 AE2 网络快照 | `dumps/appliedstoragesorter/` JSON |
| `/sorter me storageDump` | 导出存储分析报告 | `dumps/appliedstoragesorter/` JSON |
| `/sorter me bindProfile <profile>` | 绑定路由配置到当前网络 | 控制台反馈 |
| `/sorter me showProfile` | 查看当前绑定的路由配置 | 控制台输出 |
| `/sorter me plan` | 预览规则路由规划 | 控制台 + plan 日志 |
| `/sorter me planAndMove` | 执行规则路由规划 | 控制台 + plan 日志 |

> 完整命令参考见 [`docs/参考/COMMANDS_REFERENCE.md`](docs/参考/COMMANDS_REFERENCE.md)

---

## 🏗️ 架构概览

### 三条能力路径

- **主路径**：规则路由规划与执行（profile → route → zone plan → runtime topology → execute → log）— 可通过 [`SorterCommandBlock`](docs/类职责/block/SorterCommandBlock.md) GUI 的"规划并搬运"按钮触发，或通过 `/sorter me planAndMove` 命令触发
- **简化路径**：快速同类归并压实 — 可通过 [`SorterCommandBlock`](docs/类职责/block/SorterCommandBlock.md) GUI 的"整理合并"按钮触发，或通过 `/sorter merge` 命令触发
- **宏观观测**：存储健康诊断，dashboard 快照 — 可通过 [`SorterCommandBlock`](docs/类职责/block/SorterCommandBlock.md) GUI 的"存储分析"按钮触发，或通过 `/sorter me storageDump` 命令触发

### 核心架构原则

| 原则 | 说明 |
|------|------|
| **规则层不碰运行时** | `rule/**` 必须保持纯 Java，无 Minecraft/AE2 运行时依赖 |
| **运行时层不重做规则层** | RuntimeTopology 消费规则结果，不重新实现规则引擎 |
| **DAV 不是架构中心** | Digital Asset Vault 是管理入口，不是架构中心本身 |
| **分析层独立于游戏主流程** | 离线分析器不依赖 Minecraft 运行时 |
| **Logger 是基础设施** | 日志属于基础设施层，不反向定义核心模型 |

> 完整事实库（132 条原子事实，取代旧版架构散文）见 [`docs/架构/FACTS.md`](docs/架构/FACTS.md)

---

## ✅ 当前实现状态

### 当前阶段重点

| 方向 | 状态 |
|------|------|
| Logger 基础设施强化 | ➡️ 进行中 |
| SLF4J + Lombok `@Slf4j` 标准化 | ➡️ 进行中 |
| 报告型 Logger 增强 | ➡️ 进行中 |
| 前端 Dashboard | 📋 规划中 |
| 图形化规则编辑器 | 📋 规划中 |

> 详细计划见 [`docs/下一步计划.md`](docs/下一步计划.md)

---

## 🔧 离线分析工具链

项目提供多条**不依赖 Minecraft 运行时**的纯 Java 离线分析链路（Gson `JsonReader` 流式读取），包括 dump 分析、路由分析、路由建议分析、draft profile 生成等。

> 完整命令示例见 [`docs/参考/DEVELOPER_GUIDE.md`](docs/参考/DEVELOPER_GUIDE.md)

---

## 📚 下一步阅读（按角色）

| 角色 | 入口文档 |
|------|---------|
| **👤 玩家/用户** | 放置 [`SorterCommandBlock`](docs/类职责/block/SorterCommandBlock.md) 右键打开 GUI → 点击按钮操作；详细命令参考见 [`参考/COMMANDS_REFERENCE.md`](docs/参考/COMMANDS_REFERENCE.md) |
| **💻 Java 开发者** | [`参考/DEVELOPER_QUICKSTART.md`](docs/参考/DEVELOPER_QUICKSTART.md) → [`docs/架构/FACTS.md`](docs/架构/FACTS.md) |
| **🤖 AI 协作者** | [`VIBE_CODING_ENTRY.md`](docs/ai/VIBE_CODING_ENTRY.md)（vibe-coding 入口）或 [`AI_ENTRY.md`](docs/ai/AI_ENTRY.md)（完整版） |
| **🎨 前端开发者** | [`FRONTEND_PROMPT.md`](docs/ai/FRONTEND_PROMPT.md) → [`参考/前端对接说明.md`](docs/参考/前端对接说明.md) |
| **🔌 扩展开发者** | [`参考/EXTENSION_GUIDE.md`](docs/参考/EXTENSION_GUIDE.md) → [`参考/API_REFERENCE.md`](docs/参考/API_REFERENCE.md) |
| **🧪 测试人员** | [`参考/TESTING_GUIDE.md`](docs/参考/TESTING_GUIDE.md) |

### 核心文档索引（AI 第一站）

| 文档 | 说明 |
|------|------|
| [`docs/ai/AI_ENTRY.md`](docs/ai/AI_ENTRY.md) | **AI 第一站**。必读顺序 + 硬边界 + 检查清单 + 导航 |
| [`docs/架构/FACTS.md`](docs/架构/FACTS.md) | **原子事实库**（132 条）。取代旧版 GLOSSARY + ARCHITECTURE_REFERENCE + 整体逻辑 |
| [`docs/类职责总览.md`](docs/类职责总览.md) | 129 个 Java 类型审查结论 |
| [`docs/类职责/索引.md`](docs/类职责/索引.md) | 129 个类的详细职责文档索引 |
| [`docs/下一步计划.md`](docs/下一步计划.md) | 当前阶段方向与计划 |

### 参考文档

| 文档 | 说明 |
|------|------|
| [`ARCHITECTURE_DECISIONS.md`](docs/ARCHITECTURE_DECISIONS.md) | 架构决策记录 (ADR) — 10 项正式决策 |
| [`参考/DEVELOPER_GUIDE.md`](docs/参考/DEVELOPER_GUIDE.md) | 环境搭建、构建命令、开发流程、代码规范 |
| [`参考/DEVELOPER_QUICKSTART.md`](docs/参考/DEVELOPER_QUICKSTART.md) | 开发者快速入门 |
| [`参考/API_REFERENCE.md`](docs/参考/API_REFERENCE.md) | 命令接口、JSON 契约、扩展点 |
| [`参考/COMMANDS_REFERENCE.md`](docs/参考/COMMANDS_REFERENCE.md) | 完整命令参考 |
| [`参考/EXTENSION_GUIDE.md`](docs/参考/EXTENSION_GUIDE.md) | 扩展开发指南 |
| [`参考/TESTING_GUIDE.md`](docs/参考/TESTING_GUIDE.md) | 测试指南 |
| [`参考/前端对接说明.md`](docs/参考/前端对接说明.md) | 前端边界、契约文件、消费路径 |
| [`日志与JSON字段契约.md`](docs/日志与JSON字段契约.md) | 所有运行产物的字段定义 |
| [`参考/dashboard的设计哲学.md`](docs/参考/dashboard的设计哲学.md) | Dashboard 设计哲学 |

### AI 协作文档

| 文档 | 说明 |
|------|------|
| [`AI_ENTRY.md`](docs/ai/AI_ENTRY.md) | **AI 入口**（已合并 AI_DEVELOPMENT_GUIDE）— 硬边界、检查清单、命名约定、导航 |
| [`CODE_REVIEW_CHECKLIST.md`](docs/ai/CODE_REVIEW_CHECKLIST.md) | AI 代码审查清单 |
| [`REFACTORING_GUIDE.md`](docs/ai/REFACTORING_GUIDE.md) | 重构指南 |
| [`FRONTEND_PROMPT.md`](docs/ai/FRONTEND_PROMPT.md) | 前端开发 Prompt（精简版） |
| [`VIBE_CODING_ENTRY.md`](docs/ai/VIBE_CODING_ENTRY.md) | 前端 Vibe Coding 入口 |

### 预配置的 AI Agent

- **Roo** — 通过 [`.roomodes`](.roomodes) 配置了 `core-dev` / `docs` / `analysis` / `review` 四种自定义模式；项目规则见 [`CLINE.md`](CLINE.md)
- **GitHub Copilot** — 通过 `.github/copilot-instructions.md` 配置了项目级指令

---

## 🛠️ 技术栈

| 技术 | 版本 |
|------|------|
| Java | 21 |
| Gradle | 9.2.1 |
| NeoForge | 21.1.224 |
| AE2 | 19.2.17 |
| Lombok | ✅ (`@Slf4j`) |
| SLF4J | ✅ |
| JUnit 5 | ✅ |
| Gson | ✅ (JSON 编解码) |

---

## 📄 许可证

All Rights Reserved.

---

<p align="center">
  <i>Applied Storage Sorter — 更可控 · 更可解释 · 更适合大型 AE2 网络长期维护</i>
</p>
