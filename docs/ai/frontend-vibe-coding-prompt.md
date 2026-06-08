# Applied Energistics: Insight — 前端 Vibe-Coding Prompt

> **完整版 prompt**（本文档）。如需精简入口，见 [`FRONTEND_PROMPT.md`](FRONTEND_PROMPT.md)。

## 项目简介

这是一个 Minecraft AE2 模组 `appliedinsight` 的前端控制台。
后端（NeoForge 模组）通过游戏内命令生成 JSON dump，前端读取这些 JSON 提供：
- **存储健康诊断**：Zone/Bus/Cell 的容量、占用、剩余类型、健康告警
- **Storage Dashboard**：宏观总览 → 单 storage 详情 → 单 item 下钻
- **Filter Tree Editor（后续）**：可视化规则树编辑器

当前 MVP 优先做 **Storage Dashboard**，数据来自后端的 `me-dump-*.json`。

---

## 0. 新增：Filter Routing Table（过滤路由表契约）

> **新添加的页面：RulesPage（规则编辑页）**

第二期新增 **Filter Routing Table** 契约，定义前端规则编辑页的数据结构。
完整文档见 `docs/前端过滤路由表JSON契约.md`。

### 数据来源
前端从两个后端 JSON 聚合派生：
1. `RoutingProfile.json` — 位于配置目录 `appliedinsight/profiles/`
2. `FilterUiMetadata.json` — 位于配置目录 `appliedinsight/`（或打包于 mod 资源中）

### 核心 TypeScript 类型（完整版见契约文档附录 A）

```typescript
// 前端聚合视图 — 规则编辑页的主数据
interface FilterRoutingTable {
  schemaVersion: number;
  profile: ProfileMeta;
  routeRules: FlatRuleRow[];      // 规则表格行（含 filterSummary / isFallbackRow 等派生字段）
  filters: FilterTree[];          // Filter 表达式树（递归 AND/OR + 条件叶子）
  zones: ZoneOption[];            // 可用 Zone 列表
  uiMetadata: FilterUiMetadata;   // 编辑器控件配置（字段/操作符/组合器）
  preview?: RoutingPreview;       // 路由预览结果（可选）
}

// 规则表格行（每行对应一条 RouteRule + 前端派生字段）
interface FlatRuleRow {
  id: string; name: string; description: string;
  enabled: boolean; priority: number;
  action: 'ROUTE_TO_ZONE' | 'REJECT' | 'ONLY_MARK' | 'FALLBACK';
  filterId: string | null; targetZoneId: string | null;
  continueOnMatch: boolean; explanation: string;
  // 前端派生
  filterSummary: string;          // 如 "TOTAL_AMOUNT >= 4096"
  targetZoneName: string | null;
  defaultZoneName: string | null;
  isFallbackRow: boolean;         // 末尾虚拟行，表示默认回退 zone
}
```

### RulesPage 组件树（新增）

```
RulesPage
├── ProfileHeader（profile 元信息 + enabled switch）
├── FilterRoutingTable
│   ├── RuleTable（可拖拽排序的规则表格）
│   │   └── RuleRow → 点击展开 FilterTreePanel
│   │       └── FilterTreePanel（AND/OR 表达式树编辑器）
│   └── FilterExpressionEditor
│       ├── ConditionRow（field / operator / value 三联动）
│       └── GroupNode（递归 AND/OR 容器）
├── ZoneListSidebar（zone 列表，拖拽到规则行设置 targetZone）
└── RoutingPreviewPanel（输入 itemId 模拟路由决策）
```

---

## 1. 技术栈

- **React 18+** + **TypeScript**
- **Ant Design 5** — 组件库
- **React Flow**（`@xyflow/react`）— 树/DAG 编辑器（后续用）
- **Zustand** — 状态管理
- **Vite** — 构建工具

---

## 2. 模型概念

