# AI 协作者入口 (AI Assistant Entry Point)

> ⚠️ **本文档是 AI 的第一站。读完本文档 + [`架构/FACTS.md`](../架构/FACTS.md) + 目标类职责文档，即可理解项目全貌。**

**必读顺序**:
1. [`docs/架构/FACTS.md`](../架构/FACTS.md) — 原子事实库（取代旧版 GLOSSARY + ARCHITECTURE_REFERENCE + 整体逻辑）
2. [`docs/类职责总览.md`](../类职责总览.md) — 审查结论与技术债
3. [`docs/类职责/索引.md`](../类职责/索引.md) → 定位目标类

---

## 1. AI 开发原则

### 1.1 三条能力线语义

| 能力线 | 命令 | 语义 | 涉及核心类 |
|--------|------|------|-----------|
| **简化 Merge 路径** | `/sorter merge` | 同类归并，无配置 | `MergeMovePlanner`, `SorterMergeService` |
| **主路径 (Plan/PlanAndMove)** | `/sorter me ...` | 规则化整理，需配置 | `LiveZoneAllocationPlanner`, `Ae2ZoneMoveExecutor` |
| **宏观观测路径 (Storage Dashboard)** | `/sorter me storageDump` | 存储健康诊断 | `Ae2StorageAnalyzer`, `SorterStorageAnalysisService` |

**不要**：把 merge 写成通用 sorter 主入口；把 zone 主线表述成"只是 merge"；在 merge 路径中引入 profile/zone/route 逻辑；把 storageDump 分析塞进 merge 或 plan 路径。

### 1.2 必须守住的硬边界

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

**硬规则（不可违反）**：
1. `rule/**` 不导入 `net.minecraft.*` 或 `appeng.*`
2. 新增代码不能把 `route`、`zone`、`cell`、`execute` 混在一起
3. DAV / 管理卡是玩家输入入口，不是 route 引擎本身
4. logger 是基础设施，核心类不应为日志需求扭曲自身职责
5. `RuntimeTopology` 的 diagnostics 现在很薄，不要误扩张成完整分析系统
6. `network/` 包只负责网络包序列化/传输，不包含业务逻辑
7. `block/` + `blockentity/` 只负责方块声明和生命周期管理，不包含命令执行逻辑

### 1.3 当前不建议做的事

- 不要为了未来猜想引入大框架
- 不要强行抽象一整套 node/endpoint/provider 体系
- 不要把简单 DTO 拆成多层空壳 service
- 不要随意重写 `DriveMachineAccessor`（除非上游变化）
- 不要做"未来全能 mod"级别的扩张设计
- 不要为 network 包引入额外的业务抽象层

### 1.4 命名与语义约定（合并自 AI_DEVELOPMENT_GUIDE）

- `merge` 只用于快速合并路径
- `plan` / `planAndMove` 只用于 profile/zone 主线
- 不要把 `/sorter merge` 再写成通用 sorter 主入口
- 不要把 zone 主线错误表述成"只是 merge"
- `storageDump` / `storage-analysis` 用于宏观观测线

---

## 2. 修改代码前的检查清单

### 2.1 变更定性
- [ ] 这个修改属于 merge 能力线还是 me plan 能力线？
- [ ] 还是属于 storage dashboard 观测线？
- [ ] 是否涉及网络通信（network 包）？

### 2.2 边界检查
- [ ] 修改是否引入新的跨层依赖（如 rule 层引用 AE2 类）？
- [ ] 修改是否让 `route/zone/cell/execute` 混在一起？
- [ ] 是否引入了不必要的抽象？
- [ ] 若修改 network 包，是否保持了纯序列化/传输职责？

