# 基础设施总览 (Infrastructure Overview)

> 本文档描述 Applied Energistics: Insight 的基础设施层。
> 基础设施是支撑业务逻辑运转的底层能力，不参与业务决策。

## 1. Logger 基础设施

### 核心原则（来自 Logger 阶段验证通过）

- logger 一律视为基础设施，不反向定义核心模型
- 技术运行日志使用 `SLF4J + Lombok @Slf4j`，占位符风格
- 报告型输出（plan/merge/dump/analysis）由专门输出器自行拼装内容并落盘
- 核心类（`RuntimeTopology`、`RuntimeZone`、`Ae2MoveAnalyzer`、`Ae2ZoneMoveExecutor` 等）不因日志需求扭曲自身职责
- 核心层先产结构化结果，logger 再做文本渲染

### 日志体系

| 类 | 职责 | 输出位置 |
|---|------|---------|
| `SorterFileLogger` | 命令摘要日志 | `logs/appliedinsight.log` |
| `SorterMergeReportFileLogger` | merge 详细复盘日志 | `logs/appliedinsight/merge-*.log` |
| `SorterPlanFileLogger` | plan / planAndMove 详细日志 | `logs/appliedinsight/plan-*.log` / `plan-and-move-*.log` |
| `ReportFileSupport` | 共享工具：文件写入、坐标格式化、时间戳、命令头 | — |

### 日志产物

| 产物 | 类型 | 说明 |
|------|------|------|
| `logs/appliedinsight.log` | 技术运行日志 | 命令级摘要 |
| `logs/appliedinsight/plan-*.log` | 报告型 | plan 明细 |
| `logs/appliedinsight/plan-and-move-*.log` | 报告型 | planAndMove 明细 |
| `logs/appliedinsight/merge-*.log` | 报告型 | merge 复盘明细 |
| `dumps/appliedinsight/me-dump-*.json` | JSON 导出 | 网络快照 |
| `dumps/appliedinsight/storage-analysis-*.json` | JSON 导出 | 存储分析报告 |

## 2. 网络通信层

| 类 | 职责 |
|---|------|
| `SorterCommandPayload` | 命令请求网络包 |
| `SorterCommandPayloadHandler` | 命令包处理 |
| `SorterAnalysisPayload` | 分析结果网络包 |
| `SorterAnalysisPayloadHandler` | 分析包处理 |
| `FileChunkPayload` | 大文件分块传输包 |
| `FileChunkPayloadHandler` | 分块包处理 |
| `FileChunkedSender` | 大文件分块发送器 |

**原则**：`network/` 只负责网络包序列化/传输，不包含业务逻辑。（FACT-050）

## 3. 离线分析层

| 类 | 职责 |
|---|------|
| `SorterDumpAnalyzer` | dump 文件分析入口 |
| `SorterDumpRoutingAnalyzer` | 路由分析 |
| `SorterDumpRoutingSuggestionAnalyzer` | 路由建议生成 |
| `SorterDumpProfileGenerationAnalyzer` | Profile 生成 |
| `ZoneAllocationPlannerAnalyzer` | Zone 分配计划分析 |

**原则**：`analysis/` 不污染在线命令链，通过 Gradle JavaExec 任务运行。（FACT-049）

## 4. 规则引擎层

| 包 | 类数 | 职责 |
|----|------|------|
| `rule/filter/` | 17 | 物品过滤条件模型 |
| `rule/route/` | 12 | 路由规则模型 |
| `rule/zone/` | 2 | 存储区模型 |

**原则**：`rule/**` 不导入 `net.minecraft.*` 或 `appeng.*`，纯 Java 实现。（FACT-044）

## 5. 共享工具层

| 类 | 职责 |
|---|------|
| `ReportFileSupport` | 文件写入、坐标格式化、时间戳、命令头生成 |

## 相关文档

| 文档 | 说明 |
|------|------|
| [`类职责/logging/SorterFileLogger.md`](../类职责/logging/SorterFileLogger.md) | SorterFileLogger 职责 |
| [`类职责/logging/SorterMergeReportFileLogger.md`](../类职责/logging/SorterMergeReportFileLogger.md) | SorterMergeReportFileLogger 职责 |
| [`类职责/logging/SorterPlanFileLogger.md`](../类职责/logging/SorterPlanFileLogger.md) | SorterPlanFileLogger 职责 |
| [`类职责/logging/ReportFileSupport.md`](../类职责/logging/ReportFileSupport.md) | ReportFileSupport 职责 |
| [`日志与JSON字段契约.md`](../日志与JSON字段契约.md) | 所有日志产物的字段契约 |
| [`架构/FACTS.md`](../架构/FACTS.md) | 项目事实库（含基础设施相关 FACT） |
