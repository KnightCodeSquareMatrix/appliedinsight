# 前端过滤路由表 JSON 契约

> **契约版本**: 1  
> **状态**: 草案  
> **适用范围**: Filter Tree Editor（规则编辑页面）& Dashboard 路由可视化

---

## 1. 设计目标

本文档定义前端 **过滤路由表**（Filter Routing Table）的数据契约。该契约面向以下场景：

| 场景 | 说明 |
|------|------|
| **规则表格展示** | 前端将 `routeRules` 以可排序、可分页的表格渲染，每行显示 rule + filter 摘要 |
| **Filter 内联展开** | 点击规则行展开 Filter Expression 树（AND/OR 嵌套条件） |
| **规则编辑** | 新建/编辑/删除/启用/禁用/拖拽排序 RouteRule |
| **Filter 编辑** | 可视化 Filter 表达式树构建（字段-操作符-值节点） |
| **Zone 选择** | 编辑规则时可选的目标 Zone 列表 |
| **路由预览** | 对指定 item 模拟路由决策，展示匹配链路 |

### 1.1 数据流

```
后端 (RoutingProfile JSON)
       │
       ▼
前端 FilterRoutingTable (本契约)  ← 包含 FilterUiMetadata
       │
       ├── FlatRuleRow[]    → 规则表格
       ├── FilterTree[]     → Filter 表达式树
       ├── ZoneOption[]     → Zone 下拉选项
       └── UiMetadata       → 编辑器控件配置
```

---

## 2. 核心结构

### 2.1 `FilterRoutingTable` — 根对象

前端消费的主结构。由后端 `RoutingProfile` + `FilterUiMetadata` 聚合而成。

```typescript
interface FilterRoutingTable {
  /** 契约格式版本，当前为 1 */
  schemaVersion: number;

  /** Profile 元信息 */
  profile: ProfileMeta;

  /** 路由规则表格行（按 priority 升序排列） */
  routeRules: FlatRuleRow[];

  /** 所有 ItemFilter 的完整表达树（被 routeRules 引用） */
  filters: FilterTree[];

  /** 所有可用 Zone（被 routeRules 的 targetZoneId 引用） */
  zones: ZoneOption[];

  /** Filter 编辑器 UI 元数据（控件配置） */
  uiMetadata: FilterUiMetadata;

  /** 路由预览结果（可选，由后端 `/sorter me plan` 或模拟引擎注入） */
  preview?: RoutingPreview | null;
}
```

### 2.2 `ProfileMeta` — Profile 元信息

```typescript
interface ProfileMeta {
  /** Profile 唯一标识 */
  id: string;
  /** 显示名称 */
  name: string;
  /** 描述 */
  description: string;
  /** 是否启用 */
  enabled: boolean;
  /** 默认 Zone ID（无规则命中时的回退目标） */
  defaultZoneId: string;
  /** Profile 格式版本 */
  version: number;
  /** Profile 文件路径（后端用，前端可忽略） */
  path?: string;
}
```

---

## 3. 规则表格行

### 3.1 `FlatRuleRow` — 扁平的规则表格行

每条 `RouteRule` 渲染为表格中的一行。

```typescript
interface FlatRuleRow {
  /** 规则唯一 ID */
  id: string;
  /** 规则名称 */
  name: string;
  /** 规则描述 */
  description: string;
  /** 是否启用 */
  enabled: boolean;
  /** 优先级序号（升序，越小越优先） */
  priority: number;
  /** 路由动作类型 */
  action: RouteAction;
  /** 引用的 Filter ID */
  filterId: string;
  /** 目标 Zone ID（当 action=ROUTE_TO_ZONE 时有效） */
  targetZoneId: string | null;
  /** 是否在匹配后继续评估后续规则 */
  continueOnMatch: boolean;
  /** 规则说明/备注 */
  explanation: string;

  // ─── 前端展示用派生字段（非后端原始字段，由前端渲染层聚合） ───

  /** Filter 摘要文本（如 "TOTAL_AMOUNT >= 4096"） */
  filterSummary: string;
  /** 目标 Zone 名称（由前端从 zones 查找） */
  targetZoneName: string | null;
  /** 默认 Zone 名称（由前端从 profile 查找） */
  defaultZoneName: string | null;
  /** 是否为默认回退行（自动生成的虚拟行，表示默认 zone） */
  isFallbackRow: boolean;
}
```

#### `RouteAction` 枚举

