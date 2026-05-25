# DigitalAssetVaultBlock
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/block/DigitalAssetVaultBlock.java`
- **包**: `com.knightcode.appliedstoragesorter.block`
- **类型**: `class`
- **所属层**: 方块声明层

## 职责
DAV 方块定义：控制交互行为和朝向策略，并把 GUI 打开动作委托给 block entity。

## 边界检查
边界健康。方块类没有直接承载存储或 zone 逻辑。

## 抽象检查
没有过度抽象。

## 主要协作者
- `appeng.api.orientation.IOrientationStrategy`
- `appeng.api.orientation.OrientationStrategies`
- `appeng.block.AEBaseEntityBlock`
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`
- `net.minecraft.core.BlockPos`
- `net.minecraft.world.InteractionResult`
- `net.minecraft.world.entity.player.Player`
- `net.minecraft.world.level.Level`
- `net.minecraft.world.level.block.Block`
- `net.minecraft.world.level.block.state.BlockState`
- `net.minecraft.world.level.block.state.StateDefinition`
- `net.minecraft.world.phys.BlockHitResult`

## 维护备注
- createBlockStateDefinition 当前只是调用 super，属于可保留也可删除的样板代码。