| 概念            | 说明                                                           |
| --------------- | -------------------------------------------------------------- |
| **ME Network**  | 一个 AE2 网络，包含 drive、cell、storage bus                   |
| **Drive**       | 一个 drive 方块（ae2:drive / extendedae:ex_drive），有若干槽位 |
| **Cell**        | drive 槽位中插入的存储元件（如 64k 存储元件），是最小存储单元  |
| **Storage Bus** | 外部存储连接器，挂载到抽屉/箱子等外部容器                      |
| **Zone**        | 由 DAV（Digital Asset Vault）声明的逻辑存储区域，包含若干 cell |
| **Item**        | 网络中的物品种类，可分布在多个 cell 中                         |
| **Occurrence**  | 物品在一个 cell 中的出现（一个 item 可以有多次 occurrence）    |

---

## 3. 数据契约

### 3.1 主要数据源：`me-dump-*.json`

由后端 `/sorter me dump` 命令生成，放在游戏目录的 `dumps/appliedinsight/` 下。

### 3.2 JSON 结构

```typescript
// ===== 根对象 =====
interface MeDump {
  formatVersion: "1";
  generatedAt: string;         // ISO 时间戳
  command: CommandContext;
  target: TargetInfo;
  summary: NetworkSummary;
  cells: CellSnapshot[];
  items: ItemAggregate[];
}

// ===== 命令上下文 =====
interface CommandContext {
  executor: string;            // 玩家名
  dimension: string;           // 维度 id
  x: number; y: number; z: number;
  serverTime: number;          // 服务器 tick
}

// ===== 目标信息 =====
interface TargetInfo {
  targetBlockId: string;       // 目标方块 id
  targetPos: string | null;    // 坐标 "x,y,z"
  controllerPos: string | null;
}

// ===== 网络摘要 =====
interface NetworkSummary {
  networkNodeCount: number;
  driveCount: number;
  scannedCellSlotCount: number;
  mountedCellCount: number;
  uniqueItemKeyCount: number;
  duplicatedItemKeyCount: number;
  duplicatedCellReferenceCount: number;
  itemOccurrenceCount: number;
}

// ===== Cell 快照 =====
interface CellSnapshot {
  sourceBlockId: string;       // drive 方块 id
  drivePos: string;            // drive 坐标 "x,y,z"
  attachedStoragePos: string | null;  // 外部附着坐标
  externalStorageBus: boolean; // 是否 external storage bus
  slot: number;                // 槽位号
  driveCellCount: number;      // 所在 drive 的总槽位数，用于计算空槽: emptySlots = driveCellCount - 已挂载数
  distinctItemKeyCount: number;
  totalAmount: number;         // 当前 cell 内物品总量

  // 容量字段（external bus 为 null）
  totalBytes: number | null;          // AE2 cell 总字节容量
  usedBytes: number | null;           // 已用字节
  totalItemTypes: number | null;      // 最大可存物品种类数
  remainingItemTypes: number | null;  // 剩余可存种类数
  cellKind: string | null;            // "64k_cell" 等，由 totalBytes 推导
  zoneId: string | null;              // 归属 zone id（DAV 声明），未归属则为 null

  entries: CellEntry[];
}

// ===== Cell 内物品条目 =====
interface CellEntry {
  itemId: string;              // 物品 id
  displayName: string;
  amount: number;
  componentsPatchEmpty: boolean;
}

// ===== 物品聚合 =====
interface ItemAggregate {
  itemId: string;
  modId: string;
  displayName: string;
  totalAmount: number;         // 全网总量
  occurrenceCount: number;     // 分布位置数
  componentsPatchEmpty: boolean;
  maxStackSize: number;
  tags: string[];
  serializedStackNbt: string;
  occurrences: ItemOccurrence[];
}

// ===== 物品 Occurrence =====
interface ItemOccurrence {
  sourceBlockId: string;
  drivePos: string;
  attachedStoragePos: string | null;
  externalStorageBus: boolean;
  slot: number;
  amount: number;
}
```

### 3.3 补充数据源：`storage-analysis-*.json`

由 `/sorter me storageDump` 生成，包含预计算的健康指标和语义候选。

