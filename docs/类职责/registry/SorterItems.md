# SorterItems
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/registry/SorterItems.java`
- **包**: `com.knightcode.appliedstoragesorter.registry`
- **类型**: `class`
- **所属层**: 注册装配层

## 职责
集中注册模组物品，避免注册细节散落各处。

## 已注册物品（摘要）
| ID | 类型 | 备注 |
|----|------|------|
| `digital_asset_vault` | `BlockItem` | DAV 方块 |
| `dav_cell` | `DavCellItem` | 伪 AE2 盘片；可由 `dav_cell` / `dav_cell_copy` 配方获取 |
| `smart_bus` | `PartItem` | Smart Bus Part |
| `sorter_command_block` | `BlockItem` | 命令执行块 |

## 边界检查
边界健康。该类型位于应用装配或游戏集成层，依赖 NeoForge/Minecraft/AE2 是职责内的事情。

## 抽象检查
整体没有过度抽象，职责保持在可理解范围内。

## 主要协作者
- `com.knightcode.appliedstoragesorter.appliedinsight`
- `com.knightcode.appliedstoragesorter.item.DigitalAssetManagementCardItem`
- `net.minecraft.core.registries.Registries`
- `net.minecraft.world.item.BlockItem`
- `net.minecraft.world.item.Item`
- `net.neoforged.bus.api.IEventBus`
- `net.neoforged.neoforge.registries.DeferredHolder`
- `net.neoforged.neoforge.registries.DeferredRegister`

## 维护备注
- 注册器类保持扁平和集中是优点，后续继续按注册对象类别拆分即可。
