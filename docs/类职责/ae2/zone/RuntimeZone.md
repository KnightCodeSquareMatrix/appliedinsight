# RuntimeZone
- **源码路径**: `src/main/java/com/knightcode/appliedstoragesorter/ae2/zone/RuntimeZone.java`
- **包**: `com.knightcode.appliedstoragesorter.ae2.zone`
- **类型**: `class`
- **所属层**: AE2 集成层

## 职责
运行时 zone 实现，基于当前 cell 列表做目标落点选择，并承载 zone-local 的接纳与治理策略边界。

## 边界检查

边界健康。它只回答“能不能接、落到哪”，并可继续承载 zone-local 的 placement / admission / blacklist / whitelist / 容量治理策略；不参与实际搬运，不承担日志、trace、metrics 或 analyzer 解释输出。

## 抽象检查

轻量抽象，很符合当前阶段。

## 主要协作者

- `com.knightcode.appliedstoragesorter.ae2.scan.DriveCellReference`
- `appeng.api.stacks.AEItemKey`

## 维护备注
- 当前策略偏简单：优先已有同类项、再看可接收量、再看 distinct count。后续如果需要更复杂策略，可以继续留在本类演进。
- 允许继续向本类收口 zone-local 的治理规则，但不要把执行事务、技术日志、诊断采样塞进来。