```typescript
interface StorageAnalysisDump {
  formatVersion: "1";
  generatedAt: string;
  dimensionId: string | null;
  targetBlockId: string | null;
  targetPos: string | null;
  controllerPos: string | null;
  report: StorageAnalyzerReport;
}

interface StorageAnalyzerReport {
  summary: {
    networkNodeCount: number;
    storageLocationCount: number;
    nonEmptyStorageLocationCount: number;
    internalStorageLocationCount: number;
    externalStorageLocationCount: number;
    internalTotalAmount: number;
    externalTotalAmount: number;
    uniqueKeyCount: number;
    duplicatedKeyCount: number;
    totalAmount: number;
    totalOccurrenceCount: number;
    internalOnlyKeyCount: number;
    externalOnlyKeyCount: number;
    mixedLocationKeyCount: number;
    fragmentationScore: number;
    fragmentationLevel: "low" | "medium" | "high";
  };
  storageLocations: StorageLocationSummary[];
  itemDistributions: ItemDistributionSummary[];
  healthFlags: string[];       // 健康信号列表
  mostFragmentedItems: ItemDistributionSummary[];
  largestStorages: StorageLocationSummary[];
  mixedInternalExternalItems: ItemDistributionSummary[];
  suspectedSemanticCandidates: StorageSemanticCandidate[];
}
```

---

## 4. 页面结构

### MVP 三页面布局

```
+----------------------------------------------------------+
| SiderNav                                                  |
|  📊 Dashboard  (首页)                                     |
|  📦 Storages   (存储列表)                                 |
|  🧩 Items      (物品列表)                                 |
|  🔧 Rules      (规则编辑 - 后续)                          |
+----------------------------------------------------------+
```

### 页面一：Dashboard（首页）

```
+----------------------------------------------------------+
| 🟦 Health Summary Cards                                   |
|   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       |
|   │ 总容量   │ │ 已用    │ │ Cell数  │ │ 告警数  │       |
|   │ 1.2M B  │ │ 450K B  │ │ 24      │ │ 2       │       |
|   └─────────┘ └─────────┘ └─────────┘ └─────────┘       |
+----------------------------------------------------------+
| 📈 四图区域                                              |
|   ┌───────────────────┐ ┌───────────────────┐            |
|   │ Zone 容量分布(饼图) │ │ Top Items(柱状图) │            |
|   └───────────────────┘ └───────────────────┘            |
|   ┌───────────────────┐ ┌───────────────────┐            |
|   │ 容量使用率(仪表盘) │ │ Internal/External │            |
|   └───────────────────┘ └───────────────────┘            |
+----------------------------------------------------------+
| ⚠️ Health Flags / Warnings                               |
|   • Zone "bulk" 有 2 个 cell 使用率 > 80%                |
|   • 3 个 cell 未归属任何 zone                            |
|   • 疑似无限容器: drawer_at_123,45,67                    |
+----------------------------------------------------------+
| 📋 最大存储列表 / 最碎片化物品 / 混存物品                |
+----------------------------------------------------------+
```

### 页面二：Storages（存储详情）

```
+----------------------------------------------------------+
| 筛选: [搜索] [内部/外部切换] [Zone 筛选]                  |
+----------------------------------------------------------+
| 表格视图:                                                 |
| ┌──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┐|
| │位置  │Cell  │类型  │已用  │总容量│使用率 │类型  │Zone │|
| │      │种类  │      │字节  │      │      │剩余  │     │|
| ├──────┼──────┼──────┼──────┼──────┼──────┼──────┼──────┤|
| │...   │12    │64k   │12K   │64K   │19%   │51    │bulk │|
| └──────┴──────┴──────┴──────┴──────┴──────┴──────┴──────┘|
| 点击行 -> Drawer 展开 detail:                              |
|   - cell 基本信息                                         |
|   - 容量仪表盘 (进度条: 字节 + 类型)                      |
|   - Top N items in this cell                              |
|   - 吞吐/健康指标                                         |
+----------------------------------------------------------+
```

### 页面三：Items（物品浏览）

```
+----------------------------------------------------------+
| 筛选: [搜索] [Mod 筛选] [Tag 筛选] [有Components?]        |
+----------------------------------------------------------+
| 表格视图:                                                 |
| ┌────────┬────────┬──────┬──────┬──────┬──────┬──────┐   |
| │物品ID  │显示名  │Mod   │总量  │分布  │Int/Ext│操作  │   |
| ├────────┼────────┼──────┼──────┼──────┼──────┼──────┤   |
| │...     │石头    │mine  │500K  │3     │3/0   │详情  │   |
| └────────┴────────┴──────┴──────┴──────┴──────┴──────┘   |
| 点击行 -> Drawer:                                         |
|   - 物品基本信息                                          |
|   - 所有 occurrence 列表 (drive/slot/amount)              |
|   - 分布热力图                                            |
+----------------------------------------------------------+
```

