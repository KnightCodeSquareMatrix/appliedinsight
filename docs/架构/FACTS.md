# 事实库 (Fact Sheet)

> 本文档是 Applied Storage Sorter 的原子化事实集合。
> 所有事实按分类编号，可被 AI 精确引用（如 "FACT-013 规定了规则层零 AE2 依赖"）。
> 如果事实与代码不一致，以代码为准。

## 1. 项目定位

- FACT-001: Applied Storage Sorter 是一个 Minecraft NeoForge 模组，v1.21.1。
- FACT-002: 模组为 AE2 (Applied Energistics 2) 提供存储治理能力。
- FACT-003: 模组目前仅面向 AE2 内部 drive 存储。
- FACT-004: 支持的 drive 方块：`ae2:drive`、`extendedae:ex_drive`。
- FACT-005: 模组不处理 external storage、storage bus、drawer。
- FACT-006: 底层操作对象始终是 Cell（AE2 硬盘项）。
- FACT-007: Mod ID 为 `appliedstoragesorter`。
- FACT-008: 包路径根为 `com.knightcode.appliedstoragesorter`。
- FACT-009: Java 21，Parchment Mappings 2024.11.17，AE2 19.2.17。
- FACT-010: 模组代码约 31 个纯规则模型类 + 24 个 AE2 集成类 + 9 个应用服务类。

## 2. 六层架构（按依赖顺序：api → application → domain → ae2 → infrastructure → client）

- FACT-011: 系统分为六层：玩家输入层 → 规则决策层 → 规划层 → Zone 运行时层 → 执行与诊断层 → 分析/dashboard 语义层。
- FACT-012: 六层按依赖方向排列，上层依赖下层，不允许反向依赖。
- FACT-013: 规则决策层 (rule/) — 回答"item 应该去哪一个 zone"，不关心 AE2 网络有哪些 cell。
- FACT-014: 规则决策层不依赖 Minecraft 或 AE2 运行时类。这是项目最重要的架构边界。
- FACT-015: 规则层只使用 Java 标准库、Gson、自定义纯 Java 工具类。
- FACT-016: 规则层包含 3 个子包：rule/filter/ (17 类)、rule/route/ (12 类)、rule/zone/ (2 类)。
- FACT-017: 规划层 — 将 item 事实转化为 item→zone 的计划，不决定具体 target cell。
- FACT-018: 规划层包含 ZoneAllocationPlanner (离线) 和 LiveZoneAllocationPlanner (在线)。
- FACT-019: Zone 运行时层 — 将逻辑 zone 映射到真实 cell。负责 zone-local 接纳、落点、治理。
- FACT-020: Zone 运行时层不负责 extract→insert→rollback 执行事务，不负责技术日志收集。
- FACT-021: Zone 运行时层核心类：RuntimeTopology、RuntimeZone、RuntimeCell、RuntimeZoneRegistryBuilder。
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
- FACT-037: 玩家输入层 — 让玩家声明 zone，让 DAV 成为 zone 容器，让输入具有可视化和可持久化形式。
- FACT-038: DAV (DigitalAssetVault) 是管理入口，不是架构中心本身。
- FACT-039: DAV 不承担路由决策，不承担搬运执行。
- FACT-040: 管理卡 (DigitalAssetManagementCard) 只负责携带 zone 数据，不承担规划和搬运职责。
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
- FACT-072: 宏观观测路径内部链路：SorterStorageAnalysisService → Ae2StorageAnalyzer → StorageAnalyzerReport → SorterStorageAnalysisDumpWriter → Dashboard 前端。
- FACT-073: storageDump 大文件通过 FileChunkedSender 分块推送到客户端。
- FACT-074: 宏观观测路径不是执行链路，是后续宏观调控体验的主界面基础。
- FACT-075: /sorter me 子命令共 5 个：dump、bindProfile、showProfile、plan、planAndMove、storageDump。
- FACT-076: SorterCommandBlock GUI 包含 5 个功能按钮：扫描网络、存储分析、生成计划、规划并搬运、整理合并。
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
- FACT-090: ADR-009 和 ADR-010 属于项目远期规划，当前未实现。

## 6. 术语表（精简版，仅核心术语，中英文对照 + 一句话定义）

