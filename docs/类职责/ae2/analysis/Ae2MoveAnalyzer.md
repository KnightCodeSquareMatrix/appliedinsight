# Ae2MoveAnalyzer

| 维度 | 说明 |
|------|------|
| **包路径** | `ae2/analysis/Ae2MoveAnalyzer.java` |
| **定位** | 搬运可行性预检分析器 |
| **职责** | 在正式执行前检查 RuntimeTopology 是否具备执行指定 ZoneAllocationPlan 的条件（目标 zone 是否存在、是否有 runtime cell） |
| **关键方法** | `analyze(RuntimeTopology, ZoneAllocationPlan)` → `RuntimeMoveAnalysisReport` |
| **输出** | `RuntimeMoveAnalysisReport`：`fullyAdmissible` + `blockingReasons` 列表 |
| **依赖** | `RuntimeTopology`、`ZoneAllocationPlan`、`ItemZoneAssignment` |
| **被谁使用** | 规划执行链路的前置检查 |
| **边界** | 只做"能不能搬进去"判断，不做执行；不修改任何状态 |
