# SorterMeScanResult
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/SorterMeScanResult.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2`
- **类型**: `record`
- **所属层**: AE2 集成层

## 职责
作为轻量值对象/结果对象承载 SorterMeScanResult 对应的数据快照。

## 边界检查
边界健康。该类型明确属于 AE2 集成层，依赖 Minecraft/AE2 API 是合理的，并未反向污染规则层。

## 抽象检查
没有过度抽象；以值对象/枚举形式存在是合适的。

## 主要协作者
- `org.jetbrains.annotations.Nullable`
- `com.knightcode.appliedstoragesorter.ae2.scan.Ae2DriveScanSummary`
- `appeng.api.networking.IGrid`
- `net.minecraft.core.BlockPos`

## 维护备注
- 当前更像稳定的数据/常量定义，无需进一步拆分。
