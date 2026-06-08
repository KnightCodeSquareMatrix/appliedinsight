# 事实库 (Fact Sheet)

> 本文档是 **应用能源：洞察**（Applied Energistics: Insight，Mod ID `appliedinsight`）的原子化事实集合。
> 所有事实按分类编号，可被 AI 精确引用（如 "FACT-013 规定了规则层零 AE2 依赖"）。
> 如果事实与代码不一致，以代码为准。

## 1. 项目定位

- FACT-001: 应用能源：洞察（Applied Energistics: Insight）是一个 Minecraft NeoForge 模组，MC 1.21.1，当前版本 **0.9.2**。
- FACT-002: 模组为 AE2 提供存储观测、整理与扩展（merge / route / analyze / DAV / Smart Bus）。
- FACT-003: 扫描与分析覆盖内部 drive 与 DAV；宏观分析含 external 统计，merge/plan 执行仍以 cell 为操作对象。
- FACT-004: 支持的 drive 类方块：`ae2:drive`、`extendedae:ex_drive`、`appliedinsight:digital_asset_vault`。
- FACT-005: merge/plan 仅允许 **cell→cell** 与 **cell→external**（内部 drive/DAV → 内部 cell 或 Storage Bus）；external 永不为 merge 源；storageDump 可报告 external bus 占比。
- FACT-006: 底层操作对象始终是 Cell（AE2 硬盘项）或 DAV 虚拟存储池。
- FACT-007: Mod ID 为 `appliedinsight`。
- FACT-008: Java 包路径根为 `com.knightcode.appliedstoragesorter`（历史命名；主类 `AppliedStorageSorter`）。
- FACT-009: Java 21，Parchment Mappings 2024.11.17，AE2 19.2.17，NeoForge 21.1.224。
- FACT-010: 模组包含约 145 个 Java 源文件，分布在 rule/（32 类）、ae2/（~40 类）、application/（14 类）、analysis/（5 类）、plan/（3 类）、network/（13 类）、block/（4 类）、blockentity/（3 类）、client/（~15 类）、command/（2 类）、logging/（4 类）、menu/（6 类）、registry/（7 类）、profilegen/（4 类）、item/（1 类）等包中。

## 2. 六层架构

