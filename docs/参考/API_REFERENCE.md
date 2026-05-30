# API 参考手册 (API Reference)

> 本文档描述 **Applied Storage Sorter** 的后端 API 接口，包括命令接口、JSON 契约、扩展点和 Gradle 工具链。
> 面向扩展开发者、前端对接者和 AI 协作者。

---

## 📋 目录

- [命令接口](#1-命令接口)
- [JSON 契约](#2-json-契约)
- [Java API 扩展点](#3-java-api-扩展点)
- [Gradle 工具链](#4-gradle-工具链)
- [网络包协议](#5-网络包协议)
- [日志与产物](#6-日志与产物)

---

## 1. 命令接口

> **💡 普通玩家推荐使用 [`SorterCommandBlock`](类职责/block/SorterCommandBlock.md) GUI 操作**，无需记忆命令。
> 以下命令接口主要面向**开发者**和**模组管理员**，用于调试、自动化脚本和批量操作场景。

所有命令注册于 [`SorterCommands`](src/main/java/com/knightcode/appliedstoragesorter/command/SorterCommands.java:24) 的 `register()` 方法，使用 Brigadier 命令框架。

### 1.1 命令树

```
/sorter
├── merge                              # 自动整理（preview + execute 合并）
├── me
│   ├── dump                           # 导出网络快照（需 developerMode）
│   ├── storageDump                    # 导出存储分析报告（需 developerMode）
│   ├── listProfiles                   # 列出所有全局路由配置
│   ├── bindProfile <number>           # 绑定路由配置到当前网络
│   ├── showProfile                    # 查看当前绑定的路由配置
│   ├── plan                           # 预览规则路由规划
│   └── planAndMove                    # 执行规则路由规划
```

### 1.2 命令详情

#### `/sorter merge`

| 属性 | 值 |
|------|-----|
| 后端服务 | [`SorterMergeService`](src/main/java/com/knightcode/appliedstoragesorter/application/SorterMergeService.java) |
| 功能 | 扫描当前 AE2 网络，规划并执行同类物品归并压实 |
| 流程 | `GridTargetResolver → Ae2DriveScanner → MergeMovePlanner → execute → MergeReportFileLogger` |
| 输出 | 控制台反馈 + `logs/appliedstoragesorter/merge-*.log` 详细报告 |
| 前置条件 | `ENABLE_SORTER` 配置为 true；必须由玩家执行 |
| 返回值类型 | [`SorterFeedbackResult`](src/main/java/com/knightcode/appliedstoragesorter/application/result/SorterFeedbackResult.java) |

#### `/sorter me dump`

| 属性 | 值 |
|------|-----|
| 后端服务 | [`SorterDumpService`](src/main/java/com/knightcode/appliedstoragesorter/application/SorterDumpService.java) |
| 功能 | 导出当前 AE2 网络快照为 JSON，包含所有 cell、item key、容量信息 |
| 输出文件 | `dumps/appliedstoragesorter/me-dump-<timestamp>.json` |
| 前置条件 | `DEVELOPER_MODE` 配置为 true；必须由玩家执行 |
| 网络推送 | 通过 [`FileChunkedSender`](src/main/java/com/knightcode/appliedstoragesorter/network/FileChunkedSender.java) 分块推送给客户端 |
| 返回值类型 | [`SorterFeedbackResult`](src/main/java/com/knightcode/appliedstoragesorter/application/result/SorterFeedbackResult.java) |

#### `/sorter me storageDump`

| 属性 | 值 |
|------|-----|
| 后端服务 | [`SorterStorageAnalysisService`](src/main/java/com/knightcode/appliedstoragesorter/application/SorterStorageAnalysisService.java) |
| 功能 | 导出存储分析报告，包含存储位置分布、碎片化指标、健康标志、疑似语义节点 |
| 输出文件 | `dumps/appliedstoragesorter/storage-analysis-<timestamp>.json` |
| 前置条件 | `DEVELOPER_MODE` 配置为 true；必须由玩家执行 |
| 分析引擎 | [`Ae2StorageAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/ae2/analysis/Ae2StorageAnalyzer.java) |
| 网络推送 | 通过 `SorterAnalysisPayload` + `FileChunkedSender` 推送给客户端 |
| 返回值类型 | [`SorterStorageAnalysisCommandResult`](src/main/java/com/knightcode/appliedstoragesorter/application/result/SorterStorageAnalysisCommandResult.java) |

#### `/sorter me listProfiles`

| 属性 | 值 |
|------|-----|
| 后端服务 | [`SorterProfileBindingService`](src/main/java/com/knightcode/appliedstoragesorter/application/SorterProfileBindingService.java) |
| 功能 | 列出 `config/appliedstoragesorter/profiles/` 下所有可用的全局路由配置 |
| 输出 | 控制台显示 profile 列表（名称、ID、版本号） |
| 返回值类型 | [`SorterFeedbackResult`](src/main/java/com/knightcode/appliedstoragesorter/application/result/SorterFeedbackResult.java) |

#### `/sorter me bindProfile <number>`

| 属性 | 值 |
|------|-----|
| 后端服务 | [`SorterProfileBindingService`](src/main/java/com/knightcode/appliedstoragesorter/application/SorterProfileBindingService.java) |
| 参数 | `number` — `listProfiles` 输出中的序号（从 1 开始） |
| 功能 | 将当前 ME 网络（按 dimension + controller 坐标标识）绑定到指定全局 profile |
| 持久化 | 通过 [`NetworkProfileBindingStore`](src/main/java/com/knightcode/appliedstoragesorter/application/NetworkProfileBindingStore.java) 保存到 world save |
| 返回值类型 | [`SorterFeedbackResult`](src/main/java/com/knightcode/appliedstoragesorter/application/result/SorterFeedbackResult.java) |

#### `/sorter me showProfile`

| 属性 | 值 |
|------|-----|
| 后端服务 | [`SorterProfileBindingService`](src/main/java/com/knightcode/appliedstoragesorter/application/SorterProfileBindingService.java) |
| 功能 | 查看当前 ME 网络绑定的 profile 名称、ID、版本号 |
| 返回值类型 | [`SorterFeedbackResult`](src/main/java/com/knightcode/appliedstoragesorter/application/result/SorterFeedbackResult.java) |

#### `/sorter me plan`

| 属性 | 值 |
|------|-----|
| 后端服务 | [`SorterPlanService`](src/main/java/com/knightcode/appliedstoragesorter/application/SorterPlanService.java) |
| 功能 | 预览规则路由规划：读取绑定的 profile → 构建 RuntimeTopology → 生成 ZoneAllocationPlan |
| 流程 | `RoutingProfileRepository → Ae2DriveScanner → RuntimeZoneRegistryBuilder → LiveZoneAllocationPlanner` |
| 输出 | 控制台摘要 + `logs/appliedstoragesorter/plan-*.log` 详细日志 |
| 前置条件 | 当前网络必须已绑定 profile |
| 返回值类型 | [`SorterFeedbackResult`](src/main/java/com/knightcode/appliedstoragesorter/application/result/SorterFeedbackResult.java) |

#### `/sorter me planAndMove`

| 属性 | 值 |
|------|-----|
| 后端服务 | [`SorterPlanService`](src/main/java/com/knightcode/appliedstoragesorter/application/SorterPlanService.java) |
| 功能 | 执行规则路由规划：plan + 实际搬运 |
| 额外流程 | `Ae2ZoneMoveExecutor.executeDetailed()` |
| 输出 | 控制台摘要（含搬运统计）+ `logs/appliedstoragesorter/plan-and-move-*.log` 详细日志 |
| 前置条件 | 当前网络必须已绑定 profile |
| 返回值类型 | [`SorterFeedbackResult`](src/main/java/com/knightcode/appliedstoragesorter/application/result/SorterFeedbackResult.java) |

### 1.3 返回值类型

#### `SorterFeedbackResult`

```java
public record SorterFeedbackResult(boolean success, List<Component> lines)
```

- `success` — 命令是否成功
- `lines` — 反馈消息行列表（`net.minecraft.network.chat.Component`）
- 工厂方法：`failure(String)`, `failure(Component)`, `success(List<Component>)`, `success(Component)`

#### `SorterStorageAnalysisCommandResult`

```java
public record SorterStorageAnalysisCommandResult(
    boolean success,
    List<Component> lines,
    String dumpFilePath,
    int storageLocationCount,
    int uniqueKeyCount)
```

#### `SorterDumpCommandResult`

```java
public record SorterDumpCommandResult(
    boolean success,
    String message,
    String dumpFilePath,
    int mountedCellCount,
    int uniqueItemKeyCount)
```

#### `SorterMergeCommandResult`

```java
public record SorterMergeCommandResult(
    boolean success,
    String message,
    int plannedMoveCount,
    long requestedAmount,
    long movedAmount)
```

---

## 2. JSON 契约

### 2.1 me-dump JSON (`dumps/appliedstoragesorter/me-dump-*.json`)

**Schema**: [`src/main/resources/schema/sorter-network-dump.schema.json`](src/main/resources/schema/sorter-network-dump.schema.json)

| 字段 | 类型 | 说明 |
|------|------|------|
| `command` | string | 触发命令 |
| `target` | object | 目标信息（controller 坐标、维度、grid 节点数） |
| `summary` | object | 摘要（drive 数、cell 数、item key 数、总字节等） |
| `cells[]` | array | 每个 mounted cell 的详细信息 |
| `cells[].entries[]` | array | cell 内的 item key 列表 |
| `items[]` | array | 全局 item key 聚合（含 occurrences） |

> 完整字段定义见 [`docs/日志与JSON字段契约.md`](docs/日志与JSON字段契约.md#7-dumpsappliedstoragesorterme-dump-timestampjson)

### 2.2 storage-analysis JSON (`dumps/appliedstoragesorter/storage-analysis-*.json`)

| 字段 | 类型 | 说明 |
|------|------|------|
| `report.summary` | object | 摘要（总存储位置数、唯一 key 数、碎片化等级等） |
| `report.storageLocations[]` | array | 每个存储位置的详情（类型、容量、top keys） |
| `report.itemDistributions[]` | array | 物品分布（跨多少个位置、总量） |
| `report.healthFlags[]` | array | 健康标志列表 |
| `report.suspectedSemanticCandidates[]` | array | 疑似语义节点候选项 |

> 完整字段定义见 [`docs/日志与JSON字段契约.md`](docs/日志与JSON字段契约.md#8-dumpsappliedstoragesorterstorage-analysis-timestampjson)

### 2.3 routing-profile JSON

**Schema**: [`src/main/resources/schema/routing-profile.schema.json`](src/main/resources/schema/routing-profile.schema.json)

| 字段 | 类型 | 说明 |
|------|------|------|
| `zones[]` | array | 存储区定义 |
| `filters[]` | array | 过滤器定义（树形表达式） |
| `routeRules[]` | array | 路由规则（filter → zone 映射） |
| `defaultZoneId` | string | 默认存储区 ID |

> 完整字段定义见 [`docs/日志与JSON字段契约.md`](docs/日志与JSON字段契约.md#9-routing-profile-json-契约)

### 2.4 filter-ui-metadata JSON

由 [`FilterUiMetadataCli`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterUiMetadataCli.java) 导出，供 React Query Builder 消费。

| 字段 | 说明 |
|------|------|
| `fields[]` | 可用过滤字段定义（field、label、type、inputType、operators） |
| `operators[]` | 可用操作符定义（operator、label、applicableTypes） |
| `combinators[]` | 可用组合符定义（combinator、label） |

---

## 3. Java API 扩展点

### 3.1 规则层 (Rule Layer) — 纯 Java，无 Minecraft 运行时依赖

| 接口/类 | 包路径 | 说明 |
|---------|--------|------|
| [`ItemFilter`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/ItemFilter.java) | `rule.filter` | 过滤器接口，定义 `match(ItemMatchContext)` |
| [`FilterExpression`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterExpression.java) | `rule.filter` | 过滤器表达式接口（树形结构） |
| [`FilterCondition`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterCondition.java) | `rule.filter` | 叶子节点条件 |
| [`FilterGroup`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterGroup.java) | `rule.filter` | 组合节点（AND/OR） |
| [`RoutingEngine`](src/main/java/com/knightcode/appliedstoragesorter/rule/route/RoutingEngine.java) | `rule.route` | 路由引擎，决定 `item → zone` |
| [`RoutingProfile`](src/main/java/com/knightcode/appliedstoragesorter/rule/route/RoutingProfile.java) | `rule.route` | 路由配置（zones + filters + routeRules） |
| [`RouteRule`](src/main/java/com/knightcode/appliedstoragesorter/rule/route/RouteRule.java) | `rule.route` | 单条路由规则 |
| [`StorageZone`](src/main/java/com/knightcode/appliedstoragesorter/rule/zone/StorageZone.java) | `rule.zone` | 存储区定义 |
| [`StorageZoneKind`](src/main/java/com/knightcode/appliedstoragesorter/rule/zone/StorageZoneKind.java) | `rule.zone` | 存储区种类枚举 |
| [`RoutingProfileJsonCodec`](src/main/java/com/knightcode/appliedstoragesorter/rule/route/RoutingProfileJsonCodec.java) | `rule.route` | Profile JSON 编解码器 |
| [`RoutingProfileRepository`](src/main/java/com/knightcode/appliedstoragesorter/rule/route/RoutingProfileRepository.java) | `rule.route` | Profile 文件仓库 |

### 3.2 规划层 (Plan Layer)

| 类 | 包路径 | 说明 |
|----|--------|------|
| [`ZoneAllocationPlan`](src/main/java/com/knightcode/appliedstoragesorter/plan/ZoneAllocationPlan.java) | `plan` | 规划结果（item → zone 分配） |
| [`ItemZoneAssignment`](src/main/java/com/knightcode/appliedstoragesorter/plan/ItemZoneAssignment.java) | `plan` | 单物品的 zone 分配 |
| [`ZoneAllocationPlanner`](src/main/java/com/knightcode/appliedstoragesorter/plan/ZoneAllocationPlanner.java) | `plan` | 规划器接口 |
| [`LiveZoneAllocationPlanner`](src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/LiveZoneAllocationPlanner.java) | `ae2.zone` | 运行时规划器实现 |

### 3.3 运行时层 (Runtime Layer)

| 类 | 包路径 | 说明 |
|----|--------|------|
| [`RuntimeTopology`](src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/RuntimeTopology.java) | `ae2.zone` | 运行时世界模型（zone → machine → cell 三层视图） |
| [`RuntimeZone`](src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/RuntimeZone.java) | `ae2.zone` | 运行时 zone 实例 |
| [`RuntimeCell`](src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/RuntimeCell.java) | `ae2.zone` | 运行时 cell 实例 |
| [`ZoneManager`](src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/ZoneManager.java) | `ae2.zone` | Zone 管理器 |
| [`RuntimeZoneRegistryBuilder`](src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/RuntimeZoneRegistryBuilder.java) | `ae2.zone` | 从 grid 构建 RuntimeTopology |

### 3.4 执行层 (Execution Layer)

| 类 | 包路径 | 说明 |
|----|--------|------|
| [`Ae2ZoneMoveExecutor`](src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/Ae2ZoneMoveExecutor.java) | `ae2.zone` | Zone 搬运执行器 |
| [`MergeMovePlanner`](src/main/java/com/knightcode/appliedstoragesorter/ae2/sort/MergeMovePlanner.java) | `ae2.sort` | 自动整理规划器 |
| [`SorterMoveOperation`](src/main/java/com/knightcode/appliedstoragesorter/ae2/sort/SorterMoveOperation.java) | `ae2.sort` | 单次搬运操作 |
| [`PlannedMove`](src/main/java/com/knightcode/appliedstoragesorter/ae2/sort/PlannedMove.java) | `ae2.sort` | 已规划的搬运 |

### 3.5 分析层 (Analysis Layer)

| 类 | 包路径 | 说明 |
|----|--------|------|
| [`Ae2StorageAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/ae2/analysis/Ae2StorageAnalyzer.java) | `ae2.analysis` | 存储分析器 |
| [`Ae2MoveAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/ae2/analysis/Ae2MoveAnalyzer.java) | `ae2.analysis` | 搬运分析器 |
| [`SorterDumpAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/analysis/SorterDumpAnalyzer.java) | `analysis` | 离线 dump 分析器 |
| [`SorterDumpRoutingAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/analysis/SorterDumpRoutingAnalyzer.java) | `analysis` | 离线路由分析器 |
| [`SorterDumpRoutingSuggestionAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/analysis/SorterDumpRoutingSuggestionAnalyzer.java) | `analysis` | 离线路由建议分析器 |
| [`SorterDumpProfileGenerationAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/analysis/SorterDumpProfileGenerationAnalyzer.java) | `analysis` | 离线 profile 生成分析器 |
| [`ZoneAllocationPlannerAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/analysis/ZoneAllocationPlannerAnalyzer.java) | `analysis` | 离线规划分析器 |

### 3.6 Profile Generation 接口

| 类 | 包路径 | 说明 |
|----|--------|------|
| [`RoutingProfileGenerator`](src/main/java/com/knightcode/appliedstoragesorter/profilegen/RoutingProfileGenerator.java) | `profilegen` | Profile 生成器接口 |
| [`HeuristicRoutingProfileGenerator`](src/main/java/com/knightcode/appliedstoragesorter/profilegen/HeuristicRoutingProfileGenerator.java) | `profilegen` | 启发式实现 |
| [`ProfileGenerationRequest`](src/main/java/com/knightcode/appliedstoragesorter/profilegen/ProfileGenerationRequest.java) | `profilegen` | 生成请求参数 |
| [`ProfileGenerationResult`](src/main/java/com/knightcode/appliedstoragesorter/profilegen/ProfileGenerationResult.java) | `profilegen` | 生成结果 |

### 3.7 日志层 (Logging Layer)

| 类 | 包路径 | 说明 |
|----|--------|------|
| [`SorterFileLogger`](src/main/java/com/knightcode/appliedstoragesorter/logging/SorterFileLogger.java) | `logging` | 通用文件日志 |
| [`SorterPlanFileLogger`](src/main/java/com/knightcode/appliedstoragesorter/logging/SorterPlanFileLogger.java) | `logging` | Plan 日志 |
| [`SorterMergeReportFileLogger`](src/main/java/com/knightcode/appliedstoragesorter/logging/SorterMergeReportFileLogger.java) | `logging` | Merge report 日志 |
| [`ReportFileSupport`](src/main/java/com/knightcode/appliedstoragesorter/logging/ReportFileSupport.java) | `logging` | 报告文件工具基类 |

### 3.8 应用层 (Application Layer)

| 类 | 包路径 | 说明 |
|----|--------|------|
| [`SorterComponentHelper`](src/main/java/com/knightcode/appliedstoragesorter/application/SorterComponentHelper.java) | `application` | 控制台消息构建工具 |
| [`NetworkBindingKey`](src/main/java/com/knightcode/appliedstoragesorter/application/NetworkBindingKey.java) | `application` | 网络绑定键（dimension + controller 坐标） |
| [`NetworkProfileBindingStore`](src/main/java/com/knightcode/appliedstoragesorter/application/NetworkProfileBindingStore.java) | `application` | 网络-profile 绑定持久化 |

### 3.9 网络层 (Network Layer)

| 类 | 包路径 | 说明 |
|----|--------|------|
| [`FileChunkedSender`](src/main/java/com/knightcode/appliedstoragesorter/network/FileChunkedSender.java) | `network` | 分块文件发送器 |
| [`FileChunkPayload`](src/main/java/com/knightcode/appliedstoragesorter/network/FileChunkPayload.java) | `network` | 文件分块载荷 |
| [`FileChunkPayloadHandler`](src/main/java/com/knightcode/appliedstoragesorter/network/FileChunkPayloadHandler.java) | `network` | 分块接收处理器 |
| [`SorterAnalysisPayload`](src/main/java/com/knightcode/appliedstoragesorter/network/SorterAnalysisPayload.java) | `network` | 分析报告通知包 |
| [`SorterAnalysisPayloadHandler`](src/main/java/com/knightcode/appliedstoragesorter/network/SorterAnalysisPayloadHandler.java) | `network` | 分析报告处理器 |
| [`SorterCommandPayload`](src/main/java/com/knightcode/appliedstoragesorter/network/SorterCommandPayload.java) | `network` | 命令网络包 |
| [`SorterCommandPayloadHandler`](src/main/java/com/knightcode/appliedstoragesorter/network/SorterCommandPayloadHandler.java) | `network` | 命令包处理器 |

---

## 4. Gradle 工具链

### 4.1 离线分析任务

| 任务 | 主类 | 说明 |
|------|------|------|
| `analyzeSorterDump` | [`SorterDumpAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/analysis/SorterDumpAnalyzer.java) | 基础 dump 分析 |
| `routingProfileJsonCli` | [`RoutingProfileJsonCli`](src/main/java/com/knightcode/appliedstoragesorter/rule/route/RoutingProfileJsonCli.java) | 路由配置 JSON 读写 |
| `filterUiMetadataCli` | [`FilterUiMetadataCli`](src/main/java/com/knightcode/appliedstoragesorter/rule/filter/FilterUiMetadataCli.java) | Filter UI metadata 导出 |
| `analyzeSorterDumpRouting` | [`SorterDumpRoutingAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/analysis/SorterDumpRoutingAnalyzer.java) | 路由分析 |
| `analyzeSorterDumpRoutingSuggestions` | [`SorterDumpRoutingSuggestionAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/analysis/SorterDumpRoutingSuggestionAnalyzer.java) | 路由建议分析 |
| `generateRoutingProfileDraft` | [`SorterDumpProfileGenerationAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/analysis/SorterDumpProfileGenerationAnalyzer.java) | Draft profile 生成 |
| `analyzeZoneAllocationPlan` | [`ZoneAllocationPlannerAnalyzer`](src/main/java/com/knightcode/appliedstoragesorter/analysis/ZoneAllocationPlannerAnalyzer.java) | 规划分析 |

### 4.2 构建与部署任务

| 任务 | 说明 |
|------|------|
| `build` | 编译 + 测试 + 打包，自动拷贝到 `run/mods` 和 PrismLauncher |
| `copyModJar` | 拷贝 mod JAR 到 `run/mods/`（或通过 `-PmodsDir` 指定） |
| `copyModJarToPrism` | 拷贝 mod JAR 到 PrismLauncher 实例目录 |
| `quickBuild` | 跳过测试，编译 + 打包 + 拷贝到 PrismLauncher |
| `runClient` | 启动 Minecraft 客户端（带模组加载） |
| `runServer` | 启动 Minecraft 服务端 |
| `openDumpsDir` | 在文件管理器中打开 dumps 目录 |
| `syncDumpToReact` | 同步最新 dump JSON 到 React 前端项目 |

### 4.3 离线分析任务参数

```bash
# 基础 dump 分析
./gradlew analyzeSorterDump \
  -PdumpFile=src/main/resources/testfiles/your-dump.json \
  -PanalysisOutput=tmp/analysis.txt

# 路由分析
./gradlew analyzeSorterDumpRouting \
  -PprofileFile=src/main/resources/testfiles/routing-profile-example.json \
  -PdumpFile=src/main/resources/testfiles/your-dump.json \
  -PanalysisOutput=tmp/routing-analysis.txt

# 路由建议分析
./gradlew analyzeSorterDumpRoutingSuggestions \
  -PprofileFile=src/main/resources/testfiles/routing-profile-example.json \
  -PdumpFile=src/main/resources/testfiles/your-dump.json \
  -PanalysisOutput=tmp/routing-suggestions.txt

# 生成 draft profile
./gradlew generateRoutingProfileDraft \
  -PprofileFile=src/main/resources/testfiles/routing-profile-example.json \
  -PdumpFile=src/main/resources/testfiles/your-dump.json \
  -PoutputProfile=tmp/generated-routing-profile.json \
  -PanalysisOutput=tmp/generated-routing-profile.notes.txt

# 规划分析
./gradlew analyzeZoneAllocationPlan \
  -PprofileFile=src/main/resources/testfiles/routing-profile-example.json \
  -PdumpFile=src/main/resources/testfiles/your-dump.json \
  -PanalysisOutput=tmp/zone-allocation-plan.txt

# Filter UI metadata 导出
./gradlew filterUiMetadataCli \
  -PcliArgs="write-default tmp/filter-ui-metadata.json"

# 路由配置 JSON 工具（交互式）
./gradlew routingProfileJsonCli
```

---

## 5. 网络包协议

### 5.1 包类型

| 包类 | 方向 | 说明 |
|------|------|------|
| [`FileChunkPayload`](src/main/java/com/knightcode/appliedstoragesorter/network/FileChunkPayload.java) | Server → Client | 分块文件传输 |
| [`SorterAnalysisPayload`](src/main/java/com/knightcode/appliedstoragesorter/network/SorterAnalysisPayload.java) | Server → Client | 分析报告通知 |
| [`SorterCommandPayload`](src/main/java/com/knightcode/appliedstoragesorter/network/SorterCommandPayload.java) | Client → Server | 客户端命令请求 |

### 5.2 分块传输协议

大文件（如 me-dump JSON、storage-analysis JSON）通过 `FileChunkedSender` 分块发送：

1. Server 写入临时文件
2. 发送 `SorterAnalysisPayload` 通知客户端注册回调
3. 通过 `FileChunkPayload` 分块发送文件内容
4. 客户端 `FileChunkPayloadHandler` 接收并重组

---

## 6. 日志与产物

### 6.1 日志文件

| 文件路径 | 内容 |
|----------|------|
| `logs/appliedstoragesorter.log` | 命令级摘要日志 |
| `logs/appliedstoragesorter/plan-*.log` | Plan 详细日志 |
| `logs/appliedstoragesorter/plan-and-move-*.log` | PlanAndMove 详细日志 |
| `logs/appliedstoragesorter/merge-*.log` | Merge 详细报告 |

### 6.2 JSON 产物

| 文件路径 | 内容 |
|----------|------|
| `dumps/appliedstoragesorter/me-dump-*.json` | 网络快照 |
| `dumps/appliedstoragesorter/storage-analysis-*.json` | 存储分析报告 |

### 6.3 配置文件

| 文件路径 | 内容 |
|----------|------|
| `config/appliedstoragesorter/profiles/*.json` | 全局路由配置 |
| `config/appliedstoragesorter/server.toml` | 服务端配置（`ENABLE_SORTER`, `DEVELOPER_MODE`, `MAX_TRANSFERS_PER_OPERATION`） |

> 完整字段契约见 [`docs/日志与JSON字段契约.md`](docs/日志与JSON字段契约.md)

---

> **相关文档**：[`docs/架构/FACTS.md`](docs/架构/FACTS.md) | [`EXTENSION_GUIDE.md`](EXTENSION_GUIDE.md) | [`前端对接说明.md`](前端对接说明.md) | [`日志与JSON字段契约.md`](日志与JSON字段契约.md)
