# DigitalAssetVaultMenu
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/menu/DigitalAssetVaultMenu.java`
- **包**: `com.knightcode.appliedstoragesorter.menu`
- **类型**: `class`
- **所属层**: 容器交互层

## 职责
DAV 容器菜单，组织 10 个 cell 槽（2 列 x 5 行垂直布局）、1 个管理卡槽（ZoneCard 插卡槽）和玩家背包槽位。

## 边界检查
边界健康。GUI 布局与 block entity 逻辑保持分离。

## 抽象检查
没有过度抽象。

## 关键设计
- **Cell 槽布局**: 2 列 x 5 行垂直布局，从 (71, 8) 开始，适配 DAV 的垂直紧凑设计
- **管理卡槽（ZoneCard 插卡槽）**: 位于独立的卡槽面板内（纹理 y=201..222），坐标 (152, 203)，与 Drive 主体分离
- **玩家背包**: 从 (8, 84) 开始，与 Drive 的 `common/player_inventory.json` 布局一致
- **SlotSemantic**: 注册了 `APPLIEDSTORAGESORTER_MANAGEMENT_CARD` 语义（`playerSide=true`），与管理卡槽绑定；`playerSide=true` 使得管理卡槽被视为玩家侧槽位，允许直接点击插入和 shift-click 快速放入

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
- 布局坐标已从 AE2 Drive 的 5x2 水平网格改为 2x5 垂直布局，修改前需确认纹理的 srcRect 和槽位间距
- 管理卡槽位置 (152, 203) 在 Drive 纹理的卡槽面板内（纹理 y=201..222）
- 保持单一职责，不要为了未来猜想提前拆分
- 2026-05-25: `MANAGEMENT_CARD` 的 SlotSemantic `playerSide` 从 `false` 改为 `true`，修复卡片无法插入卡槽的问题
- 2026-05-25: CELL_COLS 从 5 改为 2，CELL_ROWS 从 2 改为 5，CELL_AREA_Y 从 14 改为 8；布局由水平 5 列网格转为垂直 2 列布局
