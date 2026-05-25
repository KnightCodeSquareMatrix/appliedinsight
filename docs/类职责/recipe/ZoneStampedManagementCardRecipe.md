# ZoneStampedManagementCardRecipe
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/recipe/ZoneStampedManagementCardRecipe.java`
- **包**: `com.knightcode.appliedstoragesorter.recipe`
- **类型**: `class`
- **所属层**: 配方输入层

## 职责
自定义配方：把带名称的 AE2 Name Press 文字压印到空白管理卡上。

## 边界检查
边界健康。配方只负责玩家输入生成，不承担 zone 运行时逻辑。

## 抽象检查
没有过度抽象。

## 主要协作者
- `com.knightcode.appliedstoragesorter.item.DigitalAssetManagementCardItem`
- `com.knightcode.appliedstoragesorter.registry.SorterItems`
- `com.knightcode.appliedstoragesorter.registry.SorterRecipeSerializers`
- `appeng.api.ids.AEComponents`
- `net.minecraft.core.HolderLookup`
- `net.minecraft.core.NonNullList`
- `net.minecraft.resources.ResourceLocation`
- `net.minecraft.world.item.ItemStack`
- `net.minecraft.world.item.crafting.CraftingBookCategory`
- `net.minecraft.world.item.crafting.CraftingInput`
- `net.minecraft.world.item.crafting.CustomRecipe`
- `net.minecraft.world.item.crafting.RecipeSerializer`
- 其余依赖省略 1 项，以源码为准。

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
