# CellCapacityInspector

| 维度 | 说明 |
|------|------|
| **包路径** | `ae2/CellCapacityInspector.java` |
| **定位** | AE2 StorageCell 容量反射读取工具 |
| **职责** | 通过反射安全读取 AE2 StorageCell 的 byte/type 容量；提供无限/创造型 cell 启发式检测 |
| **关键方法** | `inspect(StorageCell)` → `CellCapacity`；`isLikelyInfiniteCell(CellCapacity, String)` → `boolean`；`resolveCellKind(CellCapacity, String)` → `String`；`describeCellKind(long)` → `String` |
| **内部类型** | `CellCapacity` record：`totalBytes`, `usedBytes`, `totalItemTypes`, `remainingItemTypes`；提供 `freeBytes()` 和 `byteUsageRatio()` |
| **依赖** | AE2 `StorageCell` 接口；反射调用 `getTotalBytes`/`getUsedBytes`/`getTotalTypes`/`getRemainingItemTypes`（含 fallback 名称） |
| **被谁使用** | `SorterNetworkDumpWriter`（me-dump）、`Ae2StorageAnalyzer`（storage-analysis dump） |
| **边界** | 不处理 external storage bus（返回 null）；无限 cell 检测基于 mod ID 白名单 + 物品路径关键词 |
