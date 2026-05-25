# SorterMoveOperation
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/sort/SorterMoveOperation.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.sort`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
封装一组已规划好的底层搬运，并负责真正执行 extract -> insert -> rollback，同时产出逐条 move 的执行结果明细。

## 边界检查
边界健康。它不参与 route/zone 决策，只做执行原语。

## 抽象检查
抽象适中。内部 ExecutableMove 让外部不必暴露太多执行细节。

## 主要协作者
- `appeng.api.config.Actionable`
- `appeng.api.networking.security.IActionHost`
- `appeng.api.networking.security.IActionSource`
- `appeng.api.storage.MEStorage`

## 维护备注
- 当前实现简洁，优先保持单一职责，不要为了未来猜想提前拆分。
- 它现在已经承担“执行时采样结果”的职责，任何新增明细都应围绕 execute 阶段真实可观测数据展开，而不是回填 planner 推测值。
## 近期变更 (2026-05-24)
- **`ExecutableMove` 改为 `public`** — 原为包私有，因为 `ZoneMergePlanner`（位于 `ae2.zone` 包）需要构造 `ExecutableMove` 实例
- **添加公开 getter**：`plannedMove()`, `sourceDrive()`, `destinationDrive()`, `sourceStorage()`, `destinationStorage()`
- **`of()` / `empty()` 工厂方法改为 `public`** — 供 `ZoneMergePlanner` 和外部调用方使用
- 现在成为 `/sorter merge` 和 `/sorter me planAndMove` 两种能力线共享的执行引擎