# DigitalAssetVaultMenu
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/menu/DigitalAssetVaultMenu.java`
- **包**: `com.knightcode.appliedstoragesorter.menu`
- **类型**: `class`
- **所属层**: 容器交互层

## 职责
DAV 容器菜单，组织 10 个 cell 槽、1 个管理卡槽和玩家背包槽位。

## 边界检查
边界健康。GUI 布局与 block entity 逻辑保持分离。

## 抽象检查
没有过度抽象。

## 主要协作者
- `appeng.menu.AEBaseMenu`
- `appeng.menu.SlotSemantic`
- `appeng.menu.SlotSemantics`
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`
- `com.knightcode.appliedstoragesorter.menu.slot.DigitalAssetManagementCardSlot`
- `com.knightcode.appliedstoragesorter.menu.slot.StorageCellSlot`
- `com.knightcode.appliedstoragesorter.registry.SorterMenus`
- `net.minecraft.core.BlockPos`
- `net.minecraft.world.entity.player.Inventory`
- `net.minecraft.world.inventory.Slot`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
