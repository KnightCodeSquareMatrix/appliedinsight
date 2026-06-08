# RuntimeTopology
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/zone/RuntimeTopology.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
一次 use-case 执行中当前 ME 网络的运行时世界模型。由 `RuntimeZoneRegistryBuilder` 唯一构建。

持有：
- `Map<String, RuntimeZone> zonesById` — 按 zone id 索引的运行时 zone
- `List<RuntimeCell> unassignedCells` — 无 zone 声明的 cell（source 视图基础）
- 网络级诊断与降级状态

## 边界检查
边界健康。它是运行时事实中心，不承担规则定义、JSON 读写、执行编排职责。

## 抽象检查
符合"胖 topology"原则——允许适度偏胖以换取其他对象变薄。

## 主要协作者
- `RuntimeZone`
- `RuntimeCell`
- `RuntimeZoneRegistryBuilder`

## 变更记录 (2026-05-24)
- **新增 `unassignedCells` 字段**: 无 zone 声明的 drive cell 不再被丢弃，统一归入此列表
- **新增 `allCells()` 方法**: 返回所有 zone 内 cell + unassignedCells 的合并视图，供 `LiveZoneAllocationPlanner` 迭代使用
- **构造函数签名变更**: `(zones, diagnostics, degraded)` → `(zones, unassignedCells, diagnostics, degraded)`
- `isEmpty()` 现在同时检查 zones 和 unassignedCells
