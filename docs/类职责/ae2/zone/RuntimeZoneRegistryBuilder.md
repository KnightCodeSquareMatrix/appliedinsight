# RuntimeZoneRegistryBuilder
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/zone/RuntimeZoneRegistryBuilder.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
`RuntimeTopology` 的唯一构建入口。根据 `IGrid` 扫描结果 + DAV 声明的 zone id，构造运行时拓扑。

## 边界检查
边界健康。只构建 topology，不消费 topology。

## 抽象检查
单一职责。已取代旧的 `RuntimeZoneRegistry`。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference`
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor`
- `appeng.api.networking.IGrid`
- `appeng.api.storage.MEStorage`
- `net.minecraft.core.BlockPos`

## 变更记录 (2026-05-24)
- **不再跳过无 zone 声明的 drive**: 之前 `zoneId == null || isBlank()` 时 `continue` 跳过整台 drive
- **改为收集到 `unassignedCells`**: 无 zone 声明的 cell 归入独立列表，传入 `RuntimeTopology`
- 每个无归属 cell 使用固定的 zone id `__unassigned__`
- `RuntimeTopology` 构造函数从 3 参数扩展为 4 参数
