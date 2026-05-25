# 术语表 (Glossary)

> 本文档定义 Applied Storage Sorter 项目中的全部关键术语。
> 格式：**中文名** (English) — 职责一句话 → 关键边界 → 关联文档

---

## 核心架构术语

### 层 (Layer)
| 术语 | 英文 | 定义 |
|------|------|------|
| **玩家输入层** | Player Input Layer | 负责玩家声明治理意图的 UI/交互层。包含 DAV 方块、管理卡、菜单、Screen |
| **规则决策层** | Rule Engine Layer | 纯规则模型层，回答"item 应该去哪一个 zone"。不依赖 Minecraft/AE2 运行时 |
| **规划层** | Planning Layer | 将 dump/live grid 中的 item 事实转化为 item→zone 的计划。产出 `ZoneAllocationPlan` |
| **Zone 运行时层** | Zone Runtime Layer | 将逻辑 zone 映射到真实 cell。负责 zone→cell 的接纳、落点、治理策略 |
| **分析/dashboard 语义层** | Analysis & Dashboard Layer | AE2 网络的观测、解释与语义审核层。产出 dashboard 可消费的 snapshot |
| **执行与诊断层** | Execution & Diagnostics Layer | 真正执行 extract→insert→rollback，记录技术日志和复盘报告 |

### 能力路径 (Capability Path)
| 术语 | 英文 | 定义 |
|------|------|------|
| **主路径** | Plan / PlanAndMove Path | 完整治理链路：profile→route→zone plan→runtime topology→execute→log |
| **简化 merge 路径** | Fast Merge Path | 低复杂度场景的快速合并命令 `/sorter merge` |
| **宏观观测路径** | Storage Dashboard Path | `/sorter me storageDump` → analyzer → dashboard → 语义审核 |

---

## Zone 族

### StorageZone
| 项目 | 内容 |
|------|------|
| **定义** | 逻辑存储区定义。物品最终去向的逻辑目标，由玩家通过 DAV/管理卡声明 |
| **位置** | `rule/zone/StorageZone.java`（规则层） |
| **关键边界** | 不包含运行时信息；不包含 cell 分配信息；是纯配置模型 |
| **关联类** | `ZoneStampedManagementCardRecipe`、`DigitalAssetManagementCardItem` |

### RuntimeZone
| 项目 | 内容 |
|------|------|
| **定义** | Zone 的运行时实例。在运行时构建，持有 zone 内成员/capabilities/本地诊断 |
| **位置** | `ae2/zone/RuntimeZone.java`（AE2 集成层） |
| **关键边界** | 不负责全局拓扑关系；不负责 extract→insert→rollback 执行事务；不负责技术日志收集 |
| **内部能力** | writable / mergeable / placement / admission 判断 |
| **关联类** | `RuntimeTopology`、`ZoneManager`、`ZonePlacementDecision` |

### SourceRuntimeZone
| 项目 | 内容 |
|------|------|
| **定义** | Source 侧运行时 zone。主路径 planAndMove 中的来源方等效视图 |
| **位置** | `ae2/zone/` |
| **关键边界** | 与目标 RuntimeZone 共用同一模型，但语义上代表"从哪搬" |

### ZoneManager
| 项目 | 内容 |
|------|------|
| **定义** | `RuntimeTopology` 内部的 zone 子域管理器。负责 zone 集合、查询、分类、跨 zone 视图 |
| **位置** | `ae2/zone/ZoneManager.java`（AE2 集成层） |
| **关键边界** | 已收敛为 topology 内部组件；不再承担"世界中心"角色；`RuntimeZone` 继承自它 |

---

## Cell 族

### Cell
| 项目 | 内容 |
|------|------|
| **定义** | AE2 网络中真正存储物品的物理单元（硬盘项）。系统最终操作的单位 |
| **类型** | AE2 内部存储 cell（ME drive 中的 item） |
| **关键边界** | 本模组不处理 external storage、storage bus、drawer |

### RuntimeCell
| 项目 | 内容 |
|------|------|
| **定义** | Cell 的运行时表示。包含位置、槽位、已用字节、剩余类型数等运行时事实 |
| **位置** | `ae2/zone/RuntimeCell.java`（AE2 集成层） |
| **关键边界** | 不承担 metrics/trace/sample/debug 汇总职责；不承担执行决策职责 |
| **关联类** | `RuntimeZone`、`DriveCellReference`、`ZonePlacementDecision` |

### DriveCellReference
| 项目 | 内容 |
|------|------|
| **定义** | 扫描时产生的 cell 引用记录。包含 cell 所在 drive 坐标、槽位、item key 列表 |
| **位置** | `ae2/scan/DriveCellReference.java` |
| **关联类** | `Ae2DriveScanner`、`Ae2DriveScanSummary` |

