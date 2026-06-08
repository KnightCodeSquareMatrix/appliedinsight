# AI 协作者入口 (AI Assistant Entry Point)

> **AI 第一站**：读完本文 + [`架构/FACTS.md`](../架构/FACTS.md) 即可理解项目全貌并安全修改代码。

**必读**:
1. [`docs/架构/FACTS.md`](../架构/FACTS.md) — 原子事实库（架构、边界、术语、ADR 摘要）
2. [`docs/类职责/索引.md`](../类职责/索引.md) — 按包索引定位目标类

---

## 1. 三条能力线

| 能力线 | 命令 | 语义 | 核心类 |
|--------|------|------|--------|
| **Merge 路径** | `/sorter merge` | 同类归并，无配置 | `MergeMovePlanner`, `SorterMergeService` |
| **主路径** | `/sorter me planAndMove` | 规则化整理，需配置 | `LiveZoneAllocationPlanner`, `Ae2ZoneMoveExecutor` |
| **观测路径** | `/sorter me storageDump` | 存储健康诊断 | `Ae2StorageAnalyzer`, `SorterStorageAnalysisService` |

**不要**：把 merge 写成通用入口；在 merge 路径中引入 profile/zone/route；把 storageDump 分析塞进 merge 或 plan 路径；混淆三条能力线的术语和语义。

---

## 2. 必须守住的硬边界

```
rule/    纯规则模型     ← 无 Minecraft/AE2 依赖（FACT-013）
ae2/    AE2 集成层      ← 不反向依赖 app/（FACT-045）
app/    用例编排层      ← 不包含持久化实现（FACT-046）
logging/ 基础设施       ← 不参与业务决策（FACT-048）
analysis/ 离线分析      ← 不污染在线命令链（FACT-049）
plan/    规划模型       ← 不承担执行职责（FACT-047）
network/ 网络通信       ← 只负责序列化/传输（FACT-050）
block/ + blockentity/   ← 方块声明 + 生命周期（FACT-051）
```

**关键变更（ADR-012）**：`RuntimeCell` 已删除，替换为 `CellInfo` record。`Ae2StorageAnalyzer` 产出 `StorageDiagnosis`（含分析结论），不再是 raw data 的 `StorageAnalyzerReport`。

**硬规则**：
1. `rule/**` 不导入 `net.minecraft.*` 或 `appeng.*`
2. 不把 `route`、`zone`、`cell`、`execute` 混在一起
3. DAV / Smart Bus 是玩家设备入口，不是 route 引擎本身；**管理卡已移除**，zone 由 Profile 文件表达
4. logger 是基础设施，核心类不应为日志需求扭曲自身职责
5. `network/` 只负责网络包序列化/传输，不包含业务逻辑
6. `block/` + `blockentity/` 只负责方块声明和生命周期管理

---

## 3. 修改代码前检查清单

### 3.1 变更定性
- [ ] 属于哪条能力线（merge / me plan / storageDump）？
- [ ] 是否涉及网络通信（network 包）？

### 3.2 边界检查
- [ ] 是否引入新的跨层依赖（如 rule 层引用 AE2 类）？
- [ ] 是否让 `route/zone/cell/execute` 混在一起？
- [ ] 是否引入了不必要的抽象？

### 3.3 文档同步
- [ ] 类职责变化？→ 更新 `docs/类职责/` 对应文档
- [ ] 架构边界变化？→ 更新 `docs/架构/FACTS.md`
- [ ] JSON 契约变化？→ 更新 `docs/日志与JSON字段契约.md`
- [ ] 命令接口变化？→ 更新 `docs/参考/COMMANDS_REFERENCE.md`

### 3.4 验证
- [ ] `./gradlew compileJava` 通过
- [ ] 现有命令语义保持不变
- [ ] JSON 契约向后兼容
- [ ] 新网络包已注册 `CustomPacketPayload` 和 handler

---

## 4. 文档导航

| 需要... | 去看 |
|---------|------|
| 架构全景 + 术语 + 边界 | [`docs/架构/FACTS.md`](../架构/FACTS.md) |
| 架构决策记录 (ADR) | [`docs/ARCHITECTURE_DECISIONS.md`](../ARCHITECTURE_DECISIONS.md) |
| 查找某个类的职责 | [`docs/类职责/索引.md`](../类职责/索引.md) |
| 命令参考 | [`docs/参考/COMMANDS_REFERENCE.md`](../参考/COMMANDS_REFERENCE.md) |
| JSON 字段契约 | [`docs/日志与JSON字段契约.md`](../日志与JSON字段契约.md) |
| 前端对接说明 | [`docs/参考/前端对接说明.md`](../参考/前端对接说明.md) |
| GUI 布局 / 贴图验收 | [`docs/ai/GUI_VISUAL_CHECKLIST.md`](GUI_VISUAL_CHECKLIST.md) |
| 开发者快速入门 | [`docs/参考/DEVELOPER_QUICKSTART.md`](../参考/DEVELOPER_QUICKSTART.md) |
| 发布前任务清单 | [`docs/发布前最后任务.md`](../发布前最后任务.md) |

---

> **原则**：维护现有清晰边界；小步收口，少做幻想式重构。改 JSON 契约后同步 `docs/日志与JSON字段契约.md` 和前端 prompt。
>
> **最后更新**: 2026-06-08
