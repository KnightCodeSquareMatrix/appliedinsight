# Raw Architecture Audit — Applied Energistics: Insight

> ⚠️ **历史审计快照**（生成于 2026-06-04，commit 3fd3cc8）。**大量章节已过时**（双 DAV、管理卡、Mod ID 等）。
> **当前权威来源**：[`FACTS.md`](FACTS.md) · 注册表源码 `registry/Sorter*.java` · [`类职责总览.md`](../类职责总览.md)。
>
> 下文保留供 diff 参考；维度 4/6 中关于「两个 DAV」的描述 **不再反映 0.9.0-beta 注册表**。

---

## 0.9.0-beta 注册表快照（2026-06-08 · 以源码为准）

| 类型 | ID / 类 | 状态 |
|------|---------|------|
| Mod ID | `appliedinsight` | 活跃 |
| 方块 | `digital_asset_vault` → `DigitalAssetVaultBlock` | 活跃 |
| 方块 | `sorter_command_block` → `SorterCommandBlock` | 活跃 |
| Part | `smart_bus` → `SmartBusPart` | 活跃 |
| 方块 | `dark_matter_controller` | **未注册**（源码保留） |
| 物品 | `digital_asset_management_card` | **已移除** |
| 旧 legacy DAV / `new_digital_asset_vault` | — | **已移除** |

> **生成日期**: 2026-06-04
> **目的**: 实际代码状态的镜像快照，用于审阅"代码实际构建了什么"与"文档描述了什么"之间的一致性。
> **定位**: 不替代 `FACTS.md`（规范文档），不替代 `docs/类职责/`（职责文档）。是审计工具，审阅后可以标注"已确认"/"需修复"/"已过时"。
> **代码基线**: main 分支, commit 3fd3cc8

---

## 目录