---

## 5. 前端派生计算（前端自己算，不用后端分析）

### 5.1 容量健康指标

```typescript
// 从 CellSnapshot 派生
interface CellHealth {
  byteUsageRatio: number;      // usedBytes / totalBytes
  typeUsageRatio: number;      // (totalItemTypes - remainingItemTypes) / totalItemTypes
  isNearlyFull: boolean;       // byteUsageRatio > 0.8
  isTypeExhausted: boolean;    // remainingItemTypes < 5
  healthLevel: 'good' | 'warning' | 'critical';
}
```

### 5.2 Zone 聚合

```typescript
// 按 zoneId 聚合 cells
interface ZoneHealth {
  zoneId: string;
  cellCount: number;
  totalBytes: number;
  usedBytes: number;
  freeBytes: number;
  byteUsageRatio: number;
  totalItemTypes: number;
  usedItemTypes: number;
  remainingItemTypes: number;
  typeUsageRatio: number;
  nearlyFullCells: number;
  cells: CellSnapshot[];
}
```

### 5.3 未分配 Cell 列表

```typescript
// zoneId === null && externalStorageBus === false 的 cell
interface UnassignedCell extends CellSnapshot {}
```

### 5.4 外部存储 Bus 列表

```typescript
// externalStorageBus === true 的 cell
// 没有容量字段（totalBytes 等为 null），只有物品统计
interface ExternalBus extends CellSnapshot {}
```

---

## 6. 设计原则（来自产品讨论）

1. **先给结果，再让用户修正** — Dashboard 打开就有可用数据
2. **先给宏观，再允许微观下钻** — 首页只给 summary 和四图
3. **容量数据前端自己算** — 后端只给原始 `totalBytes / usedBytes`，前端算使用率/健康色
4. **Internal vs External 区分展示** — 内部 cell 有完整容量指标，external bus 只有物品统计
5. **Zone 归属强提示** — `zoneId` 为 null 的 cell 用灰色/虚线/标签提示"未分配"
6. **减少表格感** — 多用 Card / Progress / Statistic，少用大 Table
7. **配色固定含义** — 蓝=主信息、绿=健康、橙=告警、红=严重、灰=未配置

---

## 7. 组件树建议

```
App
├── Layout (Ant Design)
│   ├── Sider
│   │   └── NavMenu (items: Dashboard / Storages / Items / Rules)
│   ├── Content
│   │   ├── DashboardPage
│   │   │   ├── HealthSummaryCards
│   │   │   ├── ChartsRow (4 charts)
│   │   │   │   ├── ZoneCapacityPieChart
│   │   │   │   ├── TopItemsBarChart
│   │   │   │   ├── CapacityGaugeChart
│   │   │   │   └── InternalExternalRatioChart
│   │   │   ├── HealthFlagsList
│   │   │   └── LargestStoragesTable
│   │   ├── StoragesPage
│   │   │   ├── StorageFilters
│   │   │   ├── StorageTable
│   │   │   └── StorageDetailDrawer
│   │   │       ├── CellCapacityProgress
│   │   │       ├── CellTypeUsageProgress
│   │   │       └── CellEntriesTable
│   │   ├── ItemsPage
│   │   │   ├── ItemFilters
│   │   │   ├── ItemsTable
│   │   │   └── ItemDetailDrawer
│   │   │       └── OccurrenceTable
│   │   └── RulesPage (placeholder / future)
│   └── Footer
├── Stores (Zustand)
│   ├── useMeDumpStore      // 加载的 me-dump JSON
│   ├── useAnalysisStore    // 加载的 storage-analysis JSON
│   └── useHealthStore      // 派生的健康数据
└── Utils
    ├── healthCalculations.ts  // 容量/健康计算
    ├── zoneAggregation.ts     // Zone 聚合
    └── types.ts               // TypeScript 类型定义
```

---

## 8. 各页面组件 Props 设计

### DashboardPage

