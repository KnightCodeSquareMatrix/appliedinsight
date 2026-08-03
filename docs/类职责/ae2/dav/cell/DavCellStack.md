# DavCellStack
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/dav/cell/DavCellStack.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.dav.cell`
- **类型**: `class`（静态工具）
- **所属层**: AE2 集成层

## 职责
`DavCellItem` 物品栈工具：创建空白/新/指定 ID 的 cell、读写 `dav_cell_id`（`CustomData`）、识别 DAV Cell、判断空白状态、剥离 legacy 物品账本字段。物品上**永不**存库存与容量。

## 主要 API
| 方法 | 说明 |
|------|------|
| `createEmpty()` | 无 `dav_cell_id` 的空白 DAV Cell（合成产物、复制配方空白侧） |
| `createNew()` | 空白 Cell 并 `assignNewCellId`（DAV 首次自动生成） |
| `createWithCellId(UUID)` | 绑定指定 ID 的 Cell（复制配方产出） |
| `isBlank(stack)` | 是否为 DAV Cell 且尚未绑定 ID |
| `getCellId` / `assignNewCellId` / `stripToIdentityOnly` | ID 读写与 legacy 剥离 |

## 主要协作者
- `com.knightcode.appliedstoragesorter.item.DavCellItem`
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellConstants`
- `com.knightcode.appliedstoragesorter.recipe.DavCellCopyRecipe`