```typescript
type RouteAction =
  /** 路由到目标 Zone */
  | "ROUTE_TO_ZONE"
  /** 拒绝（物品不进入任何 Zone） */
  | "REJECT"
  /** 仅标记，不执行搬运 */
  | "ONLY_MARK"
  /** 回退到默认 Zone */
  | "FALLBACK"
;
```

### 3.2 规则表格列定义

前端按以下列渲染 `FlatRuleRow[]`：

| 列 | 对应字段 | 宽度 | 控件 |
|----|----------|------|------|
| 拖拽排序 | _(序号)_ | 40px | 拖拽手柄 ⠿ |
| 优先级 | `priority` | 60px | 数字输入 |
| 启用 | `enabled` | 48px | Switch |
| 规则名称 | `name` | 150px | 文本输入 |
| Filter | `filterSummary` | auto | 标签/链接 → 展开 |
| 动作 | `action` | 120px | Select (ROUTE_TO_ZONE / REJECT / ONLY_MARK / FALLBACK) |
| 目标 Zone | `targetZoneName` | 120px | Select (zones 列表) |
| 继续匹配 | `continueOnMatch` | 80px | Switch |
| 说明 | `explanation` | 120px | 文本输入 |
| 操作 | _(id)_ | 80px | 编辑/删除按钮 |

---

## 4. Filter 表达式树

### 4.1 `FilterTree` — 完整的 Filter 定义

```typescript
interface FilterTree {
  /** Filter 唯一 ID */
  id: string;
  /** Filter 名称 */
  name: string;
  /** Filter 描述 */
  description: string;
  /** 是否启用 */
  enabled: boolean;
  /** 根表达式节点（AND/OR 组合或条件） */
  root: FilterExpressionNode;
}
```

### 4.2 `FilterExpressionNode` — 表达式节点（递归结构）

```typescript
/** 表达式节点：要么是条件节点，要么是组合节点 */
type FilterExpressionNode = FilterConditionNode | FilterGroupNode;
```

#### `FilterConditionNode` — 条件节点（叶子节点）

```typescript
interface FilterConditionNode {
  type: "condition";
  /** 匹配字段 */
  field: FilterField;
  /** 操作符 */
  operator: FilterOperator;
  /** 匹配值（unary 操作符时为 null） */
  value: string | number | null;
  /** UI 辅助：解析后的显示值 */
  displayValue?: string;
}
```

#### `FilterGroupNode` — 组合节点（内部节点）

```typescript
interface FilterGroupNode {
  type: "group";
  /** 组合方式 */
  combinator: FilterCombinator;
  /** 子表达式列表 */
  rules: FilterExpressionNode[];
}
```

### 4.3 枚举定义

#### `FilterField`

```typescript
type FilterField =
  | "ITEM_ID"           // 物品 ID，如 "minecraft:cobblestone"，值类型 STRING
  | "MOD_ID"            // Mod 命名空间，如 "ae2"，值类型 STRING
  | "TAG"               // 物品标签，如 "c:ingots/iron"，值类型 STRING
  | "DISPLAY_NAME"      // 显示名称，值类型 STRING
  | "HAS_COMPONENTS"    // 是否有组件数据，值类型 BOOLEAN
  | "TOTAL_AMOUNT"      // 全网总量，值类型 NUMBER
  | "NBT_PATH"          // NBT 路径提取值，值类型 STRING
;
```

#### `FilterOperator`

```typescript
type FilterOperator =
  | "EQUALS"            // 精确相等（STRING, NUMBER）
  | "CONTAINS"          // 子串匹配（STRING，大小写不敏感）
  | "IN"                // CSV 候选列表匹配（STRING）
  | "GREATER_OR_EQUAL"  // >=（NUMBER）
  | "LESS_OR_EQUAL"     // <=（NUMBER）
  | "IS_TRUE"           // 布尔真（BOOLEAN，unary）
  | "IS_FALSE"          // 布尔假（BOOLEAN，unary）
  | "REGEX"             // 正则匹配（STRING）
;
```

#### `FilterCombinator`

```typescript
type FilterCombinator =
  | "AND"   // 所有子表达式均匹配
  | "OR"    // 任一子表达式匹配
;
```

---

## 5. Zone 选项

### 5.1 `ZoneOption` — Zone 定义

```typescript
interface ZoneOption {
  /** Zone 唯一 ID */
  id: string;
  /** Zone 显示名称 */
  name: string;
  /** Zone 描述 */
  description: string;
  /** 是否启用 */
  enabled: boolean;
  /** Zone 分类 */
  kind: ZoneKind;
  /** 是否可作为默认 Zone */
  allowAsDefault: boolean;
}
```

