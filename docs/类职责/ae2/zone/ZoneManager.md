# ZoneManager
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/ZoneManager.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `abstract class`
- **所属层**: AE2 集成层

## 职责
RuntimeZone 的抽象父类，统一 zoneId/zoneName/cell 管理和 placement 请求入口。

## 边界检查
边界健康。它抽象的是当前已经真实存在的共性，而不是想象中的未来框架。

## 抽象检查
没有过度抽象。当前只有 RuntimeZone 一个实现，但抽象粒度仍然合理，因为 add/remove/replace/planPlacement 的一致性很明确。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference`
- `appeng.api.stacks.AEItemKey`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
