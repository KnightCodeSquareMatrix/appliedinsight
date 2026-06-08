# NewDavMigrationResult
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/application/result/NewDavMigrationResult.java`
- **包**: `com.knightcode.appliedstoragesorter.application.result`
- **类型**: `record`
- **所属层**: 应用服务层

## 职责
单次 DAV 迁移调用的结果 DTO：`plannedMoveCount`、`attemptedMoveCount`、`completedMoveCount`、`movedAmount`、`foundWork`、`statusKey`（翻译 key）。

## 边界检查
边界健康。纯数据；`idle(statusKey)` 工厂方法表示无工作可做。

## 主要协作者
- `com.knightcode.appliedstoragesorter.application.NewDavMigrationService`
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`（读取并更新 GUI 状态）

## 维护备注
- 新增 idle 原因时同步 lang 与 `DigitalAssetVaultBlockEntity` 状态展示逻辑。
