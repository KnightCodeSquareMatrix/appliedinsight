# PlayerFacingAnalysis
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/client/analysis/PlayerFacingAnalysis.java`
- **包**: `com.knightcode.appliedstoragesorter.client.analysis`
- **类型**: `record`（含嵌套 record / enum）
- **所属层**: 客户端展示层

## 职责
SorterCommandBlock GUI 的**玩家向分析视图模型**。所有展示文本均为已本地化的 `Component`，Screen 只负责布局与颜色。

## 渲染契约
| 字段 | GUI 用法 |
|------|----------|
| `health` | 顶栏/摘要区整体健康色 |
| `storage` | 中栏容量、碎片化、Top 碎片物品 |
| `lines` | 右栏诊断列表；`headline` 必显，`detail` 可选，`tooltipBody` 悬停 |
| `goodNews` | 绿色勾选正面提示，无 tooltip |

## 嵌套类型
- `Severity`：INFO / WARNING / CRITICAL
- `NetworkHealthAssessment`：GOOD / FAIR / POOR / UNKNOWN
- `StorageSummary`、`AnalysisLine`、`GoodNews`

## 主要协作者
- `com.knightcode.appliedstoragesorter.client.analysis.AnalysisPresenter`（唯一生产者）
- `com.knightcode.appliedstoragesorter.client.screen.SorterCommandBlockScreen`

## 维护备注
- 不在 Screen 内重复业务判断；新 health flag 在 Presenter 中映射后再加 lang。
