# DigitalAssetManagementCardSlot
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/menu/slot/DigitalAssetManagementCardSlot.java`
- **包**: `com.knightcode.appliedstoragesorter.menu.slot`
- **类型**: `class`
- **所属层**: 容器交互层

## 职责
DAV 管理卡槽，仅接受 `DigitalAssetManagementCardItem` 类型的物品，允许玩家自由插拔。

## 边界检查
边界健康。该类型位于应用装配或游戏集成层，依赖 NeoForge/Minecraft/AE2 是职责内的事情。

## 抽象检查
整体没有过度抽象，职责保持在可理解范围内。

## 关键设计
- **`mayPlace()`**: 直接检查 `stack.is(SorterItems.DIGITAL_ASSET_MANAGEMENT_CARD.get())`，不依赖 `super.mayPlace()` 的 `IItemHandler.isItemValid()` 代理链，避免 `SlotItemHandler → InternalInventoryItemHandler → AppEngInternalInventory` 链中可能出现的代理不一致问题
- **`mayPickup()`**: 始终返回 `true`，玩家可自由取出管理卡

## 主要协作者
- `com.knightcode.appliedstoragesorter.registry.SorterItems`
- `net.minecraft.world.item.ItemStack`
- `net.neoforged.neoforge.items.IItemHandler`
- `net.neoforged.neoforge.items.SlotItemHandler`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
- 2026-05-25: `mayPlace()` 简化为仅检查物品类型（不再调用 `super.mayPlace()`），消除可能的代理链阻断问题；新增 `mayPickup()` 返回 `true` 确保可取出。
