# React Query Builder 对接说明

## 1. 当前后端边界

当前 Java 侧已经提供两类前端可直接消费的数据：

- `src/main/resources/schema/routing-profile.schema.json`
  - 用于 profile 结构校验
- `filter-ui-metadata.json`
  - 用于 Query Builder 字段、操作符、组合符配置

其中 filter 已经改为树形结构：

- `root`
- `FilterGroup`
- `FilterCondition`

这与 React Query Builder 的思路一致。

---

## 2. React Query Builder 对应关系

Java / JSON：

- `FilterGroup.combinator` -> Query Builder `combinator`
- `FilterGroup.rules` -> Query Builder `rules`
- `FilterCondition.field` -> Query Builder `field`
- `FilterCondition.operator` -> Query Builder `operator`
- `FilterCondition.value` -> Query Builder `value`

当前 combinator 值：

- `AND`
- `OR`

当前 operator 值：

- `EQUALS`
- `CONTAINS`
- `IN`
- `GREATER_OR_EQUAL`
- `LESS_OR_EQUAL`
- `IS_TRUE`
- `IS_FALSE`

这些值可以直接作为 React Query Builder 的 `name/value` 使用。

---

## 3. 元数据导出方式

Gradle 任务：

```bash
./gradlew filterUiMetadataCli
./gradlew filterUiMetadataCli -PcliArgs="write-default tmp/filter-ui-metadata.json"
./gradlew filterUiMetadataCli -PcliArgs="inspect src/main/resources/testfiles/filter-ui-metadata-example.json"
```

默认会导出：

- 字段列表
- 操作符列表
- combinator 列表
- 字段 value editor 类型

---

## 4. React 侧推荐消费方式

建议 React 项目启动时加载两份数据：

1. `routing-profile.schema.json`
2. `filter-ui-metadata.json`

然后：

- 用 `filter-ui-metadata.json` 构造 Query Builder 的：
  - fields
  - operators
  - combinators
- 用 `routing-profile.schema.json` 约束整体 profile 存储格式

---

## 5. 前端生成 profile 的强制要求

前端保存出的 JSON 必须同时满足下面两层约束：

1. **filter 树结构** 必须符合 Query Builder 对接约定
2. **整个 routing profile** 必须符合 `routing-profile.schema.json`

不要只保证 `filters[].root` 正确，而忽略 `zones`、`routeRules` 等外层字段。

### 5.1 zones 必填字段

每个 zone 必须至少包含：

- `id`
- `name`
- `kind`

其中 `kind` 只能是：

- `BULK`
- `MISC`
- `COMPONENT_SAFE`
- `SPECIAL`
- `CUSTOM`

前端**不得省略 `kind`**，也不要依赖后端推断。

### 5.2 routeRules.action 必须使用后端枚举原值

当前允许的 `action` 值只有：

- `ROUTE_TO_ZONE`
- `REJECT`
- `ONLY_MARK`
- `FALLBACK`

前端**不要自己发明别名**。

例如：

- `MARK_ONLY` 是错误值
- 正确值是 `ONLY_MARK`

### 5.3 field / operator / combinator 不要手写常量

前端应直接使用 `filter-ui-metadata.json` 导出的值，
不要在前端代码里另写一套字符串常量。

这样可以避免：

- Java matcher 与前端常量不一致
- schema 枚举值与前端下拉选项不一致
- 保存出的 JSON 无法被 Java 解析

### 5.4 保存前必须做 schema 校验

前端在提交或导出 profile 前，必须至少执行一次针对
`routing-profile.schema.json` 的校验。

建议把校验失败直接展示给用户，而不是生成一份后端无法读取的 JSON。

重点拦截以下问题：

- `zones[].kind` 缺失
- `routeRules[].action` 使用非法枚举值
- `defaultZoneId` 未指向已有 zone
- `filterId` / `targetZoneId` 空字符串

---

## 6. 最小示例

下面是一个合法的最小片段示例：

```json
{
  "zones": [
    {
      "id": "misc_zone",
      "name": "Misc Fallback",
      "description": "未命中路由规则时的默认区域。",
      "enabled": true,
      "kind": "MISC",
      "allowAsDefault": true
    }
  ],
  "filters": [
    {
      "id": "vanilla_items_filter",
      "name": "Vanilla Items Filter",
      "enabled": true,
      "root": {
        "combinator": "AND",
        "rules": [
          {
            "field": "MOD_ID",
            "operator": "EQUALS",
            "value": "minecraft"
          }
        ]
      }
    }
  ],
  "routeRules": [
    {
      "id": "rule-vanilla-route",
      "name": "Route Vanilla Items",
      "enabled": true,
      "priority": 1,
      "filterId": "vanilla_items_filter",
      "targetZoneId": "misc_zone",
      "continueOnMatch": false,
      "action": "ROUTE_TO_ZONE"
    }
  ]
}
```

---

## 7. 当前建议

React 端先不要自行发明 field/operator/action/kind 常量，
而是直接以 Java 导出的 metadata 与 schema 为准。

这样可以保证：

- Java matcher
- JSON schema
- React Query Builder
- profile 保存结果

四者使用同一套字段与枚举语义。
