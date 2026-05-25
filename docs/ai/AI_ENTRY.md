# AI 协作者入口 (AI Assistant Entry Point)

> 本文档是 AI 协作者（如 GitHub Copilot）的第一站。
> 开始处理任务前，建议先阅读以下文档（按顺序）：

**必读顺序**:
1. `docs/GLOSSARY.md` — 对齐术语
2. `docs/ARCHITECTURE_REFERENCE.md` — 理解架构全景
3. `docs/整体逻辑.md` — 深入理解主骨架与边界
4. `docs/类职责总览.md` — 审查结论与技术债
5. `docs/AI_DEVELOPMENT_GUIDE.md` — 最小协作规则

**最新增量**（2026-05-24 新增）:
- `docs/后端JSON完善指南.md` — me-dump / storage-analysis 数据契约现状
- `docs/ai/frontend-vibe-coding-prompt.md` — 前端 Dashboard 项目 prompt
- `ae2/CellCapacityInspector.java` — 共享的 cell 容量反射检测 service

---

## 1. AI 开发原则

### 1.1 两条能力线语义

当前项目有两条清晰的能力线，不要混淆：

| 能力线 | 命令 | 语义 | 涉及核心类 |
|--------|------|------|-----------|
| **快速合并** | `/sorter merge` | 同类归并，无配置 | `MergeMovePlanner`, `SorterMergeService` |
| **Zone 治理** | `/sorter me ...` | 规则化整理，需配置 | `LiveZoneAllocationPlanner`, `Ae2ZoneMoveExecutor` |

**不要**：
- 把 `/sorter merge` 再写成通用 sorter 主入口
- 把 zone 主线错误表述成"只是 merge"
- 在 merge 路径中引入 profile/zone/route 逻辑

### 1.2 必须守住的硬边界

```
┌──────────────────────────────────────────────┐
│  rule/    纯规则模型     ← 无 Minecraft/AE2 依赖 │
│  ae2/    AE2 集成层      ← 不反向依赖 app/      │
│  app/    用例编排层      ← 不包含持久化实现      │
│  logging/ 基础设施       ← 不参与业务决策        │
│  analysis/ 离线分析      ← 不污染在线命令链      │
│  plan/    规划模型       ← 不承担执行职责        │
└──────────────────────────────────────────────┘
```

**硬规则（不可违反）**：
1. `rule/**` 不导入 `net.minecraft.*` 或 `appeng.*`
2. 新增代码不能把 `route`、`zone`、`cell`、`execute` 混在一起
3. DAV / 管理卡是玩家输入入口，不是 route 引擎本身
4. logger 是基础设施，核心类不应为日志需求扭曲自身职责
5. `RuntimeTopology` 的 diagnostics 现在很薄，不要误扩张成完整分析系统

### 1.3 当前不建议做的事

- 不要为了未来猜想引入大框架
- 不要强行抽象一整套 node/endpoint/provider 体系
- 不要把简单 DTO 拆成多层空壳 service
- 不要随意重写 `DriveMachineAccessor`（除非上游变化）
- 不要做"未来全能 mod"级别的扩张设计

---

## 2. 修改代码前的检查清单

处理任何修改请求前，依次回答以下问题：

### 2.1 变更定性
- [ ] 这个修改属于 merge 能力线还是 me plan 能力线？
- [ ] 还是属于 storage dashboard 观测线？

### 2.2 边界检查
- [ ] 修改是否引入新的跨层依赖（如 rule 层引用 AE2 类）？
- [ ] 修改是否让 `route/zone/cell/execute` 混在一起？
- [ ] 是否引入了不必要的抽象？

### 2.3 文档更新
- [ ] 是否影响了类的职责定位？→ 更新 `docs/类职责/` 对应文档
- [ ] 是否影响了架构边界？→ 更新 `docs/整体逻辑.md`
- [ ] 是否影响了类职责总览的结论？→ 更新 `docs/类职责总览.md`
- [ ] 是否影响了 JSON 契约？→ 更新 `docs/日志与JSON字段契约.md`

### 2.4 验证
- [ ] `./gradlew compileJava` 是否通过？
- [ ] 现有命令语义是否保持不变？
- [ ] JSON 契约（如有改动）是否向后兼容？
- [ ] 若新增命令/功能，`SorterCommandBlock` GUI 是否需同步更新？

---

## 3. 常见修改场景模板

