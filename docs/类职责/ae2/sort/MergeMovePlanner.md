# MergeMovePlanner
- **源码路径**: `src/main/java/com/knightcode/appliedinsight/ae2/sort/MergeMovePlanner.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.sort`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
`/sorter merge` 的核心 planner：基于“同 key 合并”策略生成 cell 到 cell 的搬运计划。

## 边界检查
边界健康。它明确服务快速 merge 模式，不与新的 zone 规划/执行主线混淆。

## 抽象检查
没有过度抽象，名称也比原先更准确地表达了“只做 merge”的功能边界。

## 主要协作者
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference`
- `com.knightcode.appliedstoragesorter.ae2.scan.DriveMachineAccessor`
- `appeng.api.config.Actionable`
- `appeng.api.networking.IGrid`
- `appeng.api.networking.security.IActionHost`
- `appeng.api.networking.security.IActionSource`
- `appeng.api.stacks.AEItemKey`
- `appeng.api.storage.MEStorage`
- `appeng.api.storage.cells.StorageCell`

## 维护备注
- 当前由 `/sorter merge` 消费；只要继续坚持“快速合并杂物”定位，这个类就不需要承担 route / zone 相关职责。
