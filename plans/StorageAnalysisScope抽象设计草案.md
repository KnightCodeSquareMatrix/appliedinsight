# StorageAnalysisScope 抽象设计草案

> 状态：草案  
> 日期：2026-06-01  
> 主题：为 ME Network / Zone / DAV 三类分析入口设计统一的只读分析视图，避免引入过早的 `AbstractRuntimeStorageObject` 继承体系。

---

## 1. 背景

当前存储分析能力已经围绕 `/sorter me storageDump` 形成了宏观观测路径：

```text
SorterStorageAnalysisService
    -> Ae2StorageAnalyzer
    -> StorageAnalyzerReport
    -> Dashboard / GUI / dump
```

现有 `Ae2StorageAnalyzer.analyze(IGrid grid)` 同时承担了两类职责：

1. 从 AE2 runtime 中扫描 storage locations；
2. 对这些 storage locations 做统计、碎片化分析、健康标记和语义候选判断。

随着后续分析入口扩展，分析目标可能不再只是整个 ME 网络，还可能包括：

- 整个 ME Network；
- 某个 Zone；
- 某个 DAV 管理范围。

因此需要一个抽象层，让不同来源的分析目标最终统一成一种只读输入，供 analyzer 消费。

---

## 2. 不采用 `AbstractRuntimeStorageObject` 的原因

不建议将 ME Network、Zone、DAV、Cell 抽象成共同父类：

```text
AbstractRuntimeStorageObject
├── Network
├── Zone
├── DAV
└── Cell
```

原因是这些对象语义并不等价：

| 对象 | 本质 | 是否是真正存储对象 |
|---|---|---|
| ME Network | 运行时网络范围 / topology scope | 否 |
| Zone | 逻辑治理目标，映射到若干 cell | 否 |
| DAV | 玩家输入入口 / zone 容器 / 管理界面 | 否 |
| Cell | AE2 硬盘单元，真实存储载体 | 是 |

如果抽象成 `AbstractRuntimeStorageObject`，很容易出现以下问题：

- Network、Zone、DAV 被迫实现 `usedBytes()`、`insert()`、`extract()` 等不自然方法；
- Zone 的规则语义、RuntimeTopology 的映射语义、Cell 的物理存储语义被混在一起；
- DAV 被误提升为架构中心；
- Analyzer、Executor、Planner 的职责边界变得模糊；
- 违反“不要强行抽象一整套 node / endpoint / provider 体系”的当前架构约束。

因此本草案采用 **Scope + View** 结构，而不是 abstract class 继承树。

---

## 3. 设计目标

### 3.1 目标

- 允许 analyzer 支持 ME Network / Zone / DAV 等不同分析入口；
- 将“扫描运行时世界”和“执行统计分析”解耦；
- 让 analyzer 消费统一的只读视图；
- 避免将分析视图污染成执行模型；
- 为后续 Dashboard、离线 JSON 分析、Zone 局部分析留下扩展点。

### 3.2 非目标

本抽象第一阶段不负责：

- 执行 insert / extract / move；
- 判断 item 应去哪个 zone；
- 生成 ZoneAllocationPlan；
- 决定具体 target cell；
- 修改 DAV / Zone / world override；
- 取代 RuntimeTopology；
- 统一三条能力路径。

---

## 4. 核心思想

一句话：

> ME Network / Zone / DAV 不再直接进入 Analyzer，而是先被解析成统一的 `StorageAnalysisScope`；Analyzer 只看这个 scope 里的 storage locations 和 item amounts。

结构不是：

```text
AbstractRuntimeStorageObject
├── Network
├── Zone
└── DAV
```

而是：

```text
ME Network / Zone / DAV
        │
        ▼
Scope Resolver
        │
        ▼
StorageAnalysisScope
        │
        ├── StorageLocationView
        │       ├── CellCapacityView
        │       └── KeyAmountView
        │
        ▼
StorageAnalyzer
        │
        ▼
StorageAnalyzerReport
```

---

## 5. 推荐包结构

第一阶段建议放在现有 AE2 analysis 体系内，避免过早迁移到全局 `analysis/` 包：

