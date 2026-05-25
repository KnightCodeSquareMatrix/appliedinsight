# DriveMachineAccessor
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/scan/DriveMachineAccessor.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.scan`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
把 AE2 Drive、ExtendedAE Drive 和 DAV 统一抽象成可扫描的 DriveMachine，并暴露 cell 访问能力。

## 边界检查
边界基本正确：这是专门的 AE2 适配层。但它依赖反射和 blockId 白名单，属于明确的技术债。

## 抽象检查
抽象层级合适，没有为了未来做大而发明复杂接口；当前项目规模下用内部 DriveMachine 包一层就够了。

## 主要协作者
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`
- `appeng.api.networking.IGrid`
- `appeng.api.networking.security.IActionHost`
- `appeng.api.storage.MEStorage`
- `appeng.api.storage.cells.StorageCell`
- `net.minecraft.core.BlockPos`
- `net.minecraft.core.registries.BuiltInRegistries`
- `net.minecraft.world.level.block.entity.BlockEntity`

## 维护备注
- 反射访问 getCellCount/getCellInventory/getOriginalCellInventory 容易受上游实现变化影响。
- SUPPORTED_DRIVE_BLOCK_IDS 是显式白名单，后续如果支持更多设备，需要同步维护。