```typescript
interface DashboardPageProps {
  meDump: MeDump | null;           // 主要数据源（或从 store 读）
  analysisDump: StorageAnalysisDump | null; // 辅助数据源
}

// 派生数据
interface DashboardViewModel {
  globalHealth: {
    totalBytes: number;
    usedBytes: number;
    freeBytes: number;
    byteUsageRatio: number;
    totalCellCount: number;
    emptyCellCount: number;
    nearlyFullCellCount: number;
    unassignedCellCount: number;
    healthLevel: 'good' | 'warning' | 'critical';
  };
  zoneHealth: ZoneHealth[];
  unassignedCells: CellSnapshot[];
  externalBuses: CellSnapshot[];
  topItems: ItemAggregate[];  // 按 totalAmount 排序取 top N
  healthFlags: string[];
}
```

### StoragesPage

```typescript
interface StoragesPageProps {
  cells: CellSnapshot[];          // 全部 cell
}

interface StorageTableRow {
  key: string;
  drivePos: string;
  slot: number;
  sourceBlockId: string;
  externalStorageBus: boolean;
  cellKind: string | null;
  distinctItemKeyCount: number;
  totalAmount: number;
  totalBytes: number | null;
  usedBytes: number | null;
  byteUsageRatio: number | null;
  totalItemTypes: number | null;
  remainingItemTypes: number | null;
  typeUsageRatio: number | null;
  zoneId: string | null;
  healthLevel: 'good' | 'warning' | 'critical' | 'na';
}
```

### ItemsPage

```typescript
interface ItemsPageProps {
  items: ItemAggregate[];         // 全部物品
}

interface ItemsTableRow {
  key: string;
  itemId: string;
  modId: string;
  displayName: string;
  totalAmount: number;
  occurrenceCount: number;
  componentsPatchEmpty: boolean;
  maxStackSize: number;
}
```

---

## 9. 第一版不做（但后续要做）

以下功能属于后续迭代，第一版不做以避免范围膨胀：

- **Filter Tree Editor**（规则编辑页面）
- **Trace Explorer**（物品路径追踪）
- **写回 override**（前端修改 -> 保存到游戏）
- **撤销/重做/版本对比**
- **多级联动过滤**

---

## 10. 关键实现提示

### 10.1 加载 dump 文件
开发时把 `me-dump-*.json` 和 `storage-analysis-*.json` 放在 `public/dumps/` 下，
前端 fetch 加载。后续通过/file协议直接读取游戏目录。

### 10.2 健康计算示例

```typescript
function computeCellHealth(cell: CellSnapshot): CellHealth | null {
  if (cell.totalBytes === null || cell.usedBytes === null) return null;
  const byteUsageRatio = cell.totalBytes > 0 ? cell.usedBytes / cell.totalBytes : 0;
  const typeUsageRatio = (cell.totalItemTypes && cell.remainingItemTypes !== null)
    ? (cell.totalItemTypes - cell.remainingItemTypes) / cell.totalItemTypes
    : 0;
  const isNearlyFull = byteUsageRatio > 0.8;
  const isTypeExhausted = (cell.remainingItemTypes ?? 999) < 5;
  const healthLevel = isNearlyFull || isTypeExhausted ? 'critical'
    : byteUsageRatio > 0.6 ? 'warning'
    : 'good';
  return { byteUsageRatio, typeUsageRatio, isNearlyFull, isTypeExhausted, healthLevel };
}
```

### 10.3 Zone 聚合示例

```typescript
function aggregateZones(cells: CellSnapshot[]): ZoneHealth[] {
  const grouped = new Map<string, CellSnapshot[]>();
  for (const cell of cells) {
    const zid = cell.zoneId ?? '__unassigned__';
    if (!grouped.has(zid)) grouped.set(zid, []);
    grouped.get(zid)!.push(cell);
  }
  return Array.from(grouped.entries()).map(([zoneId, zoneCells]) => {
    const hasCapacity = zoneCells.some(c => c.totalBytes !== null);
    const totalBytes = zoneCells.reduce((s, c) => s + (c.totalBytes ?? 0), 0);
    const usedBytes = zoneCells.reduce((s, c) => s + (c.usedBytes ?? 0), 0);
    // ... 聚合计算
    return { zoneId, cellCount: zoneCells.length, totalBytes, usedBytes, /* ... */ };
  });
}
```