- FACT-011: 系统分为六层（按依赖顺序）：玩家输入层 → 规则决策层 → 规划层 → Zone 运行时层 → 执行与诊断层 → 分析/dashboard 语义层。
- FACT-012: 六层按依赖方向排列，上层依赖下层，不允许反向依赖。
- FACT-013: 规则决策层 (rule/) — 回答"item 应该去哪一个 zone"，不关心 AE2 网络有哪些 cell。
- FACT-014: 规则决策层不依赖 Minecraft 或 AE2 运行时类。这是项目最重要的架构边界。
- FACT-015: 规则层只使用 Java 标准库、Gson、自定义纯 Java 工具类。
- FACT-016: 规则层包含 3 个子包：rule/filter/ (17 类)、rule/route/ (12 类)、rule/zone/ (2 类)。
- FACT-017: 规划层 — 将 item 事实转化为 item→zone 的计划，不决定具体 target cell。
- FACT-018: 规划层包含 ZoneAllocationPlanner (离线) 和 LiveZoneAllocationPlanner (在线)。
- FACT-019: Zone 运行时层 — 将逻辑 zone 映射到真实 cell。负责 zone-local 接纳、落点、治理。
- FACT-020: Zone 运行时层不负责 extract→insert→rollback 执行事务，不负责技术日志收集。
- FACT-021: Zone 运行时层核心类：RuntimeTopology、RuntimeZone、CellInfo、RuntimeZoneRegistryBuilder。CellInfo 是纯数据 record，替代已删除的 RuntimeCell（ADR-012）。
- FACT-022: RuntimeTopology 是"已解释后的世界"（zone 列表、machine 列表、cell 列表）。
- FACT-023: RuntimeTopology 允许适度偏胖（胖 topology 原则），以换取其他对象变薄。
- FACT-024: RuntimeTopology 不负责规则决策、搬运执行、持久化。
- FACT-025: RuntimeZoneRegistryBuilder 是 RuntimeTopology 的唯一构建入口。
- FACT-026: 执行与诊断层 — 真正执行 extract→insert→rollback，记录技术日志和复盘报告。
- FACT-027: 执行层不重新做 route，不重新定义 zone 策略。
- FACT-028: merge 和 zone move 两条路径共享同一执行引擎 SorterMoveOperation.execute()。
- FACT-029: logger（SorterFileLogger 等）一律视为基础设施层，不参与业务决策。
- FACT-030: 核心模型不因日志需求而增加字段或方法（Logger 不作为核心模型的一部分）。
- FACT-031: 分析/dashboard 语义层 — AE2 网络的观测、解释与语义审核层。
- FACT-032: 分析层产出 dashboard 可消费的 snapshot，提出节点语义候选。
- FACT-033: 分析层不直接执行搬运，不替用户做不可撤销的最终语义判定。
- FACT-034: 分析层当前只做最小搬运可行性判断，不展开完整网络分析体系。
- FACT-035: 分析层高度可疑节点默认按 infinite-like 处理，用户可纠偏（开箱即用优先原则）。
- FACT-036: analysis/ 和 profilegen/ 可离线运行，不污染在线命令链。
- FACT-037: 玩家输入层 — SorterCommandBlock GUI、DAV 扩容 GUI、Smart Bus 过滤器 GUI；Profile 文件绑定替代旧管理卡。
- FACT-038: DAV (Digital Asset Vault / 数字资产库) — 方块 ID `digital_asset_vault`，自带 2048 字节 / 126 types 基础容量（相当于 2×1k Cell），吸收空 Storage Cell 可继续扩容，作为 ME 存储节点。
- FACT-039: DAV 不承担 route 决策，不承担 zone move 执行；内部实现类仍带 `NewDav*` 前缀（历史命名）。
- FACT-040: **管理卡 (DigitalAssetManagementCard) 已在 beta 移除**；zone 数据改由 `config/appliedinsight/profiles/` + 网络绑定表达。
- FACT-041: SorterCommandBlock 是推荐给普通玩家的操作入口（GUI 按钮触发命令）。
- FACT-042: SorterCommandBlock 不承担命令执行逻辑，只提供网络接入点和 GUI 入口。
- FACT-043: SorterCommandBlock 与 `/sorter` 命令共享同一套后端服务。

## 3. 架构边界（硬约束）

- FACT-044: `rule/**` 不得导入 `net.minecraft.*` 或 `appeng.*`。
- FACT-045: `ae2/**` 不反向依赖 `application/`。
- FACT-046: `application/` 不包含持久化实现。
- FACT-047: `plan/**` 不承担执行职责。
- FACT-048: `logging/**` 不参与业务决策。
- FACT-049: `analysis/` 不污染在线命令链。
- FACT-050: `network/` 只负责网络包序列化/传输，不包含业务逻辑。
- FACT-051: `block/` + `blockentity/` 只负责方块声明和生命周期管理，不包含命令执行逻辑。
- FACT-052: 新增代码不得把 route、zone、cell、execute 混在一起。
- FACT-053: 运行时层不重做规则层决策。Ae2ZoneMoveExecutor 只消费 plan，不自行决定 route。
- FACT-054: dashboard 语义层不越权执行。analyzer 提出候选项，dashboard 展示并允许用户纠偏。
- FACT-055: 最终 world override 应作为事实层被后续流程消费。
- FACT-056: 前端契约层和运行时执行层严格分离。前端不决定具体 cell、运行时搬运顺序、zone 内部整理逻辑。
- FACT-057: 前端负责表达玩家规则意图、展示网络宏观事实、呈现系统建议、允许低成本纠偏。
- FACT-058: 前端不负责决定具体 cell、运行时搬运顺序、zone 内部整理逻辑、DAV 实时映射。

## 4. 三条能力路径

