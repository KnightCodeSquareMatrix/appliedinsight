# 应用能源：洞察 — Roo 项目规则

> 英文：**Applied Energistics: Insight** · Mod ID: `appliedinsight` · 社区简称 **AE 洞察**

> 本文档定义 Roo AI 助手在本项目中的行为规则。
> 首次接入时，Roo 会自动读取此文件。

---

## 项目概述

Applied Energistics: Insight 是一个 Minecraft NeoForge 模组，为 AE2 网络提供智能存储管理功能。
- **Mod ID**: `appliedinsight`
- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.224
- **AE2**: 19.2.17
- **Java**: 21
- **Gradle**: 9.2.1

---

## 首次接入流程

当 Roo 首次接入此项目时，按以下顺序阅读文档：

1. `docs/架构/FACTS.md` — 原子事实库（架构、边界、术语、ADR）
2. `docs/ai/AI_ENTRY.md` — AI 协作者完整入口
3. `docs/类职责/索引.md` — 类职责索引，按包定位

---

## 架构规则

### 层依赖

```
rule/    纯规则模型     ← 无 Minecraft/AE2 依赖
ae2/     AE2 集成层      ← 不反向依赖 app/
app/     用例编排层      ← 不包含持久化实现
logging/ 基础设施       ← 不参与业务决策
analysis/ 离线分析      ← 不污染在线命令链
plan/    规划模型       ← 不承担执行职责
network/ 网络通信       ← 只负责序列化/传输
block/ + blockentity/  ← 方块声明 + 生命周期
```

### 三条能力线

| 能力线 | 命令 | 语义 |
|--------|------|------|
| 快速合并 | `/sorter merge` | 同类归并，无配置 |
| Zone 治理 | `/sorter me ...` | 规则化整理，需配置 |
| 宏观观测 | `/sorter me storageDump` | 存储健康诊断 |

### 硬规则

1. `rule/**` 不导入 `net.minecraft.*` 或 `appeng.*`
2. 新增代码不能把 `route`、`zone`、`cell`、`execute` 混在一起
3. DAV / 管理卡是玩家输入入口，不是 route 引擎本身
4. logger 是基础设施，核心类不应为日志需求扭曲自身职责
5. `network/` 包只负责网络包序列化/传输
6. `block/` + `blockentity/` 只负责方块声明和生命周期管理

---

## 修改代码流程

1. **先读文档**：阅读相关类职责文档（`docs/类职责/`）
2. **再读源码**：理解当前实现
3. **判断能力线**：属于 merge / me plan / storageDump 哪条线？
4. **修改代码**：保持架构边界
5. **同步文档**：更新类职责文档、索引、整体逻辑.md（如影响架构边界）
6. **编译验证**：`./gradlew compileJava`

---

## 文档维护规则

- 类职责文档使用统一表格格式
- 新增/删除类后同步更新 `docs/类职责/索引.md`
- JSON 契约变化同步更新 `docs/日志与JSON字段契约.md`
- 命令接口变化同步更新 `docs/参考/COMMANDS_REFERENCE.md`
- 架构边界变化同步更新 `docs/架构/FACTS.md`

---

## 参考文档索引

| 文档 | 用途 |
|------|------|
| `docs/架构/FACTS.md` | 原子事实库（架构、边界、术语、ADR） |
| `docs/ARCHITECTURE_DECISIONS.md` | 架构决策记录 (ADR) 完整版 |
| `docs/ai/AI_ENTRY.md` | AI 协作者入口 |
| `docs/类职责/索引.md` | 类职责索引（按包） |
| `docs/类职责总览.md` | 类审查结论与技术债 |
| `docs/参考/COMMANDS_REFERENCE.md` | 命令完整参考 |
| `docs/参考/API_REFERENCE.md` | 后端 API 参考 |
| `docs/参考/DEVELOPER_QUICKSTART.md` | 开发者快速入门 |
| `docs/参考/EXTENSION_GUIDE.md` | 扩展开发指南 |
| `docs/参考/TESTING_GUIDE.md` | 测试指南 |
| `docs/参考/前端对接说明.md` | 前端契约 |
| `docs/日志与JSON字段契约.md` | JSON 字段说明 |
| `docs/发布前最后任务.md` | 发布前任务清单 |