1. [维度 1: 代码物理结构清单](#维度-1-代码物理结构清单)
2. [维度 2: 类依赖矩阵与边界违规](#维度-2-类依赖矩阵与边界违规)
3. [维度 3: 三条能力线调用链追踪](#维度-3-三条能力线调用链追踪)
4. [维度 4: 注册项全量清单](#维度-4-注册项全量清单)
5. [维度 5: 文档覆盖率矩阵](#维度-5-文档覆盖率矩阵)
6. [维度 6: 两个 DAV 实现对比](#维度-6-两个-dav-实现对比)
7. [维度 7: 技术债/不一致清单](#维度-7-技术债不一致清单)

---

## 维度 1: 代码物理结构清单

### 总体规模

| 指标 | 数值 |
|------|------|
| Java 源文件总数 (src/main/java) | **147** |
| 总代码行数 | **15,164** |
| 测试文件数 (src/test) | **4** |
| 包数量 | **20** (含子包) |

### 按层分类统计

| 层 | 包 | 文件数 | 总行数 | 说明 |
|----|-----|--------|--------|------|
| **入口/装配** | root | 2 | ~96 | `AppliedStorageSorter.java` (79行), `AppliedStorageSorterClient.java` (17行) |
| **配置** | root | 1 | 84 | `Config.java` — 所有 ModConfigSpec 定义 |
| **注册装配** | registry/ | 7 | ~253 | Blocks, Items, BlockEntities, Menus, Capabilities, CreativeTabs, RecipeSerializers |
| **方块声明** | block/ | 3 | ~178 | 旧DAV (38行), 新DAV (70行), SCB (70行) |
| **方块实体** | blockentity/ | 3 | ~1035 | 旧DAV BE (105行), 新DAV BE (779行), SCB BE (151行) |
| **物品** | item/ | 1 | 148 | `DigitalAssetManagementCardItem` |
| **容器菜单** | menu/ + menu/slot/ | 5 | ~512 | 旧DAV Menu (100行), 新DAV Menu (301行), SCB Menu (58行), 2个Slot |
| **AE2 集成** | ae2/ (含7个子包) | 39 | ~3,900 | 核心引擎层 |
| **应用服务** | application/ + result/ | 13 | ~680 | 编排服务 + 结果VO |
| **网络通信** | network/ | 10 | ~540 | 8个Payload + 2个Handler + FileChunkedSender |
| **纯规则模型** | rule/ (含3个子包) | 32 | ~2,100 | **零 Minecraft/AE2 依赖** |
| **规划模型** | plan/ | 3 | ~278 | **零 Minecraft/AE2 依赖** |
| **离线分析** | analysis/ | 5 | ~1,420 | **零 Minecraft/AE2 依赖** |
| **Profile生成** | profilegen/ | 4 | ~288 | **零 Minecraft/AE2 依赖** |
| **日志** | logging/ | 4 | ~1,635 | 文件日志 (有 net.minecraft 边界违规) |
| **客户端** | client/ (含3个子包) | 10 | ~1,410 | 屏幕、分析展示器、格式化器、Widget |
| **命令** | command/ | 2 | ~1,030 | SorterCommands (109行), GenTestItemsCommand (921行) |

### 每个文件的详细清单 (按包，含行数)

#### 入口层 (root)
- `AppliedStorageSorter.java` — 79 行 — `@Mod` 主入口，注册 DeferredRegister、Config、网络 Payload、命令
- `AppliedStorageSorterClient.java` — 17 行 — `@Mod(dist=CLIENT)` 客户端入口，注册 Screens
- `Config.java` — 84 行 — ModConfigSpec: ENABLE_SORTER, DEVELOPER_MODE, MAX_TRANSFERS, VERBOSE_LOGGING, DEBUG_LOGGING, ENERGY_COST_*, DAV_AUTO_EXPAND_*

#### 注册层 (registry/)
- `SorterBlocks.java` — 40 行 — 3 个方块: DIGITAL_ASSET_VAULT, digital_asset_vault, SORTER_COMMAND_BLOCK
- `SorterItems.java` — 35 行 — 4 个物品: 管理卡 + 3 个 BlockItem
- `SorterBlockEntities.java` — 51 行 — 3 个 BE 类型，DAV 含 AE2 连线
- `SorterMenus.java` — 51 行 — 3 个 MenuType (含 IMenuTypeExtension)
- `SorterCapabilities.java` — 27 行 — 注册 IN_WORLD_GRID_NODE_HOST 到 3 个 BE
- `SorterCreativeTabs.java` — 21 行 — 方块→FUNCTIONAL_BLOCKS, 管理卡→INGREDIENTS
- `SorterRecipeSerializers.java` — ~28 行 — ZoneStampedManagementCardRecipe 序列化器

#### 方块层 (block/)
- `DigitalAssetVaultBlock.java` — 38 行 — extends AEBaseEntityBlock<DigitalAssetVaultBlockEntity>
- `DigitalAssetVaultBlock.java` — 70 行 — extends Block implements EntityBlock
- `SorterCommandBlock.java` — 70 行 — extends HorizontalDirectionalBlock implements EntityBlock

#### 方块实体层 (blockentity/)
- `DigitalAssetVaultBlockEntity.java` — 105 行 — extends DriveBlockEntity, 管理卡槽
- `DigitalAssetVaultBlockEntity.java` — 779 行 — 自定义 MEStorage 实现, 13 状态状态机, cell 吸收, 自动扩容
- `SorterCommandBlockEntity.java` — 151 行 — implements IInWorldGridNodeHost, 管理 AE2 节点生命周期

#### AE2 集成层 (ae2/)

| 子包 | 类 | 行数 | 继承/实现 | 核心职责 |
|------|-----|------|-----------|----------|
| ae2/ | `Ae2ControllerTargetResolver` | 80 | -- | 射线追踪网格目标解析 (玩家看向 ME Controller) |
| ae2/ | `Ae2GridTargetResult` | 61 | record | 网格目标解析结果，含工厂方法 |
| ae2/ | `CellCapacityInspector` | 221 | -- | 反射读取 StorageCell 容量；检测无限/创造 Cell |
| ae2/ | `GridNetworkLocator` | 43 | -- | 稳定网络锚点定位 (E>S>W>N 优先级) |
| ae2/ | `GridTargetResolver` | 15 | @FunctionalInterface | 策略接口：解析 CommandSource → GridTarget |
| ae2/ | `SorterMeScanResult` | 118 | record | 增强扫描结果 (含 drive/cell/item 计数) |
| ae2/analysis/ | `Ae2MoveAnalyzer` | 53 | -- | 移动前分析 (zone 存在性 + 分配有效性检查) |
| ae2/analysis/ | `Ae2StorageAnalyzer` | 448 | -- | 全量存储分析引擎 (碎片化、健康标志、无限容器检测) |
| ae2/analysis/ | `RuntimeMoveAnalysisReport` | 7 | record | 移动可接受性检查结果 |
| ae2/analysis/ | `StorageAnalyzerReport` | 89 | record | 综合分析报告 (Gson 可序列化) |
| ae2/dav/ | `NewDavStorage` | 64 | implements MEStorage | 新 DAV 的 MEStorage 实现 (委托给 BE) |
| ae2/dav/ | `NewDavStorageProvider` | 19 | implements IStorageProvider | 挂载 DAV 存储 (优先级 100000) |
| ae2/dump/ | `SorterNetworkDumpResult` | 9 | record | Dump 结果 |
| ae2/dump/ | `SorterNetworkDumpWriter` | 342 | -- | 全量网络 dump JSON (v1 格式) |
| ae2/dump/ | `SorterStorageAnalysisDumpResult` | 17 | record | 分析 dump 结果 |
| ae2/dump/ | `SorterStorageAnalysisDumpWriter` | 84 | -- | 委托 Ae2StorageAnalyzer, 写入分析 JSON |
| ae2/scan/ | `Ae2DriveScanSummary` | 10 | record | 扫描摘要 |
| ae2/scan/ | `Ae2DriveScanner` | 62 | -- | 扫描 grid 所有 drive, 识别重复 item key |
| ae2/scan/ | `DriveCellReference` | 19 | record | Cell 标识符 |
| ae2/scan/ | `DriveMachineAccessor` | 348 | -- | **核心门面**: 发现所有支持 drive, 封装 cell 访问 |
| ae2/sort/ | `EnergyCostCalculator` | 161 | -- | 对数能耗公式 (位运算 log2) |
| ae2/sort/ | `EnergyCostEstimate` | 45 | record | 能耗价值对象 |
| ae2/sort/ | `MergeMovePlanner` | 91 | -- | 合并规划：同类 item 归并到第一个出现的 cell |
| ae2/sort/ | `PlannedMove` | 8 | record | 单个计划搬运 |
| ae2/sort/ | `SorterMoveExecutionResult` | 39 | record | 执行结果 |
| ae2/sort/ | `SorterMoveOperation` | 152 | -- | **共享执行引擎**: extract→insert→rollback 事务 |
| ae2/zone/ | `Ae2ZoneMoveExecutor` | 103 | -- | 区域移动编排器 (薄层) |
| ae2/zone/ | `LiveZoneAllocationPlanner` | 155 | -- | 在线规划：扫描 grid → 指纹识别 item → RoutingEngine 决策 → ZoneAllocationPlan |
| ae2/zone/ | `RuntimeCell` | 95 | -- | 运行时 Cell 包装 (含 simulateAcceptedAmount) |
| ae2/zone/ | `RuntimeTopology` | 71 | -- | 运行时拓扑世界模型 |
| ae2/zone/ | `RuntimeZone` | 68 | extends ZoneManager | 具体 zone: 放置策略实现 |
| ae2/zone/ | `RuntimeZoneRegistryBuilder` | 58 | -- | 从 grid 构建 RuntimeTopology |
| ae2/zone/ | `ZoneManager` | 95 | abstract | Zone 管理抽象基类 |
| ae2/zone/ | `ZoneMergePlanner` | 316 | -- | 区域合并规划 (指纹匹配 → 放置决策 → SorterMoveOperation) |
| ae2/zone/ | `ZoneMoveExecutionDebugReport` | 31 | record | 调试诊断 |
| ae2/zone/ | `ZoneMoveExecutionDetailedResult` | 12 | record | 详细执行结果 |
| ae2/zone/ | `ZoneMoveExecutionResult` | 20 | record | 执行结果 |
| ae2/zone/ | `ZonePlacementDecision` | 44 | record | 放置决策 |

#### 应用服务层 (application/)
- `NetworkBindingKey.java` — 24 — record: dimensionId + controllerPos → 存储 key
- `NetworkProfileBindingStore.java` — 87 — 持久化网络↔profile绑定 → JSON 文件
- `NewDavMigrationService.java` — 16 — **桩**: migrateOnce() 始终返回 idle
- `SorterComponentHelper.java` — 59 — 聊天 Component 构建工具
- `SorterDumpService.java` — 64 — 编排 /sorter me dump
- `SorterMergeService.java` — 82 — 编排 /sorter merge
- `SorterPlanService.java` — 161 — 编排 /sorter me plan / planAndMove
- `SorterProfileBindingService.java` — 124 — 编排 profile 绑定命令
- `SorterStorageAnalysisService.java` — 127 — 编排 /sorter me storageDump + 分块传输
- `result/NewDavMigrationResult.java` — 14 — record
- `result/SorterDumpCommandResult.java` — 17 — record
- `result/SorterFeedbackResult.java` — 31 — record (通用命令结果)
- `result/SorterMergeCommandResult.java` — 17 — record (存在但未在主流程使用)
- `result/SorterStorageAnalysisCommandResult.java` — 26 — record

#### 网络层 (network/)
- `FileChunkPayload.java` — 41 — record: fileId + chunkIndex + totalChunks + byte[]
- `FileChunkPayloadHandler.java` — 90 — 客户端分块重组 (ConcurrentHashMap)
- `FileChunkedSender.java` — 55 — 服务端分块发送 (128KB/chunk)
- `NewDavSetExpansionCellPayload.java` — 26 — record: BlockPos + expansionCellId
- `NewDavSetExpansionCellPayloadHandler.java` — 30 — 服务端处理: 设置扩容 cell ID
- `NewDavTogglePayload.java` — 30 — record: BlockPos + toggleType (MIGRATE/AUTO_ACCEPT/AUTO_EXPAND)
- `NewDavTogglePayloadHandler.java` — 46 — 服务端处理: 切换 BE 设置
- `SorterAnalysisPayload.java` — 28 — record: fileId (通知客户端新分析报告可用)
- `SorterAnalysisPayloadHandler.java` — 74 — 客户端处理: 注册分块回调, 反序列化 StorageAnalyzerReport
- `SorterCommandPayload.java` — 32 — record: buttonId (CMD_ME_DUMP=1 到 CMD_MERGE=5)
- `SorterCommandPayloadHandler.java` — 93 — 服务端处理: 验证菜单+节点在线, 分发到各 Service

#### 纯规则模型层 (rule/)

**rule/filter/** (18 个文件):
- `FilterCombinator.java` — 6 — enum: AND/OR
- `FilterCombinatorDefinition.java` — 27 — record: UI 元数据
- `FilterCondition.java` — 26 — record implements FilterExpression: 叶子节点
- `FilterExpression.java` — 4 — sealed interface
- `FilterField.java` — 11 — enum: ITEM_ID/MOD_ID/TAG/DISPLAY_NAME/HAS_COMPONENTS/TOTAL_AMOUNT/NBT_PATH
- `FilterFieldDefinition.java` — 29 — record: UI 元数据
- `FilterGroup.java` — 21 — record implements FilterExpression: 组合节点
- `FilterMatchMode.java` — 6 — enum: ALL/ANY (遗留)
- `FilterOperator.java` — 12 — enum: EQUALS/CONTAINS/IN/GREATER_OR_EQUAL/LESS_OR_EQUAL/IS_TRUE/IS_FALSE/REGEX
- `FilterOperatorDefinition.java` — 23 — record: UI 元数据
- `FilterUiMetadata.java` — 84 — record: 完整 UI schema
- `FilterUiMetadataCli.java` — 54 — CLI 工具
- `FilterUiMetadataJsonCodec.java` — 43 — JSON 编解码
- `FilterValueType.java` — 7 — enum: STRING/NUMBER/BOOLEAN
- `ItemFilter.java` — 53 — record: 命名+启用的 Filter (含表达式树根)
- `ItemFilterMatcher.java` — 171 — **核心匹配器**: 评估 FilterExpression
- `ItemMatchContext.java` — 33 — record: 评估上下文
- `NbtPathExtractor.java` — 88 — Gson JsonElement 路径导航 (支持数组索引)

**rule/route/** (12 个文件):
- `RouteAction.java` — 8 — enum: ROUTE_TO_ZONE/REJECT/ONLY_MARK/FALLBACK
- `RouteRule.java` — 41 — record: 路由规则 (id/name/enabled/priority/filterId/targetZoneId/continueOnMatch/action)
- `RoutingDecision.java` — 23 — record: 决策结果
- `RoutingDecisionType.java` — 9 — enum: ROUTED/REJECTED/MARKED_ONLY/FALLBACK/NO_MATCH
- `RoutingEngine.java` — 115 — **核心路由引擎**: 排序规则→评估 Filter→决策
- `RoutingEngineExample.java` — 69 — 示例 profile
- `RoutingExplanation.java` — 17 — record: 完整解释
- `RoutingProfile.java` — 90 — record: 完整路由 profile (zones/filters/rules/defaultZone)
- `RoutingProfileJsonCli.java` — 47 — CLI 工具
- `RoutingProfileJsonCodec.java` — 297 — JSON 编解码 (兼容旧格式)
- `RoutingProfileRepository.java` — 66 — 从 config/profiles/*.json 加载
- `RoutingRuleEvaluation.java` — 27 — record: 单规则评估结果

**rule/zone/** (2 个文件):
- `StorageZone.java` — 30 — record: zone 定义 (id/name/kind/allowAsDefault)
- `StorageZoneKind.java` — 9 — enum: BULK/MISC/COMPONENT_SAFE/SPECIAL/CUSTOM

#### 其他层
- **plan/** (3 文件, 278 行): `ItemZoneAssignment`, `ZoneAllocationPlan`, `ZoneAllocationPlanner`
- **profilegen/** (4 文件, 288 行): `RoutingProfileGenerator` (接口), `HeuristicRoutingProfileGenerator`, `ProfileGenerationRequest`, `ProfileGenerationResult`
- **analysis/** (5 文件, 约 1,420 行): 离线 CLI 分析工具 (DumpAnalyzer, RoutingAnalyzer, SuggestionAnalyzer, ProfileGenerationAnalyzer, ZoneAllocationPlannerAnalyzer)
- **logging/** (4 文件, 约 1,635 行): `ReportFileSupport`, `SorterFileLogger`, `SorterMergeReportFileLogger` (752行，最大), `SorterPlanFileLogger` (415行)
- **client/** (10 文件, 约 1,410 行): 3 个 Screen, AnalysisPresenter (450行), PlayerFacingAnalysis, SorterTerminalLayout, GuiRect, SorterClientRegistrations, ClientStorageAnalysisCache, ClientAnalysisPayloadHandler (空壳), CompactNumberFormatter, LocationIdFormatter, NewDavToggleButton
- **command/** (2 文件, 约 1,030 行): `SorterCommands` (109行), `GenTestItemsCommand` (921行)
- **item/** (1 文件, 148 行): `DigitalAssetManagementCardItem`
- **menu/slot/** (2 文件, 53 行): `DigitalAssetManagementCardSlot`, `StorageCellSlot`
- **recipe/** (1 文件): `ZoneStampedManagementCardRecipe`
- **test/** (4 文件): `ItemFilterMatcherTest` (20 测试), `RoutingProfileJsonCodecTest` (1 测试), `ZoneAllocationPlannerTest` (1 测试), `RuntimeCellTransferTest` (11 测试)

### 按类型统计

| 类型 | 数量 |
|------|------|
| record | ~52 |
| class | ~78 |
| enum | ~10 |
| interface (含 sealed) | ~5 |
| abstract class | ~2 |

---

## 维度 2: 类依赖矩阵与边界违规

### 关键边界合规状态

| 包 | 规则 | net.minecraft 违规 | appeng 违规 | 状态 |
|----|------|-------------------|-------------|------|
| `rule/filter/` (18 文件) | 禁止 net.minecraft/appeng | **0** | **0** | ✅ CLEAN |
| `rule/route/` (12 文件) | 禁止 net.minecraft/appeng | **0** | **0** | ✅ CLEAN (注1) |
| `rule/zone/` (2 文件) | 禁止 net.minecraft/appeng | **0** | **0** | ✅ CLEAN |
| `plan/` (3 文件) | 禁止 net.minecraft/appeng | **0** | **0** | ✅ CLEAN |
| `profilegen/` (4 文件) | 禁止 net.minecraft/appeng | **0** | **0** | ✅ CLEAN |
| `analysis/` (5 文件) | 禁止 net.minecraft/appeng | **0** | **0** | ✅ CLEAN |
| **`logging/` (4 文件)** | 禁止 net.minecraft/appeng | **4** | **2** | ❌ **FAIL** |

**注1**: `RoutingProfileRepository.java` 导入了 `net.neoforged.fml.loading.FMLPaths`（NeoForge 加载器 API，非 Minecraft 或 AE2）。这是框架级耦合，技术上不是 Minecraft/AE2 违规，但值得标记。

### Logging 包具体违规明细

| 文件 | net.minecraft 导入 | appeng 导入 |
|------|-------------------|-------------|
| `ReportFileSupport.java` | `CommandSourceStack`, `BlockPos`, `BuiltInRegistries`, `Entity` | `AEItemKey` |
| `SorterFileLogger.java` | `CommandSourceStack` | — |
| `SorterMergeReportFileLogger.java` | `CommandSourceStack` | `IGrid`, `AEItemKey`, `StorageCell` |
| `SorterPlanFileLogger.java` | `CommandSourceStack` | — |

### 项目内依赖关系 (核心类→核心类)

```
SorterCommands → SorterDumpService, SorterMergeService, SorterPlanService,
                 SorterProfileBindingService, SorterStorageAnalysisService

SorterCommandPayloadHandler → SorterDumpService, SorterMergeService, SorterPlanService,
                                SorterStorageAnalysisService

SorterDumpService → Ae2ControllerTargetResolver, SorterNetworkDumpWriter, Ae2DriveScanner,
                    SorterFileLogger, FileChunkedSender

SorterMergeService → Ae2ControllerTargetResolver, MergeMovePlanner, SorterFileLogger,
                     SorterMergeReportFileLogger, Ae2DriveScanner

SorterPlanService → Ae2ControllerTargetResolver, Ae2DriveScanner, RuntimeZoneRegistryBuilder,
                    LiveZoneAllocationPlanner, Ae2ZoneMoveExecutor, RoutingProfileRepository,
                    NetworkProfileBindingStore, SorterPlanFileLogger

SorterStorageAnalysisService → Ae2ControllerTargetResolver, Ae2StorageAnalyzer,
                                SorterStorageAnalysisDumpWriter, SorterFileLogger, FileChunkedSender

MergeMovePlanner → DriveMachineAccessor, SorterMoveOperation
Ae2ZoneMoveExecutor → ZoneMergePlanner, SorterMoveOperation
ZoneMergePlanner → DriveMachineAccessor, SorterMoveOperation
LiveZoneAllocationPlanner → RoutingEngine, RuntimeTopology
RuntimeZoneRegistryBuilder → DriveMachineAccessor

RoutingEngine → ItemFilterMatcher (rule.filter)
ItemFilterMatcher → NbtPathExtractor (rule.filter)
RoutingProfileRepository → RoutingProfileJsonCodec (rule.route)

DigitalAssetVaultBlockEntity → CellCapacityInspector, NewDavStorage, Config
NewDavStorage → DigitalAssetVaultBlockEntity (反向依赖！)
DriveMachineAccessor → DigitalAssetVaultBlockEntity (zone ID 检测)
```

### 关键依赖发现

1. **`DriveMachineAccessor` 是超级门面**: 被 10+ 个类引用，是整个 AE2 集成层的中枢
2. **`SorterMoveOperation` 是共享执行引擎**: 两条能力线 (merge + zone) 都通过它执行
3. **`NewDavStorage → DigitalAssetVaultBlockEntity`**: ae2 层的类反向依赖 blockentity 层的类。不太理想但在这个场景下是可以理解的 (MEStorage 委托给 BE)
4. **`DriveMachineAccessor → DigitalAssetVaultBlockEntity`**: ae2.scan 层通过 `instanceof` 检查耦合到 blockentity。如果未来有更多 DAV 类型，这种耦合需要抽象
5. **`RoutingProfileRepository` → `FMLPaths`**: rule 层唯一的外部框架依赖。不影响核心边界但值得标记

### 层间依赖方向验证

```
command → application → ae2 → blockentity  ✅ (正确方向)
                       ae2 → blockentity  ⚠️ (DriveMachineAccessor.instanceof)
                       ae2/dav → blockentity  ⚠️ (NewDavStorage → BE)
rule → (纯 Java)  ✅ (完全隔离)
plan → rule  ✅
analysis → rule  ✅
profilegen → analysis + rule  ✅
application → logging  ✅
logging → ae2 + net.minecraft  ❌ (logging 包违规)
ae2/zone → plan + rule  ✅
ae2/sort → (独立)  ✅
```

---

## 维度 3: 三条能力线调用链追踪

### 路径 1: Merge 路径 (`/sorter merge`)

```
SorterCommands.runMerge(CommandContext)
  └─ SorterMergeService.execute(source, Ae2ControllerTargetResolver::resolveGridTarget)
       ├─ resolver.resolve(source) → Ae2GridTargetResult {grid, controllerPos, ...}
       ├─ Ae2DriveScanner.scan(grid) → Ae2DriveScanSummary (before)
       ├─ SorterMergeReportFileLogger.captureSnapshot(grid) → MergeNetworkSnapshot (before)
       ├─ MergeMovePlanner.plan(grid, maxTransfers) → SorterMoveOperation
       │    └─ DriveMachineAccessor.findSupportedDrives(grid)
       │    └─ firstCellByKey 映射 → SIMULATE 插入验证 → PlannedMove + ExecutableMove
       ├─ SorterFileLogger.logSorterMergePlan(...) → logs/appliedinsight.log
       ├─ moveOperation.execute() → SorterMoveExecutionResult
       │    └─ extract(MODULATE) → insert(MODULATE) → rollback(if partial)
       │    └─ EnergyCostCalculator.estimate(result) [if ENERGY_COST_ENABLED]
       │         └─ formula: α×N + β×log₂(1+A) + γ×log₂(1+T) + δ×log₂(1+avgDist)
       ├─ Ae2DriveScanner.scan(grid) → Ae2DriveScanSummary (after)
       ├─ SorterMergeReportFileLogger.captureSnapshot(grid) → MergeNetworkSnapshot (after)
       ├─ SorterMergeReportFileLogger.logMergeReport(...) → logs/.../merge-{ts}.log
       └─ SorterFileLogger.logSorterMergeExecution(...)
  └─ SorterCommands.sendFeedback(source, result) → 聊天反馈
```

**Path 1 独占类**: `MergeMovePlanner`, `SorterMergeService`, `SorterMergeReportFileLogger`, `SorterMergeCommandResult`

### 路径 2: Plan 路径 (`/sorter me plan` / `planAndMove`)

```
SorterCommands.runPlan / runPlanAndMove(CommandContext)
  └─ SorterPlanService.execute(source, executeMove, resolver)
       ├─ resolver.resolve(source) → Ae2GridTargetResult
       ├─ NetworkProfileBindingStore.findProfileId(bindingKey) → profileId
       ├─ RoutingProfileRepository.findById(profileId) → RoutingProfile
       ├─ Ae2DriveScanner.scan(grid) → Ae2DriveScanSummary
       ├─ RuntimeZoneRegistryBuilder.buildTopology(grid) → RuntimeTopology
       │    └─ DriveMachineAccessor → zoneId via DigitalAssetVaultBlockEntity
       ├─ LiveZoneAllocationPlanner.plan(profile, topology, registries) → ZoneAllocationPlan
       │    └─ Fingerprint each item → RoutingEngine.decide(profile, context) → RoutingDecision
       ├─ [if executeMove]:
       │    ├─ Ae2ZoneMoveExecutor.executeDetailed(grid, plan, topology, ...) → DetailedResult
       │    │    ├─ ZoneMergePlanner.plan(plan, topology, grid, ...) → PlanResult(SorterMoveOperation)
       │    │    │    └─ Fingerprint matching → Zone.planPlacement() → ZonePlacementDecision
       │    │    └─ operation.execute() → SorterMoveExecutionResult [共享引擎]
       │    └─ SorterPlanFileLogger.logPlanAndMove(...) → logs/.../plan-and-move-{ts}.log
       └─ [if !executeMove]:
            └─ SorterPlanFileLogger.logPlan(...) → logs/.../plan-{ts}.log
  └─ SorterCommands.sendFeedback(source, result) → 聊天反馈
```

**Path 2 独占类**: `SorterPlanService`, `RuntimeZoneRegistryBuilder`, `RuntimeTopology`, `RuntimeZone`, `RuntimeCell`, `ZoneManager`, `LiveZoneAllocationPlanner`, `ZoneMergePlanner`, `Ae2ZoneMoveExecutor`, `ZonePlacementDecision`, `ZoneMoveExecutionResult`, `ZoneMoveExecutionDetailedResult`, `ZoneMoveExecutionDebugReport`, `ZoneAllocationPlan`, `ItemZoneAssignment`, `NetworkBindingKey`, `NetworkProfileBindingStore`, `SorterProfileBindingService`, `RoutingProfileRepository`, `SorterPlanFileLogger`

### 路径 3: Dashboard 路径 (`/sorter me storageDump`)

```
SorterCommands.runMeStorageDump(CommandContext)
  └─ SorterStorageAnalysisService.execute(source, resolver)
       ├─ resolver.resolve(source) → Ae2GridTargetResult
       ├─ SorterStorageAnalysisDumpWriter.dump(gridTarget) → DumpResult
       │    └─ Ae2StorageAnalyzer.analyze(grid) → StorageAnalyzerReport
       │         └─ DriveMachineAccessor.findSupportedDrives(grid)
       │         └─ CellCapacityInspector.inspect(cell) → CellCapacity
       │         └─ 碎片化分数 = totalDistributions / uniqueKeyCount
       │         └─ 健康标志: high_fragmentation, mixed_internal_external, 无限容器候选
       ├─ SorterFileLogger.logSorterMeStorageAnalysis(...)
       ├─ [if source entity is ServerPlayer]:
       │    ├─ GSON.toJson(report) → byte[]
       │    ├─ PacketDistributor.sendToPlayer(player, SorterAnalysisPayload(fileId))
       │    └─ FileChunkedSender.sendFile(player, fileId, tempFile)
       │         └─ 128KB chunks → FileChunkPayload × N
       └─ Returns SorterStorageAnalysisCommandResult

  [CLIENT SIDE]:
  SorterAnalysisPayloadHandler.handle(payload)
    └─ registerCallback: FileChunkPayloadHandler.registerCallback(fileId, callback)
  FileChunkPayloadHandler.handle(chunkPayload)
    └─ ConcurrentHashMap reassembly
    └─ onComplete: callback → GSON.fromJson(json, StorageAnalyzerReport.class) → cache
  SorterCommandBlockScreen.containerTick()
    └─ SorterAnalysisPayloadHandler.getReport() → StorageAnalyzerReport?
  AnalysisPresenter.present(report) → PlayerFacingAnalysis
    └─ InfiniteContainerCatalog, StorageSummary, healthFlags → AnalysisLines
    └─ NetworkHealthAssessment: GOOD / FAIR / POOR
  SorterCommandBlockScreen.renderBg()
    └─ 中列: health lamp, storage counts, capacity %, fragmentation level
    └─ 右列: diagnostic lines (!! / ! / i 前缀), hover tooltips
    └─ 底栏: feedback bar
```

**Path 3 独占类**: `SorterStorageAnalysisService`, `Ae2StorageAnalyzer`, `StorageAnalyzerReport`, `CellCapacityInspector`, `SorterStorageAnalysisDumpWriter`, `SorterStorageAnalysisDumpResult`, `FileChunkPayload`, `FileChunkPayloadHandler`, `FileChunkedSender`, `SorterAnalysisPayload`, `SorterAnalysisPayloadHandler`, `ClientStorageAnalysisCache`, `ClientAnalysisPayloadHandler`, `AnalysisPresenter`, `PlayerFacingAnalysis`, `SorterCommandBlockScreen`, `SorterTerminalLayout`

### 共享基础设施 (三条路径共同使用)

| 共享类 | Path 1 | Path 2 | Path 3 | 角色 |
|--------|--------|--------|--------|------|
| `Ae2ControllerTargetResolver` | ✅ | ✅ | ✅ | 网格目标解析 |
| `Ae2GridTargetResult` | ✅ | ✅ | ✅ | 解析结果 |
| `GridTargetResolver` | ✅ | ✅ | ✅ | 策略接口 |
| `Ae2DriveScanner` | ✅ | ✅ | — | Drive 扫描 |
| `DriveMachineAccessor` | ✅ | ✅ | ✅ | 核心门面 |
| `SorterMoveOperation` | ✅ | ✅ | — | 共享执行引擎 |
| `EnergyCostCalculator` | ✅ | ✅ | — | 能耗计算 |
| `SorterFileLogger` | ✅ | — | ✅ | 命令级日志 |
| `Config` | ✅ | ✅ | ✅ | 全局配置 |
| `SorterComponentHelper` | ✅ | ✅ | ✅ | UI 反馈工具 |
| `SorterCommandPayload` | ✅ | ✅ | ✅ | 客户端→服务端 |
| `SorterCommandPayloadHandler` | ✅ | ✅ | ✅ | 服务端分发 |

### 分叉点

```
用户输入
  ├─ 聊天命令 /sorter merge → SorterMergeService (Path 1)
  ├─ 聊天命令 /sorter me plan → SorterPlanService (Path 2)
  ├─ 聊天命令 /sorter me storageDump → SorterStorageAnalysisService (Path 3)
  └─ GUI 按钮 → SorterCommandPayload (CMD_*) → SorterCommandPayloadHandler
       ├─ CMD_MERGE → Path 1
       ├─ CMD_ME_PLAN / CMD_ME_PLAN_AND_MOVE → Path 2
       └─ CMD_ME_STORAGE_DUMP → Path 3
```

三条路径在 `SorterCommandPayloadHandler` 的 switch 语句处分叉，之后各自使用不同的 Service 和引擎，**仅在 `SorterMoveOperation.execute()` (共享执行引擎) 和 `DriveMachineAccessor` (共享门面) 两处汇合**。

---

## 维度 4: 注册项全量清单

### 方块 (3)

| ID | 类 | BE 类 | 模型纹理 | 配方 |
|----|-----|-------|----------|------|
| `digital_asset_vault` | `DigitalAssetVaultBlock` | `DigitalAssetVaultBlockEntity` | `ae2:block/drive` | **缺失 (P0)** |
| `digital_asset_vault` | `DigitalAssetVaultBlock` | `DigitalAssetVaultBlockEntity` | `ae2:block/drive` | **缺失** |
| `sorter_command_block` | `SorterCommandBlock` | `SorterCommandBlockEntity` | `ae2:block/drive/drive_front` | **缺失 (P0)** |

### 方块实体 (3)

| ID | 类 | AE2 连线 | 能力 |
|----|-----|----------|------|
| `digital_asset_vault` | `DigitalAssetVaultBlockEntity` | `block.setBlockEntity()` + `registerBlockEntityItem()` | `IN_WORLD_GRID_NODE_HOST` |
| `digital_asset_vault` | `DigitalAssetVaultBlockEntity` | **无 AE2 连线** | `IN_WORLD_GRID_NODE_HOST` |
| `sorter_command_block` | `SorterCommandBlockEntity` | 标准 GridHelper | `IN_WORLD_GRID_NODE_HOST` |

### 物品 (4)

| ID | 类 | 类型 |
|----|-----|------|
| `digital_asset_management_card` | `DigitalAssetManagementCardItem` | 独立物品 (有配方) |
| `digital_asset_vault` | BlockItem | 方块物品 |
| `digital_asset_vault` | BlockItem | 方块物品 |
| `sorter_command_block` | BlockItem | 方块物品 |

### 菜单 (3)

| ID | 类 | 数据槽 |
|----|-----|--------|
| `digital_asset_vault_menu` | `DigitalAssetVaultMenu` extends `AEBaseMenu` | 10 cell + 1 card |
| `digital_asset_vault_menu` | `DigitalAssetVaultMenu` extends `AbstractContainerMenu` | 1 input + 多 DataSlot |
| `sorter_command_block_menu` | `SorterCommandBlockMenu` extends `AbstractContainerMenu` | NetworkStatus DataSlot |

### 创造标签页

- 方块 → `FUNCTIONAL_BLOCKS` tab
- 管理卡 → `INGREDIENTS` tab

### 网络 Payload (8)

| Payload | 方向 | 用途 |
|---------|------|------|
| `SorterCommandPayload` | C→S | 命令按钮 (5 种命令) |
| `SorterAnalysisPayload` | S→C | 分析报告就绪通知 |
| `FileChunkPayload` | S→C | 分块文件传输 (128KB) |
| `NewDavTogglePayload` | C→S | DAV 设置切换 |
| `NewDavSetExpansionCellPayload` | C→S | DAV 扩容 cell ID |
| **缺失: `SorterCommandResultPayload`** | **S→C** | **计划中但未实现** |

### 命令节点

```
/sorter
  merge
  genTestItems [slot-count]
    nbtHeavy <level>
  me
    dump                              [developerMode 守卫]
    storageDump                        [developerMode 守卫]
    listProfiles                       [developerMode 守卫]
    bindProfile <number>               [developerMode 守卫]
    showProfile                        [developerMode 守卫]
    plan                               [developerMode 守卫]
    planAndMove                        [developerMode 守卫]
```

### 配方

| 产物 | 类型 | 状态 |
|------|------|------|
| `digital_asset_management_card` | 无序合成 (name_tag + 4×gold_ingot) | ✅ 存在 |
| `digital_asset_vault` | 无 | ❌ P0 缺失 |
| `sorter_command_block` | 无 | ❌ P0 缺失 |
| `digital_asset_vault` | 无 | ❌ 缺失 |

### 纹理/GUI 资产

| 资产 | 尺寸 | 状态 |
|------|------|------|
| `digital_asset_vault.png` | 256×256 | ✅ 存在 |
| `sorter_command_block.png` | 未确认 | ⚠️ 存在但可能未使用 (SCB 屏幕由代码绘制) |
| `digital_asset_vault.png` | 226×228 | ✅ 存在 |
| `digital_asset_management_card.png` | 16×16 | ✅ 存在 |

---

## 维度 5: 文档覆盖率矩阵

### 概况

| 指标 | 数值 |
|------|------|
| Java 源文件 (src/main/java) | 147 |
| 类职责文档 (docs/类职责/) | 133 |
| 索引中声称的类数 | 132 |
| 测试文件 (src/test) | 4 |
| FACTS 数量 | 133 (FACT-001 ~ FACT-133) |
| ADR 数量 | 11 (ADR-001 ~ ADR-011) |

### 缺失文档的代码文件 (至少 15 个)

以下 Java 类在 `docs/类职责/` 中**没有**对应的 .md 文件：

| 包 | 类 | 原因 |
|----|-----|------|
| block/ | `DigitalAssetVaultBlock` | 新 DAV 系列，文档未跟进 |
| blockentity/ | `DigitalAssetVaultBlockEntity` | 同上 (779 行，最大的类之一！) |
| menu/ | `DigitalAssetVaultMenu` | 同上 |
| client/screen/ | `DigitalAssetVaultScreen` | 同上 |
| ae2/dav/ | `NewDavStorage` | 同上 |
| ae2/dav/ | `NewDavStorageProvider` | 同上 |
| application/ | `NewDavMigrationService` | 同上 |
| application/result/ | `NewDavMigrationResult` | 同上 |
| network/ | `NewDavTogglePayload` | 同上 |
| network/ | `NewDavTogglePayloadHandler` | 同上 |
| network/ | `NewDavSetExpansionCellPayload` | 同上 |
| network/ | `NewDavSetExpansionCellPayloadHandler` | 同上 |
| client/gui/widget/ | `NewDavToggleButton` | 同上 |
| client/format/ | `LocationIdFormatter` | 可能是遗漏 |
| client/screen/ | `SorterTerminalLayout` | 可能是遗漏 |
| client/screen/ | `GuiRect` | 可能是遗漏 |
| client/screen/ | `PlayerFacingAnalysis` (record) | 可能在 client/analysis/ 路径下? |
| ae2/zone/ | `Ae2MoveAnalyzer` (实际在 ae2/analysis/) | 需确认 |

### 可能存在但无对应代码的文档文件

| 文档文件 | 对应代码存在? |
|----------|---------------|
| `recipe/ZoneStampedManagementCardRecipe.md` | ✅ 存在 |
| `registry/SorterRecipeSerializers.md` | ✅ 存在 |
| `client/format/CompactNumberFormatter.md` | ✅ 存在 (但 LocationIdFormatter 缺失) |
| `client/analysis/AnalysisPresenter.md` | 检查中 |
| `client/analysis/PlayerFacingAnalysis.md` | 检查中 |
| `client/screen/SorterTerminalLayout.md` | **缺失** |
| `client/screen/GuiRect.md` | **缺失** |
| `ae2/analysis/Ae2MoveAnalyzer.md` | ✅ 存在 |

### 文档债务摘要

- **新 DAV 系统** (13 个文件, 约 1,500+ 行代码) 完全没有类职责文档
- **3 个小工具类** (LocationIdFormatter, SorterTerminalLayout, GuiRect) 也没有文档
- 索引文件 `docs/类职责/索引.md` 声称 132 个类，但实际文档数 133，代码文件 147——**缺口约 14-16 个**
- 索引文件中是否有 `ae2/dav/` 子目录? 需要核实

---

## 维度 6: 两个 DAV 实现对比

### 核心差异一览

| 维度 | 旧 DAV (DigitalAssetVault) | 新 DAV (NewDigitalAssetVault) |
|------|---------------------------|-------------------------------|
| **设计理念** | AE2 Drive 的薄封装 + zone 卡槽 | 独立存储后端 (cell 吸收模型) |
| **Block 继承** | `AEBaseEntityBlock<DigitalAssetVaultBlockEntity>` | `Block implements EntityBlock` |
| **BE 继承** | `DriveBlockEntity` (AE2 内置) | `BlockEntity implements InternalInventoryHost, ISaveProvider, IInWorldGridNodeHost` |
| **Menu 继承** | `AEBaseMenu` | `AbstractContainerMenu` |
| **存储模型** | 10 个物理 cell 槽 | `LinkedHashMap<AEItemKey, Long>` (内存存储) |
| **Cell 处理** | Cell 插入槽中，AE2 管理 | Cell 被消耗 (吸收)，容量累加 |
| **容量来源** | Cell 自身的存储容量 (AE2 管理) | `absorbedBytes` + `absorbedTypeCapacity` (累计) |
| **类型限制** | 受 Cell 类型限制 (如 63 种/cell) | 累计 `absorbedTypeCapacity` (可无限扩展) |
| **状态管理** | 无 | 13 状态枚举 + 状态同步 |
| **自动扩容** | 无 | 3 阶段系统: 检测→计算→提交→轮询 |
| **配置** | 无独立配置 | 4 个 DAV 专属 Config 值 |
| **格子数量** | 10 cell 槽 + 1 管理卡槽 | 1 输入槽 (吸收用) |
| **GUI** | AE2 Drive 纹理 (176×223) | 自定义纹理 (226×228) + 3 个开关 + EditBox |
| **网络包** | 无自定义 Payload | NewDavTogglePayload + NewDavSetExpansionCellPayload |
| **IStorageProvider** | 无 (AE2 Drive 内部处理) | `NewDavStorageProvider` (优先级 100000) |
| **Zone 集成** | ✅ `getDeclaredZoneId()` → 管理卡 | ❌ Zone 系统完全不知道新 DAV |
| **AE2 注册连线** | `block.setBlockEntity()` + `registerBlockEntityItem()` | **无** (可能缺失) |
| **迁移能力** | 无 | 桩 (NewDavMigrationService → 始终 idle) |
| **文档覆盖** | ✅ 完整 | ❌ 全部 13 个文件缺失 |

### 状态机 (新 DAV)

```
IDLE → ABSORBED / NOT_STORAGE_CELL / STACK_COUNT_NOT_ONE / NON_EMPTY_CELL /
       UNKNOWN_CAPACITY / UNKNOWN_TYPE_CAPACITY / INFINITE_CELL /
       MIGRATION_PENDING / AUTO_EXPAND_CRAFTING / AUTO_EXPAND_COMPLETED /
       AUTO_EXPAND_NO_PATTERN / AUTO_EXPAND_FAILED
```

### 共存状态分析

两个 DAV 实现**同时注册、同时存在于游戏中**。它们有不同的方块 ID (`digital_asset_vault` vs `digital_asset_vault`)，可以同时放置在同一个网络中。

**关键问题**:
1. 旧 DAV 通过 `DriveMachineAccessor` 参与 zone 系统，新 DAV **不参与**——两个 DAV 放入同一个网络会导致 zone 系统只看到旧 DAV 的 cell
2. 新 DAV 的 `NewDavStorageProvider` 以优先级 100000 挂载到 ME 网络——与旧 DAV 的 AE2 内置存储实现**可能冲突或产生意外行为**
3. 新 DAV 没有 AE2 注册连线 (`block.setBlockEntity()` / `registerBlockEntityItem()`)，**可能导致网络节点生命周期管理不完整**
4. 没有迁移路径——玩家无法将旧 DAV 的 cell 迁移到新 DAV

---

## 维度 7: 技术债/不一致清单

### 7.1 严重问题 (P0 — 阻塞发布)

| ID | 描述 | 来源 |
|----|------|------|
| P0-01 | `SorterCommandBlock` 缺少方块模型/blockstate/纹理 — 游戏中显示为紫黑方块 | 发布前最后任务.md |
| P0-02 | `DigitalAssetVault` (旧 DAV) 缺少合成配方 | 发布前最后任务.md |
| P0-03 | `SorterCommandBlock` 缺少合成配方 | 发布前最后任务.md |
| P0-04 | `neoforge.mods.toml` 模板展开未验证 | 发布前最后任务.md |
| P0-05 | 语言文件可能缺少命令反馈键 (需核实 143 个键是否完整) | 发布前最后任务.md |

### 7.2 高优先级问题 (P1)

| ID | 描述 |
|----|------|
| P1-01 | **核心路径零测试**: RoutingEngine, SorterMoveOperation, MergeMovePlanner, Ae2ZoneMoveExecutor, EnergyCostCalculator 均无测试 |
| P1-02 | **文档-代码一致性未检查**: FACTS.md (133 条) vs. 实际代码 |
| P1-03 | **`SorterCommandResultPayload` 缺失**: S→C 结果回传路径不完整 |
| P1-04 | **新 DAV 无类职责文档**: 13 个文件 (约 1,500+ 行) 完全没有文档 |
| P1-05 | **Logging 包违反边界**: 4 个文件导入 net.minecraft + appeng (FACT-048 规定 logger 不参与业务决策) |
| P1-06 | **新 DAV 无 Zone 集成**: `DriveMachineAccessor.getDeclaredZoneId()` 不知道新 DAV 的存在 |
| P1-07 | **新 DAV 缺少 AE2 注册连线**: 无 `block.setBlockEntity()` / `registerBlockEntityItem()` 调用 |

### 7.3 中优先级问题 (P2)

| ID | 描述 |
|----|------|
| P2-01 | **新 DAV 迁移完全为桩**: `NewDavMigrationService.migrateOnce()` 始终返回 idle。开关、计时器、状态字段、NBT 队列全部存在但无实际逻辑 |
| P2-02 | **反射代码重复**: `CellCapacityInspector.invokeLong()` 和 `SorterMergeReportFileLogger` 中独立实现了相同的反射模式。AE2 内部改名会同时破坏两处 |
| P2-03 | **`DriveMachineAccessor` instanceof 耦合**: 通过 `instanceof DigitalAssetVaultBlockEntity` 检测 zone ID——不支持新 DAV 或其他未来 DAV 类型 |
| P2-04 | **`SorterCommandBlock.createBlockStateDefinition`**: 标记为可能多余的样板代码 |
| P2-05 | **新 DAV 与旧 DAV 共存行为未定义**: 两个独立注册的存储后端同时在网络中运行，行为未经过测试 |
| P2-06 | **`NewDavStorage` ← `DigitalAssetVaultBlockEntity`** 循环: ae2 层依赖 blockentity 层 |
| P2-07 | **`RoutingProfileRepository` ← `FMLPaths`**: rule 层依赖 NeoForge 加载器 API |

### 7.4 低优先级问题 (P3 — 已明确推迟到 v2.0)

| ID | 描述 |
|----|------|
| P3-01 | HTTP API Server (RESTful) — ADR-009 (提议阶段) |
| P3-02 | H2 嵌入式数据库 — ADR-010 (提议阶段) |
| P3-03 | 账号管理 (bcrypt + JWT) |
| P3-04 | React Dashboard |
| P3-05 | 高级纹理 |
| P3-06 | 标准 JSON 配方系统 (替换 ZoneStampedManagementCardRecipe) |
| P3-07 | 3 个结果查看器 GUI (C1 分析结果 / C2 计划结果 / C3 Merge 结果) |
| P3-08 | DAV Zone 信息面板 |
| P3-09 | 配置管理 GUI (D1/D2/D3) |

### 7.5 不一致与遗留问题

| ID | 描述 |
|----|------|
| I-01 | **"新版dav设计" 文件不完整**: 根目录下 `新版dav设计` 文件只有 2 行截断内容 ("nbt采用分级策略，20字符以内统一不计算。\n20-50字符，")，缺少完整设计 |
| I-02 | **`SorterMergeCommandResult` 存在但未使用**: 主流程使用 `SorterFeedbackResult` 替代 |
| I-03 | **`Ae2MoveAnalyzer` + `RuntimeMoveAnalysisReport` 存在但未在任何路径中调用** |
| I-04 | **`ClientAnalysisPayloadHandler` 为空壳**: 标注为迁移产物 |
| I-05 | **SCB 屏幕纹理**: 存在旧版纹理文件但 SCB 宽屏终端 (440×224) 由代码绘制，纹理文件可能是过时的 |
| I-06 | **无 TODO/FIXME/HACK 标记**: 全代码库搜索未找到任何此类注释。可能是清洁代码习惯，也可能是债务未被标记 |
| I-07 | **硬编码值未入 Config**: `NODE_IDLE_POWER=1`, `DEFAULT_MIGRATION_TICK_INTERVAL=20`, `MAX_EXPANSION_RETRIES=3`, `DAV_PRIORITY=100000` |
| I-08 | **`FilterMatchMode.ALL/ANY`**: 标记为遗留枚举，已被 `FilterGroup` 替代但未删除 |

### 7.6 代码质量问题

| ID | 描述 |
|----|------|
| Q-01 | `GenTestItemsCommand` 921 行 — 是第二大文件，含大量硬编码物品模板 |
| Q-02 | `SorterMergeReportFileLogger` 752 行 — 最大文件，承担过多职责 (captureSnapshot + logMergeReport + 反射 + JSON) |
| Q-03 | `DigitalAssetVaultBlockEntity` 779 行 — 第三大文件，混合了存储、吸收、扩容、迁移、序列化 |
| Q-04 | `SorterCommandBlockScreen` 508 行 — GUI 渲染、事件处理、分析展示混合在一起 |
| Q-05 | `Ae2StorageAnalyzer` 448 行 — 分析逻辑全部内联，缺少提取的子分析器 |

---

## 附录: 数据来源说明

- **行数**: `wc -l` 在 Windows bash 下统计，含空行和注释
- **依赖矩阵**: 手工 grep 每个 Java 文件的 import 段 + Agent 交叉验证
- **调用链**: 阅读每个方法的源代码，追踪方法调用
- **文档统计**: `find` 列出 docs/类职责/ 所有 .md 文件 + 逐个比对
- **边界违规**: 对 rule/, plan/, profilegen/, analysis/, logging/ 包中每个文件检查 `import net.minecraft` 和 `import appeng`

---

> **审阅说明**: 请在审阅后在每个发现旁标注状态：
> - ✅ 已确认 (无需修改)
> - 🔧 需修复 (附修复计划)
> - 📦 已过时 (此发现已不再适用)
> - ❓ 需进一步调查
