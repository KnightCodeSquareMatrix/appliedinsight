# SorterRecipeSerializers
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/registry/SorterRecipeSerializers.java`
- **包**: `com.knightcode.appliedstoragesorter.registry`
- **类型**: `class`
- **所属层**: 注册装配层

## 职责
集中注册模组自定义 `RecipeSerializer`，避免配方序列化器散落各处。

## 已注册序列化器
| ID | 配方类 | 说明 |
|----|--------|------|
| `appliedinsight:dav_cell_copy` | `DavCellCopyRecipe` | 空白 + 已绑定 DAV Cell → 2 张同 ID DAV Cell |

> 历史 `ZoneStampedManagementCardRecipe`（管理卡压印）已随管理卡移除而注销。

## 边界检查
边界健康。该类型位于应用装配层，依赖 NeoForge/Minecraft 是职责内的事情。

## 主要协作者
- `com.knightcode.appliedstoragesorter.recipe.DavCellCopyRecipe`
- `com.knightcode.appliedstoragesorter.AppliedStorageSorter`
- `net.minecraft.core.registries.Registries`
- `net.minecraft.world.item.crafting.RecipeSerializer`
- `net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer`
- `net.neoforged.neoforge.registries.DeferredRegister`

## 维护备注
- 在 `AppliedStorageSorter` 构造中调用 `SorterRecipeSerializers.register(modEventBus)`。
