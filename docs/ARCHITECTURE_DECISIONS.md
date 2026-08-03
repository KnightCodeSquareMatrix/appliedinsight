# 架构决策记录 (Architecture Decision Records)

> 本文档记录 **Applied Energistics: Insight** 的关键架构决策及其理由。
> 每个 ADR 包含：**标题**、**状态**、**上下文**、**决策**、**后果**。
> 面向架构师、核心贡献者和 AI 协作者。

---

## 📋 目录

- [ADR-001: 六层架构分层](#adr-001-六层架构分层)
- [ADR-002: 规则层保持纯 Java，无 Minecraft 运行时依赖](#adr-002-规则层保持纯-java无-minecraft-运行时依赖)
- [ADR-003: RuntimeTopology 允许适度"胖"](#adr-003-runtimetopology-允许适度胖)
- [ADR-004: Logger 作为基础设施层](#adr-004-logger-作为基础设施层)
- [ADR-005: DAV 不是架构中心](#adr-005-dav-不是架构中心)
- [ADR-006: 三条能力路径独立演进](#adr-006-三条能力路径独立演进)
- [ADR-007: 离线分析链路独立于 Minecraft 运行时](#adr-007-离线分析链路独立于-minecraft-运行时)
- [ADR-008: 前端契约与运行时执行分离](#adr-008-前端契约与运行时执行分离)
- [ADR-009: HTTP API 认证方案](#adr-009-http-api-认证方案)
- [ADR-010: H2 嵌入式数据库](#adr-010-h2-嵌入式数据库)
- [ADR-011: DAV 自动扩容](#adr-011-dav-自动扩容)
- [ADR-012: Ae2StorageAnalyzer 扶正为运行时分析引擎](#adr-012-ae2storageanalyzer-扶正为运行时分析引擎)
- [ADR-013: SmartBus -- Single Block Three-Mode Smart Bus](#adr-013-smartbus----single-block-three-mode-smart-bus)
- [ADR-014: SmartBus 收口改造计划（模式模型对齐 + 交互收敛 + 资源补齐）](#adr-014-smartbus-收口改造计划模式模型对齐--交互收敛--资源补齐)

---

## ADR-001: 六层架构分层

| 属性 | 值 |
|------|-----|
| **状态** | ✅ 已采纳 |
| **决定时间** | 项目初期 |
| **影响范围** | 全局 |

### 上下文

项目从单一的"内部盘碎片整理"工具逐步扩展为 AE2 存储治理平台。随着功能增加（自动整理、规则路由、存储观测），需要清晰的架构分层来管理复杂度。

### 决策

将系统划分为六层，每层有明确的职责和依赖方向：

```
玩家输入层 (Command)
    ↓
规则决策层 (Rule)          ← 纯 Java，无 Minecraft 运行时依赖
    ↓
规划层 (Plan)
    ↓
Zone 运行时层 (Runtime)
    ↓
执行与诊断层 (Execution)
    ↓
分析 / Dashboard 语义层 (Analysis)
```

### 后果

**正面**：
- 各层职责清晰，便于独立演进
- 规则层可独立测试，无需 Minecraft 环境
- 新开发者容易理解系统边界

**负面**：
- 跨层调用需要经过明确的接口，增加少量样板代码
- 某些功能（如 planAndMove）需要串联多层，调用链较长

---

## ADR-002: 规则层保持纯 Java，无 Minecraft 运行时依赖

| 属性 | 值 |
|------|-----|
| **状态** | ✅ 已采纳 |
| **决定时间** | 项目中期 |
| **影响范围** | `rule/**` 包 |

### 上下文

规则层（`rule/filter/`、`rule/route/`、`rule/zone/`）定义了存储区、过滤器、路由规则等核心模型。如果这些模型依赖 Minecraft 或 AE2 运行时类，将导致：
- 无法在离线分析中使用
- 单元测试需要 Mock 复杂的 Minecraft 环境
- 前端无法直接消费规则模型

### 决策

**`rule/**` 包下的所有类不得依赖 Minecraft 或 AE2 运行时类**。规则层只使用：
- Java 标准库
- Gson（JSON 编解码）
- 自定义纯 Java 工具类

运行时层（`ae2/`）负责将规则模型"解释"为运行时对象。

### 后果

**正面**：
- 离线分析器可直接使用规则模型
- 单元测试简单快速
- 规则模型可序列化为 JSON，供前端消费

**负面**：
- 运行时层需要额外的"解释"步骤（如 `RuntimeZoneRegistryBuilder`）
- 某些规则验证需要在运行时层重复

---

## ADR-003: RuntimeTopology 允许适度"胖"

| 属性 | 值 |
|------|-----|
| **状态** | ✅ 已采纳 |
| **决定时间** | 项目中期 |
| **影响范围** | `RuntimeTopology`、`RuntimeZone`、`RuntimeCell` |

### 上下文

RuntimeTopology 是运行时的"世界模型"，包含 zone → machine → cell 三层视图。设计时面临选择：让 Topology 保持精简，将逻辑分散到多个辅助类；还是让 Topology 适度"胖"，承载更多查询和诊断能力。

### 决策

**允许 RuntimeTopology 适度"胖"，以换取其他对象保持精简**。

具体来说，RuntimeTopology 负责：
- 持有"已解释后的世界"（zone 列表、machine 列表、cell 列表）
- 提供查询方法（按 zone 查 cell、按 cell 查 zone 等）
- 提供诊断信息（未分配 cell、外部存储等）

但不负责：
- 规则决策（那是 RoutingEngine 的事）
- 搬运执行（那是 Ae2ZoneMoveExecutor 的事）
- 持久化（那是 ProfileRepository 的事）

### 后果

**正面**：
- RuntimeZone / RuntimeCell 保持轻量，职责单一
- 查询逻辑集中，避免分散
- 诊断功能容易添加

**负面**：
- RuntimeTopology 类可能较大，需要维护其内聚性
- 需要警惕"把所有东西都塞进 Topology"的倾向

---

## ADR-004: Logger 作为基础设施层

| 属性 | 值 |
|------|-----|
| **状态** | ✅ 已采纳 |
| **决定时间** | 项目中期 |
| **影响范围** | `logging/**` 包 |

### 上下文

项目产生多种日志和报告（merge report、plan log、dump 摘要等）。如果日志逻辑侵入核心模型，会导致：
- 核心模型因日志需求而变形
- 日志格式变更影响核心逻辑
- 难以替换日志实现

### 决策

**Logger 一律视为基础设施**，属于独立的基础设施层：
- 日志类（`SorterFileLogger`、`SorterPlanFileLogger`、`SorterMergeReportFileLogger`）不反向定义核心模型
- 核心模型不因日志需求而增加字段或方法
- 日志格式变更不影响核心逻辑

### 后果

**正面**：
- 核心模型保持纯净
- 日志可独立演进
- 容易添加新的日志格式

**负面**：
- 日志类需要从核心模型提取所需数据，可能产生重复的格式化逻辑
- 需要维护日志与核心模型的同步（字段变更时日志可能需要更新）

---

## ADR-005: DAV 不是架构中心

| 属性 | 值 |
|------|-----|
| **状态** | ✅ 已采纳 |
| **决定时间** | 项目中期 |
| **影响范围** | `DigitalAssetVault`、`DigitalAssetManagementCard` |

### 上下文

Digital Asset Vault (DAV) 是玩家与模组交互的主要方块。早期设计可能倾向于以 DAV 为中心组织架构，但这会导致：
- DAV 承担过多职责
- 非 DAV 功能（如 merge 命令）难以集成
- 架构耦合于特定方块

### 决策

**DAV 是管理入口，不是架构中心本身**。架构中心是：
- 规则模型（zone / filter / route）
- 运行时模型（RuntimeTopology / RuntimeZone）
- 分析模型（StorageAnalyzerReport）

DAV 只是这些模型的"展示窗口"之一。

### 后果

**正面**：
- 架构不绑定于特定方块
- 命令（如 `/sorter merge`）可以独立于 DAV 工作
- 未来可以添加其他管理入口

**负面**：
- DAV 的 UI 需要从多个模型聚合数据
- 玩家可能困惑于"为什么有些功能不需要 DAV"

---

## ADR-006: 三条能力路径独立演进

| 属性 | 值 |
|------|-----|
| **状态** | ✅ 已采纳 |
| **决定时间** | 项目中期 |
| **影响范围** | 全局 |

### 上下文

项目当前有三条主要能力路径：
1. **主路径 (Plan/PlanAndMove)**
2. **简化 Merge 路径**
3. **宏观观测路径 (Storage Dashboard)**

它们共享部分基础设施（扫描、日志），但核心逻辑不同。

### 决策

**三条能力路径独立演进，不强行统一**：
- 每条路径有自己的入口（命令）、核心类、输出格式
- 共享基础设施（扫描、日志、网络包）通过公共接口复用
- 不强行将 merge 路径改造为规则路由路径

### 后果

**正面**：
- 每条路径可以独立优化
- 新路径可以快速添加，不影响现有路径
- 避免过度抽象

**负面**：
- 某些功能可能重复实现（如扫描逻辑）
- 需要维护三条路径的文档

---

## ADR-007: 离线分析链路独立于 Minecraft 运行时

| 属性 | 值 |
|------|-----|
| **状态** | ✅ 已采纳 |
| **决定时间** | 项目中期 |
| **影响范围** | `analysis/**` 包、`build.gradle` 中的 JavaExec 任务 |

### 上下文

项目需要在不启动 Minecraft 的情况下分析 dump 数据。如果分析器依赖 Minecraft 运行时，将导致：
- 分析需要完整的 Minecraft 环境
- 无法集成到 CI/CD 流程
- 分析速度慢

### 决策

**离线分析器设计为纯 Java 应用**，通过 Gradle `JavaExec` 任务运行：
- 使用 Gson `JsonReader` 流式读取 dump JSON
- 使用纯 Java 模型计算
- 不依赖 Minecraft client/server 环境
- 可集成到 CI/CD 流程

### 后果

**正面**：
- 分析速度快
- 可在任何 Java 环境中运行
- 适合 CI/CD 集成

**负面**：
- 离线分析器无法访问运行时信息（如 grid 状态）
- 某些分析需要运行时数据时，需要额外的 dump 步骤

---

## ADR-008: 前端契约与运行时执行分离

| 属性 | 值 |
|------|-----|
| **状态** | ✅ 已采纳 |
| **决定时间** | 项目中期 |
| **影响范围** | 前端对接、`RoutingProfile`、`RuntimeTopology` |

### 上下文

前端（React/Web）需要与后端交互，但前端不应直接参与运行时执行。如果前端契约与运行时执行耦合，将导致：
- 前端需要理解 AE2 grid、DAV capability 等运行时细节
- 前端变更可能影响执行逻辑
- 安全风险（前端直接操作存储）

### 决策

**前端契约层和运行时执行层严格分离**：

```
前端生成: RoutingProfile (zones + filters + routeRules)
    ↓
后端规则层消费: RoutingProfileJsonCodec → RoutingEngine
    ↓
运行时层消费规则结果: LiveZoneAllocationPlanner → ZoneAllocationPlan
    ↓
AE2 执行层: Ae2ZoneMoveExecutor
```

前端负责：
- 表达玩家规则意图
- 展示网络宏观事实
- 呈现系统建议
- 让用户对智能默认结果做低成本纠偏

前端不负责：
- 决定具体 cell
- 决定运行时搬运顺序
- 决定 zone 内部整理逻辑
- 决定 DAV 在网络中的实时映射

### 后果

**正面**：
- 前端不需要理解 AE2 运行时细节
- 后端执行逻辑可以独立演进
- 安全边界清晰

**负面**：
- 前端无法提供"实时"的执行状态
- 某些高级功能（如手动指定 cell）需要额外的后端接口

---

---

## ADR-009: HTTP API 认证方案

| 属性 | 值 |
|------|-----|
| **状态** | 📋 提议 |
| **决定时间** | 项目远期规划 |
| **影响范围** | `http/` 包、`command/SorterCommands.java` |

### 上下文

项目计划引入 HTTP API Server，提供 REST API 供前端/手机访问。这带来了认证需求：
- 需要区分不同管理员的操作权限
- 不能依赖外部认证服务（如 OAuth、LDAP），因为模组在离线环境中运行
- 密码不能明文存储
- API 调用需要无状态认证（避免 session 管理）

### 决策

**Mod 内置账号管理，使用 bcrypt + JWT 方案**：

1. **账号管理**：通过游戏内命令管理
   - `/sorter api addUser <username> <password>` — 添加 API 用户
   - `/sorter api removeUser <username>` — 删除 API 用户
   - `/sorter api listUsers` — 列出所有 API 用户
2. **密码存储**：bcrypt 哈希（使用 `org.mindrot.jbcrypt.BCrypt` 或等效实现）
3. **会话认证**：JWT（HMAC-SHA256 签名），服务端持有签名密钥
4. **Token 有效期**：可配置，默认 24 小时
5. **API 鉴权**：所有 REST 端点要求 `Authorization: Bearer <token>` 头

**技术选型原则**：
- 优先使用 JDK 内置能力（如 `javax.crypto.Mac` 用于 HMAC）
- 最小外部依赖：仅引入 bcrypt 和 JWT 库
- 不引入 Spring Security 等重型框架

### 后果

**正面**：
- 零外部依赖的认证方案
- 无状态 API 认证，适合 REST 架构
- 账号管理完全在游戏内完成，无需外部服务
- bcrypt 提供合理的密码安全强度

**负面**：
- 需要维护用户表（H2 或 JSON 文件）
- JWT 无法撤销（除非维护黑名单）
- 需要处理密钥轮换

---

## ADR-010: H2 嵌入式数据库

| 属性 | 值 |
|------|-----|
| **状态** | 📋 提议 |
| **决定时间** | 项目远期规划 |
| **影响范围** | `db/` 包、`logging/` 包、`ae2/dump/` 包 |

### 上下文

项目当前使用纯 JSON 文件存储 storageDump 扫描结果（`dumps/appliedinsight/me-dump-*.json`）。随着功能扩展，JSON 文件方案暴露出以下限制：
- 不支持查询（按物品、按 zone、按时间范围）
- 不支持增量更新，每次扫描全量重写
- 大文件时读写性能差
- 不支持历史数据归档与趋势分析

### 决策

**引入 H2 嵌入式数据库，替代纯 JSON 文件方案**：

1. **技术选型**：[H2 Database](https://www.h2database.com/) 嵌入式模式
   - 零外部进程依赖，嵌入在 JVM 中运行
   - 支持 SQL 标准，兼容性好
   - 文件级持久化，无需安装数据库服务
2. **定位**：基础设施层，与 `logging/` 同级
3. **替换范围**：
   - `SorterStorageAnalysisService` 等 dump 服务增加 H2 写入路径
   - 保留 JSON 导出能力作为"导出"功能（非主要存储）
4. **不替换**：技术运行日志（`SorterFileLogger`）仍使用文本文件

**数据模型（初步）**：

```sql
-- 扫描结果表
CREATE TABLE scan_snapshots (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    network_id  VARCHAR(64)  NOT NULL,
    scan_time   TIMESTAMP    NOT NULL,
    dump_json   CLOB         NOT NULL,  -- 原始 JSON 快照
    format_ver  INT          NOT NULL DEFAULT 1
);

-- 物品分布表（便于按物品查询）
CREATE TABLE item_distributions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    snapshot_id BIGINT       NOT NULL REFERENCES scan_snapshots(id),
    item_id     VARCHAR(256) NOT NULL,
    zone_id     VARCHAR(64),
    cell_count  INT          NOT NULL,
    total_count BIGINT       NOT NULL
);
```

### 后果

**正面**：
- 支持 SQL 查询，按物品/zone/时间范围灵活检索
- 增量更新，避免全量重写
- 历史数据归档，支持趋势分析
- 为 dashboard 提供结构化数据源

**负面**：
- 新增外部依赖（H2 Database）
- 需要维护数据库 schema 迁移
- 嵌入式数据库的并发写入限制（单连接）
- 需要处理数据库文件损坏恢复

---

---

## ADR-011: DAV 自动扩容

| 属性 | 值 |
|------|-----|
| **状态** | ✅ 已采纳 |
| **决定时间** | 项目中期 |
| **影响范围** | `blockentity/DigitalAssetVaultBlockEntity`、`client/screen/DigitalAssetVaultScreen`、`network/`、`Config.java` |

### 上下文

DAV 采用 Cell 吸收模型：每个 DAV 自带相当于两张空 1k Cell 的基础容量（2048 bytes + 126 types，通过 getter 叠加、不写入 NBT）；玩家插入空 Cell 时，Cell 被消耗并转化为额外的 bytes + types 容量。当 DAV 容量即将耗尽时，目标玩家（不会维护 AE2 的大冒险家）不应被要求手动监控容量、手动物造 Cell、手动插入。系统应当自动通过 ME 网络补齐容量。

### 决策

**DAV 支持"自动扩容 Cell"配置，在容量达到阈值时自动从 ME 网络提取 Cell 并吸收。**

1. **玩家配置**：在 DAV GUI 中提供开关 toggle 和文本输入框（输入扩容 Cell 物品 ID，如 `ae2:item_storage_cell_64k`）
2. **触发条件**：`insertStoredItem()` 成功后检查，bytes 使用率 ≥ 90% 或 types 使用率 ≥ 90% 时触发
3. **提取方式**：通过 `grid.getStorageService().getInventory().extract()` 从 ME 网络直接提取可用 Cell，若不可用则轮询等待
4. **吸收方式**：复用现有 Cell 吸收逻辑（`CellCapacityInspector` 验证 + 容量累加）
5. **安全保护**：冷却时间（200 ticks）、活跃任务互斥、最大重试 3 次、全局 Config 开关
6. **持久化**：暂存 BlockEntity NBT，远期迁移至 vault 后端（ADR-010）

### 后果

**正面**：
- 目标玩家不再需要监控容量，DAV 自动维持可用容量
- 与已有 Cell 吸收模型无缝衔接
- 模块独立，不影响 insert/extract 热路径
- 安全保护完善：冷却、互斥、重试、降级

**负面**：
- BlockEntity 职责进一步加重（过渡状态，远期由 vault 后端承载）
- 如果玩家配置了昂贵 Cell 可能导致意外资源消耗
- 需要 ME 网络中存在对应 Cell（玩家需要提前准备库存或合成样板）

---

---

## ADR-012: Ae2StorageAnalyzer 扶正为运行时分析引擎

| 属性 | 值 |
|------|-----|
| **状态** | 🔄 部分实现（Phase 1: CellInfo 替换 RuntimeCell 已完成；Phase 2: StorageDiagnosis 替换 StorageAnalyzerReport 进行中） |
| **决定时间** | 2026-06-04 |
| **影响范围** | `ae2/analysis/`、`ae2/zone/`、`client/analysis/`、`logging/`、`application/` |

### 上下文

当前项目存在三个"分析相关"的概念，语义混淆：

| 位置 | 当前职责 | 实际角色 |
|------|---------|---------|
| `analysis/` (5个CLI) | 读 JSON dump → 离线深度分析 | **离线分析器**（未来对接 Rust/H2） |
| `ae2/analysis/Ae2StorageAnalyzer` | 扫 grid → 产出 `StorageAnalyzerReport` | **运行时分析引擎**（最初的设计意图，但被推迟到开发后期实现） |
| `client/analysis/AnalysisPresenter` | 将 Report 转为 GUI 视图模型 | **展示适配器**（但混入了分析判断逻辑） |

核心问题：`Ae2StorageAnalyzer` 产出的是 raw data（`StorageAnalyzerReport`），而不是分析结论。多个消费者（`AnalysisPresenter`、`SorterStorageAnalysisService`、`SorterMergeReportFileLogger`）各自从 raw data 重新推导相同的语义结论——碎片化严重程度、健康评估、无限容器分类——导致同一语义决策散落在 3 个包中。

同时，`RuntimeCell` 类最初为 move 操作设计（`simulateAcceptedAmount`、`containsExactItem`、`distinctItemKeyCount`），但它也被 zone 系统和即将扶正的分析引擎消费——大量方法是特定消费者不需要的。

### 决策

#### 决策一：Ae2StorageAnalyzer 扶正为唯一的运行时分析引擎

- 输入从 `IGrid` 改为 `RuntimeTopology`（消除独立扫描，复用已有的结构模型）
- 产出从 `StorageAnalyzerReport`（raw data）升级为 **`StorageDiagnosis`**（含分析结论）
- `StorageDiagnosis` 是所有消费者的**唯一共享入口**——GUI、日志、聊天反馈、dump 都只读它
- `AnalysisPresenter` 中的分析判断（`InfiniteContainerCatalog`、`NetworkHealthAssessment`、severity 映射）回归服务端，成为 `StorageDiagnosis` 的一部分
- `CellCapacityInspector` 的判断逻辑（`isLikelyInfiniteCell`、`resolveCellKind`）收归 `Ae2StorageAnalyzer`，不再作为独立工具类被多处在不同语义下调用

#### 决策二：`analysis/` 包定位明确——离线分析

- `analysis/` (CLI 工具) 保持离线，只在 Gradle `JavaExec` 下运行
- 不与运行时路径交互，不污染在线命令链（FACT-049）
- 未来 H2 数据库（ADR-010）和 Rust 分析内核在此层接入
- Dump 降级为开发者工具：仅将 `StorageDiagnosis` + `RuntimeTopology` 序列化为 JSON，供离线分析播种和 H2 导入。不再有独立的 dump 分析路径

#### 决策三：消灭 `RuntimeCell`，改为纯数据 record

- `RuntimeCell` 的所有行为方法删除：`simulateAcceptedAmount()`、`containsExactItem()`、`distinctItemKeyCount()`
- 替换为 `CellInfo` record：仅包含 cell 的身份元数据（位置、容量、类型、zoneId）+ `MEStorage` 引用
- 需要 per-item 数据时，实时查询 `cellInfo.storage().getAvailableStacks()`（AE2 live API），不做快照
- Merge 路径直接使用 `CellInfo` 遍历，调 `MEStorage.extract()` / `MEStorage.insert()`
- Zone 内部持有 `CellInfo` 列表作为选择候选池，但选择逻辑不对外暴露

#### 决策四：Zone 封装 cell 级别操作

- Zone 公开 API：`acceptItem(key, amount) → ZonePlacementDecision`
- cell 选择策略（优先已有 item 的 cell、按 acceptedAmount 降序、按 distinctItemKeyCount 升序、按 slot index）是 Zone 的内部实现
- 外部消费者（`ZoneMergePlanner`、`Ae2ZoneMoveExecutor`）不再直接操作 cell，通过 Zone 间接操作
- 没有配置 zone 时（Merge 路径），`MergeMovePlanner` 直接使用 `List<CellInfo>` 做 first-cell 归并，不经过 Zone 抽象

### 新数据流

```
                    DriveMachineAccessor.findSupportedDrives(grid)  ← 只扫一次
                           │
                           ▼
                    RuntimeTopology
                    (共享结构: Map<zoneId, Zone>, List<CellInfo>, diagnostics)
                      │
          ┌───────────┼───────────┐
          ▼           ▼           ▼
     Plan 路径     Analyze 路径   Dump (开发者工具)
  Zone.acceptItem  Ae2Storage     StorageDiagnosis
  + MoveExecute    Analyzer       .toJson() 或
                   .diagnose()    .toH2Import()
                      │
                      ▼
               StorageDiagnosis
               (结论: healthAssessment, fragmentation,
                cellClassifications, recommendations)
                      │
          ┌───────────┼───────────┐
          ▼           ▼           ▼
        GUI         Chat反馈    日志报告
  (直接渲染字段,  (SorterComponent  (SorterPlanFileLogger
   不再判断        Helper.format    引用 diagnosis
   severity)      Diagnosis行)     字段)
```

### CellInfo record 设计快照

```java
/**
 * Pure-data snapshot of one storage cell inside the topology.
 * Zero behavior — just identity, capacity metadata, and
 * a live MEStorage handle for on-demand item enumeration.
 */
record CellInfo(
    DriveCellReference reference,    // 位置标识
    String zoneId,                   // 所属 zone (__unassigned__ if none)
    String sourceBlockId,            // drive/storage_bus 方块 ID
    IActionHost actionHost,          // AE2 安全上下文
    MEStorage storage,               // 实时查询入口 (getAvailableStacks)
    String cellItemId,               // 如 "ae2:item_storage_cell_64k"
    CellCapacity capacity            // totalBytes, usedBytes, types
) {}
```

### StorageDiagnosis 设计快照（字段级）

```java
record StorageDiagnosis(
    NetworkIdentity network,                // dimensionId, controllerPos
    StorageSummary summary,                 // 总量/容量/碎片化分数/分级
    Map<String, ZoneDiagnosis> perZone,     // 按 zone 的诊断
    List<CellClassification> cellClasses,   // 每 cell 的分类 (容量层级, 无限/普通)
    List<HealthSignal> healthSignals,       // 带 severity 的健康标志
    List<Recommendation> recommendations    // 可执行建议 (可选, Level B)
) {}
```

### 与现有 ADR 的关系

| ADR | 影响 |
|-----|------|
| **ADR-003** (RuntimeTopology 允许适度"胖") | **部分取代**。ADR-003 允许 RuntimeCell 承载查询/诊断能力。本次决策将 cell 行为剥离到 Zone，cell 降为纯数据，Topology 只持有结构。 |
| **ADR-005** (DAV 不是架构中心) | **强化**。分析引擎成为系统的"诊断中心"，进一步巩固 rule/zone/runtime 核心三角。 |
| **ADR-007** (离线分析链路独立) | **补充**。明确 `analysis/` 包与运行时 `ae2/analysis/` 的边界——一个离线消费 dump，一个在线生产 diagnosis。 |
| **ADR-006** (三条能力路径独立演进) | **不影响独立性**。Merge/Plan/Dashboard 仍然独立，但 Dashboard 路径不再有自己的分析分支，改为消费共享 Diagnosis。 |
| **ADR-010** (H2 数据库) | **做准备**。`StorageDiagnosis` 的结构化字段直接成为 H2 写入 schema，dump 是诊断的序列化而非独立扫描。 |

### 后果

**正面**：
- 扫描只发生一次（`DriveMachineAccessor` → `RuntimeTopology` 同时服务 Plan 和 Analyze，当前是两次独立扫描）
- 分析结论单一来源（`StorageDiagnosis`），消除 4 个消费者各自解释 raw data 的分叉问题
- `AnalysisPresenter` 缩为纯粹的 data→component 映射层，不包含业务判断
- Cell 模型简化：`CellInfo` 是纯数据，Zone 封装策略，各自消费者按需使用
- 为 Rust 离线分析内核提供清晰的序列化边界（`StorageDiagnosis` + `CellInfo` 都是 record，天然可序列化）
- 符合"block entity 代码设计薄"原则——`RuntimeTopology` 指针可以直接传给 Rust 侧解析
- 150 张 cell 的规模下，per-item 实时查询 `getAvailableStacks()` 不会有性能问题

**负面**：
- `Ae2StorageAnalyzer` 的输入参数从 `IGrid` 改为 `RuntimeTopology`，需要同步修改 `SorterStorageAnalysisService` 中的调用
- 新 DAV (`DigitalAssetVaultBlockEntity`) 的 cell 目前不被 `RuntimeZoneRegistryBuilder` 覆盖，需要在 `DriveMachineAccessor` 中补充支持或单独处理
- `StorageDiagnosis` 需要网络序列化方案（替代当前的 `StorageAnalyzerReport` 分块传输）。如果诊断数据量明显小于 dump，传输可以更轻量
- 需要回填新 DAV 13 个文件的类职责文档（维度 5 已识别）
- `AnalysisPresenter` 的 `InfiniteContainerCatalog` 和健康评估逻辑迁回服务端后，客户端需要重新对接新 Diagnosis 结构

---


---

## ADR-013: SmartBus -- Single Block Three-Mode Smart Bus

| Status | ✅ 已采纳（经 ADR-014 收口，独立 Block 改为 AE2 Part） |
| Date | 2026-06-04 |
| Scope | SmartBusBlock, SmartBusBE, SmartBusMenu, SmartManagementCard, rule/filter reuse |

### Context

AE2 built-in buses (Import/Export/Storage Bus) only support drag-and-drop itemKey matching.
No mod-level, tag-level, regex, or NBT filtering. Players need a bus that carries the
rule/filter DSL.

AE2 also requires separate items per bus type, cluttering inventory.

### Decision

**Single block, three modes:**
- One block: SmartBusBlock, ID appliedinsight:smart_bus
- Three modes: IMPORT / EXPORT / STORAGE, stored as blockstate smart_mode + BE NBT
- Sneak-right-click cycles mode: IMPORT -> EXPORT -> STORAGE -> IMPORT
- Each mode will have distinct visual (textures TBD; blockstate ready)

**Card carries filter rules:**
- New item: SmartManagementCardItem (CUSTOM_DATA pattern, same as existing card)
- Card stores: mode + filter (FilterExpression JSON) + optional sourceFilter
- Card slot in block GUI; no card = block does nothing (safe default)
- Card is portable: pull it out, put it in another SmartBus, rules follow

**Runtime behavior:**
- IMPORT: poll adjacent container -> ItemFilterMatcher -> extract matching -> insert to ME network
- EXPORT: query ME network -> ItemFilterMatcher -> extract matching -> insert to adjacent container
- STORAGE: register IStorageProvider, accept only filter-matching inserts

**Reuse rule/filter DSL:**
- Card filter serializes FilterExpression JSON directly
- Shares FilterUiMetadata schema with RoutingProfile
- Future Dashboard filter editor works on both
- No new filter semantics invented

### Consequences

Positive:
- One block replaces three AE2 bus types
- Full filter DSL: mod, tag, regex, NBT
- Portable card: rules travel with the card
- Reuses existing ItemFilterMatcher, FilterUiMetadata

Negative:
- New full pipeline: Block + BE + Menu + Screen + Network Payload
- Card codec needed (similar to RoutingProfileJsonCodec)
- IMPORT/EXPORT polling in server tick adds load
- Distinct textures needed for three modes (future)
- rule/route + rule/zone fate still pending (ADR-012 discussion concluded they can be cut)


> **相关文档**：[`架构/FACTS.md`](架构/FACTS.md)（原子事实库） | [`ai/AI_ENTRY.md`](ai/AI_ENTRY.md)（AI 入口） | [`参考/前端对接说明.md`](参考/前端对接说明.md) | [`下一步计划.md`](下一步计划.md#10-未来方向http-api-server--h2-数据库)

---

## ADR-014: SmartBus 收口改造计划（模式模型对齐 + 交互收敛 + 资源补齐）

| 属性 | 值 |
|------|-----|
| **状态** | ✅ 已采纳 |
| **决定时间** | 2026-06-04 |
| **影响范围** | `ae2/part/SmartBusPart`、`SmartBusMenu`、`SmartBusScreen`、`SmartManagementCardItem`、`SmartCardConfigScreen`、`SmartBusModePayload`、`SorterItems`、`assets/appliedinsight/smart_bus*` |

### 上下文

在实现验证阶段确认，SmartBus 若继续作为独立 `Block` / `BlockEntity` 存在，将无法获得 AE2 总线的关键行为：
- 无法挂载在 `CableBusBlockEntity` 上与 cable / facade / 其他 parts 共存
- 无法复用 AE2 part 自带的碰撞箱聚合
- 无法复用 AE2 的白色 placement preview / part outline 逻辑
- 无法以 AE2 part 语义接入网络与菜单定位

因此，原先的 block 形态只适合作为早期原型，不适合作为最终实现方向。

### 决策

#### 决策一：SmartBus 改为 AE2 Part，而不是独立方块

- `smart_bus` 物品改为 AE2 `PartItem`
- 真正的运行时对象为 `ae2/part/SmartBusPart`
- 宿主为 AE2 `CableBusBlockEntity` / `IPartHost`
- 旧 `SmartBusBlock` / `SmartBusBlockEntity` 路线退出活跃实现路径

#### 决策二：保留“三模式单对象”产品语义，但由 Part 持有模式

- `SmartBusPart` 持有 IMPORT / EXPORT / STORAGE 模式
- `SmartManagementCardItem` 只存 filter JSON，不再存 mode
- 模式切换入口：
  1. 空手潜行右击 placed part
  2. SmartBus GUI 内按钮

#### 决策三：碰撞箱、预览与视觉语义全部对齐 AE2 Part 机制

- `SmartBusPart.getBoxes()` 提供各模式的真实 part 体积
- placement preview 和 outline 由 AE2 part 系统自动负责
- 静态模型按模式映射到 AE2 import/export/storage bus 资源语义
- `smart_bus` item model 改为 part item 风格，而不是 block-item fallback

#### 决策四：菜单与 payload 以 part 定位，不再以 block BE 定位

- `SmartBusMenu` 绑定 `SmartBusPart`
- `SmartBusModePayload` 以 host `BlockPos + side` 寻址
- GUI 层只展示 mode / card / filter summary，不承担复杂规则编辑

### 已完成实现

- `smart_bus` 已注册为 AE2 `PartItem<SmartBusPart>`
- `SmartBusPart` 已提供：
  - mode 状态
  - card slot inventory
  - part collision boxes
  - static model selection
  - GUI opening / mode cycling
- `SmartBusMenu` / `SmartBusScreen` / `SmartBusModePayload` 已迁移到 part 语义
- `SmartCardConfigScreen` 已收敛为 filter-only 编辑器
- `./gradlew compileJava` 与 `./gradlew build` 均通过

### 仍未完成

- IMPORT / EXPORT / STORAGE 的真实运行逻辑
- 存储模式对应的 `IStorageProvider` 暴露路径
- 与现有 Filter DSL 的正式 matcher 执行链
- 自定义美术与 recipe 定稿

### 后果

**正面**：
- SmartBus 终于具备 AE2 part 的正确放置、预览、碰撞箱和宿主语义
- 未来接入真实 import/export/storage 行为时，不再需要推翻宿主层实现
- 玩家看到的是“真正的 AE2 总线扩展件”，不是伪装方块

**负面**：
- 原 ADR 中“单方块三模式”的字面描述失效，需要按 part 语义理解
- 原型阶段写下的 block/blockentity 文档与会话总结需要同步修正

### 与现有 ADR 的关系

| ADR | 影响 |
|-----|------|
| **ADR-013** | **被实现细化并纠偏**。保留“单对象三模式 + 卡片带过滤规则”的产品语义，但宿主从独立方块改为 AE2 part。 |
| **ADR-005** | **一致**。SmartBus 仍是玩家输入入口，而非运行时决策引擎本身。 |
| **ADR-008** | **一致**。复杂过滤编辑仍由卡片独立界面承担，而非塞进 part 的小型 GUI。 |

> **验收口径（当前阶段）**：`smart_bus` 能作为 AE2 part 物品参与放置；编译与完整构建通过；GUI 和模式切换使用 part 寻址；不再依赖旧 `SmartBusBlock` / `SmartBusBlockEntity` 活跃实现路径。
