# SmartBusMode
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/block/SmartBusMode.java`
- **包**: `com.knightcode.appliedstoragesorter.block`
- **类型**: `enum`（`StringRepresentable`）
- **所属层**: 方块声明层

## 职责
智能总线运行模式枚举：

| 模式 | 含义 |
|------|------|
| `EMPTY` | 不搬运 |
| `IMPORT` | 相邻库存 → ME |
| `EXPORT` | ME → 相邻库存 |

`next()` 循环：`EMPTY → IMPORT → EXPORT → IMPORT → …`（EMPTY 仅作初始/关闭态）。

## 边界检查
边界健康。仅模式定义与序列化；搬运逻辑在 `SmartBusPart`。

## 持久化 / 网络
- NBT 存 `name` 字符串（`empty`/`import`/`export`）
- `fromStoredName` 兼容旧值 `storage` → `IMPORT`
- `fromNetworkOrdinal` 供菜单/GUI 同步

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.part.SmartBusPart`
- `com.knightcode.appliedstoragesorter.menu.SmartBusMenu`

## 维护备注
- 显示名 lang：`smart_bus_mode.appliedinsight.*`（若与代码命名空间不一致需对齐）。
