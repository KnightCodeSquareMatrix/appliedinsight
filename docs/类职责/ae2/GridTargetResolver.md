# GridTargetResolver

| 维度 | 说明 |
|------|------|
| **包路径** | `ae2/GridTargetResolver.java` |
| **定位** | 网格目标解析策略抽象（函数式接口） |
| **职责** | 定义从 `CommandSourceStack` 解析 AE2 网格目标的策略接口，service 层通过此接口获取 `Ae2GridTargetResult` |
| **关键方法** | `resolve(CommandSourceStack)` → `Ae2GridTargetResult` |
| **实现** | `Ae2ControllerTargetResolver`（射线检测默认实现）；`SorterCommandPayloadHandler` 中的 `nodeResolver`（基于方块节点） |
| **被谁使用** | `SorterDumpService`、`SorterMergeService`、`SorterPlanService`、`SorterStorageAnalysisService` |
| **边界** | 不关心具体解析方式，只定义契约 |
