# NetworkBindingKey
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/application/NetworkBindingKey.java`
- **包**: `com.knightcode.appliedstoragesorter.application`
- **类型**: `record`
- **所属层**: 应用服务层

## 职责
作为轻量值对象/结果对象承载 NetworkBindingKey 对应的数据快照。

## 边界检查
边界健康。该类型位于应用装配或游戏集成层，依赖 NeoForge/Minecraft/AE2 是职责内的事情。

## 抽象检查
没有过度抽象；以值对象/枚举形式存在是合适的。

## 主要协作者
- `net.minecraft.core.BlockPos`

## 维护备注
- 当前更像稳定的数据/常量定义，无需进一步拆分。
