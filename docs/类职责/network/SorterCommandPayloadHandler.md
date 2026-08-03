# SorterCommandPayloadHandler

| 维度 | 说明 |
|------|------|
| **包路径** | `network/SorterCommandPayloadHandler.java` |
| **定位** | 方块 GUI 命令请求服务端处理器 |
| **职责** | 接收 `SorterCommandPayload`，验证方块节点在线状态，构建 `CommandSourceStack`（基于方块节点绕过射线检测），根据 `buttonId` 分发到对应 Service |
| **关键方法** | `handle(SorterCommandPayload, IPayloadContext)` |
| **分发表** | `CMD_ME_DUMP` → `SorterDumpService`；`CMD_ME_STORAGE_DUMP` → `SorterStorageAnalysisService`；`CMD_ME_PLAN` → `SorterPlanService(executeMove=false)`；`CMD_ME_PLAN_AND_MOVE` → `SorterPlanService(executeMove=true)`；`CMD_MERGE` → `SorterMergeService` |
| **依赖** | `SorterCommandPayload`、`SorterCommandBlockMenu`、`SorterDumpService`、`SorterMergeService`、`SorterPlanService`、`SorterStorageAnalysisService` |
| **边界** | 使用基于方块节点的 `GridTargetResolver`（绕过射线检测），与命令行的 `Ae2ControllerTargetResolver` 不同 |