#### `ZoneKind`

```typescript
type ZoneKind =
  | "BULK"             // 大宗物品（量大价低）
  | "MISC"             // 杂项（默认兜底）
  | "COMPONENT_SAFE"   // 组件安全（保留 NBT/组件）
  | "SPECIAL"          // 特殊（高价值/受控）
  | "CUSTOM"           // 自定义
;
```

---

## 6. Filter 编辑器 UI 元数据

### 6.1 `FilterUiMetadata` — 编辑器控件配置

前端根据此元数据动态渲染 Filter 条件编辑器。

```typescript
interface FilterUiMetadata {
  /** Schema 版本 */
  schemaVersion: number;
  /** 可用字段定义（决定"field"下拉选项） */
  fields: FilterFieldDefinition[];
  /** 可用操作符定义（决定"operator"下拉选项） */
  operators: FilterOperatorDefinition[];
  /** 可用组合器定义（决定"AND/OR"切换） */
  combinators: FilterCombinatorDefinition[];
}
```

### 6.2 `FilterFieldDefinition`

```typescript
interface FilterFieldDefinition {
  /** 字段枚举名 */
  name: FilterField;
  /** UI 标签 */
  label: string;
  /** UI 提示文字 */
  description: string;
  /** 值类型 */
  valueType: FilterValueType;
  /** 值编辑器类型 */
  valueEditorType: FilterValueEditorType;
  /** 该字段支持的操作符 */
  supportedOperators: FilterOperator[];
}
```

#### `FilterValueType`

```typescript
type FilterValueType = "STRING" | "NUMBER" | "BOOLEAN";
```

#### `FilterValueEditorType`

```typescript
type FilterValueEditorType =
  | "text"      // 文本输入框
  | "number"    // 数字输入框
  | "none"      // 无需输入（IS_TRUE / IS_FALSE）
;
```

### 6.3 `FilterOperatorDefinition`

```typescript
interface FilterOperatorDefinition {
  /** 操作符枚举名 */
  name: FilterOperator;
  /** UI 标签（短符号） */
  label: string;
  /** UI 提示文字 */
  description: string;
  /** 是否为 unary（无需值） */
  unary: boolean;
}
```

### 6.4 `FilterCombinatorDefinition`

```typescript
interface FilterCombinatorDefinition {
  /** 组合器枚举名 */
  name: FilterCombinator;
  /** UI 标签 */
  label: string;
}
```

---

## 7. 路由预览

### 7.1 `RoutingPreview` — 单物品路由模拟结果

由后端注入或前端本地模拟，用于"测试此规则"功能。

```typescript
interface RoutingPreview {
  /** 被评估的物品上下文 */
  item: PreviewItemContext;
  /** 最终决策 */
  decision: RoutingDecision;
  /** 逐规则评估链路 */
  evaluations: RuleEvaluation[];
}
```

### 7.2 `PreviewItemContext`

```typescript
interface PreviewItemContext {
  /** 物品 ID */
  itemId: string;
  /** Mod ID */
  modId: string;
  /** 显示名称 */
  displayName: string;
  /** 物品标签 */
  tags: string[];
  /** 是否有组件数据 */
  hasComponents: boolean;
  /** 全网总量 */
  totalAmount: number;
}
```

### 7.3 `RoutingDecision`

```typescript
interface RoutingDecision {
  /** 决策类型 */
  decisionType: "ROUTED" | "REJECTED" | "MARKED_ONLY" | "FALLBACK" | "NO_MATCH";
  /** 最终目标 Zone ID */
  finalZoneId: string | null;
  /** 命中的规则 ID */
  matchedRuleId: string | null;
  /** 命中的 Filter ID */
  matchedFilterId: string | null;
  /** 是否使用了默认 Zone */
  fallbackUsed: boolean;
}
```

### 7.4 `RuleEvaluation`

```typescript
interface RuleEvaluation {
  /** 评估的规则 ID */
  ruleId: string;
  /** 评估的 Filter ID */
  filterId: string;
  /** 是否匹配 */
  matched: boolean;
  /** 规则动作 */
  action: RouteAction;
  /** 目标 Zone ID */
  targetZoneId: string | null;
  /** 规则说明 */
  explanation: string;
}
```

---

## 8. 完整 JSON 示例

