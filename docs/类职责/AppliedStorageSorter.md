# AppliedStorageSorter
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/AppliedStorageSorter.java`
- **包**: `com.knightcode.appliedstoragesorter`
- **类型**: `class`（`@Mod` 服务端入口）
- **所属层**: 入口/装配层

## 对外名称
- **英文**: Applied Energistics: Insight
- **中文**: 应用能源：洞察（社区简称 AE 洞察）
- **Mod ID**: `appliedinsight`（Java 类名/包名仍为历史命名 `AppliedStorageSorter` / `appliedstoragesorter`）

## 职责
模组服务端入口。负责注册配置、方块/物品/菜单/配方序列化器、网络 Payload、命令与 capability，挂到 NeoForge 事件总线。

## 边界检查
边界健康。它只做启动期装配，不承载业务流程，也没有把命令、规则、AE2 运行时逻辑塞进入口类。

## 主要协作者
- `com.knightcode.appliedstoragesorter.command.SorterCommands`
- `com.knightcode.appliedstoragesorter.registry.*`
- `com.knightcode.appliedstoragesorter.network.*`（Payload 注册）

## 维护备注
- 若 Mod ID 与 Java 入口类名未来统一重命名，需同步 `@Mod` 注解、`gradle.properties` 与文档中的「对外名称 / Mod ID / 源码类名」三段说明。
