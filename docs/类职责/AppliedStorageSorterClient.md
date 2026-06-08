# AppliedStorageSorterClient
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/AppliedStorageSorterClient.java`
- **包**: `com.knightcode.appliedstoragesorter`
- **类型**: `class`（`@Mod(dist=CLIENT)` 客户端入口）
- **所属层**: 入口/装配层

## 职责
模组客户端入口。注册 Screen（`SorterInitScreens`）、预加载 ScreenStyle，挂接 NeoForge 客户端事件。

## 边界检查
边界健康。客户端专属逻辑没有泄漏到服务端入口。

## 主要协作者
- `com.knightcode.appliedstoragesorter.client.SorterInitScreens`
- `com.knightcode.appliedstoragesorter.client.SorterClientRegistrations`

## 维护备注
- Mod ID 为 `appliedinsight`；类名保留历史命名 `AppliedStorageSorterClient`。
