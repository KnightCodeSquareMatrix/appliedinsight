# 后端 JSON 完善指南

## 1. 当前状态

项目当前有两个 dump 命令，职责明确：

| 命令 | 文件 | 定位 | 消费端 |
|---|---|---|---|
| `/sorter me dump` | `me-dump-*.json` | 微观事实层 | 详情页、下钻页、存储健康诊断 |
| `/sorter me storageDump` | `storage-analysis-*.json` | 宏观分析层 | 首页 overview、summary cards、健康告警 |

当前 **me-dump 已基本完善**，storage-analysis 暂未改动。

---

## 2. me-dump.json

### 2.1 当前 CellDump 完整字段

已实现在 `SorterNetworkDumpWriter.CellDump` 中：

```java
private record CellDump(
    String sourceBlockId,          // drive 方块 id
    String drivePos,               // drive 坐标
    String attachedStoragePos,     // 附着存储坐标（external bus 挂载的目标）
    boolean externalStorageBus,    // 是否 external storage bus
    int slot,                      // 槽位号
    int driveCellCount,            // 所在 drive 的总槽位数 ★
    int distinctItemKeyCount,      // cell 内不同物品数
    long totalAmount,              // cell 内物品总数量
    List<CellEntryDump> entries,   // 物品明细
    Long totalBytes,               // cell 总字节容量 ★
    Long usedBytes,                // cell 已用字节 ★
    Integer totalItemTypes,        // cell 最大可存种类数 ★
    Integer remainingItemTypes,    // cell 剩余可存种类数 ★
    String cellKind,               // cell 类型描述（"64k_cell"）★
    String zoneId) {               // 归属 zone id（DAV 声明）★
}
```

标 ★ 的为 v2 新增字段。

### 2.2 获取容量数据的 Java 代码

已经在 `SorterNetworkDumpWriter` 中实现，参考 `SorterMergeReportFileLogger.inspectCellStats()` 的反射写法：

```java
// 在 dump 循环中，对非 external bus 的 slot：
StorageCell originalCell = drive.getOriginalCellInventory(slot);
if (originalCell != null) {
    capacity = inspectCellCapacity(originalCell);
    cellKind = describeCellKind(capacity.totalBytes());
}

// 反射读取 StorageCell 容量
private static CellCapacity inspectCellCapacity(StorageCell storageCell) {
    Long totalBytes = invokeLongMethod(storageCell, "getBytes");
    Long usedBytes = invokeLongMethod(storageCell, "getUsedBytes");
    Integer totalItemTypes = invokeIntMethod(storageCell, "getTotalTypes");
    Integer remainingItemTypes = invokeIntMethod(storageCell, "getRemainingItemTypes");
    if (totalBytes == null || usedBytes == null) return null;
    return new CellCapacity(totalBytes, usedBytes, totalItemTypes, remainingItemTypes);
}

// cellKind 由 totalBytes 按标准 AE2 cell 尺寸推导
private static String describeCellKind(long totalBytes) {
    if (totalBytes <= 1024) return "1k_cell";
    if (totalBytes <= 4096) return "4k_cell";
    if (totalBytes <= 16384) return "16k_cell";
    if (totalBytes <= 65536) return "64k_cell";
    if (totalBytes <= 262144) return "256k_cell";
    return totalBytes + "b_cell";
}
```

### 2.3 zoneId 字段

来自 `DriveMachine.getDeclaredZoneId()`，该方法是 DAV 方块实体（`DigitalAssetVaultBlockEntity`）的接口：

```java
String zoneId = drive.getDeclaredZoneId().orElse(null);
```

- 如果 drive 是 DAV 且已声明 zone → 返回 zoneId
- 如果 drive 是普通 ae2:drive → 返回 empty，序列化为 null
- 如果 drive 是 storage bus → 返回 empty，序列化为 null

### 2.4 attachedStoragePos 字段

已在原始代码中存在，来自 `drive.attachedStoragePos()`：

- 对 storage bus：返回挂载目标方块坐标
- 对普通 drive：返回 empty，序列化为 null

### 2.5 外部 storage bus 的容量处理