```json
{
  "schemaVersion": 1,
  "profile": {
    "id": "default_profile",
    "name": "Default Profile",
    "description": "Example routing profile",
    "enabled": true,
    "defaultZoneId": "misc",
    "version": 1
  },
  "routeRules": [
    {
      "id": "bulk_route",
      "name": "Route bulk items",
      "description": "",
      "enabled": true,
      "priority": 10,
      "action": "ROUTE_TO_ZONE",
      "filterId": "bulk_filter",
      "targetZoneId": "bulk",
      "continueOnMatch": false,
      "explanation": "",
      "filterSummary": "TOTAL_AMOUNT >= 4096",
      "targetZoneName": "Bulk",
      "defaultZoneName": "Misc",
      "isFallbackRow": false
    },
    {
      "id": "component_route",
      "name": "Route component items",
      "description": "",
      "enabled": true,
      "priority": 20,
      "action": "ROUTE_TO_ZONE",
      "filterId": "component_filter",
      "targetZoneId": "misc",
      "continueOnMatch": false,
      "explanation": "Components are routed conservatively.",
      "filterSummary": "HAS_COMPONENTS is true",
      "targetZoneName": "Misc",
      "defaultZoneName": "Misc",
      "isFallbackRow": false
    },
    {
      "id": "__fallback__",
      "name": "(Default Zone Fallback)",
      "description": "Items not matching any rule fall back to the default zone.",
      "enabled": true,
      "priority": 9999,
      "action": "FALLBACK",
      "filterId": null,
      "targetZoneId": "misc",
      "continueOnMatch": false,
      "explanation": "Fallback to default zone when no rules match.",
      "filterSummary": "(catch-all, no filter)",
      "targetZoneName": "Misc",
      "defaultZoneName": "Misc",
      "isFallbackRow": true
    }
  ],
  "filters": [
    {
      "id": "bulk_filter",
      "name": "Bulk threshold",
      "description": "",
      "enabled": true,
      "root": {
        "type": "group",
        "combinator": "AND",
        "rules": [
          {
            "type": "condition",
            "field": "TOTAL_AMOUNT",
            "operator": "GREATER_OR_EQUAL",
            "value": 4096,
            "displayValue": "4096"
          }
        ]
      }
    },
    {
      "id": "component_filter",
      "name": "Component-safe items",
      "description": "",
      "enabled": true,
      "root": {
        "type": "group",
        "combinator": "AND",
        "rules": [
          {
            "type": "condition",
            "field": "HAS_COMPONENTS",
            "operator": "IS_TRUE",
            "value": null,
            "displayValue": ""
          }
        ]
      }
    }
  ],
  "zones": [
    {
      "id": "bulk",
      "name": "Bulk",
      "description": "Large-volume items",
      "enabled": true,
      "kind": "BULK",
      "allowAsDefault": false
    },
    {
      "id": "misc",
      "name": "Misc",
      "description": "Default catch-all zone",
      "enabled": true,
      "kind": "MISC",
      "allowAsDefault": true
    }
  ],
  "uiMetadata": {
    "schemaVersion": 1,
    "fields": [
      {
        "name": "ITEM_ID",
        "label": "Item ID",
        "description": "Full Minecraft item id such as ae2:sky_stone_block.",
        "valueType": "STRING",
        "valueEditorType": "text",
        "supportedOperators": ["EQUALS", "CONTAINS", "IN"]
      },
      {
        "name": "MOD_ID",
        "label": "Mod ID",
        "description": "Minecraft namespace / mod id such as ae2 or mekanism.",
        "valueType": "STRING",
        "valueEditorType": "text",
        "supportedOperators": ["EQUALS", "CONTAINS", "IN"]
      },
      {
        "name": "TAG",
        "label": "Tag",
        "description": "Item tag such as c:ingots/iron or forge:storage_blocks.",
        "valueType": "STRING",
        "valueEditorType": "text",
        "supportedOperators": ["EQUALS", "CONTAINS", "IN"]
      },
      {
        "name": "DISPLAY_NAME",
        "label": "Display Name",
        "description": "Localized item display name from the dump.",
        "valueType": "STRING",
        "valueEditorType": "text",
        "supportedOperators": ["EQUALS", "CONTAINS", "IN"]
      },
      {
        "name": "HAS_COMPONENTS",
        "label": "Has Components",
        "description": "Whether the item stack carries non-empty component patch data.",
        "valueType": "BOOLEAN",
        "valueEditorType": "none",
        "supportedOperators": ["IS_TRUE", "IS_FALSE"]
      },
      {
        "name": "TOTAL_AMOUNT",
        "label": "Total Amount",
        "description": "Total amount seen in the analyzed network dump.",
        "valueType": "NUMBER",
        "valueEditorType": "number",
        "supportedOperators": ["EQUALS", "GREATER_OR_EQUAL", "LESS_OR_EQUAL"]
      }
    ],
    "operators": [
      { "name": "EQUALS", "label": "=", "description": "Exact equality match.", "unary": false },
      { "name": "CONTAINS", "label": "contains", "description": "Case-insensitive substring match.", "unary": false },
      { "name": "IN", "label": "in", "description": "Comma-separated candidate list match.", "unary": false },
      { "name": "GREATER_OR_EQUAL", "label": ">=", "description": "Numeric greater-than-or-equal comparison.", "unary": false },
      { "name": "LESS_OR_EQUAL", "label": "<=", "description": "Numeric less-than-or-equal comparison.", "unary": false },
      { "name": "IS_TRUE", "label": "is true", "description": "Unary boolean true check.", "unary": true },
      { "name": "IS_FALSE", "label": "is false", "description": "Unary boolean false check.", "unary": true }
    ],
    "combinators": [
      { "name": "AND", "label": "and" },
      { "name": "OR", "label": "or" }
    ]
  }
}
```

