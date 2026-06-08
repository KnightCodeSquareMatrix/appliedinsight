# NewDavStorageProvider
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/dav/NewDavStorageProvider.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.dav`
- **类型**: `class`（实现 `IStorageProvider`）
- **所属层**: AE2 集成层

## 职责
在 DAV 的 `IGridNode` 上注册 `NewDavStorage` 到 AE2 存储挂载列表。优先级常量 `DAV_PRIORITY = 100000`。

## 边界检查
边界健康。薄包装，仅 `mountInventories` 一处逻辑。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.dav.NewDavStorage`
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`（构造并挂到 mainNode）

## 维护备注
- 调整 DAV 在网格中的存储优先级时只改 `DAV_PRIORITY` 常量并实机验证 pull/push 行为。
