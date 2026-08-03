# DavCellItem
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/item/DavCellItem.java`
- **包**: `com.knightcode.appliedstoragesorter.item`
- **类型**: `class`
- **所属层**: 物品/玩家输入层

## 职责
玩家可见的「伪存储盘」物品：外形像 AE2 Cell，**不是** `StorageCells` 处理的真 Cell。物品上仅保存 `dav_cell_id`（UUID，空白 Cell 无此字段）；容量与库存均在 `DavCellSavedData`。`appendHoverText` 说明 identity-only 语义；空白 Cell 显示「未绑定存储后端」；F3 高级 tooltip 显示已绑定 Cell ID。

## 玩家获取
| 配方 ID | 输入 | 产出 |
|---------|------|------|
| `appliedinsight:dav_cell` | `ae2:item_storage_cell_1k` + `ae2:redstone_card` | 1 张空白 DAV Cell |
| `appliedinsight:dav_cell_copy` | 空白 DAV Cell + 已绑定 ID 的 DAV Cell | 2 张相同 ID 的 DAV Cell |

复制配方仅复制访问点钥匙（cell ID），不复制 SavedData 库存。相同 ID 的 Cell 可堆叠；不同 ID 因 `CustomData` 不同不可堆叠。

## 边界检查
边界健康。不可插入 ME Drive；放入 DAV 的 Cell 槽后，ID 决定连接的 `DavCellBackend`。同一 ID 的 Cell 可在多个 DAV 间移动或共享访问。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.dav.cell.DavCellStack`
- `com.knightcode.appliedstoragesorter.registry.SorterItems`
- `com.knightcode.appliedstoragesorter.recipe.DavCellCopyRecipe`

## 维护备注
- DAV GUI 内悬停 tooltip 由 `DigitalAssetVaultScreen.renderTooltip` 覆盖，避免与槽位说明双层叠加。