---

## 9. 规则表格操作指南

### 9.1 行操作

| 操作 | 行为 |
|------|------|
| **拖拽排序** | 修改 `priority`，重新生成 `FlatRuleRow[]` |
| **启用/禁用** | 切换 `enabled` |
| **编辑 Filter** | 打开 Filter 树编辑器，修改后更新 `filterSummary` |
| **修改动作** | Select 切换 `action`（当 action ≠ ROUTE_TO_ZONE 时，`targetZoneId` 置 null） |
| **修改目标 Zone** | Select 从 `zones` 中选择 |
| **新增规则** | 生成新 ID，默认 priority = 最后一个规则 priority + 10 |
| **删除规则** | 确认后从列表移除 |
| **展开 Filter 树** | 行内展开或新面板显示 `FilterTree.root` |

### 9.2 回退行

- 表格末尾始终显示一个虚拟的"回退行"（`isFallbackRow: true`）
- 该行不可删除，不可拖拽
- 点击可编辑 profile 的 `defaultZoneId`
- 该行表示：**没有任何规则命中时，物品默认路由到 defaultZone**

### 9.3 Filter 表达式树编辑器

```
┌─ Filter Group ──────────────────────────────────────┐
│ [AND ▼]  [+ Add Condition]  [+ Add Group]           │
│                                                      │
│ ┌─ Condition ─────────────────────────────────┐      │
│ │ [TOTAL_AMOUNT ▼] [>= ▼] [4096]       [✕]   │      │
│ └──────────────────────────────────────────────┘      │
│                                                      │
│ ┌─ Condition ─────────────────────────────────┐      │
│ │ [HAS_COMPONENTS ▼] [is true ▼]       [✕]   │      │
│ └──────────────────────────────────────────────┘      │
└──────────────────────────────────────────────────────┘
```

- `field` 下拉 → `uiMetadata.fields` 决定选项
- `operator` 下拉 → 根据所选 `field` 的 `supportedOperators` 过滤
- `value` 输入 → 根据所选 `field` 的 `valueEditorType` 决定控件
- 当 `operator.unary === true` 时，value 输入隐藏
- 组合下拉 `AND / OR` 可随时切换

---

## 10. 与后端 Java 模型对照

| 前端类型 | 后端 Java 类 | 映射关系 |
|----------|-------------|----------|
| `FilterRoutingTable` | — | 前端聚合视图，无直接映射 |
| `ProfileMeta` | `RoutingProfile` 头部字段 | `id`, `name`, `description`, `enabled`, `defaultZoneId`, `version` |
| `FlatRuleRow` | `RouteRule` + 派生字段 | Rule 字段直接映射，`filterSummary`/`targetZoneName` 为前端派生 |
| `FilterTree` | `ItemFilter` + `FilterExpression` | `root` 映射为递归树 |
| `FilterConditionNode` | `FilterCondition` | `type: "condition"` 为前端标记，后端无此字段 |
| `FilterGroupNode` | `FilterGroup` | `type: "group"` 为前端标记，后端无此字段 |
| `ZoneOption` | `StorageZone` | 字段逐一定义 |
| `FilterUiMetadata` | `FilterUiMetadata` | 后端已存在的独立 JSON |
| `RoutingPreview` | `RoutingExplanation` / `RoutingDecision` | 后端 `RoutingEngine.explain()` 产出 |

### 10.1 后端 → 前端聚合流程

