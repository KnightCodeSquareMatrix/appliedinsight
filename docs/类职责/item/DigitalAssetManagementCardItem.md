# DigitalAssetManagementCardItem
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/item/DigitalAssetManagementCardItem.java`
- **包**: `com.knightcode.appliedstoragesorter.item`
- **类型**: `class`
- **所属层**: 物品/玩家输入层

## 职责
管理卡物品，实现 zoneId/zoneName 的读写、清理和 tooltip 展示。

## 边界检查
边界健康。玩家输入载体和规则运行时没有耦死。

## 抽象检查
没有过度抽象。

## 主要协作者
- `net.minecraft.ChatFormatting`
- `net.minecraft.core.component.DataComponents`
- `net.minecraft.nbt.CompoundTag`
- `net.minecraft.network.chat.Component`
- `net.minecraft.world.item.Item`
- `net.minecraft.world.item.ItemStack`
- `net.minecraft.world.item.TooltipFlag`
- `net.minecraft.world.item.component.CustomData`
- `org.jetbrains.annotations.Nullable`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
