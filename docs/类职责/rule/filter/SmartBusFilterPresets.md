# SmartBusFilterPresets
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/rule/filter/SmartBusFilterPresets.java`
- **包**: `com.knightcode.appliedstoragesorter.rule.filter`
- **类型**: `class`（工具类 + `Preset` 枚举）
- **所属层**: 纯规则模型层

## 职责
为 Smart Bus 游戏内预设与离线 filter editor 样板提供**同一套** `FilterExpression` 定义，避免 Java / `templates.js` 漂移。

## 预设一览

| `Preset` | id | 表达式摘要 |
|---|---|---|
| `ALL_ITEMS` | `all_items` | `ITEM_ID` REGEX `.+` |
| `DURABILITY_ITEMS` | `durability_items` | OR: `TAG` = `minecraft:enchantable/durability`；NBT `max_damage`；NBT `damage` |
| `ORES` | `ores` | OR: ore 相关 TAG / ITEM_ID 规则 |

`toFilterJson()` 输出 Pretty Gson JSON，供 `SmartBusScreen` 预设按钮写入 GUI。

## 「有耐久」设计说明

- **主条件**为物品标签 `minecraft:enchantable/durability`，可匹配满耐久盔甲/工具（仅 NBT `damage` 会漏掉未写入组件的满耐久堆叠）。
- NBT `components.minecraft:damage` 表示**耐久损耗**，不是攻击力；攻击力在 `attribute_modifiers` 中。
- NBT `damage` 规则作为 mod 兜底（如无标签的 `packing_tape` 等）。

玩家文档：[`guidebook/smart-bus.md`](../../../../guidebook/smart-bus.md) · JSON 契约 §9.11。

## 扩展新预设

1. 在 `SmartBusFilterPresets.Preset` 增加枚举项与 `FilterExpression` 工厂方法。
2. 同步 `tools/filter-editor/templates.js` 与 `i18n.js` / 语言文件 tooltip。
3. 在 `SmartBusScreen` 增加预设按钮（当前为三枚）。
4. 更新 guidebook 与 `docs/日志与JSON字段契约.md` §9.11。

## 主要协作者
- `com.knightcode.appliedstoragesorter.client.screen.SmartBusScreen`
- `tools/filter-editor/templates.js`
- `com.knightcode.appliedstoragesorter.rule.filter.FilterExpressionJsonCodec`

## 维护备注
- 单元测试：`ItemFilterMatcherTest.durabilityPresetMatchesTagOnlyItem` 等。
