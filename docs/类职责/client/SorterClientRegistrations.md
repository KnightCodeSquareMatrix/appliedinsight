# SorterClientRegistrations
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/client/SorterClientRegistrations.java`
- **包**: `com.knightcode.appliedstoragesorter.client`
- **类型**: `class`
- **所属层**: 客户端展示层

## 职责
承载客户端注册或展示逻辑。

## 边界检查
边界健康。该类型位于应用装配或游戏集成层，依赖 NeoForge/Minecraft/AE2 是职责内的事情。

## 抽象检查
整体没有过度抽象，职责保持在可理解范围内。

## 主要协作者
- `com.knightcode.appliedstoragesorter.client.screen.DigitalAssetVaultScreen`
- `com.knightcode.appliedstoragesorter.registry.SorterMenus`
- `net.neoforged.neoforge.client.event.RegisterMenuScreensEvent`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
