# ZoneMergePlanner
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/ZoneMergePlanner.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
将 zone 分配计划转化为可执行的 merge 操作。扫描 source drives，匹配 plan assignment，通过 `RuntimeZone.planPlacement()` 确定 target cell，产出 `SorterMoveOperation`。

与 `MergeMovePlanner` 互补：
- `MergeMovePlanner`: 全网络同类归并（`/sorter merge`）
- `ZoneMergePlanner`: 按 zone 治理规则的跨 zone 搬运（`/sorter me planAndMove`）

两者都产出 `SorterMoveOperation`，共享同一套执行引擎。

## 内部数据结构

### `ItemFingerprint`（私有 record）
物品指纹，用于快速匹配 zone assignment。使用 `AEItemKey.hashCode()`（已缓存，O(1)）替代完整 NBT 序列化，配合耐久值和附魔 bitset 区分同一物品的不同状态。

| 字段 | 类型 | 说明 |
|------|------|------|
| `itemId` | `String` | 物品 ID，如 `minecraft:diamond_sword` |
| `componentHash` | `int` | `AEItemKey.hashCode()` — 已缓存，O(1) 获取，覆盖 item + 所有 component |
| `damageValue` | `int` | 耐久值，非装备/工具为 0 |
| `enchantData` | `long[]` | 附魔等级 bitset，每 8 bit 存一种附魔的等级（0-255），每个 long 存 8 种 |

### `PlanResult`（公开 record）
`plan()` 的返回结果。包含可执行的 `SorterMoveOperation` 和诊断统计数据（scan counts, skip counts, sample messages）。

## 关键方法

### `plan()`
签名：`plan(ZoneAllocationPlan, RuntimeTopology, IGrid, int, HolderLookup.Provider) → PlanResult`

- 新增 `HolderLookup.Provider registryAccess` 参数，用于在 `indexAssignments()` 中反序列化 assignment 中的 NBT
- 调用链：`SorterPlanService` → `Ae2ZoneMoveExecutor` → `ZoneMergePlanner`

### `fingerprintOf()`
从 `AEItemKey` 构造 `ItemFingerprint`，使用 `AEItemKey.hashCode()`（O(1)）替代 `stack.saveOptional(registryAccess).toString()`（完整 NBT 序列化）。

### `extractEnchantData()`
从 `ItemStack` 中提取附魔数据，编码为 `long[]` bitset。无附魔时返回空数组。

### `indexAssignments()`
从 `ZoneAllocationPlan` 的 `movableAssignments()` 构建 `ItemFingerprint → ItemZoneAssignment` 索引。
- 使用 `TagParser.parseTag()` + `ItemStack.parse()` 反序列化 `serializedStackNbt`（一次性的 NBT 解析，不是每次扫描都做）
- 跳过 NBT 解析失败或空物品的 assignment

## 边界检查
边界健康。不参与 route 规则决策，不定义 zone 策略。

## 抽象检查
单一职责。替代了 `Ae2ZoneMoveExecutor` 中原有的内联扫描/匹配/执行逻辑。

## 产出类型
- `ZoneMergePlanner.PlanResult` — 内部 record，包含 `SorterMoveOperation` 和诊断统计数据

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference`
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor`
- `com.knightcode.appliedstoragesorter.ae2.sort.PlannedMove`
- `com.knightcode.appliedstoragesorter.ae2.sort.SorterMoveOperation`
- `com.knightcode.appliedstoragesorter.plan.ItemZoneAssignment`
- `com.knightcode.appliedstoragesorter.plan.ZoneAllocationPlan`
- `appeng.api.networking.IGrid`
- `appeng.api.stacks.AEItemKey`
- `appeng.api.storage.MEStorage`
- `net.minecraft.core.HolderLookup.Provider`

## 维护备注
- 与 `MergeMovePlanner` 共享 `SorterMoveOperation` 执行引擎，不要在此引入独立的 extract/insert 逻辑。
- 诊断数据通过 `PlanResult` 返回给 `Ae2ZoneMoveExecutor` 组装最终结果。
- `ItemFingerprint` 使用 `AEItemKey.hashCode()` + 耐久 + 附魔 bitset 替代完整 NBT 序列化，大幅提升扫描性能。
- `indexAssignments()` 中的 NBT 反序列化是一次性的（仅在 `plan()` 调用时执行），不会影响每次扫描的性能。