```text
src/main/java/com/knightcode/appliedinsight/ae2/analysis/
├── Ae2StorageAnalyzer.java
├── Ae2StorageAnalysisScopeResolver.java
├── StorageAnalyzerReport.java
└── view/
    ├── StorageAnalysisScope.java
    ├── StorageAnalysisScopeKind.java
    ├── StorageLocationView.java
    ├── StorageLocationKind.java
    ├── CellCapacityView.java
    ├── KeyAmountView.java
    ├── ResolvedStorageAnalysisScope.java
    └── ResolvedStorageLocationView.java
```

如果后续需要支持离线 JSON dump 或非 AE2 来源，可以再考虑将 `view/` 移动到更纯的 `analysis/model/`。

---

## 6. 核心接口设计

### 6.1 StorageAnalysisScope

表示一次分析的范围。

```java
public interface StorageAnalysisScope {
    StorageAnalysisScopeKind kind();

    String scopeId();

    String displayName();

    List<? extends StorageLocationView> locations();

    default boolean isEmpty() {
        return locations().isEmpty();
    }
}
```

对应 enum：

```java
public enum StorageAnalysisScopeKind {
    ME_NETWORK,
    ZONE,
    DAV
}
```

#### 字段语义

| 方法 | 说明 |
|---|---|
| `kind()` | 分析范围类型：ME 网络、Zone、DAV |
| `scopeId()` | 稳定 ID，用于 report、dashboard、缓存或日志 |
| `displayName()` | 面向用户展示的名称 |
| `locations()` | 此 scope 下所有可观测 storage locations |

示例：

```text
kind = ME_NETWORK
scopeId = network:<controller-pos-or-grid-id>
displayName = ME Network at 10,64,20
```

```text
kind = ZONE
scopeId = zone:ores
displayName = Zone ores
```

```text
kind = DAV
scopeId = dav:<block-pos>
displayName = DAV at 12,64,20
```

---

### 6.2 StorageLocationView

表示 scope 里的一个可观测存储位置。

它不一定是 cell；它可以代表：

- drive slot 中的内部 cell；
- external storage bus 背后的 storage；
- 未来某种可观测 storage node。

```java
public interface StorageLocationView {
    String locationId();

    String sourceBlockId();

    StorageLocationKind storageKind();

    String hostPos();

    Optional<String> attachedStoragePos();

    Optional<String> attachedStorageBlockId();

    boolean externalStorageBus();

    int slot();

    Optional<String> zoneId();

    Optional<CellCapacityView> capacity();

    List<KeyAmountView> availableStacks();
}
```

对应 enum：

```java
public enum StorageLocationKind {
    INTERNAL_CELL,
    EXTERNAL_STORAGE,
    UNKNOWN
}
```

#### 字段语义

| 方法 | 说明 |
|---|---|
| `locationId()` | 当前 storage location 的稳定 ID |
| `sourceBlockId()` | 提供该 storage location 的方块 ID |
| `storageKind()` | 内部 cell / external storage / unknown |
| `hostPos()` | host 方块位置，字符串化 BlockPos |
| `attachedStoragePos()` | external storage bus 背后连接的存储位置，可为空 |
| `attachedStorageBlockId()` | external storage bus 背后连接的方块 ID，可为空 |
| `externalStorageBus()` | 是否来自 external storage bus |
| `slot()` | slot index；无 slot 语义时可约定为 `-1` |
| `zoneId()` | 已声明或已解析的 zone id，可为空 |
| `capacity()` | 容量视图；external storage 或未知来源可为空 |
| `availableStacks()` | 当前 location 中的 key + amount 列表 |

---

### 6.3 CellCapacityView

容量信息单独抽出，避免 `StorageLocationView` 过胖。

```java
public record CellCapacityView(
        Long totalBytes,
        Long usedBytes,
        Integer totalItemTypes,
        Integer remainingItemTypes,
        String cellKind) {
}
```

说明：

- 使用包装类型是为了表达“不知道 / 不适用”；
- 对 internal AE2 cell，一般可以填充这些字段；
- 对 external storage，不一定有 byte / type capacity 信息。