```
RoutingProfile.json (后端原始)
  ├── profile 元信息 → ProfileMeta
  ├── zones[]       → ZoneOption[]
  ├── filters[]     → FilterTree[]          (递归转换 FilterExpression)
  ├── routeRules[]  → FlatRuleRow[]         (查找 Zone name, 摘要 Filter)
  └── defaultZoneId → FlatRuleRow[isFallbackRow=true]

FilterUiMetadata.json (后端独立)
  └── uiMetadata    → FilterUiMetadata
```

> **注意**: `FilterRoutingTable` 是前端聚合视图，不需要后端新建一个 JSON 文件。  
> 前端直接从 `RoutingProfile.json` + `FilterUiMetadata.json` 两文件派生出此结构。

---

## 11. JSON Schema

完整的 JSON Schema 定义（用于校验 `FilterRoutingTable` 结构）：

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://appliedinsight.knightcode.com/schema/filter-routing-table.schema.json",
  "title": "Applied Energistics: Insight - Frontend Filter Routing Table",
  "description": "Frontend-facing aggregated view of routing rules, filters, zones, and UI metadata.",
  "type": "object",
  "required": ["schemaVersion", "profile", "routeRules", "filters", "zones", "uiMetadata"],
  "properties": {
    "schemaVersion": { "type": "integer", "minimum": 1 },
    "profile": { "$ref": "#/$defs/profileMeta" },
    "routeRules": {
      "type": "array",
      "items": { "$ref": "#/$defs/flatRuleRow" }
    },
    "filters": {
      "type": "array",
      "items": { "$ref": "#/$defs/filterTree" }
    },
    "zones": {
      "type": "array",
      "items": { "$ref": "#/$defs/zoneOption" }
    },
    "uiMetadata": { "$ref": "#/$defs/filterUiMetadata" },
    "preview": { "$ref": "#/$defs/routingPreview" }
  },
  "$defs": {
    "profileMeta": {
      "type": "object",
      "required": ["id", "name", "defaultZoneId", "version"],
      "properties": {
        "id": { "type": "string" },
        "name": { "type": "string" },
        "description": { "type": "string" },
        "enabled": { "type": "boolean" },
        "defaultZoneId": { "type": "string" },
        "version": { "type": "integer" },
        "path": { "type": "string" }
      }
    },
    "flatRuleRow": {
      "type": "object",
      "required": ["id", "name", "priority", "action", "filterId"],
      "properties": {
        "id": { "type": "string" },
        "name": { "type": "string" },
        "description": { "type": "string" },
        "enabled": { "type": "boolean" },
        "priority": { "type": "integer" },
        "action": { "type": "string", "enum": ["ROUTE_TO_ZONE", "REJECT", "ONLY_MARK", "FALLBACK"] },
        "filterId": { "type": ["string", "null"] },
        "targetZoneId": { "type": ["string", "null"] },
        "continueOnMatch": { "type": "boolean" },
        "explanation": { "type": "string" },
        "filterSummary": { "type": "string" },
        "targetZoneName": { "type": ["string", "null"] },
        "defaultZoneName": { "type": ["string", "null"] },
        "isFallbackRow": { "type": "boolean" }
      }
    },
    "filterTree": {
      "type": "object",
      "required": ["id", "name", "root"],
      "properties": {
        "id": { "type": "string" },
        "name": { "type": "string" },
        "description": { "type": "string" },
        "enabled": { "type": "boolean" },
        "root": { "$ref": "#/$defs/filterExpressionNode" }
      }
    },
    "filterExpressionNode": {
      "oneOf": [
        { "$ref": "#/$defs/filterConditionNode" },
        { "$ref": "#/$defs/filterGroupNode" }
      ]
    },
    "filterConditionNode": {
      "type": "object",
      "required": ["type", "field", "operator"],
      "properties": {
        "type": { "const": "condition" },
        "field": { "type": "string" },
        "operator": { "type": "string" },
        "value": { "type": ["string", "number", "null"] },
        "displayValue": { "type": "string" }
      }
    },
    "filterGroupNode": {
      "type": "object",
      "required": ["type", "combinator", "rules"],
      "properties": {
        "type": { "const": "group" },
        "combinator": { "type": "string", "enum": ["AND", "OR"] },
        "rules": { "type": "array", "items": { "$ref": "#/$defs/filterExpressionNode" } }
      }
    },
    "zoneOption": {
      "type": "object",
      "required": ["id", "name", "kind"],
      "properties": {
        "id": { "type": "string" },
        "name": { "type": "string" },
        "description": { "type": "string" },
        "enabled": { "type": "boolean" },
        "kind": { "type": "string", "enum": ["BULK", "MISC", "COMPONENT_SAFE", "SPECIAL", "CUSTOM"] },
        "allowAsDefault": { "type": "boolean" }
      }
    },
    "filterUiMetadata": {
      "type": "object",
      "required": ["schemaVersion", "fields", "operators", "combinators"],
      "properties": {
        "schemaVersion": { "type": "integer" },
        "fields": { "type": "array", "items": { "$ref": "#/$defs/filterFieldDefinition" } },
        "operators": { "type": "array", "items": { "$ref": "#/$defs/filterOperatorDefinition" } },
        "combinators": { "type": "array", "items": { "$ref": "#/$defs/filterCombinatorDefinition" } }
      }
    },
    "filterFieldDefinition": {
      "type": "object",
      "required": ["name", "label", "valueType", "valueEditorType", "supportedOperators"],
      "properties": {
        "name": { "type": "string" },
        "label": { "type": "string" },
        "description": { "type": "string" },
        "valueType": { "type": "string", "enum": ["STRING", "NUMBER", "BOOLEAN"] },
        "valueEditorType": { "type": "string", "enum": ["text", "number", "none"] },
        "supportedOperators": { "type": "array", "items": { "type": "string" } }
      }
    },
    "filterOperatorDefinition": {
      "type": "object",
      "required": ["name", "label", "unary"],
      "properties": {
        "name": { "type": "string" },
        "label": { "type": "string" },
        "description": { "type": "string" },
        "unary": { "type": "boolean" }
      }
    },
    "filterCombinatorDefinition": {
      "type": "object",
      "required": ["name", "label"],
      "properties": {
        "name": { "type": "string" },
        "label": { "type": "string" }
      }
    },
    "routingPreview": {
      "type": "object",
      "required": ["item", "decision", "evaluations"],
      "properties": {
        "item": { "$ref": "#/$defs/previewItemContext" },
        "decision": { "$ref": "#/$defs/routingDecision" },
        "evaluations": { "type": "array", "items": { "$ref": "#/$defs/ruleEvaluation" } }
      }
    },
    "previewItemContext": {
      "type": "object",
      "required": ["itemId", "modId", "displayName", "totalAmount"],
      "properties": {
        "itemId": { "type": "string" },
        "modId": { "type": "string" },
        "displayName": { "type": "string" },
        "tags": { "type": "array", "items": { "type": "string" } },
        "hasComponents": { "type": "boolean" },
        "totalAmount": { "type": "integer" }
      }
    },
    "routingDecision": {
      "type": "object",
      "required": ["decisionType"],
      "properties": {
        "decisionType": { "type": "string", "enum": ["ROUTED", "REJECTED", "MARKED_ONLY", "FALLBACK", "NO_MATCH"] },
        "finalZoneId": { "type": ["string", "null"] },
        "matchedRuleId": { "type": ["string", "null"] },
        "matchedFilterId": { "type": ["string", "null"] },
        "fallbackUsed": { "type": "boolean" }
      }
    },
    "ruleEvaluation": {
      "type": "object",
      "required": ["ruleId", "filterId", "matched", "action"],
      "properties": {
        "ruleId": { "type": "string" },
        "filterId": { "type": "string" },
        "matched": { "type": "boolean" },
        "action": { "type": "string" },
        "targetZoneId": { "type": ["string", "null"] },
        "explanation": { "type": "string" }
      }
    }
  }
}
```

---

## 12. 变更记录

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1 | 2026-05-25 | 初始草案 — 定义前端 Filter Routing Table 完整契约 |

---

## 附录 A：TypeScript 类型定义汇总

将此文件保存为前端项目的 `src/types/filter-routing-table.ts`：

```typescript
// ─── Enums ───

