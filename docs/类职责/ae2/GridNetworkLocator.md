# GridNetworkLocator

| 维度 | 说明 |
|------|------|
| **包路径** | `ae2/GridNetworkLocator.java` |
| **定位** | AE2 网格稳定标识定位器 |
| **职责** | 从 AE2 网格中计算稳定的网络标识坐标，按东南西北优先级取第一个控制器坐标 |
| **关键方法** | `locateAnchor(IGrid)` → `BlockPos` |
| **依赖** | AE2 `IGrid`、`ControllerBlockEntity` |
| **被谁使用** | `Ae2ControllerTargetResolver`、`NetworkBindingKey` |
| **边界** | 无控制器时返回 null；排序优先级：东(高X) > 南(高Z) > 西(低X) > 北(低Z) |