---

### 6.4 KeyAmountView

表示某个 storage location 中一个 key 的数量事实。

推荐使用纯 DTO，而不是直接暴露 AE2 `AEKey`：

```java
public record KeyAmountView(
        String keyType,
        String keyId,
        String displayName,
        long amount,
        int maxStackSize) {
}
```

理由：

- 当前 `StorageAnalyzerReport.KeyAmountSummary` 已经使用相同语义字段；
- analyzer 可以更接近 dashboard / offline 分析层；
- 降低后续从 JSON dump 构造分析输入的成本；
- 避免把 AE2 runtime key 传播到 report 统计逻辑之外。

---

## 7. 默认 record 实现

### 7.1 ResolvedStorageAnalysisScope

```java
public record ResolvedStorageAnalysisScope(
        StorageAnalysisScopeKind kind,
        String scopeId,
        String displayName,
        List<? extends StorageLocationView> locations) implements StorageAnalysisScope {

    public ResolvedStorageAnalysisScope {
        locations = List.copyOf(locations);
    }
}
```

### 7.2 ResolvedStorageLocationView

```java
public record ResolvedStorageLocationView(
        String locationId,
        String sourceBlockId,
        StorageLocationKind storageKind,
        String hostPos,
        Optional<String> attachedStoragePos,
        Optional<String> attachedStorageBlockId,
        boolean externalStorageBus,
        int slot,
        Optional<String> zoneId,
        Optional<CellCapacityView> capacity,
        List<KeyAmountView> availableStacks) implements StorageLocationView {

    public ResolvedStorageLocationView {
        attachedStoragePos = attachedStoragePos == null ? Optional.empty() : attachedStoragePos;
        attachedStorageBlockId = attachedStorageBlockId == null ? Optional.empty() : attachedStorageBlockId;
        zoneId = zoneId == null ? Optional.empty() : zoneId;
        capacity = capacity == null ? Optional.empty() : capacity;
        availableStacks = List.copyOf(availableStacks);
    }
}
```

---

## 8. Resolver 设计

新增 resolver：

```java
public final class Ae2StorageAnalysisScopeResolver {
    private Ae2StorageAnalysisScopeResolver() {
    }

    public static StorageAnalysisScope fromNetwork(IGrid grid) {
        ...
    }

    public static StorageAnalysisScope fromZone(IGrid grid, String zoneId) {
        ...
    }

    public static StorageAnalysisScope fromDav(IGrid grid, BlockPos davPos) {
        ...
    }
}
```

### 8.1 Resolver 职责

Resolver 负责从具体来源提取只读分析事实：

- 扫描 AE2 network；
- 找到支持的 drive / storage bus；
- 读取 cell inventory；
- 读取原始 cell capacity；
- 解析 cell kind；
- 读取 declared zone id；
- 构造 `StorageAnalysisScope`。

### 8.2 Resolver 可以依赖的对象

Resolver 可以依赖 AE2 runtime 与项目集成类：

- `IGrid`；
- `DriveMachineAccessor`；
- `CellCapacityInspector`；
- `RuntimeTopology`；
- DAV block entity；
- zone card / profile binding 信息。

### 8.3 Analyzer 不再直接依赖这些扫描细节

当前 `Ae2StorageAnalyzer.analyze(IGrid grid)` 中类似逻辑：

```java
DriveMachineAccessor.findSupportedDrives(grid)
location.getCellInventory(slot)
location.getOriginalCellInventory(slot)
CellCapacityInspector.inspect(originalCell)
CellCapacityInspector.resolveCellKind(...)
location.getDeclaredZoneId()
storage.getAvailableStacks()
```

后续可逐步迁移到 resolver 中。

---

## 9. Analyzer 入口调整

保留当前入口以兼容已有调用：

```java
public static StorageAnalyzerReport analyze(IGrid grid) {
    return analyze(Ae2StorageAnalysisScopeResolver.fromNetwork(grid));
}
```

新增核心入口：

```java
public static StorageAnalyzerReport analyze(StorageAnalysisScope scope) {
    ...
}
```

调整后职责为：