export type RouteAction = 'ROUTE_TO_ZONE' | 'REJECT' | 'ONLY_MARK' | 'FALLBACK';

export type FilterField =
  | 'ITEM_ID' | 'MOD_ID' | 'TAG' | 'DISPLAY_NAME'
  | 'HAS_COMPONENTS' | 'TOTAL_AMOUNT' | 'NBT_PATH';

export type FilterOperator =
  | 'EQUALS' | 'CONTAINS' | 'IN' | 'GREATER_OR_EQUAL'
  | 'LESS_OR_EQUAL' | 'IS_TRUE' | 'IS_FALSE' | 'REGEX';

export type FilterCombinator = 'AND' | 'OR';

export type FilterValueType = 'STRING' | 'NUMBER' | 'BOOLEAN';

export type FilterValueEditorType = 'text' | 'number' | 'none';

export type ZoneKind = 'BULK' | 'MISC' | 'COMPONENT_SAFE' | 'SPECIAL' | 'CUSTOM';

export type DecisionType = 'ROUTED' | 'REJECTED' | 'MARKED_ONLY' | 'FALLBACK' | 'NO_MATCH';

// ─── Core Structures ───

export interface FilterRoutingTable {
  schemaVersion: number;
  profile: ProfileMeta;
  routeRules: FlatRuleRow[];
  filters: FilterTree[];
  zones: ZoneOption[];
  uiMetadata: FilterUiMetadata;
  preview?: RoutingPreview | null;
}

