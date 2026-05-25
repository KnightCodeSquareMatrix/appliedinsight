# AppliedStorageSorterClient
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/AppliedStorageSorterClient.java`
- **包**: `com.knightcode.appliedstoragesorter`
- **类型**: `class`
- **所属层**: 入口/装配层

## 职责
模组客户端入口。只注册客户端 screen 和配置界面扩展点。

## 边界检查
边界健康。客户端专属逻辑没有泄漏到服务端入口。

## 抽象检查
没有过度抽象，职责非常单一。

## 主要协作者
- `com.knightcode.appliedstoragesorter.client.SorterClientRegistrations`
- `net.neoforged.api.distmarker.Dist`
- `net.neoforged.bus.api.IEventBus`
- `net.neoforged.fml.ModContainer`
- `net.neoforged.fml.common.Mod`
- `net.neoforged.neoforge.client.gui.ConfigurationScreen`
- `net.neoforged.neoforge.client.gui.IConfigScreenFactory`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