```text
Ae2StorageAnalysisScopeResolver.fromNetwork(grid)
    ├── 扫描 AE2 网络
    ├── 读取 cell/storage 内容
    ├── 读取容量
    └── 生成 StorageAnalysisScope

Ae2StorageAnalyzer.analyze(scope)
    ├── 聚合 item 分布
    ├── 计算 fragmentation
    ├── 判断 health flags
    ├── 识别 semantic candidates
    └── 生成 StorageAnalyzerReport
```

---

## 10. 三类入口的数据流

### 10.1 分析整个 ME Network

```text
/sorter me storageDump
        │
        ▼
resolve controller / grid
        │
        ▼
Ae2StorageAnalysisScopeResolver.fromNetwork(grid)
        │
        ▼
StorageAnalysisScope(kind = ME_NETWORK)
        │
        ▼
Ae2StorageAnalyzer.analyze(scope)
        │
        ▼
StorageAnalyzerReport
```

### 10.2 分析某个 Zone

```text
/sorter me analyzeZone ores
        │
        ▼
resolve controller / grid
        │
        ▼
load bound profile / runtime topology / zone mapping
        │
        ▼
Ae2StorageAnalysisScopeResolver.fromZone(grid, "ores")
        │
        ▼
StorageAnalysisScope(kind = ZONE, scopeId = "zone:ores")
        │
        ▼
Ae2StorageAnalyzer.analyze(scope)
        │
        ▼
StorageAnalyzerReport
```

### 10.3 分析某个 DAV

```text
玩家打开 DAV GUI
        │
        ▼
client 发送 analyzeDav 请求
        │
        ▼
server resolve DAV block entity
        │
        ▼
读取 DAV 管理卡 / zone 配置 / 绑定关系
        │
        ▼
Ae2StorageAnalysisScopeResolver.fromDav(grid, davPos)
        │
        ▼
StorageAnalysisScope(kind = DAV)
        │
        ▼
Ae2StorageAnalyzer.analyze(scope)
        │
        ▼
StorageAnalyzerReport
```

---

## 11. 与 StorageAnalyzerReport 的映射

`StorageAnalysisScope` 是输入，`StorageAnalyzerReport` 是输出。

映射关系：

```text
StorageAnalysisScope.locations()
        │
        ├── 每个 StorageLocationView
        │       ▼
        │   StorageAnalyzerReport.StorageLocationSummary
        │
        └── 每个 KeyAmountView 按 key 聚合
                ▼
            StorageAnalyzerReport.ItemDistributionSummary
```

`StorageLocationView` 到 `StorageLocationSummary` 的字段来源：

| 输出字段 | 来源 |
|---|---|
| `locationId` | `location.locationId()` |
| `sourceBlockId` | `location.sourceBlockId()` |
| `attachedStorageBlockId` | `location.attachedStorageBlockId()` |
| `storageKind` | `location.storageKind()` |
| `hostPos` | `location.hostPos()` |
| `attachedStoragePos` | `location.attachedStoragePos()` |
| `externalStorageBus` | `location.externalStorageBus()` |
| `slot` | `location.slot()` |
| `distinctKeyCount` | `location.availableStacks().size()` filtered by amount > 0 |
| `totalAmount` | sum of `KeyAmountView.amount()` |
| `networkAmountShare` | analyzer 根据 scope total 计算 |
| `topKeys` | analyzer 从 `availableStacks()` 排序截断 |
| `totalBytes` | `location.capacity().totalBytes()` |
| `usedBytes` | `location.capacity().usedBytes()` |
| `totalItemTypes` | `location.capacity().totalItemTypes()` |
| `remainingItemTypes` | `location.capacity().remainingItemTypes()` |
| `cellKind` | `location.capacity().cellKind()` |
| `zoneId` | `location.zoneId()` |

---

## 12. 依赖方向

目标依赖方向：

```text
AE2 Runtime
    │
    ▼
Ae2StorageAnalysisScopeResolver
    │
    ▼
StorageAnalysisScope / StorageLocationView
    │
    ▼
Ae2StorageAnalyzer
    │
    ▼
StorageAnalyzerReport
```