---

## DAV 族

### DigitalAssetVault (DAV)
| 项目 | 内容 |
|------|------|
| **定义** | 数字资产库方块。玩家声明 zone 的容器，让 zone 具有可视化和可持久化形式 |
| **位置** | `block/DigitalAssetVaultBlock.java`、`blockentity/DigitalAssetVaultBlockEntity.java` |
| **关键边界** | 不承担路由决策；不承担搬运执行；是管理入口而非架构中心本身 |
| **关联类** | `DigitalAssetVaultMenu`、`DigitalAssetVaultScreen` |

### DigitalAssetManagementCard
| 项目 | 内容 |
|------|------|
| **定义** | 管理卡物品。携带 zone 数据的可持久化物品，放入 DAV 后声明 zone |
| **位置** | `item/DigitalAssetManagementCardItem.java` |
| **关键边界** | 不直接承担规划和搬运职责；只负责携带 zone 数据 |
| **关联类** | `ZoneStampedManagementCardRecipe`（配方） |

---

## Profile 族

### RoutingProfile
| 项目 | 内容 |
|------|------|
| **定义** | 路由配置文件。包含一组 `RouteRule`，决定 item→zone 的映射规则 |
| **位置** | `rule/route/RoutingProfile.java`（纯规则层） |
| **关键边界** | 纯数据模型；不包含 Minecraft/AE2 依赖；通过 JSON codec 持久化 |
| **关联类** | `RoutingEngine`、`RouteRule`、`ItemFilter` |

### NetworkProfileBindingStore
| 项目 | 内容 |
|------|------|
| **定义** | 网络与 profile 的绑定存储。将 ME 网络 ID 映射到已绑定的 RoutingProfile |
| **位置** | `application/NetworkProfileBindingStore.java` |
| **关键边界** | 属于 application 层胶水；不承担规则执行职责 |

### RoutingEngine
| 项目 | 内容 |
|------|------|
| **定义** | 路由引擎。静态工具类，评估 RoutingProfile 对 item 上下文的匹配结果 |
| **位置** | `rule/route/RoutingEngine.java`（纯规则层） |
| **方法** | `decide()` → `RoutingDecision`，`explain()` → 解释为何匹配/不匹配 |
| **关键边界** | 不知道 AE2 网络里有哪些 cell；不知道最终会插入哪个 drive 槽位 |

---

## 运行时族

### RuntimeTopology
| 项目 | 内容 |
|------|------|
| **定义** | 一次 use-case 执行中当前 ME 网络的运行时世界模型。ConfigSnapshot + IGrid 的产物 |
| **位置** | `ae2/zone/RuntimeTopology.java`（AE2 集成层） |
| **核心组成** | `Map<String, RuntimeZone> zonesById`、`List<String> diagnostics`、`boolean degraded` |
| **关键原则** | 允许适度偏胖的运行时核心对象；胖 topology 换取其他对象变薄 |
| **关联类** | `RuntimeZoneRegistryBuilder`（唯一构建入口） |

### RuntimeZoneRegistryBuilder
| 项目 | 内容 |
|------|------|
| **定义** | 运行时拓扑的唯一构建入口。根据 DAV 声明 + 网络扫描结果 → RuntimeTopology |
| **位置** | `ae2/zone/RuntimeZoneRegistryBuilder.java` |
| **关键边界** | buildTopology() 是对外唯一运行时构建入口；已取代旧的 RuntimeZoneRegistry |

### ZonePlacementDecision
| 项目 | 内容 |
|------|------|
| **定义** | Zone 放置决策结果。描述 item 应落入哪个 cell 及落点分析 |
| **位置** | `ae2/zone/ZonePlacementDecision.java` |
| **关联类** | `RuntimeZone`（`doPlanPlacement()` 产出） |

---

## 规则族

### RouteRule
| 项目 | 内容 |
|------|------|
| **定义** | 路由规则。定义 item→zone 的映射条件。包含 filter + target zone 引用 |
| **位置** | `rule/route/RouteRule.java`（纯规则层） |
| **关联类** | `RoutingProfile`、`ItemFilter` |

### ItemFilter
| 项目 | 内容 |
|------|------|
| **定义** | 物品过滤器。定义 item 匹配条件（item id、mod id、tag、名称等） |
| **位置** | `rule/filter/`（纯规则层，17 个类型） |
| **关键边界** | 纯模型；无 Minecraft/AE2 依赖 |

---

## 规划族

