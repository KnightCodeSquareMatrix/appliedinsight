# DigitalAssetVaultBlockEntity
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/blockentity/DigitalAssetVaultBlockEntity.java`
- **包**: `com.knightcode.appliedstoragesorter.blockentity`
- **类型**: `class`
- **所属层**: 方块实体层

## 职责
DAV 核心方块实体：继承 AE2 DriveBlockEntity，并额外维护管理卡槽（ZoneCard 插卡槽）、zone 声明读取和菜单打开。

## 边界检查
边界健康。AE2 存储行为复用在这里，规则层只通过 declaredZoneId 间接感知它。

## 抽象检查
没有过度抽象；通过继承 DriveBlockEntity 是符合真实复用关系的。

## 关键设计
- **管理卡槽（ZoneCard 插卡槽）**: 使用 `AppEngInternalInventory` 维护一个独立的 1 格卡槽，与 Drive 的 10 个 cell 槽分离
- **卡槽过滤**: 内嵌 `DigitalAssetManagementCardFilter`，只允许放入 `DigitalAssetManagementCardItem`
- **UI 一致性**: Menu 和 Screen 的布局与 AE2 Drive 对齐，管理卡槽位于 GUI 右上角，类似 AE2 设备的升级卡槽样式

## 主要协作者
- `appeng.api.inventories.InternalInventory`
- `appeng.blockentity.storage.DriveBlockEntity`
- `appeng.util.inv.AppEngInternalInventory`
- `appeng.util.inv.filter.IAEItemFilter`
- `com.knightcode.appliedstoragesorter.item.DigitalAssetManagementCardItem`
- `com.knightcode.appliedstoragesorter.menu.DigitalAssetVaultMenu`
- `com.knightcode.appliedstoragesorter.registry.SorterBlockEntities`
- `com.knightcode.appliedstoragesorter.registry.SorterItems`
- `net.minecraft.core.BlockPos`
- `net.minecraft.core.HolderLookup`
- `net.minecraft.nbt.CompoundTag`
- `net.minecraft.world.SimpleMenuProvider`
- 其余依赖省略 4 项，以源码为准。

## 维护备注
- 内嵌的 DigitalAssetManagementCardFilter 简单直接，没有必要提前拆成独立类。
- 管理卡槽（ZoneCard 插卡槽）是 DAV 区别于普通 Drive 的核心特征，新增卡槽类型时需同步更新 Menu 和 Screen 布局。