### 场景 A：新增一个命令

**步骤**：
1. 在 `SorterCommands.java` 中用 Brigadier 注册新命令
2. 在 `application/` 下创建对应的 Service 类
3. 在 `logging/` 选择或创建对应的 Logger
4. 创建 `docs/类职责/` 文档（新增的 Service、Logger 等）
5. 更新 `docs/类职责/索引.md`
6. 更新 `docs/整体逻辑.md`（若新增能力路径）
7. 若需在 GUI 方块中使用，更新 `SorterCommandBlockScreen` + `SorterCommandPayload`
8. 验证：`./gradlew compileJava`

**参考**：现有 `SorterPlanService.java` 和 `SorterPlanFileLogger.java` 的模式。

### 场景 B：修改执行逻辑（如 Ae2ZoneMoveExecutor）

**步骤**：
1. 阅读 `docs/类职责/ae2/zone/Ae2ZoneMoveExecutor.md`
2. 阅读 `docs/历史文档-仅供AI参考/执行链重构-cline提示词.md`（历史文档）
3. 阅读 `docs/RuntimeTopology-设计草案.md`
4. 确认修改范围：
   - 只改执行策略？→ 不影响其他层
   - 改执行结果模型？→ 需同步更新 Logger
   - 引入新的回滚逻辑？→ 确认不污染 `RuntimeZone` 和 `RuntimeCell`
5. 修改后更新对应职责文档
6. 验证：`./gradlew compileJava`

### 场景 C：修改 dump / JSON 输出

**步骤**：
1. 阅读 `docs/后端JSON完善指南.md` — 了解当前覆盖状况
2. 确认修改范围：
   - me-dump 写入器：`SorterNetworkDumpWriter.java`
   - storage-analysis 分析器：`Ae2StorageAnalyzer.java` + `StorageAnalyzerReport.java`
   - Cell 容量检测：`CellCapacityInspector.java`（共享工具，两个 dump 都调用）
3. 修改后更新 `docs/日志与JSON字段契约.md`
4. 更新 JSON Schema（`src/main/resources/schema/sorter-network-dump.schema.json`）
5. 同步更新前端 prompt：`docs/ai/frontend-vibe-coding-prompt.md`
6. 验证：`./gradlew build` + `./gradlew copyModJarToPrism`

### 场景 D：修改日志/字段契约

**步骤**：
1. 阅读 `docs/日志与JSON字段契约.md`
2. 确定修改类型：
   - 新增字段？→ 注意向后兼容
   - 修改现有字段？→ 检查前端消费方
   - 新增日志文件？→ 按照现有 Logger 模式
3. 更新 `docs/日志与JSON字段契约.md`
4. 更新对应的 Logger 类职责文档

---

## 4. 当前阶段重点

> 参考 `docs/下一步计划.md` 获取最新方向。

### ✅ 已完成

- ✅ RuntimeTopology 已成为运行时统一主入口
- ✅ 运行时主干已完成一轮安全收口
- ✅ SorterCommandBlock — 带 GUI 的 ME 网络命令执行方块（替换手动命令输入）
- ✅ me-dump 容量字段：`totalBytes` / `usedBytes` / `totalItemTypes` / `remainingItemTypes` / `cellKind` / `zoneId` / `driveCellCount`
- ✅ storage-analysis 容量字段同步（同上字段）
- ✅ `CellCapacityInspector` — 共享的 cell 容量反射检测 service
- ✅ 前端 vibe-coding prompt（`docs/ai/frontend-vibe-coding-prompt.md`）
- ✅ 自动部署到 PrismLauncher（`./gradlew copyModJarToPrism`）

### ➡️ 下一步

- 日志基础设施收敛（参考 `docs/下一步计划.md`）
- Filter Tree Editor 前端实现（参考 `docs/ai/frontend-vibe-coding-prompt.md`）

### 当前不优先处理

- RuntimeTopology 的零碎打磨
- Analyzer 完整分析系统的扩张
- Ae2ZoneMoveExecutor 的大拆解

---

## 5. 项目版本快照

| 项目 | 版本/值 |
|------|---------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.224 |
| AE2 | 19.2.17 (range [19.2.17, 20)) |
| Java | 21 |
| Mod | 1.0.0 |
| Mod ID | `appliedstoragesorter` |
| JSON 格式版本 | 1 |
| Gradle | 9.2.1 |

---