### 10.4 Route Tree（后续）的推荐数据结构

当后续实现 Filter Tree Editor 时，推荐用嵌套树 JSON 而不是 nodes+edges：

```typescript
type NodeType = 'root' | 'condition' | 'logic' | 'action' | 'fallback';
type LogicOp = 'AND' | 'OR' | 'NOT' | 'SEQUENCE';

interface RuleTreeNode {
  id: string;
  type: NodeType;
  name: string;
  enabled: boolean;
  // condition 特有
  field?: string;
  operator?: string;
  value?: any;
  // logic 特有
  op?: LogicOp;
  children?: RuleTreeNode[];
  // action 特有
  actionKind?: 'routeToZone' | 'buffer' | 'manualReview' | 'continue';
  zoneId?: string;
  stopOnMatch?: boolean;
}
```

---

## 11. 参考 UI 设计（来自前期产品讨论）

### 总体风格
- 控制台感，深色或浅色模式，背景浅灰，内容白卡片
- **三栏布局**：左导航、中内容、右详情（Drawer）
- 状态用 Tag/Badge 强提示

### 颜色语义
| 颜色         | 含义                   |
| ------------ | ---------------------- |
| 蓝 `#1677ff` | 主信息、选中           |
| 绿 `#52c41a` | 健康、正常             |
| 橙 `#faad14` | 告警、受限、近满       |
| 红 `#ff4d4f` | 严重、耗尽、异常       |
| 灰 `#d9d9d9` | 未配置、未归属、未启用 |

### Trace 视图（后续做）
参考 Jaeger/Datadog Trace：
- 上方筛选
- 中间 trace 树/时间轴
- 点击节点 -> 右侧详情
- 支持展开/折叠/高亮异常

### 节点卡片（后续 Filter Tree Editor）
- 条件卡片：`条件名 | 摘要 | 命中量`
- 逻辑卡片：`AND | 3个子条件`
- 动作卡片：`→ Zone 名称 | 优先级`

---

## 12. 开发步骤（建议顺序）

### Day 1-2：项目骨架 + 数据加载
1. Vite + React + TypeScript 初始化
2. 安装 Ant Design / Zustand / 图表库（@ant-design/charts 或 echarts）
3. 定义 TypeScript 类型（完整 me-dump 类型）
4. 数据加载 hook（fetch / 拖入文件）
5. Zustand store 基础

### Day 3-4：Dashboard 页面
1. HealthSummaryCards（4 个 Statistic Card）
2. 四图区域（饼图/柱状图/仪表盘）
3. HealthFlags 列表
4. LargestStorages / 最碎片化物品表格
5. 健康计算工具函数

### Day 5-6：Storages 页面
1. StorageTable 带容量/类型进度条
2. 筛选栏（搜索/内部外部/Zone）
3. StorageDetailDrawer（容量仪表盘 + entries 表格）
4. Zone 聚合视图

### Day 7：Items 页面
1. ItemsTable
2. 筛选栏（搜索/Mod/Tag）
3. ItemDetailDrawer（occurrence 列表 + 分布信息）

---

## 13. 参考文件路径

### 数据文件
- me-dump 示例: `dumps/appliedinsight/me-dump-20260519-225051.json`（约 140KB）
- storage-analysis 示例: `dumps/appliedinsight/storage-analysis-*.json`
- JSON Schema: `src/main/resources/schema/sorter-network-dump.schema.json`

### 后端文档
- 后端契约文档: `docs/前端对接契约-需求-约束.md`
- 后端对接说明: `docs/前端对接说明.md`
- 字段契约: `docs/日志与JSON字段契约.md`
- 后端 API 参考: `docs/API_REFERENCE.md`
- 命令参考: `docs/COMMANDS_REFERENCE.md`
- 架构参考 + 术语表: `docs/架构/FACTS.md`
- Dashboard 设计哲学: `docs/dashboard的设计哲学.md`
- 过滤路由表 JSON 契约: `docs/前端过滤路由表JSON契约.md`

### 前端文档
- 精简入口: `docs/ai/FRONTEND_PROMPT.md`
- 完整 prompt（本文档）: `docs/ai/frontend-vibe-coding-prompt.md`