external storage bus 没有 `StorageCell`，所以：
- `totalBytes` / `usedBytes` / `totalItemTypes` / `remainingItemTypes` / `cellKind` → 全部为 null
- `entries` 仍正常填充（物品明细）

前端据此判断：`totalBytes === null` 即为外部存储，不做容量诊断。

---

## 3. storage-analysis.json

### 3.1 当前正确的项（维持不变）

| 字段 | 说明 |
|---|---|
| `report.summary` | 网络摘要（节点数、位置数、总量、碎片化等） |
| `report.storageLocations` | 存储位置列表（区分 internal/external） |
| `report.itemDistributions` | 物品分布（总量、位置数、内外分布） |
| `report.healthFlags` | 健康信号（`high_fragmentation` 等） |
| `report.mostFragmentedItems` | 最碎片化物品 |
| `report.largestStorages` | 最大存储位置 |
| `report.mixedInternalExternalItems` | 内外混存物品 |
| `report.suspectedSemanticCandidates` | 疑似语义节点（无限容器等） |

### 3.2 当前状态（已同步更新 ✅）

storage-analysis 的 `StorageLocationSummary` 已同步追加容量字段和 zoneId：

```java
// StorageAnalyzerReport.StorageLocationSummary 现包含：
Long totalBytes;
Long usedBytes;
Integer totalItemTypes;
Integer remainingItemTypes;
String cellKind;
String zoneId;
```

与 me-dump 的 CellDump 容量字段来源相同（`getOriginalCellInventory` → `StorageCell` 反射），数据一致。

### 3.3 需确认项（已无待办）

| 问题 | 现状 |
|---|---|
| `storageLocations[].totalBytes` 等容量字段 | ✅ 已输出 |
| `storageLocations[].zoneId` | ✅ 已输出 |
| `itemDistributions[].maxStackSize` | ✅ 已有 |

### 3.3 建议新增（低优先级）

如果后续需要，可以在 `StorageLocationSummary` 中补充：

```java
// 在 StorageAnalyzerReport.StorageLocationSummary 中可选追加
Long totalBytes;          // 同 me-dump
Long usedBytes;
Integer totalItemTypes;
Integer remainingItemTypes;
String cellKind;
String zoneId;
```

优先原则：**宏观 overview 消费 `storage-analysis`，微观详情消费 `me-dump`**。
容量字段主要在下钻页使用，由 me-dump 提供即可，storage-analysis 不必重复。

---

## 4. 两份 Dump 一致性

### 4.1 交叉校验清单

| 校验项 | me-dump | storage-analysis | 预期关系 |
|---|---|---|---|
| 唯一物品数 | `summary.uniqueItemKeyCount` | `report.summary.uniqueKeyCount` | 应相等 |
| occurrence 数 | `summary.itemOccurrenceCount` | `report.summary.totalOccurrenceCount` | 应相等 |
| 总量 | `cells[].totalAmount`之和 | `report.summary.totalAmount` | 应相等 |
| drive 数 | `summary.driveCount` | — | me-dump 特有 |
| internal/external 分类 | `cells[].externalStorageBus` | `report.storageLocations[].externalStorageBus` | 按需对齐 |
| zoneId | `cells[].zoneId` | — | me-dump 特有 |

### 4.2 一致性保障

两份 dump 在同一个 `Igrid` 上生成，但执行路径不同：
- me-dump：`SorterNetworkDumpWriter` → 遍历 cell 输出逐槽位明细 + 按物品聚合
- storage-analysis：`Ae2StorageAnalyzer` → 遍历 cell 输出存储位置摘要 + 物品分布

**两者不共享中间结果，但来源相同**，理论上应一致。不一致时以 me-dump 为准（更接近原始事实）。

---

## 5. 前端兼容性说明

### 5.1 DataNormalizer 适配

前端读取 me-dump 时应做一层归一化，处理以下情况：

```typescript
interface NormalizedCell {
  // 必填（始终存在）
  drivePos: string;
  slot: number;
  sourceBlockId: string;
  externalStorageBus: boolean;
  distinctItemKeyCount: number;
  totalAmount: number;
  entries: CellEntry[];

  // v2 新增（向后兼容，可能为 undefined / null）
  driveCellCount: number;           // 旧版无此字段，兜底 0
  totalBytes: number | null;        // external bus 为 null
  usedBytes: number | null;
  totalItemTypes: number | null;
  remainingItemTypes: number | null;
  cellKind: string | null;
  zoneId: string | null;
}
```

