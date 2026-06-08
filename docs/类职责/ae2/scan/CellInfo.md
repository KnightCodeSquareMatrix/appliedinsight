# CellInfo
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/scan/CellInfo.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.scan`
- **类型**: `record`
- **所属层**: AE2 集成层 / Zone 运行时层

## 职责
拓扑中单个 Cell 的**纯数据快照**（ADR-012 替代已删除的 `RuntimeCell`）：
- 身份：`DriveCellReference`、zoneId、sourceBlockId
- 容量元数据：`CellCapacity`
- 实时查询句柄：`MEStorage` + `IActionHost`（需要 per-item 数据时调用 AE2 live API，不缓存物品列表）

## 边界检查
边界健康。零行为方法（除 `hasCapacity()` 便捷判断）；不包含 extract/insert 逻辑。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference`
- `com.knightcode.appliedstoragesorter.ae2.zone.RuntimeZoneRegistryBuilder`（构建时填充）
- `com.knightcode.appliedstoragesorter.ae2.zone.RuntimeTopology`

## 维护备注
- 勿在 record 上添加业务方法；执行与规划逻辑放在 executor/planner 中。
