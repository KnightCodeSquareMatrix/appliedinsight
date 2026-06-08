# SmartBusPart
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/part/SmartBusPart.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.part`
- **类型**: `class`（`AEBasePart` + `IGridTickable`）
- **所属层**: AE2 集成层 / 玩家输入层

## 职责
智能总线 AE2 Part 运行时核心：
- **模式**：`EMPTY` / `IMPORT` / `EXPORT`（Shift+扳手循环，见 `SmartBusMode`）
- **过滤器**：JSON 字符串，复用 `rule/filter` DSL（`FilterExpressionJsonCodec` + `ItemFilterMatcher`）
- **IMPORT**：从相邻 `IItemHandler` 抽物品 → ME 网络（经 filter 匹配）
- **EXPORT**：从 ME 网络取物品 → 相邻库存（经 filter 匹配）
- **吞吐**：每 tick 最多 `Config.SMART_BUS_STACK_TRANSFERS_PER_TICK` 次整堆搬运（默认 12）

## 边界检查
边界健康。filter 匹配走纯 rule 层 matcher；网络 IO 走 AE2 API；不嵌入 route/zone 逻辑。

## 持久化
- NBT：`smart_mode`、`smart_filter`
- 网络同步：模式 ordinal + filter JSON 供客户端 GUI

## 主要协作者
- `com.knightcode.appliedstoragesorter.block.SmartBusMode`
- `com.knightcode.appliedstoragesorter.menu.SmartBusMenu`
- `com.knightcode.appliedstoragesorter.network.SmartBusFilterPayload` / `SmartBusModePayload`
- `com.knightcode.appliedstoragesorter.rule.filter.*`

## 维护备注
- UV 调试模型：`Config.DEBUG_SMART_BUS_UV`（LETTERBOX/SQUARE），发布前保持 OFF。
- Part 模型：`assets/appliedinsight/models/part/smart_bus_*.json`