### 5.2 前端派生计算（me-dump 源）

```typescript
// Cell 剩余能力摘要
function cellRemainingSummary(cell: NormalizedCell) {
  const freeBytes = (cell.totalBytes ?? 0) - (cell.usedBytes ?? 0);
  const freeTypes = cell.remainingItemTypes ?? 0;

  // 还能放 xx 种新物品
  const canFitNewTypes = Math.max(0, freeTypes);

  // 还能放 xx 组（按已有物品平均每单位字节估算，假设一组64个）
  let canFitStacks = 0;
  if (freeBytes > 0 && cell.totalAmount > 0 && cell.usedBytes != null && cell.usedBytes > 0) {
    const avgBytesPerItem = cell.usedBytes / cell.totalAmount;
    const bytesPerStack = avgBytesPerItem * 64;
    canFitStacks = Math.floor(freeBytes / bytesPerStack);
  }

  return { freeBytes, canFitNewTypes, canFitStacks };
}

// Drive 空槽数
function driveEmptySlots(driveCells: NormalizedCell[]): number {
  if (driveCells.length === 0) return 0;
  return driveCells[0].driveCellCount - driveCells.length;
}

// Zone 聚合
function zoneAggregate(cells: NormalizedCell[]): ZoneHealth[] {
  const grouped = groupBy(cells, c => c.zoneId ?? '__unassigned__');
  return Array.from(grouped.entries()).map(([zoneId, zoneCells]) => ({
    zoneId,
    cellCount: zoneCells.length,
    totalBytes: sum(zoneCells, c => c.totalBytes ?? 0),
    usedBytes: sum(zoneCells, c => c.usedBytes ?? 0),
    nearlyFullCells: zoneCells.filter(c =>
      c.totalBytes && c.usedBytes && (c.usedBytes / c.totalBytes) > 0.8
    ).length,
    unassigned: zoneId === '__unassigned__',
  }));
}
```

### 5.3 前端兼容性处理

- 旧版 me-dump（无容量字段）：前端读取时 `totalBytes` 为 undefined → 归一化为 null → UI 显示"N/A"
- 新版 me-dump（有容量字段）：正常展示健康仪表盘
- `driveCellCount` 旧版缺失 → 前端只显示 "?"，不产生空槽计算

---

## 6. 优先级

### P0（已完成 ✅）
- me-dump CellDump 追加 `totalBytes` / `usedBytes` / `totalItemTypes` / `remainingItemTypes`
- me-dump CellDump 追加 `cellKind` / `zoneId` / `driveCellCount`
- storage-analysis StorageLocationSummary 追加 `totalBytes` / `usedBytes` / `totalItemTypes` / `remainingItemTypes` / `cellKind` / `zoneId`
- JSON Schema 同步更新
- 字段契约文档同步更新

### P1（已完成 ✅）
- storage-analysis 中 `StorageLocationSummary` 追加容量字段和 `zoneId`（已随 P0 同步完成）

### P2（未来）
- 两份 dump 的 `formatVersion` 统一管理
- 新增 `/sorter me healthDump` 命令做一次命中预览 + 容量汇总

---

## 附录：涉及的文件

| 文件 | 说明 |
|---|---|
| `src/main/java/.../ae2/dump/SorterNetworkDumpWriter.java` | me-dump 写入器，已修改 |
| `src/main/java/.../ae2/analysis/Ae2StorageAnalyzer.java` | storage-analysis 分析器，未修改 |
| `src/main/java/.../ae2/analysis/StorageAnalyzerReport.java` | 分析报告类型，未修改 |
| `src/main/resources/schema/sorter-network-dump.schema.json` | me-dump JSON Schema，已同步 |
| `docs/日志与JSON字段契约.md` | 字段契约文档，已同步 |
| `docs/前端对接说明.md` | 前端对接说明，已同步 |
| `docs/前端对接契约-需求-约束.md` | 前端契约需求，已同步 |
| `docs/ai/frontend-vibe-coding-prompt.md` | AI 前端编码 prompt，已同步 |
