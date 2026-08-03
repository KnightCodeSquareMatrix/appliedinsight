# SorterCommandBlockEntity

| 维度 | 说明 |
|------|------|
| **包路径** | `blockentity/SorterCommandBlockEntity.java` |
| **定位** | 命令执行方块实体（AE2 网络节点） |
| **职责** | 管理 AE2 `IManagedGridNode` 生命周期（create/destroy）；提供 `openMenu()` 打开命令 GUI；实现 `IInWorldGridNodeHost` 暴露网格节点 |
| **关键方法** | `getMainNode()` → `IManagedGridNode`；`isNodeOnline()` → `boolean`；`openMenu(Player)` |
| **生命周期** | `onLoad()` 注册 first-tick → `onReady()` 调用 `create()`；`setRemoved()`/`onChunkUnloaded()` 调用 `destroy()` |
| **依赖** | AE2 `GridHelper`、`IManagedGridNode`、`IInWorldGridNodeHost` |
| **被谁使用** | `SorterCommandBlock`、`SorterCommandBlockMenu`、`SorterCommandPayloadHandler` |
| **边界** | 不承担命令执行逻辑，只提供网络接入点 |
