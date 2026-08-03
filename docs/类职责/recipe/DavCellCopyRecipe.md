# DavCellCopyRecipe
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/recipe/DavCellCopyRecipe.java`
- **包**: `com.knightcode.appliedstoragesorter.recipe`
- **类型**: `class`
- **所属层**: 配方输入层

## 职责
自定义工作台配方：空白 DAV Cell + 已绑定 `dav_cell_id` 的 DAV Cell → 2 张共享源 ID 的 DAV Cell。两张输入均被消耗；仅复制访问点钥匙，不复制 `DavCellSavedData` 中的库存。

## 匹配规则
- 合成格内**恰好** 2 张 `DavCellItem`，无其他物品。
- 其中一张为空白 Cell（`DavCellStack.isBlank`），另一张带有效 `dav_cell_id`。
- 若出现两张均已绑定但 ID 不同，不匹配。

## 边界检查
边界健康。配方只负责玩家侧物品生成，不承担 zone 运行时或后端迁移逻辑。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellStack`
- `com.knightcode.appliedstoragesorter.registry.SorterRecipeSerializers`
- `net.minecraft.world.item.crafting.CustomRecipe`
- `net.minecraft.world.item.crafting.CraftingInput`

## 数据文件
- 配方：`data/appliedinsight/recipe/dav_cell_copy.json`（`type`: `appliedinsight:dav_cell_copy`）
- 进度：`data/appliedinsight/advancement/recipes/misc/dav_cell_copy.json`

## 维护备注
- 产出 `count: 2` 依赖相同 `CustomData` 的 DAV Cell 可堆叠；不同 cell ID 因组件不同不会合并。
