# DriveMachineAccessor
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/scan/DriveMachineAccessor.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.scan`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
把 AE2 Drive、ExtendedAE Drive 和 DAV 统一抽象成可扫描的 DriveMachine，并暴露 cell 访问能力。

## 边界检查
边界正确：这是专门的 AE2 适配层，`ae2/` 层可以依赖 AE2 API。

## 抽象检查
抽象层级合适，没有为了未来做大而发明复杂接口；当前项目规模下用内部 DriveMachine 包一层就够了。

## 主要协作者
- `appeng.api.implementations.blockentities.IChestOrDrive` — 编译时接口优先访问
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`
- `appeng.api.networking.IGrid`
- `appeng.api.networking.security.IActionHost`
- `appeng.api.storage.MEStorage`
- `appeng.api.storage.cells.StorageCell`
- `net.minecraft.core.BlockPos`
- `net.minecraft.core.registries.BuiltInRegistries`
- `net.minecraft.world.level.block.entity.BlockEntity`

## 维护备注
- 优先通过 `IChestOrDrive` 接口访问 drive 信息（编译时安全），反射仅作为 fallback。
- `SUPPORTED_DRIVE_BLOCK_IDS` 是显式白名单，后续如果支持更多设备，需要同步维护。
- 反射 fallback 保留在 `findZeroArgMethod` / `findSingleIntMethod` 中，用于不支持 `IChestOrDrive` 的第三方 drive。