export interface ProfileMeta {
  id: string;
  name: string;
  description: string;
  enabled: boolean;
  defaultZoneId: string;
  version: number;
  path?: string;
}

export interface FlatRuleRow {
  id: string;
  name: string;
  description: string;
  enabled: boolean;
  priority: number;
  action: RouteAction;
  filterId: string | null;
  targetZoneId: string | null;
  continueOnMatch: boolean;
  explanation: string;
  // 前端派生字段
  filterSummary: string;
  targetZoneName: string | null;
  defaultZoneName: string | null;
  isFallbackRow: boolean;
}

export interface FilterTree {
  id: string;
  name: string;
  description: string;
  enabled: boolean;
  root: FilterExpressionNode;
}

export type FilterExpressionNode = FilterConditionNode | FilterGroupNode;

export interface FilterConditionNode {
  type: 'condition';
  field: FilterField;
  operator: FilterOperator;
  value: string | number | null;
  displayValue?: string;
}

export interface FilterGroupNode {
  type: 'group';
  combinator: FilterCombinator;
  rules: FilterExpressionNode[];
}

export interface ZoneOption {
  id: string;
  name: string;
  description: string;
  enabled: boolean;
  kind: ZoneKind;
  allowAsDefault: boolean;
}

// ─── UI Metadata ───

export interface FilterUiMetadata {
  schemaVersion: number;
  fields: FilterFieldDefinition[];
  operators: FilterOperatorDefinition[];
  combinators: FilterCombinatorDefinition[];
}

export interface FilterFieldDefinition {
  name: FilterField;
  label: string;
  description: string;
  valueType: FilterValueType;
  valueEditorType: FilterValueEditorType;
  supportedOperators: FilterOperator[];
}

export interface FilterOperatorDefinition {
  name: FilterOperator;
  label: string;
  description: string;
  unary: boolean;
}

export interface FilterCombinatorDefinition {
  name: FilterCombinator;
  label: string;
}

// ─── Routing Preview ───

export interface RoutingPreview {
  item: PreviewItemContext;
  decision: RoutingDecision;
  evaluations: RuleEvaluation[];
}

export interface PreviewItemContext {
  itemId: string;
  modId: string;
  displayName: string;
  tags: string[];
  hasComponents: boolean;
  totalAmount: number;
}

export interface RoutingDecision {
  decisionType: DecisionType;
  finalZoneId: string | null;
  matchedRuleId: string | null;
  matchedFilterId: string | null;
  fallbackUsed: boolean;
}

export interface RuleEvaluation {
  ruleId: string;
  filterId: string;
  matched: boolean;
  action: RouteAction;
  targetZoneId: string | null;
  explanation: string;
}
```

---

## 附录：相关文档

| 文档 | 说明 |
|------|------|
| [`前端对接说明.md`](前端对接说明.md) | 前端对接说明 — 前端边界与后端消费方式 |
| [`前端对接契约-需求-约束.md`](前端对接契约-需求-约束.md) | 前端对接契约、需求与约束 |
| [`日志与JSON字段契约.md`](日志与JSON字段契约.md) | 日志与 JSON 字段契约 — 第 9 节 profile JSON 字段定义 |
| [`后端JSON完善指南.md`](后端JSON完善指南.md) | 后端 JSON 完善指南 |
| [`COMMANDS_REFERENCE.md`](COMMANDS_REFERENCE.md) | 命令参考 — 所有命令的详细说明 |
| [`docs/架构/FACTS.md`](docs/架构/FACTS.md) | 架构参考 + 术语表 — 六层架构、三条能力路径、术语定义（FACT-091 ~ FACT-120） |
| [`dashboard的设计哲学.md`](dashboard的设计哲学.md) | Dashboard 设计哲学 — 先给结果再纠偏 |
