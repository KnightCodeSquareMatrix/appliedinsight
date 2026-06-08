# StorageDiagnosis
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/analysis/StorageDiagnosis.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.analysis`
- **类型**: `record`（含多个嵌套 record）
- **所属层**: AE2 集成层 / 分析语义层

## 职责
ADR-012 统一分析结论模型。`Ae2StorageAnalyzer` 产出此 record，供 GUI、聊天反馈、日志 dump 消费。相比旧 `StorageAnalyzerReport`：
- `HealthSignal` 带 `Severity`，消费者无需自行猜测严重级别
- `CellSignal` 显式记录 cell 分类，而非 ad-hoc 字符串

## 边界检查
边界健康。纯数据 record，无 AE2 副作用；不执行搬运、不修改网络状态。

## 主要嵌套类型
| 类型 | 用途 |
|------|------|
| `NetworkSummary` | 网络级统计（节点数、内外部存量、碎片化分数等） |
| `StorageLocation` | 单存储位置快照 |
| `ItemDistribution` | 物品分布 / 碎片化条目 |
| `HealthSignal` | 健康标志 + 严重级别 |
| `CellSignal` | Cell 分类信号 |

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.analysis.Ae2StorageAnalyzer`（生产者）
- `com.knightcode.appliedstoragesorter.application.SorterStorageAnalysisService`
- `com.knightcode.appliedstoragesorter.ae2.dump.SorterStorageAnalysisDumpWriter`

## 维护备注
- 迁移进行中：部分客户端路径仍消费 `StorageAnalyzerReport`，需逐步切换至本类型。
- 新增 health flag 时同步 `AnalysisPresenter` 与 lang `analysis.appliedinsight.presenter.*`。