- FACT-091: **DAV (DigitalAssetVault / 数字资产库)** — 方块的 zone 容器，管理入口，不是架构中心。
- FACT-092: **管理卡 (DigitalAssetManagementCard / ZoneCard)** — 携带 zone 数据的可持久化物品。
- FACT-093: **SorterCommandBlock (命令执行方块)** — GUI 操作入口，推荐给普通玩家，5 个功能按钮。
- FACT-094: **RoutingProfile (路由配置)** — 规则层的配置文件，包含一组 RouteRule。
- FACT-095: **RoutingEngine (路由引擎)** — 静态工具类，决定 item 去往哪个 zone。
- FACT-096: **RouteRule (路由规则)** — 定义 item→zone 映射条件（filter + target zone）。
- FACT-097: **ItemFilter (物品过滤器)** — 规则层 17 个纯模型类，定义 item 匹配条件。
- FACT-098: **Zone (存储区)** — 逻辑上的物品分组目标。StorageZone（纯配置）→ RuntimeZone（运行时实例）。
- FACT-099: **RuntimeZone (运行时 Zone)** — Zone 的运行时实例，继承 ZoneManager，负责放置/接纳判断。
- FACT-100: **RuntimeTopology (运行时拓扑)** — 一次 use-case 执行中的 ME 网络运行时世界模型。
- FACT-101: **RuntimeCell (运行时 Cell)** — Cell 的运行时表示，含位置/槽位/已用字节/剩余类型数。
- FACT-102: **Cell (存储单元)** — AE2 网络中真正存储物品的物理单元（硬盘项），系统最终操作对象。
- FACT-103: **ZoneAllocationPlan (Zone 分配计划)** — 规划层产出，item→zone 分配方案。
- FACT-104: **LiveZoneAllocationPlanner (在线规划器)** — 基于绑定的 profile/zone/filter 为 item 决定目标 zone。
- FACT-105: **Ae2ZoneMoveExecutor (Zone 搬运执行器)** — extract→insert→rollback 事务编排器，薄编排。
- FACT-106: **MergeMovePlanner (合并规划器)** — 快速合并路径的规划器，不与 profile/zone/route 混用。
- FACT-107: **SorterMoveOperation (搬运操作)** — 批量执行引擎，merge 和 zone move 共享。
- FACT-108: **Ae2StorageAnalyzer (存储分析器)** — 从 live AE2 network 提取宏观快照，识别碎片化/无限容器。
- FACT-109: **Ae2MoveAnalyzer (搬运分析器)** — 回答"能不能搬进去"，给出最小阻塞原因。
- FACT-110: **SorterFileLogger / SorterPlanFileLogger / SorterMergeReportFileLogger** — 日志基础设施族，不参与业务决策。
- FACT-111: **RuntimeZoneRegistryBuilder** — RuntimeTopology 的唯一构建入口。
- FACT-112: **ZonePlacementDecision (放置决策)** — 描述 item 应落入哪个 cell 及落点分析。
- FACT-113: **Ae2DriveScanner (Drive 扫描器)** — 枚举网络内所有 drive 及其 cell。
- FACT-114: **DriveCellReference (扫描 Cell 引用)** — 扫描时产生的 cell 引用记录。
- FACT-115: **NetworkProfileBindingStore (网络绑定存储)** — 将 ME 网络 ID 映射到绑定的 RoutingProfile。
- FACT-116: **SorterCommandBlockScreen (命令方块 GUI)** — 渲染 5 个按钮，通过 SorterCommandPayload 发送命令。
- FACT-117: **FileChunkedSender (分块文件发送器)** — 将大文件分块通过网络包推送给客户端。
- FACT-118: **SorterDumpService / SorterPlanService / SorterMergeService** — 应用服务层用例编排。
- FACT-119: **HeuristicRoutingProfileGenerator (启发式 Profile 生成器)** — 基于 dump 分析生成 zone/filter/route 草案。
- FACT-120: **Ae2ControllerTargetResolver (控制器目标解析器)** — 命令入口，解析玩家命令中的 grid 目标。

## 7. 日志产物

- FACT-121: 技术运行日志 → `logs/appliedstoragesorter.log`（文本，命令摘要/异常上下文）。
- FACT-122: Plan 报告 → `logs/appliedstoragesorter/plan-*.log`（文本，zone plan 明细）。
- FACT-123: PlanAndMove 报告 → `logs/appliedstoragesorter/plan-and-move-*.log`（文本，执行明细）。
- FACT-124: Merge 报告 → `logs/appliedstoragesorter/merge-*.log`（文本，merge 前后对比/收益）。
- FACT-125: ME Dump → `dumps/appliedstoragesorter/me-dump-*.json`（JSON，cell 分布快照）。
- FACT-126: Storage Analysis → `dumps/appliedstoragesorter/storage-analysis-*.json`（JSON，analyzer 分析结果）。
- FACT-127: JSON 格式版本为 1。

## 8. 版本与技术栈

- FACT-128: Minecraft 1.21.1, NeoForge 21.1.224, AE2 19.2.17。
- FACT-129: Java 21, Parchment Mappings 2024.11.17。
- FACT-130: Mod 版本 1.0.0。
- FACT-131: 所有命令使用 Brigadier 框架注册。
- FACT-132: Config.java 包含 ENABLE_SORTER 等开关配置。

> **最后更新**: 2026-05-28
