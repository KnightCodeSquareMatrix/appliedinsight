# SmartBusMenu
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/menu/SmartBusMenu.java`
- **包**: `com.knightcode.appliedstoragesorter.menu`
- **类型**: `class`（`AEBaseMenu`）
- **所属层**: 容器交互层

## 职责
Smart Bus Part 的菜单容器：持有 `SmartBusPart` 引用，向客户端 Screen 暴露 `getMode()`、`getFilterJson()`、`hasValidFilter()` 等只读状态；`stillValid` 校验 Part 仍在对应 `BlockPos`+`Direction`。

## 边界检查
边界健康。无物品槽；`quickMoveStack` 恒返回空。filter/mode 写入经 network payload 在服务端 Part 完成。

## 工厂
- 服务端：`SmartBusMenu(containerId, playerInv, host)`
- 客户端：`fromNetwork(...)` 通过 `PartHelper.getPart` 解析 host

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.part.SmartBusPart`
- `com.knightcode.appliedstoragesorter.client.screen.SmartBusScreen`
- `com.knightcode.appliedstoragesorter.registry.SorterMenus`

## 维护备注
- Screen JSON：`assets/appliedinsight/screens/smart_bus.json`
