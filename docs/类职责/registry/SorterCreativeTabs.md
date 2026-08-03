# SorterCreativeTabs
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/registry/SorterCreativeTabs.java`
- **包**: `com.knightcode.appliedstoragesorter.registry`
- **类型**: `class`
- **所属层**: 注册装配层

## 职责
向原版创造模式标签页注入模组物品。

## 当前注入
| 标签页 | 物品 |
|--------|------|
| `FUNCTIONAL_BLOCKS` | `digital_asset_vault`、`smart_bus`、`sorter_command_block` |
| `INGREDIENTS` | `dav_cell` |

## 边界检查
边界健康。该类型位于应用装配层，依赖 NeoForge/Minecraft 是职责内的事情。

## 主要协作者
- `com.knightcode.appliedstoragesorter.registry.SorterItems`
- `net.minecraft.world.item.CreativeModeTabs`
- `net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent`

## 维护备注
- 注册器类保持扁平和集中是优点，后续继续按注册对象类别拆分即可。
