# LiveZoneAllocationPlanner
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/zone/LiveZoneAllocationPlanner.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
对实时 AE2 网络做 item 聚合，并把 RoutingEngine 的结果投影成 ZoneAllocationPlan。

## 边界检查
边界健康。它位于 AE2 运行时层，负责“实时数据源 -> 纯 plan 模型”的桥接。

## 抽象检查
没有过度抽象，但与离线 ZoneAllocationPlanner 存在一定重复。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor`
- `com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment`
- `com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan`
- `com.knightcode.appliedstoragesorter.rule.filter.ItemMatchContext`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingDecision`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingEngine`
- `com.knightcode.appliedstoragesorter.rule.route.RoutingProfile`
- `appeng.api.stacks.AEItemKey`
- `net.minecraft.core.HolderLookup`
- `net.minecraft.core.registries.BuiltInRegistries`
- `net.minecraft.tags.TagKey`
- `net.minecraft.world.item.ItemStack`

## 维护备注
- 如果离线/在线规划继续演进，可抽出共享的 item 聚合到 assignment 转换逻辑。

## 近期变更 (2026-05-24)
- **签名变更**: `plan(RoutingProfile, IGrid)` → `plan(RoutingProfile, RuntimeTopology, HolderLookup.Provider)`
- 不再通过 `DriveMachineAccessor` 独立扫描 grid，改为迭代 `topology.allCells()` 获取 item 信息
- 消除了 plan 阶段和 topology 构建阶段之间的重复扫描
