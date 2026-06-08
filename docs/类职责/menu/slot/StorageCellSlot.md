# StorageCellSlot
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/menu/slot/StorageCellSlot.java`
- **包**: `com.knightcode.appliedstoragesorter.menu.slot`
- **类型**: `class`
- **所属层**: 容器交互层

## 职责
承载容器菜单或槽位限制逻辑。

## 边界检查
边界健康。该类型位于应用装配或游戏集成层，依赖 NeoForge/Minecraft/AE2 是职责内的事情。

## 抽象检查
整体没有过度抽象，职责保持在可理解范围内。

## 主要协作者
- `appeng.api.storage.StorageCells`
- `net.minecraft.world.item.ItemStack`
- `net.neoforged.neoforge.items.IItemHandler`
- `net.neoforged.neoforge.items.SlotItemHandler`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
