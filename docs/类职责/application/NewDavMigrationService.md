# NewDavMigrationService
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/application/NewDavMigrationService.java`
- **包**: `com.knightcode.appliedstoragesorter.application`
- **类型**: `final class`（静态方法）
- **所属层**: 应用服务层

## 职责
DAV「导入现有库存」单次迁移步骤：当玩家开启 migrate 开关且 DAV 已接入网格时，从 ME 全局库存扫描物品，将网络中尚未存在于 DAV 的 stack 迁入 DAV（受字节/类型余量与 `maxTransfers` 限制）。

## 边界检查
边界健康。不持有状态；每次调用由 `DigitalAssetVaultBlockEntity` 定时触发；结果通过 `NewDavMigrationResult` 返回。

## 主要协作者
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`
- `com.knightcode.appliedstoragesorter.application.result.NewDavMigrationResult`
- `appeng.api.storage.MEStorage` / `KeyCounter`

## 维护备注
- 详细日志在 `Config.VERBOSE_LOGGING` 开启时输出 `[DAV-Migrate]` 行。
- 状态 lang key 前缀：`screen.appliedinsight.digital_asset_vault.status.migration_*`