### ZoneAllocationPlan
| 项目 | 内容 |
|------|------|
| **定义** | Zone 分配计划。规划层产出的计划结果，描述 item→zone 分配方案 |
| **位置** | `plan/ZoneAllocationPlan.java`（规划模型层） |
| **关联类** | `LiveZoneAllocationPlanner`、`ItemZoneAssignment` |

### LiveZoneAllocationPlanner
| 项目 | 内容 |
|------|------|
| **定义** | 在线规划器。基于当前 ME 网络绑定的 profile/zone/filter/router，为 item 决定目标 zone |
| **位置** | `ae2/zone/LiveZoneAllocationPlanner.java` |

### ZoneAllocationPlannerAnalyzer
| 项目 | 内容 |
|------|------|
| **定义** | 离线规划分析器。脱机分析 dump 文件，生成 zone 分配计划供审查 |
| **位置** | `analysis/ZoneAllocationPlannerAnalyzer.java` |

---

## 执行族

### Ae2ZoneMoveExecutor
| 项目 | 内容 |
|------|------|
| **定义** | 执行器。真正执行 extract→insert→rollback 的事务编排器 |
| **位置** | `ae2/zone/Ae2ZoneMoveExecutor.java`（AE2 集成层） |
| **方法** | `execute()`、`executeDetailed()` |
| **关键边界** | 不重新做 route；不重新定义 zone 策略；消费 plan 不自行决定 route |
| **当前约束** | `MAX_SAMPLE_MESSAGES = 100` |

### MergeMovePlanner
| 项目 | 内容 |
|------|------|
| **定义** | 快速合并规划器。负责 `/sorter merge` 路径的合并规划 |
| **位置** | `ae2/sort/MergeMovePlanner.java` |
| **关键边界** | 不与 profile/zone/route 逻辑混用；是保留的简化 merge 模式 |

### PlannedMove
| 项目 | 内容 |
|------|------|
| **定义** | 计划搬运单元。描述一次搬运操作（从哪到哪、搬什么、搬多少） |
| **位置** | `ae2/sort/PlannedMove.java` |

### SorterMoveOperation
| 项目 | 内容 |
|------|------|
| **定义** | 搬运操作记录。merge 路径中的执行操作单元 |
| **位置** | `ae2/sort/SorterMoveOperation.java` |

### ZoneMoveExecutionResult / ZoneMoveExecutionDetailedResult
| 项目 | 内容 |
|------|------|
| **定义** | 执行结果模型。记录整体执行结果与详细逐操作结果 |
| **位置** | `ae2/zone/ZoneMoveExecutionResult.java`、`ZoneMoveExecutionDetailedResult.java` |
| **关联类** | `ZoneMoveExecutionDebugReport`（调试报告） |

---

## 分析族

### Ae2StorageAnalyzer
| 项目 | 内容 |
|------|------|
| **定义** | 存储分析器。从 live AE2 network 提取宏观快照，生成 StorageAnalyzerReport |
| **位置** | `ae2/analysis/Ae2StorageAnalyzer.java` |
| **产出** | 识别 dominant storage / fragmentation / health / 疑似无限容器 |

### Ae2MoveAnalyzer
| 项目 | 内容 |
|------|------|
| **定义** | 搬运可行性分析器。回答"能不能搬进去"，给出最小阻塞原因 |
| **位置** | `ae2/analysis/Ae2MoveAnalyzer.java` |
| **当前范围** | 只做最小搬运可行性判断；不展开完整网络分析体系 |

### StorageAnalyzerReport
| 项目 | 内容 |
|------|------|
| **定义** | 存储分析报告。dashboard 可消费的分析结果 |
| **位置** | `ae2/analysis/StorageAnalyzerReport.java` |

### SorterStorageAnalysisService
| 项目 | 内容 |
|------|------|
| **定义** | 存储分析服务。`/sorter me storageDump` 命令的后端编排 |
| **位置** | `application/SorterStorageAnalysisService.java` |
| **产出** | `SorterStorageAnalysisDumpWriter` + `SorterFileLogger` |

---

## 日志族

### SorterFileLogger
| 项目 | 内容 |
|------|------|
| **定义** | 通用文件日志器。负责 `logs/appliedstoragesorter.log` 的技术日志输出 |
| **位置** | `logging/SorterFileLogger.java` |
| **基础设施定位** | 属于基础设施层；不参与业务决策 |

### SorterPlanFileLogger
| 项目 | 内容 |
|------|------|
| **定义** | Plan 文件日志器。负责 plan/planAndMove 的报告型输出 |
| **位置** | `logging/SorterPlanFileLogger.java` |

