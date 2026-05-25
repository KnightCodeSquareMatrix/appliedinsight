# AppliedStorageSorter
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/AppliedStorageSorter.java`
- **包**: `com.knightcode.appliedstoragesorter`
- **类型**: `class`
- **所属层**: 入口/装配层

## 职责
模组服务端入口。负责注册配置、方块/物品/菜单/配方序列化器，以及把命令和 capability 挂到正确事件总线。

## 边界检查
边界健康。它只做启动期装配，不承载业务流程，也没有把命令、规则、AE2 运行时逻辑塞进入口类。

## 抽象检查
没有过度抽象；作为 composition root 很合适。

## 主要协作者
- `org.slf4j.Logger`
- `com.knightcode.appliedstoragesorter.command.SorterCommands`
- `com.knightcode.appliedstoragesorter.registry.SorterBlockEntities`
- `com.knightcode.appliedstoragesorter.registry.SorterBlocks`
- `com.knightcode.appliedstoragesorter.registry.SorterCapabilities`
- `com.knightcode.appliedstoragesorter.registry.SorterCreativeTabs`
- `com.knightcode.appliedstoragesorter.registry.SorterItems`
- `com.knightcode.appliedstoragesorter.registry.SorterMenus`
- `com.knightcode.appliedstoragesorter.registry.SorterRecipeSerializers`
- `com.mojang.logging.LogUtils`
- `net.neoforged.bus.api.IEventBus`
- `net.neoforged.fml.ModContainer`
- 其余依赖省略 4 项，以源码为准。

## 维护备注
- 如果以后注册内容继续增多，可以按“注册器分组”继续保持现在这种拆分方式。
