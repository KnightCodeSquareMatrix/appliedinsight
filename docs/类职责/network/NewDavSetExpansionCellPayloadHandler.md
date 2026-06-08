# NewDavSetExpansionCellPayloadHandler
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/network/NewDavSetExpansionCellPayloadHandler.java`
- **包**: `com.knightcode.appliedstoragesorter.network`
- **类型**: `final class`
- **所属层**: 网络通信层

## 职责
处理 `NewDavSetExpansionCellPayload`：校验 `DigitalAssetVaultMenu` 打开且 pos 匹配后，调用 `blockEntity.setExpansionCellId(cellId)`。

## 边界检查
边界健康。不验证 cell 是否可合成——校验与样板检测在 BlockEntity 侧异步刷新。

## 主要协作者
- `com.knightcode.appliedstoragesorter.network.NewDavSetExpansionCellPayload`
- `com.knightcode.appliedstoragesorter.blockentity.DigitalAssetVaultBlockEntity`

## 维护备注
- 在 `AppliedStorageSorter.registerPayloads` 注册 play-to-server。