### SorterMergeReportFileLogger
| 项目 | 内容 |
|------|------|
| **定义** | Merge 报告日志器。负责 merge 前后分布、收益、明细的复盘报告 |
| **位置** | `logging/SorterMergeReportFileLogger.java` |

### ReportFileSupport
| 项目 | 内容 |
|------|------|
| **定义** | 报告文件基础设施。日志器的基础支持工具 |
| **位置** | `logging/ReportFileSupport.java` |

---

## 命令族

### /sorter merge
| 项目 | 内容 |
|------|------|
| **语义** | 快速合并命令。把同类物品尽量合并到更少的 cell |
| **后端** | `SorterMergeService` → `MergeMovePlanner` → `SorterMergeReportFileLogger` |
| **关键边界** | 不涉及 profile/zone/route 逻辑 |

### /sorter me dump
| 项目 | 内容 |
|------|------|
| **语义** | 导出当前 ME 网络的 cell 分布 JSON dump |
| **后端** | `SorterDumpService` → `SorterNetworkDumpWriter` |

### /sorter me storageDump
| 项目 | 内容 |
|------|------|
| **语义** | 导出 storage analysis 宏观快照（含 analyzer 分析） |
| **后端** | `SorterStorageAnalysisService` → `Ae2StorageAnalyzer` → dashboard snapshot |

### /sorter me bindProfile
| 项目 | 内容 |
|------|------|
| **语义** | 将 RoutingProfile 绑定到当前 ME 网络 |
| **后端** | `SorterProfileBindingService` → `NetworkProfileBindingStore` |

### /sorter me showProfile
| 项目 | 内容 |
|------|------|
| **语义** | 查看当前绑定的 RoutingProfile |
| **后端** | `SorterProfileBindingService` |

### /sorter me plan
| 项目 | 内容 |
|------|------|
| **语义** | 生成 zone 分配计划预览 |
| **后端** | `SorterPlanService` → `LiveZoneAllocationPlanner` → `SorterPlanFileLogger` |

### /sorter me planAndMove
| 项目 | 内容 |
|------|------|
| **语义** | 生成 zone 分配计划并执行搬运 |
| **后端** | `SorterPlanService` + `Ae2ZoneMoveExecutor` → `SorterPlanFileLogger` |

---

## 其他重要术语

| 中文 | 英文 | 定义 |
|------|------|------|
| **Ae2ControllerTargetResolver** | — | 命令层入口，解析玩家命令中的 grid 目标 |
| **Ae2GridTargetResult** | — | 目标解析结果，包含 grid/controller/block 等上下文 |
| **Ae2DriveScanner** | — | ME drive 扫描器，枚举网络内所有 drive 及其 cell |
| **Ae2DriveScanSummary** | — | 扫描摘要，汇总全网 cell 分布 |
| **DriveMachineAccessor** | — | drive 机器访问器。使用反射+block id 白名单适配 drive-like 机器 |
| **Config** | — | 模组配置类。含 `ENABLE_SORTER` 等开关 |
| **AppliedStorageSorter** | — | 模组主入口类。NeoForge mod 初始化 |
| **SorterCommands** | — | 命令注册入口。使用 Brigadier 框架 |
| **NetworkBindingKey** | — | 网络绑定键。标识一个 ME 网络的身份 |
| **SorterDumpService** | — | dump 服务编排 |
| **SorterPlanService** | — | plan 服务编排 |
| **SorterMergeService** | — | merge 服务编排 |
| **SorterProfileBindingService** | — | profile 绑定服务编排 |
| **SorterDumpAnalyzer** | — | dump 分析器，离线分析 cell 分布数据 |
| **SorterDumpProfileGenerationAnalyzer** | — | dump 规则草案生成器 |
| **SorterDumpRoutingAnalyzer** | — | dump 路由分析器 |
| **SorterDumpRoutingSuggestionAnalyzer** | — | dump 路由建议分析器 |

---

## 项目版本信息

| 项目 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.224 |
| AE2 (Applied Energistics 2) | 19.2.17 [19.2.17, 20) |
| Parchment Mappings | 2024.11.17 |
| Mod Version | 1.0.0 |
| Java | 21 |
| JSON 格式版本 | 1 |
| Mod ID | `appliedstoragesorter` |

---

> **文档导航**
> - 架构全景：`docs/ARCHITECTURE_REFERENCE.md`
> - 整体逻辑：`docs/整体逻辑.md`
> - 类职责总览：`docs/类职责总览.md`
> - 类职责索引：`docs/类职责/索引.md`
> - AI 协作者入口：`docs/ai/AI_ENTRY.md`
