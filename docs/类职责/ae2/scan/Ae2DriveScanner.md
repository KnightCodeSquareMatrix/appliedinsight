# Ae2DriveScanner
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/scan/Ae2DriveScanner.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.scan`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
服务 AE2 网络扫描过程，把 drive/cell/item 状态转换成可消费的数据。

## 边界检查
边界健康。该类型明确属于 AE2 集成层，依赖 Minecraft/AE2 API 是合理的，并未反向污染规则层。

## 抽象检查
整体没有过度抽象，职责保持在可理解范围内。

## 主要协作者
- `appeng.api.networking.IGrid`
- `appeng.api.stacks.AEItemKey`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