- FACT-059: 三条能力路径：主路径 (Plan/PlanAndMove)、简化 Merge 路径、宏观观测路径 (Storage Dashboard)。
- FACT-060: 三条能力路径独立演进，不强行统一。（ADR-006）
- FACT-061: 共享基础设施（扫描、日志、网络包）通过公共接口复用。
- FACT-062: 主路径 /sorter me planAndMove：完整治理链路 profile→route→zone plan→runtime topology→execute→log。
- FACT-063: 主路径内部链路：Ae2ControllerTargetResolver → NetworkProfileBindingStore → LiveZoneAllocationPlanner → RuntimeZoneRegistryBuilder → Ae2ZoneMoveExecutor → SorterPlanFileLogger。
- FACT-064: 主路径关键数据变换：规则决策 (ItemContext→RoutingDecision) → 规划 (RoutingDecision+RuntimeTopology→ZoneAllocationPlan) → 放置决策 (ZoneAllocationPlan→ZonePlacementDecision) → 执行 (ZonePlacementDecision→ExecutionResult)。
- FACT-065: 主路径命令顺序：`/sorter me bindProfile` → `/sorter me plan` → `/sorter me planAndMove`。
- FACT-066: 简化 Merge 路径 /sorter merge：低复杂度场景入口，同类物品合并到更少的 cell。
- FACT-067: Merge 路径内部链路：SorterMergeService → MergeMovePlanner → SorterMoveOperation → SorterMergeReportFileLogger。
- FACT-068: Merge 路径无需配置 profile/zone/DAV。
- FACT-069: Merge 路径不是规则化治理主线，但刻意保留的简化 merge 模式。
- FACT-070: Merge 路径不与 profile/zone/route 逻辑混用。
- FACT-071: 宏观观测路径 /sorter me storageDump：导出 AE2 网络宏观快照，识别 dominant storage / fragmentation / health。
- FACT-072: 宏观观测路径内部链路（ADR-012 迁移中）：SorterStorageAnalysisService → Ae2StorageAnalyzer.analyze(RuntimeTopology) → StorageDiagnosis → Dashboard 前端 / 日志。旧链路产出 StorageAnalyzerReport 逐步被 StorageDiagnosis 替代。
- FACT-073: storageDump 大文件通过 FileChunkedSender 分块推送到客户端。
- FACT-074: 宏观观测路径不是执行链路，是后续宏观调控体验的主界面基础。
- FACT-075: /sorter me 子命令共 7 个：dump、storageDump、listProfiles、bindProfile、showProfile、plan、planAndMove。
- FACT-076: SorterCommandBlock GUI（宽屏终端 440×224）包含操作按钮（分析、整理、合并）和存储概览面板，通过 SorterCommandPayload 发送命令到服务端。
- FACT-077: SorterMoveOperation 是两种能力线（merge 和 zone move）的共享批量执行引擎。
- FACT-078: SorterMoveOperation.execute() 执行 extract→insert→rollback 事务循环。
- FACT-079: Ae2ZoneMoveExecutor 是薄编排，消费 plan。

## 5. 关键设计决策（ADR 摘要）

