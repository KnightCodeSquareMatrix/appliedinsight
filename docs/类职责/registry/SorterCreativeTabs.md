# SorterCreativeTabs
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/registry/SorterCreativeTabs.java`
- **包**: `com.knightcode.appliedstoragesorter.registry`
- **类型**: `class`
- **所属层**: 注册装配层

## 职责
负责集中注册模组对象，避免注册细节散落各处。

## 边界检查
边界健康。该类型位于应用装配或游戏集成层，依赖 NeoForge/Minecraft/AE2 是职责内的事情。

## 抽象检查
整体没有过度抽象，职责保持在可理解范围内。

## 主要协作者
- `net.minecraft.world.item.CreativeModeTabs`
- `net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent`

## 维护备注
- 注册器类保持扁平和集中是优点，后续继续按注册对象类别拆分即可。
