# 架构决策记录 (Architecture Decision Records)

> 本文档记录 **Applied Storage Sorter** 的关键架构决策及其理由。
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

项目当前使用纯 JSON 文件存储 storageDump 扫描结果（`dumps/appliedstoragesorter/me-dump-*.json`）。随着功能扩展，JSON 文件方案暴露出以下限制：
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

> **相关文档**：[`架构/FACTS.md`](架构/FACTS.md)（原子事实库） | [`ai/AI_ENTRY.md`](ai/AI_ENTRY.md)（AI 入口） | [`参考/前端对接说明.md`](参考/前端对接说明.md) | [`下一步计划.md`](下一步计划.md#10-未来方向http-api-server--h2-数据库)