## 6. 构建与部署

### 常用命令

```sh
# 编译
./gradlew compileJava

# 完整构建 + 自动复制到 PrismLauncher
./gradlew build copyModJarToPrism

# 仅复制到 PrismLauncher（需先 build）
./gradlew copyModJarToPrism

# 自定义 mods 目录
./gradlew build -PmodsDir=/path/to/minecraft/mods
```

### PrismLauncher 路径

```
C:\Users\KnightCode\AppData\Roaming\PrismLauncher\instances\1.21.1\minecraft\mods\
```

### 数据 Dump 路径

```
C:\Users\KnightCode\AppData\Roaming\PrismLauncher\instances\1.21.1\minecraft\dumps\appliedstoragesorter\
```

---

## 7. 关键文件索引

### Java 源文件（模块入口）

| 文件 | 职责 |
|------|------|
| `AppliedStorageSorter.java` | Mod 主类，注册事件、网络包 |
| `Config.java` | 服务器配置 |
| `SorterCommands.java` | 命令注册 |

### 核心数据流

| 文件 | 职责 |
|------|------|
| `ae2/dump/SorterNetworkDumpWriter.java` | me-dump 写入器 |
| `ae2/dump/SorterStorageAnalysisDumpWriter.java` | storage-analysis 写入器 |
| `ae2/analysis/Ae2StorageAnalyzer.java` | 存储分析器 |
| `ae2/analysis/StorageAnalyzerReport.java` | 分析报告类型 |
| `ae2/CellCapacityInspector.java` | ✅ 新增：cell 容量检测共享工具 |
| `ae2/zone/RuntimeTopology.java` | 运行时拓扑 |
| `ae2/zone/RuntimeZone.java` | Zone 运行时 |
| `ae2/zone/Ae2ZoneMoveExecutor.java` | 搬运执行器 |
| `rule/route/RoutingEngine.java` | 路由决策引擎 |
| `rule/route/RoutingProfile.java` | 路由配置模型 |
| `logging/SorterFileLogger.java` | 命令级摘要日志 |
| `logging/SorterPlanFileLogger.java` | plan 详细日志 |
| `logging/SorterMergeReportFileLogger.java` | merge 复盘日志 |

### GUI / 方块

| 文件 | 职责 |
|------|------|
| `block/SorterCommandBlock.java` | ME 网络命令执行方块 |
| `blockentity/SorterCommandBlockEntity.java` | 命令方块 BE |
| `block/DigitalAssetVaultBlock.java` | 数字资产库方块 |
| `blockentity/DigitalAssetVaultBlockEntity.java` | DAV BE |
| `menu/DigitalAssetVaultMenu.java` | DAV 菜单 |
| `client/screen/DigitalAssetVaultScreen.java` | DAV 界面 |
| `network/SorterCommandPayload.java` | 网络包 |

---

## 8. 快速引用导航

| 我需要... | 去看 |
|-----------|------|
| 理解一个术语 | `docs/GLOSSARY.md` |
| 理解架构全景 | `docs/ARCHITECTURE_REFERENCE.md` |
| 理解项目主骨架 | `docs/整体逻辑.md` |
| 查看类职责总览 | `docs/类职责总览.md` |
| 查看某个类的职责 | `docs/类职责/索引.md` → 对应类文档 |
| 理解前端契约 | `docs/前端对接说明.md` |
| 理解 JSON 字段 | `docs/日志与JSON字段契约.md` |
| 了解后端 JSON 完善现状 | `docs/后端JSON完善指南.md` |
| 前端 Dashboard 开发 | `docs/ai/frontend-vibe-coding-prompt.md` |
| 了解 Dashboard 哲学 | `docs/dashboard的设计哲学.md` |
| 了解 Analyzer 定位 | `docs/Analyzer-阶段定位草案.md` |
| 了解 RuntimeTopology | `docs/RuntimeTopology-设计草案.md` |
| 了解存储语义层 | `docs/存储节点语义层.md` |
| 了解当前阶段计划 | `docs/下一步计划.md` |
| 回溯设计演变 | `docs/历史文档-仅供AI参考/` |
| 构建命令 | `./gradlew build copyModJarToPrism` |

---

> **一句话原则**：优先维护现有清晰边界；小步收口，少做幻想式重构。
> 改 JSON 契约后记得同步 `docs/日志与JSON字段契约.md` 和前端 prompt。
