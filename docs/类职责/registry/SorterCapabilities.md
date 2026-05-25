# SorterCapabilities
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/registry/SorterCapabilities.java`
- **包**: `com.knightcode.appliedstoragesorter.registry`
- **类型**: `class`
- **所属层**: 注册装配层

## 职责
注册 DAV 的 AE2 capability，确保它能以 in-world grid node host 身份加入 ME 网络。

## 边界检查
边界健康。AE2 capability 细节被集中在 registry 层处理。

## 抽象检查
没有过度抽象。

## 主要协作者
- `appeng.api.AECapabilities`
- `net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent`

## 维护备注
- 这是 DAV 能被网络识别的关键装配点，文档中应该长期保留。