含义：

- Resolver 可以知道 AE2 runtime、DAV、Zone、RuntimeTopology；
- View 接口尽量保持只读与低依赖；
- Analyzer 不直接扫描 AE2 runtime；
- Report 是输出 DTO，不反向影响 runtime 模型。

---

## 13. 第一阶段实施建议

### 阶段 1：引入 view 接口与 record

新增：

```text
ae2/analysis/view/StorageAnalysisScope.java
ae2/analysis/view/StorageAnalysisScopeKind.java
ae2/analysis/view/StorageLocationView.java
ae2/analysis/view/StorageLocationKind.java
ae2/analysis/view/CellCapacityView.java
ae2/analysis/view/KeyAmountView.java
ae2/analysis/view/ResolvedStorageAnalysisScope.java
ae2/analysis/view/ResolvedStorageLocationView.java
```

不改变现有行为。

### 阶段 2：新增 network resolver

新增：

```text
ae2/analysis/Ae2StorageAnalysisScopeResolver.java
```

先只实现：

```java
fromNetwork(IGrid grid)
```

将当前 `Ae2StorageAnalyzer.analyze(IGrid grid)` 中的扫描逻辑迁移到 resolver。

### 阶段 3：Analyzer 新增 scope 入口

新增：

```java
public static StorageAnalyzerReport analyze(StorageAnalysisScope scope)
```

保留兼容入口：

```java
public static StorageAnalyzerReport analyze(IGrid grid) {
    return analyze(Ae2StorageAnalysisScopeResolver.fromNetwork(grid));
}
```

### 阶段 4：支持 zone scope

新增：

```java
fromZone(IGrid grid, String zoneId)
```

第一版可基于 `declaredZoneId` 过滤 locations。

后续如需更精确，可结合 `RuntimeTopology` 或 profile binding。

### 阶段 5：支持 DAV scope

新增：

```java
fromDav(IGrid grid, BlockPos davPos)
```

第一版只负责把 DAV 管理范围解析成一组 zone / locations，不在 analyzer 中引入 DAV 语义。

---

## 14. 明确禁止事项

为了避免分析视图变成执行模型，以下方法不应出现在 `StorageAnalysisScope` 或 `StorageLocationView` 中：

```java
insert(...)
extract(...)
move(...)
route(...)
plan(...)
execute(...)
simulateInsert(...)
canAccept(...)
storage()
actionHost()
```

这些职责分别属于：

| 问题 | 所属层 |
|---|---|
| item 应去哪 | rule / route |
| item 分配到哪个 zone | plan |
| zone 映射哪些真实 cell | runtime topology |
| 具体搬运 | executor / SorterMoveOperation |
| 宏观健康诊断 | analysis |
| DAV 管理配置 | player input / block entity |

---

## 15. 开放问题

1. `scopeId()` 对 ME network 应使用 controller pos、grid id，还是 profile binding id？
2. `fromZone()` 第一版是否只使用 declared zone id，还是必须通过 RuntimeTopology？
3. DAV scope 应表达“DAV 自身管理的 zone 集合”，还是“当前 DAV 选中的 active zone”？
4. `StorageAnalyzerReport.NetworkSummary` 是否需要在未来改名为更中性的 `ScopeSummary`？
5. `StorageLocationKind.EXTERNAL_STORAGE` 是否应进一步细分为 storage bus、unknown external、infinite-like candidate？
6. `KeyAmountView` 是否需要额外携带 NBT / component hash，用于区分同 ID 不同组件物品？

---

## 16. 推荐结论

当前阶段建议采用：

```text
StorageAnalysisScope
    └── StorageLocationView
            ├── CellCapacityView
            └── KeyAmountView
```

而不是：

```text
AbstractRuntimeStorageObject
    ├── Network
    ├── Zone
    ├── DAV
    └── Cell
```

最终原则：

> 分析对象统一为 scope，执行对象统一为 cell，配置对象保持 zone / DAV 各自语义，不抽象成共同父类。

这样可以在不破坏现有架构边界的前提下，让 ME Network、Zone、DAV 共享同一套 analyzer 统计逻辑。