- FACT-080: ADR-001（六层架构）：项目分为六层，每层明确职责和依赖方向。已采纳。
- FACT-081: ADR-002（规则层纯 Java）：rule/** 不得依赖 Minecraft 或 AE2 运行时。已采纳。
- FACT-082: ADR-003（胖 Topology）：RuntimeTopology 允许适度偏胖。已采纳。
- FACT-083: ADR-004（Logger 即基础设施）：Logger 视为独立基础设施层，不反向定义核心模型。已采纳。
- FACT-084: ADR-005（DAV 不是架构中心）：DAV 是管理入口，架构中心是规则模型、运行时模型、分析模型。已采纳。
- FACT-085: ADR-006（三条能力路径独立演进）：不强行统一 Merge、Zone、Dashboard 三条路径。已采纳。
- FACT-086: ADR-007（离线分析独立于运行时）：analysis/ 和 profilegen/ 通过 Gradle JavaExec 任务运行。已采纳。
- FACT-087: ADR-008（前端契约分离）：前端通过 JSON schema 与后端解耦，不直接依赖运行时。已采纳。
- FACT-088: ADR-009（HTTP API 认证）：Mod 内置账号管理，bcrypt + JWT，通过 `/sorter api` 命令管理。**提议阶段**。
- FACT-089: ADR-010（H2 嵌入式数据库）：替代纯 JSON 文件，支持 SQL 查询和增量更新。**提议阶段**。
- FACT-090: ADR-009（HTTP API 认证）和 ADR-010（H2 嵌入式数据库）属于项目远期规划，当前未实现。
- FACT-091: ADR-011（DAV 自动扩容）：容量阈值触发时从 ME 网络合成/提取并吸收 Cell。已采纳（默认阈值见 Config）。
- FACT-092: ADR-012（Ae2StorageAnalyzer 扶正）：将 Ae2StorageAnalyzer 升级为唯一运行时分析引擎，产出 StorageDiagnosis（含分析结论），替换 StorageAnalyzerReport（raw data）。RuntimeCell 替换为 CellInfo record。**部分实现中**。
- FACT-093: ADR-013（SmartBus）：单方块三模式（IMPORT/EXPORT/STORAGE）智能总线，卡片携带 filter 规则，复用 rule/filter DSL。已采纳并完成 Part 形态收口（ADR-014）。
- FACT-094: ADR-014（SmartBus 收口改造）：SmartBus 从独立 Block 改为 AE2 Part（SmartBusPart），卡片只存 filter JSON 不再存 mode。已采纳并实现。

## 6. 术语表（精简版，仅核心术语，中英文对照 + 一句话定义）

- **DAV (Digital Asset Vault / 数字资产库)** — 方块 ID `digital_asset_vault`，内置 2048 字节 / 126 types，吸收 Cell 可继续扩容，ME 存储节点。
- **管理卡 (DigitalAssetManagementCard)** — **已移除**（beta）；由 Profile 文件 + bindProfile 替代。
- FACT-093: **SorterCommandBlock (命令执行方块)** — GUI 操作入口，推荐给普通玩家，提供 3 个核心操作按钮和存储概览面板。
- FACT-094: **RoutingProfile (路由配置)** — 规则层的配置文件，包含一组 RouteRule。
- FACT-095: **RoutingEngine (路由引擎)** — 静态工具类，决定 item 去往哪个 zone。
- FACT-096: **RouteRule (路由规则)** — 定义 item→zone 映射条件（filter + target zone）。
- FACT-097: **ItemFilter (物品过滤器)** — 规则层 17 个纯模型类，定义 item 匹配条件。
- FACT-098: **Zone (存储区)** — 逻辑上的物品分组目标。StorageZone（纯配置）→ RuntimeZone（运行时实例）。
- FACT-099: **RuntimeZone (运行时 Zone)** — Zone 的运行时实例，继承 ZoneManager，负责放置/接纳判断。
- FACT-100: **RuntimeTopology (运行时拓扑)** — 一次 use-case 执行中的 ME 网络运行时世界模型。
- FACT-101: **CellInfo (单元信息)** — Cell 的纯数据 record 表示（ADR-012），含位置/容量/类型/zoneId + MEStorage 引用，替代已删除的 RuntimeCell。零行为方法，需要 per-item 数据时实时查询 AE2 live API。
- FACT-102: **Cell (存储单元)** — AE2 网络中真正存储物品的物理单元（硬盘项），系统最终操作对象。
- FACT-103: **ZoneAllocationPlan (Zone 分配计划)** — 规划层产出，item→zone 分配方案。
- FACT-104: **LiveZoneAllocationPlanner (在线规划器)** — 基于绑定的 profile/zone/filter 为 item 决定目标 zone。
- FACT-105: **Ae2ZoneMoveExecutor (Zone 搬运执行器)** — extract→insert→rollback 事务编排器，薄编排。
- FACT-106: **MergeMovePlanner (合并规划器)** — 快速合并路径的规划器，不与 profile/zone/route 混用。
- FACT-107: **SorterMoveOperation (搬运操作)** — 批量执行引擎，merge 和 zone move 共享。
- FACT-108: **Ae2StorageAnalyzer (存储分析器)** — 运行时分析引擎（ADR-012 扶正），输入 RuntimeTopology，产出 StorageDiagnosis（含分析结论：健康评估、碎片化、cell 分类、建议）。是 GUI/日志/聊天反馈的唯一共享分析入口。
- FACT-109: **Ae2MoveAnalyzer (搬运分析器)** — 回答"能不能搬进去"的移动前分析器。**当前孤立**：存在于代码中但未被任何能力路径调用。
- FACT-110: **SorterFileLogger / SorterPlanFileLogger / SorterMergeReportFileLogger** — 日志基础设施族，不参与业务决策。
- FACT-111: **RuntimeZoneRegistryBuilder** — RuntimeTopology 的唯一构建入口。
- FACT-112: **ZonePlacementDecision (放置决策)** — 描述 item 应落入哪个 cell 及落点分析。
- FACT-113: **Ae2DriveScanner (Drive 扫描器)** — 枚举网络内所有 drive 及其 cell。
- FACT-114: **DriveCellReference (扫描 Cell 引用)** — 扫描时产生的 cell 引用记录。
- FACT-115: **NetworkProfileBindingStore (网络绑定存储)** — 将 ME 网络 ID 映射到绑定的 RoutingProfile。
- FACT-116: **SorterCommandBlockScreen (命令方块 GUI)** — 渲染三栏终端界面：左列 3 个操作按钮，中列存储概览区块，右列诊断列表；通过 SorterCommandPayload 发送命令。
- FACT-117: **FileChunkedSender (分块文件发送器)** — 将大文件分块通过网络包推送给客户端。
- FACT-118: **SorterDumpService / SorterPlanService / SorterMergeService** — 应用服务层用例编排。
- FACT-119: **HeuristicRoutingProfileGenerator (启发式 Profile 生成器)** — 基于 dump 分析生成 zone/filter/route 草案。
- FACT-120: **Ae2ControllerTargetResolver (控制器目标解析器)** — 命令入口，解析玩家命令中的 grid 目标。
- FACT-121: **StorageDiagnosis (存储诊断)** — ADR-012 引入的统一分析结论 record，包含 NetworkSummary、per-zone 诊断、CellClassification 列表、HealthSignal（含 Severity）、Recommendation。替代 StorageAnalyzerReport 作为所有消费者的唯一共享分析入口。
- FACT-122: **SmartBus / SmartBusPart (智能总线)** — AE2 Part，IMPORT/EXPORT 模式 + JSON 过滤器，默认 12 stack/tick 吞吐；含 bundled 离线 filter editor（`tools/filter-editor/`）。
- FACT-123: **Digital Asset Vault (数字资产库)** — 唯一 DAV 实现（`digital_asset_vault`）；旧版 DAV / `new_digital_asset_vault` / 管理卡已注销。支持导入现有库存、自动接收 Cell、自动扩容（ADR-011）。

## 7. 日志产物

- FACT-124: 技术运行日志 → `logs/appliedinsight.log`（文本，命令摘要/异常上下文）。
- FACT-125: Plan 报告 → `logs/appliedinsight/plan-*.log`（文本，zone plan 明细）。
- FACT-126: PlanAndMove 报告 → `logs/appliedinsight/plan-and-move-*.log`（文本，执行明细）。
- FACT-127: Merge 报告 → `logs/appliedinsight/merge-*.log`（文本，merge 前后对比/收益）。
- FACT-128: ME Dump → `dumps/appliedinsight/me-dump-*.json`（JSON，cell 分布快照）。
- FACT-129: Storage Analysis → `dumps/appliedinsight/storage-analysis-*.json`（JSON，StorageDiagnosis 序列化结果）。
- FACT-130: JSON 格式版本为 1。

## 8. 版本与技术栈

- FACT-131: Minecraft 1.21.1, NeoForge 21.1.224, AE2 19.2.17。
- FACT-132: Java 21, Parchment Mappings 2024.11.17。
- FACT-133: Mod 版本 **0.9.2**（对外名：Applied Energistics: Insight / 应用能源：洞察）。
- FACT-134: 所有命令使用 Brigadier 框架注册；根命令仍为 `/sorter`。
- FACT-135: Config.java 含 ENABLE_SORTER、DEVELOPER_MODE、SMART_BUS_STACK_TRANSFERS_PER_TICK、DAV 自动扩容、Filter Editor URL 等。
- FACT-136: 客户端分析翻译层 (`client/analysis/`) — `AnalysisPresenter` 将服务端 `StorageDiagnosis` 转为 `PlayerFacingAnalysis`。
- FACT-137: `dark_matter_controller` 方块类仍保留于源码，**未注册**到游戏（材质测试用，beta 对玩家不可见）。

> **最后更新**: 2026-06-09（0.9.2 merge 路线收紧）
