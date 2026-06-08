# AnalysisPresenter
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/client/analysis/AnalysisPresenter.java`
- **包**: `com.knightcode.appliedstoragesorter.client.analysis`
- **类型**: `final class`（静态 `present()`）
- **所属层**: 客户端展示层

## 职责
将服务端下发的 **`StorageAnalyzerReport`**（技术 JSON 结构）转换为 GUI 可渲染的 **`PlayerFacingAnalysis`**：
- 网络健康评估（GOOD / FAIR / POOR）
- 中间栏容量摘要
- 右侧诊断行（headline + detail + hover tooltip）
- 正面消息（good news）

## 边界检查
边界健康。仅做客户端视图转换，不发起网络请求、不修改服务端状态。严重级别映射与 health flag 文案在 lang `analysis.appliedinsight.presenter.*`。

## 主要协作者
- `com.knightcode.appliedstoragesorter.client.analysis.PlayerFacingAnalysis`
- `com.knightcode.appliedstoragesorter.ae2.analysis.StorageAnalyzerReport`（当前输入）
- `com.knightcode.appliedstoragesorter.client.format.LocationIdFormatter`
- `com.knightcode.appliedstoragesorter.client.screen.SorterCommandBlockScreen`

## 维护备注
- ADR-012：目标输入类型为 `StorageDiagnosis`；迁移完成后 `present()` 应接受新类型并删除 report 分支。
