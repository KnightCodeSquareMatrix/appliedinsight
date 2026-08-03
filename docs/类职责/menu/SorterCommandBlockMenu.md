# SorterCommandBlockMenu

| 维度 | 说明 |
|------|------|
| **包路径** | `menu/SorterCommandBlockMenu.java` |
| **定位** | 命令执行方块容器菜单 |
| **职责** | 提供 `SorterCommandBlockEntity` 的容器菜单，持有方块实体引用供 `SorterCommandPayloadHandler` 使用 |
| **关键方法** | `getBlockEntity()` → `SorterCommandBlockEntity` |
| **依赖** | `SorterCommandBlockEntity`、`SorterMenus` |
| **被谁使用** | `SorterCommandBlockScreen`、`SorterCommandPayloadHandler` |