### 2.3 文档更新
- [ ] 是否影响了类的职责定位？→ 更新 `docs/类职责/` 对应文档
- [ ] 是否影响了架构边界？→ 更新 `docs/架构/FACTS.md`
- [ ] 是否影响了类职责总览的结论？→ 更新 `docs/类职责总览.md`
- [ ] 是否影响了 JSON 契约？→ 更新 `docs/日志与JSON字段契约.md`
- [ ] 是否影响了命令接口？→ 更新 `docs/参考/COMMANDS_REFERENCE.md`
- [ ] 是否影响了 API 契约？→ 更新 `docs/参考/API_REFERENCE.md`

### 2.4 验证
- [ ] `./gradlew compileJava` 是否通过？
- [ ] 现有命令语义是否保持不变？
- [ ] JSON 契约（如有改动）是否向后兼容？
- [ ] 若新增命令/功能，`SorterCommandBlock` GUI 是否需同步更新？
- [ ] 若新增网络包，是否注册了对应的 `CustomPacketPayload` 和 handler？

---

## 3. 快速文档导航

| 我需要... | 去看 |
|-----------|------|
| 对齐术语 + 架构全景 | [`docs/架构/FACTS.md`](../架构/FACTS.md) |
| 审查结论与技术债 | [`docs/类职责总览.md`](../类职责总览.md) |
| 查看某个类的职责 | [`docs/类职责/索引.md`](../类职责/索引.md) → 对应文档 |
| 理解架构决策 | [`docs/ARCHITECTURE_DECISIONS.md`](../ARCHITECTURE_DECISIONS.md) |
| 命令完整参考 | [`docs/参考/COMMANDS_REFERENCE.md`](../参考/COMMANDS_REFERENCE.md) |
| 后端 API 参考 | [`docs/参考/API_REFERENCE.md`](../参考/API_REFERENCE.md) |
| 开发者环境搭建 | [`docs/参考/DEVELOPER_QUICKSTART.md`](../参考/DEVELOPER_QUICKSTART.md) |
| 扩展开发指南 | [`docs/参考/EXTENSION_GUIDE.md`](../参考/EXTENSION_GUIDE.md) |
| 测试指南 | [`docs/参考/TESTING_GUIDE.md`](../参考/TESTING_GUIDE.md) |
| 前端契约 | [`docs/参考/前端对接说明.md`](../参考/前端对接说明.md) |
| JSON 字段契约 | [`docs/日志与JSON字段契约.md`](../日志与JSON字段契约.md) |
| 当前阶段计划 | [`docs/下一步计划.md`](../下一步计划.md) |
| 代码审查清单 | [`docs/ai/CODE_REVIEW_CHECKLIST.md`](../ai/CODE_REVIEW_CHECKLIST.md) |
| 重构指南 | [`docs/ai/REFACTORING_GUIDE.md`](../ai/REFACTORING_GUIDE.md) |
| 前端 Dashboard 开发 | [`docs/ai/frontend-vibe-coding-prompt.md`](../ai/frontend-vibe-coding-prompt.md) |
| 前端 prompt（精简） | [`docs/ai/FRONTEND_PROMPT.md`](../ai/FRONTEND_PROMPT.md) |
| Roo 项目规则 | [`CLINE.md`](../../CLINE.md) |
| 历史设计演变 | [`docs/历史文档-仅供AI参考/`](../历史文档-仅供AI参考/) |
| Dashboard 设计哲学 | [`docs/参考/dashboard的设计哲学.md`](../参考/dashboard的设计哲学.md) |
| Analyzer 定位 | [`docs/历史文档-仅供AI参考/Analyzer-阶段定位草案.md`](../历史文档-仅供AI参考/Analyzer-阶段定位草案.md) |
| RuntimeTopology 设计 | [`docs/历史文档-仅供AI参考/RuntimeTopology-设计草案.md`](../历史文档-仅供AI参考/RuntimeTopology-设计草案.md) |
| 存储语义层 | [`docs/参考/存储节点语义层.md`](../参考/存储节点语义层.md) |

---

> **一句话原则**：优先维护现有清晰边界；小步收口，少做幻想式重构。
> 改 JSON 契约后记得同步 `docs/日志与JSON字段契约.md` 和前端 prompt。
